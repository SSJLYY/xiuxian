package com.xiuxian.game.service;

import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.PlayerSkill;
import com.xiuxian.game.entity.Skill;
import com.xiuxian.game.dto.response.SkillResponse;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.mapper.PlayerSkillMapper;
import com.xiuxian.game.mapper.SkillMapper;
import com.xiuxian.game.util.GameCalculator;
import com.xiuxian.game.util.GameConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final PlayerProfileMapper playerProfileMapper;
    private final GameCalculator gameCalculator;

    public List<Skill> getAllSkills() {
        return skillMapper.selectList(null);
    }

    public List<Skill> getAvailableSkills(Integer playerId) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
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
        PlayerProfile player = playerProfileMapper.selectById(playerId);
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
                throw new IllegalArgumentException("灵石不足，需要 " + skill.getRequiredSpiritStones() + " 灵石");
            }
            
            long oldSpiritStones = player.getSpiritStones();
            player.setSpiritStones(oldSpiritStones - skill.getRequiredSpiritStones());
            playerProfileMapper.updateById(player);
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
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        int points = player.getSkillPoints() == null ? 0 : player.getSkillPoints();
        if (points <= 0) {
            throw new IllegalArgumentException(GameConstants.ERROR_INSUFFICIENT_RESOURCES + ": 技能点不足");
        }
        player.setSkillPoints(points - 1);
        playerProfileMapper.updateById(player);
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
    
}
