package com.xiuxian.game.modules.skill.service;

import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.skill.entity.Skill;
import com.xiuxian.game.modules.skill.entity.PlayerSkill;
import com.xiuxian.game.modules.skill.entity.SkillCombo;
import com.xiuxian.game.modules.skill.entity.PlayerSkillComboRecord;
import com.xiuxian.game.dto.response.SkillResponse;
import com.xiuxian.game.dto.SkillComboResult;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.skill.mapper.PlayerSkillMapper;
import com.xiuxian.game.modules.skill.mapper.SkillMapper;
import com.xiuxian.game.modules.skill.mapper.SkillComboMapper;
import com.xiuxian.game.modules.skill.mapper.PlayerSkillComboRecordMapper;
import com.xiuxian.game.common.util.GameCalculator;
import com.xiuxian.game.common.util.GameConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 技能服务类
 * 负责技能系统的所有业务逻辑
 *
 * 主要功能：
 * - 技能学习和升级
 * - 技能装备和卸载
 * - 技能属性加成计算
 * - 技能伤害和效果计算
 * - 技能经验管理
 *
 * @author xiuxian
 * @version 1.0
 */
@Slf4j
@Service
@ConditionalOnProperty(value = "app.features.skills.enabled", havingValue = "true")
@RequiredArgsConstructor
public class SkillService {

    private final SkillMapper skillMapper;
    private final PlayerSkillMapper playerSkillMapper;
    private final PlayerService playerService; // 模块边界：通过PlayerService访问玩家数据
    private final GameCalculator gameCalculator;
    private final SkillComboMapper skillComboMapper;
    private final PlayerSkillComboRecordMapper playerSkillComboRecordMapper;

    /**
     * 连招检测时间窗口（秒）
     * 在此时间内使用的技能将被视为连续技能
     */
    private static final int COMBO_TIME_WINDOW_SECONDS = 3;

    public List<Skill> getAllSkills() {
        return skillMapper.selectList(null);
    }

    public List<Skill> getAvailableSkills(Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) throw new IllegalArgumentException("玩家不存在");
        return skillMapper.selectByUnlockLevelLessThanEqual(player.getLevel());
    }

    public List<PlayerSkill> getPlayerSkills(Integer playerId) {
        return playerSkillMapper.selectByPlayerId(playerId);
    }

    public List<SkillResponse> getPlayerSkillDetails(Integer playerId) {
        List<PlayerSkill> list = getPlayerSkills(playerId);
        java.util.ArrayList<SkillResponse> res = new java.util.ArrayList<>();
        for (PlayerSkill ps : list) {
            Skill s = skillMapper.selectById(ps.getSkillId());
            SkillResponse.SkillSummary summary = SkillResponse.SkillSummary.builder()
                    .id(s.getId())
                    .name(s.getName())
                    .description(s.getDescription())
                    .type(s.getSkillType())
                    .unlockLevel(s.getUnlockLevel())
                    .maxLevel(s.getMaxLevel())
                    .build();
            SkillResponse sr = SkillResponse.builder()
                    .id(ps.getId())
                    .level(ps.getLevel())
                    .equipped(ps.getEquipped())
                    .slotNumber(ps.getSlotNumber())
                    .cooldown(getSkillCooldown(ps))
                    .manaCost(getSkillManaCost(ps))
                    .skill(summary)
                    .build();
            res.add(sr);
        }
        return res;
    }

    public List<PlayerSkill> getEquippedSkills(Integer playerId) {
        return playerSkillMapper.selectByPlayerIdAndEquipped(playerId, true);
    }

    /**
     * 学习技能
     * 玩家学习新技能，需要满足等级要求和灵石消耗
     *
     * @param skillId 技能ID
     * @param playerId 玩家ID
     * @return 学习后的玩家技能信息
     * @throws IllegalArgumentException 当玩家不存在、技能不存在、等级不足、灵石不足或已学习该技能时抛出异常
     */
    @Transactional
    public PlayerSkill learnSkill(Integer skillId, Integer playerId) {
        log.info("========== 学习技能 ==========");
        log.info("玩家ID: {}, 技能ID: {}", playerId, skillId);

        // 1. 验证玩家是否存在
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            log.error("玩家不存在: ID={}", playerId);
            throw new IllegalArgumentException("玩家不存在");
        }
        log.info("玩家信息: 昵称={}, 等级={}, 灵石={}",
                player.getNickname(), player.getLevel(), player.getSpiritStones());

        // 2. 验证技能是否存在
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            log.error("技能不存在: ID={}", skillId);
            throw new IllegalArgumentException("技能不存在");
        }
        log.info("技能信息: 名称={}, 解锁等级={}, 所需灵石={}",
                skill.getName(), skill.getUnlockLevel(), skill.getRequiredSpiritStones());

        // 3. 检查等级要求
        if (skill.getUnlockLevel() > player.getLevel()) {
            log.warn("等级不足: 玩家等级={}, 需要等级={}", player.getLevel(), skill.getUnlockLevel());
            throw new IllegalArgumentException(GameConstants.ERROR_REQUIREMENTS_NOT_MET +
                    ": 角色等级不足，需要" + skill.getUnlockLevel() + "级");
        }

        // 4. 检查是否已学习
        PlayerSkill existing = playerSkillMapper.selectByPlayerIdAndSkillId(playerId, skillId);
        if (existing != null) {
            log.warn("已学习该技能: 玩家ID={}, 技能ID={}", playerId, skillId);
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 已经学习过该技能");
        }

        // 5. 检查并扣除灵石
        if (skill.getRequiredSpiritStones() != null && skill.getRequiredSpiritStones() > 0) {
            if (player.getSpiritStones() < skill.getRequiredSpiritStones()) {
                log.warn("灵石不足: 拥有={}, 需要={}",
                        player.getSpiritStones(), skill.getRequiredSpiritStones());
                throw new IllegalArgumentException("灵石不足，需要" + skill.getRequiredSpiritStones() + " 灵石");
            }

            long oldSpiritStones = player.getSpiritStones();
            player.setSpiritStones(oldSpiritStones - skill.getRequiredSpiritStones());
            playerService.savePlayerProfile(player);
            log.info("扣除灵石: {} -> {} (-{})",
                    oldSpiritStones, player.getSpiritStones(), skill.getRequiredSpiritStones());
        }

        // 6. 创建玩家技能记录
        PlayerSkill playerSkill = PlayerSkill.builder()
                .playerId(playerId)
                .skillId(skillId)
                .level(1)
                .experience(0)
                .equipped(false)
                .slotNumber(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        playerSkillMapper.insert(playerSkill);

        PlayerSkill savedSkill = playerSkillMapper.selectById(playerSkill.getId());
        log.info("技能学习成功: 玩家技能ID={}, 技能名称={}, 初始等级=1",
                savedSkill.getId(), skill.getName());
        log.info("========== 学习技能完成 ==========");

        return savedSkill;
    }

    /**
     * 为新玩家初始化基础技能
     */
    @Transactional
    public void initializePlayerSkills(PlayerProfile player) {
        // 获取所有1级解锁的技能
        List<Skill> basicSkills = skillMapper.selectByUnlockLevelLessThanEqual(1);

        for (Skill skill : basicSkills) {
            // 检查玩家是否已经拥有该技能
            PlayerSkill existingSkill = playerSkillMapper.selectByPlayerIdAndSkillId(player.getId(), skill.getId());
            if (existingSkill == null) {
                PlayerSkill playerSkill = PlayerSkill.builder()
                        .playerId(player.getId())
                        .skillId(skill.getId())
                        .level(1)
                        .experience(0)
                        .equipped(false)
                        .slotNumber(0)
                        .build();
                playerSkillMapper.insert(playerSkill);
            }
        }
    }

    /**
     * 计算技能升级所需经验
     */
    private int calculateSkillUpgradeExp(int currentLevel) {
        // 技能升级经验需求：基础100，每级递增50
        return 100 + (currentLevel - 1) * 50;
    }

    /**
     * 计算技能实际伤害
     */
    public double calculateSkillDamage(PlayerSkill playerSkill) {
        Skill skill = skillMapper.selectById(playerSkill.getSkillId());
        int skillLevel = playerSkill.getLevel();
        double damage = skill.getBaseDamage() + (skillLevel - 1) * skill.getDamagePerLevel();
        String type = skill.getSkillType();
        if ("防御".equals(type)) damage = damage * 0.1;
        else if ("辅助".equals(type)) damage = damage * 0.05;
        return Math.max(0, damage);
    }

    /**
     * 获取技能冷却时间（秒）
     */
    public int getSkillCooldown(PlayerSkill playerSkill) {
        int baseCooldown = skillMapper.selectById(playerSkill.getSkillId()).getCooldown();
        int skillLevel = playerSkill.getLevel();
        int reducedCooldown = Math.max(1, baseCooldown - (skillLevel - 1) / 2);
        return reducedCooldown;
    }

    /**
     * 获取技能消耗法力
     */
    public int getSkillManaCost(PlayerSkill playerSkill) {
        int baseCost = skillMapper.selectById(playerSkill.getSkillId()).getManaCost();
        int skillLevel = playerSkill.getLevel();
        return baseCost + (skillLevel - 1);
    }

    /**
     * 技能使用后增加经验
     */
    @Transactional
    public void addSkillExperience(Integer playerSkillId, int expGain) {
        PlayerSkill playerSkill = playerSkillMapper.selectById(playerSkillId);
        if (playerSkill == null) throw new IllegalArgumentException("玩家技能不存在");
        playerSkill.setExperience(playerSkill.getExperience() + expGain);
        Skill skill = skillMapper.selectById(playerSkill.getSkillId());
        while (playerSkill.getExperience() >= calculateSkillUpgradeExp(playerSkill.getLevel())
                && playerSkill.getLevel() < skill.getMaxLevel()) {
            int requiredExp = calculateSkillUpgradeExp(playerSkill.getLevel());
            playerSkill.setExperience(playerSkill.getExperience() - requiredExp);
            playerSkill.setLevel(playerSkill.getLevel() + 1);
            playerSkill.setExperience(playerSkill.getExperience() + 20);
        }
        playerSkillMapper.updateById(playerSkill);
    }

    @Transactional
    public PlayerSkill upgradeSkill(Integer playerSkillId, Integer playerId) {
        PlayerSkill playerSkill = playerSkillMapper.selectById(playerSkillId);
        if (playerSkill == null) throw new IllegalArgumentException(GameConstants.ERROR_SKILL_NOT_FOUND + ": 玩家技能不存在");
        if (!playerSkill.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 无权操作该技能");
        }
        Skill skill = skillMapper.selectById(playerSkill.getSkillId());
        if (playerSkill.getLevel() >= skill.getMaxLevel()) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 技能已达到最大等级");
        }
        int currentLevel = playerSkill.getLevel();
        int requiredExp = calculateSkillUpgradeExp(currentLevel);
        if (playerSkill.getExperience() < requiredExp) {
            throw new IllegalArgumentException(GameConstants.ERROR_INSUFFICIENT_RESOURCES + ": 技能经验不足，无法升级技能");
        }
        playerSkill.setExperience(playerSkill.getExperience() - requiredExp);
        playerSkill.setLevel(currentLevel + 1);
        playerSkillMapper.updateById(playerSkill);
        return playerSkillMapper.selectById(playerSkillId);
    }

    @Transactional
    public PlayerSkill upgradeSkillByPoints(Integer playerSkillId, Integer playerId) {
        PlayerSkill playerSkill = playerSkillMapper.selectById(playerSkillId);
        if (playerSkill == null) throw new IllegalArgumentException(GameConstants.ERROR_SKILL_NOT_FOUND + ": 玩家技能不存在");
        if (!playerSkill.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 无权操作该技能");
        }
        Skill skill = skillMapper.selectById(playerSkill.getSkillId());
        if (playerSkill.getLevel() >= skill.getMaxLevel()) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 技能已达到最大等级");
        }
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        int points = player.getSkillPoints() == null ? 0 : player.getSkillPoints();
        if (points <= 0) {
            throw new IllegalArgumentException(GameConstants.ERROR_INSUFFICIENT_RESOURCES + ": 技能点不足");
        }
        player.setSkillPoints(points - 1);
        playerService.savePlayerProfile(player);
        playerSkill.setLevel(playerSkill.getLevel() + 1);
        playerSkillMapper.updateById(playerSkill);
        return playerSkillMapper.selectById(playerSkillId);
    }

    @Transactional
    public PlayerSkill equipSkill(Integer playerSkillId, Integer slotNumber, Integer playerId) {
        PlayerSkill playerSkill = playerSkillMapper.selectById(playerSkillId);
        if (playerSkill == null) throw new IllegalArgumentException(GameConstants.ERROR_SKILL_NOT_FOUND + ": 玩家技能不存在");
        if (!playerSkill.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 无权操作该技能");
        }
        List<PlayerSkill> equippedSkills = playerSkillMapper.selectByPlayerIdAndEquipped(playerId, true);
        for (PlayerSkill ps : equippedSkills) {
            if (ps.getSlotNumber() != null && ps.getSlotNumber().equals(slotNumber)) {
                ps.setEquipped(false);
                ps.setSlotNumber(0);
                playerSkillMapper.updateById(ps);
            }
        }
        playerSkill.setEquipped(true);
        playerSkill.setSlotNumber(slotNumber);
        playerSkillMapper.updateById(playerSkill);
        return playerSkillMapper.selectById(playerSkillId);
    }

    @Transactional
    public PlayerSkill unequipSkill(Integer playerSkillId, Integer playerId) {
        PlayerSkill playerSkill = playerSkillMapper.selectById(playerSkillId);
        if (playerSkill == null) throw new IllegalArgumentException(GameConstants.ERROR_SKILL_NOT_FOUND + ": 玩家技能不存在");
        if (!playerSkill.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 无权操作该技能");
        }
        playerSkill.setEquipped(false);
        playerSkill.setSlotNumber(0);
        playerSkillMapper.updateById(playerSkill);
        return playerSkillMapper.selectById(playerSkillId);
    }

    /**
     * 计算玩家装备技能的总属性加成
     */
    public Map<String, Integer> calculateSkillBonuses(Integer playerId) {
        List<PlayerSkill> equippedSkills = playerSkillMapper.selectByPlayerIdAndEquipped(playerId, true);
        Map<String, Integer> bonuses = new HashMap<>();

        bonuses.put("health", 0);
        bonuses.put("mana", 0);
        bonuses.put("attack", 0);
        bonuses.put("defense", 0);
        bonuses.put("speed", 0);

        for (PlayerSkill playerSkill : equippedSkills) {
            Skill skill = skillMapper.selectById(playerSkill.getSkillId());
            if (skill != null) {
                bonuses.put("health", bonuses.get("health") +
                    (skill.getHealthBonus() != null ? skill.getHealthBonus() * playerSkill.getLevel() : 0));
                bonuses.put("mana", bonuses.get("mana") +
                    (skill.getManaBonus() != null ? skill.getManaBonus() * playerSkill.getLevel() : 0));
                bonuses.put("attack", bonuses.get("attack") +
                    (skill.getAttackBonus() != null ? skill.getAttackBonus() * playerSkill.getLevel() : 0));
                bonuses.put("defense", bonuses.get("defense") +
                    (skill.getDefenseBonus() != null ? skill.getDefenseBonus() * playerSkill.getLevel() : 0));
                bonuses.put("speed", bonuses.get("speed") +
                    (skill.getSpeedBonus() != null ? skill.getSpeedBonus() * playerSkill.getLevel() : 0));
            }
        }

        return bonuses;
    }

    // ==================== 技能连招系统 ====================

    /**
     * 获取玩家可用的连招列表
     * @param playerId 玩家ID
     * @return 可用的连招列表
     */
    public List<SkillCombo> getAvailableCombos(Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            return Collections.emptyList();
        }
        return skillComboMapper.selectAvailableCombos(player.getLevel());
    }

    /**
     * 获取所有激活的连招
     * @return 激活的连招列表
     */
    public List<SkillCombo> getAllActiveCombos() {
        return skillComboMapper.selectActiveCombos();
    }

    /**
     * 检测技能使用后是否触发连招
     * @param playerId 玩家ID
     * @param skillId 当前使用的技能ID
     * @param baseDamage 基础伤害（用于计算连招加成）
     * @return 连招结果
     */
    @Transactional
    public SkillComboResult checkAndTriggerCombo(Integer playerId, Integer skillId, int baseDamage) {
        // 1. 获取玩家等级，检查连招是否满足等级要求
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            return SkillComboResult.builder().triggered(false).build();
        }

        // 2. 获取玩家已学习的技能列表
        List<PlayerSkill> playerSkills = playerSkillMapper.selectByPlayerId(playerId);
        Set<Integer> learnedSkillIds = playerSkills.stream()
                .map(PlayerSkill::getSkillId)
                .collect(Collectors.toSet());

        // 3. 获取玩家可用的连招
        List<SkillCombo> availableCombos = skillComboMapper.selectAvailableCombos(player.getLevel());
        if (availableCombos.isEmpty()) {
            // 记录技能使用
            recordSkillUsage(playerId, skillId, false, null);
            return SkillComboResult.builder().triggered(false).build();
        }

        // 4. 获取最近的技能使用记录（时间窗口内）
        LocalDateTime timeThreshold = LocalDateTime.now().minusSeconds(COMBO_TIME_WINDOW_SECONDS);
        List<PlayerSkillComboRecord> recentRecords = playerSkillComboRecordMapper.findRecentRecords(playerId, 5);

        // 5. 构建最近的技能序列
        List<Integer> recentSkillSequence = recentRecords.stream()
                .filter(r -> r.getUsedAt().isAfter(timeThreshold))
                .map(PlayerSkillComboRecord::getSkillId)
                .collect(Collectors.toList());

        // 添加当前技能
        recentSkillSequence.add(skillId);

        // 6. 检查是否匹配任何连招
        for (SkillCombo combo : availableCombos) {
            if (combo.getSkillSequence() == null || combo.getSkillSequence().isEmpty()) {
                continue;
            }

            // 解析连招序列
            List<Integer> comboSequence = parseSkillSequence(combo.getSkillSequence());
            if (comboSequence.isEmpty()) {
                continue;
            }

            // 检查玩家是否拥有连招所需的所有技能
            boolean hasAllSkills = comboSequence.stream().allMatch(learnedSkillIds::contains);
            if (!hasAllSkills) {
                continue;
            }

            // 检查序列是否匹配
            if (isSequenceMatch(recentSkillSequence, comboSequence)) {
                // 触发连招！
                double bonusPercent = combo.getComboBonus().doubleValue();
                int bonusDamage = (int) (baseDamage * bonusPercent / 100);
                int finalDamage = baseDamage + bonusDamage;

                // 记录连招触发
                recordSkillUsage(playerId, skillId, true, combo.getId());

                log.info("玩家{}触发了连招: {}，伤害加成: {}% (+{})",
                        playerId, combo.getName(), bonusPercent, bonusDamage);

                return SkillComboResult.builder()
                        .triggered(true)
                        .comboName(combo.getName())
                        .comboDescription(combo.getDescription())
                        .bonusPercent(bonusPercent)
                        .bonusDamage(bonusDamage)
                        .finalDamage(finalDamage)
                        .comboIcon("combo_" + combo.getId())
                        .build();
            }
        }

        // 未触发连招
        recordSkillUsage(playerId, skillId, false, null);
        return SkillComboResult.builder().triggered(false).finalDamage(baseDamage).build();
    }

    /**
     * 解析技能序列JSON字符串
     * @param sequenceJson JSON格式的技能序列，如 "[4, 2]"
     * @return 技能ID列表
     */
    private List<Integer> parseSkillSequence(String sequenceJson) {
        List<Integer> result = new ArrayList<>();
        if (sequenceJson == null || sequenceJson.isEmpty()) {
            return result;
        }

        // 移除方括号
        String cleaned = sequenceJson.replaceAll("[\\[\\]]", "");
        if (cleaned.isEmpty()) {
            return result;
        }

        // 分割并解析
        String[] parts = cleaned.split(",");
        for (String part : parts) {
            try {
                result.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {
                // 忽略无效的数字
            }
        }
        return result;
    }

    /**
     * 检查技能序列是否匹配连招序列
     * 检查最近N个技能是否与连招序列匹配
     */
    private boolean isSequenceMatch(List<Integer> recentSequence, List<Integer> comboSequence) {
        if (recentSequence.size() < comboSequence.size()) {
            return false;
        }

        // 检查recentSequence的末尾是否与comboSequence匹配
        int startIndex = recentSequence.size() - comboSequence.size();
        for (int i = 0; i < comboSequence.size(); i++) {
            if (!recentSequence.get(startIndex + i).equals(comboSequence.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 记录技能使用
     */
    @Transactional
    public void recordSkillUsage(Integer playerId, Integer skillId, boolean triggeredCombo, Integer comboId) {
        PlayerSkillComboRecord record = PlayerSkillComboRecord.builder()
                .playerId(playerId)
                .skillId(skillId)
                .usedAt(LocalDateTime.now())
                .triggeredCombo(triggeredCombo)
                .comboId(comboId)
                .build();
        playerSkillComboRecordMapper.insert(record);

        // 清理旧记录（保留最近20条）
        LocalDateTime oldThreshold = LocalDateTime.now().minusMinutes(10);
        List<PlayerSkillComboRecord> allRecords = playerSkillComboRecordMapper.findRecentRecords(playerId, 100);
        if (allRecords.size() > 20) {
            playerSkillComboRecordMapper.deleteOldRecords(playerId, oldThreshold);
        }
    }

    /**
     * 获取连招信息DTO
     */
    public Map<String, Object> getComboInfo(SkillCombo combo) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", combo.getId());
        info.put("name", combo.getName());
        info.put("description", combo.getDescription());
        info.put("bonusPercent", combo.getComboBonus());
        info.put("requiredLevel", combo.getRequiredLevel());

        // 解析技能序列并获取技能名称
        List<Integer> skillIds = parseSkillSequence(combo.getSkillSequence());
        List<String> skillNames = new ArrayList<>();
        for (Integer skillId : skillIds) {
            Skill skill = skillMapper.selectById(skillId);
            if (skill != null) {
                skillNames.add(skill.getName());
            }
        }
        info.put("skillSequence", skillNames);

        return info;
    }

    /**
     * 获取玩家的连招统计
     */
    public Map<String, Object> getPlayerComboStats(Integer playerId) {
        List<PlayerSkillComboRecord> allRecords = playerSkillComboRecordMapper.findRecentRecords(playerId, 1000);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSkillUses", allRecords.size());

        // 统计各连招触发次数
        Map<Integer, Long> comboTriggerCounts = allRecords.stream()
                .filter(r -> r.getTriggeredCombo() && r.getComboId() != null)
                .collect(Collectors.groupingBy(PlayerSkillComboRecord::getComboId, Collectors.counting()));
        stats.put("comboTriggerCounts", comboTriggerCounts);

        // 计算总连招次数
        long totalCombos = comboTriggerCounts.values().stream().mapToLong(Long::longValue).sum();
        stats.put("totalCombos", totalCombos);

        return stats;
    }

    // ===================== 供EnhancedCombatService使用的接口（模块边界规范） =====================

    /**
     * 根据ID获取技能模板（供EnhancedCombatService使用）
     */
    public Skill getSkillById(Integer skillId) {
        return skillMapper.selectById(skillId);
    }

    /**
     * 根据玩家ID和技能ID获取玩家技能（供EnhancedCombatService使用）
     */
    public PlayerSkill getPlayerSkillByPlayerAndSkill(Integer playerId, Integer skillId) {
        return playerSkillMapper.selectByPlayerIdAndSkillId(playerId, skillId);
    }
}
