package com.xiuxian.game.service;

import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.PlayerSkill;
import com.xiuxian.game.entity.Skill;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.mapper.PlayerSkillMapper;
import com.xiuxian.game.mapper.SkillMapper;
import com.xiuxian.game.util.GameCalculator;
import com.xiuxian.game.util.GameConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    public List<PlayerSkill> getEquippedSkills(Integer playerId) {
        return playerSkillMapper.selectByPlayerIdAndEquipped(playerId, true);
    }

    @Transactional
    public PlayerSkill learnSkill(Integer skillId, Integer playerId) {
        PlayerProfile player = playerProfileMapper.selectById(playerId);
        if (player == null) throw new IllegalArgumentException("玩家不存在");
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) throw new IllegalArgumentException("技能不存在");
        if (skill.getUnlockLevel() > player.getLevel()) {
            throw new IllegalArgumentException(GameConstants.ERROR_REQUIREMENTS_NOT_MET + ": 角色等级不足，无法学习该技能");
        }
        PlayerSkill existing = playerSkillMapper.selectByPlayerIdAndSkillId(playerId, skillId);
        if (existing != null) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 已经学习过该技能");
        }
        PlayerSkill playerSkill = PlayerSkill.builder()
                .playerId(playerId)
                .skillId(skillId)
                .level(1)
                .experience(0)
                .equipped(false)
                .slotNumber(0)
                .build();
        playerSkillMapper.insert(playerSkill);
        return playerSkillMapper.selectById(playerSkill.getId());
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
        playerSkill.setExperience(playerSkill.getExperience() + 10);
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

    
}
