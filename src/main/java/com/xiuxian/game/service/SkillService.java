package com.xiuxian.game.service;

import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.PlayerSkill;
import com.xiuxian.game.entity.Skill;
import com.xiuxian.game.repository.PlayerProfileRepository;
import com.xiuxian.game.repository.PlayerSkillRepository;
import com.xiuxian.game.repository.SkillRepository;
import com.xiuxian.game.util.GameCalculator;
import com.xiuxian.game.util.GameConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import com.xiuxian.game.util.Java8Compatibility;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final PlayerSkillRepository playerSkillRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final GameCalculator gameCalculator;

    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    public List<Skill> getAvailableSkills(Integer playerId) {
        PlayerProfile player = playerProfileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("玩家不存在"));
        return skillRepository.findByUnlockLevelLessThanEqual(player.getLevel());
    }

    public List<PlayerSkill> getPlayerSkills(Integer playerId) {
        PlayerProfile player = playerProfileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("玩家不存在"));
        return playerSkillRepository.findByPlayer(player);
    }

    public List<PlayerSkill> getEquippedSkills(Integer playerId) {
        PlayerProfile player = playerProfileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("玩家不存在"));
        return playerSkillRepository.findByPlayerAndEquipped(player, true);
    }

    @Transactional
    public PlayerSkill learnSkill(Integer skillId, Integer playerId) {
        PlayerProfile player = playerProfileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("玩家不存在"));
        
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("技能不存在"));

        if (skill.getUnlockLevel() > player.getLevel()) {
            throw new IllegalArgumentException(GameConstants.ERROR_REQUIREMENTS_NOT_MET + ": 角色等级不足，无法学习该技能");
        }

        Optional<PlayerSkill> existingSkill = playerSkillRepository.findByPlayerAndSkill(player, skill);
        if (existingSkill.isPresent()) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 已经学习过该技能");
        }

        PlayerSkill playerSkill = PlayerSkill.builder()
                .player(player)
                .skill(skill)
                .level(1)
                .experience(0)
                .equipped(false)
                .slotNumber(0)
                .build();

        return playerSkillRepository.save(playerSkill);
    }

    /**
     * 为新玩家初始化基础技能
     */
    @Transactional
    public void initializePlayerSkills(PlayerProfile player) {
        // 获取所有1级解锁的技能
        List<Skill> basicSkills = skillRepository.findByUnlockLevelLessThanEqual(1);
        
        for (Skill skill : basicSkills) {
            // 检查玩家是否已经拥有该技能
            Optional<PlayerSkill> existingSkill = playerSkillRepository.findByPlayerAndSkill(player, skill);
            if (Java8Compatibility.isEmpty(existingSkill)) {
                PlayerSkill playerSkill = PlayerSkill.builder()
                        .player(player)
                        .skill(skill)
                        .level(1)
                        .experience(0)
                        .equipped(false)
                        .slotNumber(0)
                        .build();
                playerSkillRepository.save(playerSkill);
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
        Skill skill = playerSkill.getSkill();
        int skillLevel = playerSkill.getLevel();
        
        // 基础伤害 + (等级-1) * 每级伤害增长
        double damage = skill.getBaseDamage() + (skillLevel - 1) * skill.getDamagePerLevel();
        
        // 根据技能类型应用修正系数
        switch (skill.getSkillType()) {
            case "攻击":
                // 攻击技能保持原始伤害
                break;
            case "防御":
                // 防御技能返回减伤值
                damage = damage * 0.1; // 防御技能效果较弱
                break;
            case "辅助":
                // 辅助技能返回效果值
                damage = damage * 0.05; // 辅助技能效果更弱
                break;
            default:
                break;
        }
        
        return Math.max(0, damage);
    }

    /**
     * 获取技能冷却时间（秒）
     */
    public int getSkillCooldown(PlayerSkill playerSkill) {
        // 技能等级越高，冷却时间越短
        int baseCooldown = playerSkill.getSkill().getCooldown();
        int skillLevel = playerSkill.getLevel();
        
        // 每级减少0.5秒冷却时间，最低1秒
        int reducedCooldown = Math.max(1, baseCooldown - (skillLevel - 1) / 2);
        return reducedCooldown;
    }

    /**
     * 获取技能消耗法力
     */
    public int getSkillManaCost(PlayerSkill playerSkill) {
        // 技能等级越高，法力消耗略微增加
        int baseCost = playerSkill.getSkill().getManaCost();
        int skillLevel = playerSkill.getLevel();
        
        // 每级增加1点法力消耗
        return baseCost + (skillLevel - 1);
    }

    /**
     * 技能使用后增加经验
     */
    @Transactional
    public void addSkillExperience(Integer playerSkillId, int expGain) {
        PlayerSkill playerSkill = playerSkillRepository.findById(playerSkillId)
                .orElseThrow(() -> new IllegalArgumentException("玩家技能不存在"));
        
        playerSkill.setExperience(playerSkill.getExperience() + expGain);
        
        // 检查是否可以升级
        while (playerSkill.getExperience() >= calculateSkillUpgradeExp(playerSkill.getLevel()) 
                && playerSkill.getLevel() < playerSkill.getSkill().getMaxLevel()) {
            int requiredExp = calculateSkillUpgradeExp(playerSkill.getLevel());
            playerSkill.setExperience(playerSkill.getExperience() - requiredExp);
            playerSkill.setLevel(playerSkill.getLevel() + 1);
            
            // 升级奖励经验
            playerSkill.setExperience(playerSkill.getExperience() + 20);
        }
        
        playerSkillRepository.save(playerSkill);
    }

    @Transactional
    public PlayerSkill upgradeSkill(Integer playerSkillId, Integer playerId) {
        PlayerSkill playerSkill = playerSkillRepository.findById(playerSkillId)
                .orElseThrow(() -> new IllegalArgumentException(GameConstants.ERROR_SKILL_NOT_FOUND + ": 玩家技能不存在"));

        if (!playerSkill.getPlayer().getId().equals(playerId)) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 无权操作该技能");
        }

        if (playerSkill.getLevel() >= playerSkill.getSkill().getMaxLevel()) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 技能已达到最大等级");
        }

        // 计算升级所需经验（技能经验）
        int currentLevel = playerSkill.getLevel();
        int requiredExp = calculateSkillUpgradeExp(currentLevel);
        
        if (playerSkill.getExperience() < requiredExp) {
            throw new IllegalArgumentException(GameConstants.ERROR_INSUFFICIENT_RESOURCES + ": 技能经验不足，无法升级技能");
        }

        // 扣除经验并升级
        playerSkill.setExperience(playerSkill.getExperience() - requiredExp);
        playerSkill.setLevel(currentLevel + 1);

        // 升级时给予少量奖励经验，便于下次升级
        playerSkill.setExperience(playerSkill.getExperience() + 10);

        return playerSkillRepository.save(playerSkill);
    }

    @Transactional
    public PlayerSkill equipSkill(Integer playerSkillId, Integer slotNumber, Integer playerId) {
        PlayerSkill playerSkill = playerSkillRepository.findById(playerSkillId)
                .orElseThrow(() -> new IllegalArgumentException(GameConstants.ERROR_SKILL_NOT_FOUND + ": 玩家技能不存在"));

        if (!playerSkill.getPlayer().getId().equals(playerId)) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 无权操作该技能");
        }

        // 检查槽位是否已被占用
        PlayerProfile player = playerProfileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("玩家不存在"));
        playerSkillRepository.findByPlayerAndEquipped(player, true).stream()
                .filter(ps -> ps.getSlotNumber().equals(slotNumber))
                .forEach(ps -> {
                    ps.setEquipped(false);
                    ps.setSlotNumber(0);
                    playerSkillRepository.save(ps);
                });

        playerSkill.setEquipped(true);
        playerSkill.setSlotNumber(slotNumber);
        return playerSkillRepository.save(playerSkill);
    }

    @Transactional
    public PlayerSkill unequipSkill(Integer playerSkillId, Integer playerId) {
        PlayerSkill playerSkill = playerSkillRepository.findById(playerSkillId)
                .orElseThrow(() -> new IllegalArgumentException(GameConstants.ERROR_SKILL_NOT_FOUND + ": 玩家技能不存在"));

        if (!playerSkill.getPlayer().getId().equals(playerId)) {
            throw new IllegalArgumentException(GameConstants.ERROR_INVALID_OPERATION + ": 无权操作该技能");
        }

        playerSkill.setEquipped(false);
        playerSkill.setSlotNumber(0);
        return playerSkillRepository.save(playerSkill);
    }

    @Transactional
    public void initializeDefaultSkills() {
        if (skillRepository.count() == 0) {
            // 基础技能
            Skill basicAttack = Skill.builder()
                    .name("基础攻击")
                    .description("基础的攻击技能，对敌人造成物理伤害")
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(10.0)
                    .damagePerLevel(5.0)
                    .cooldown(0)
                    .manaCost(0)
                    .skillType("攻击")
                    .element("无")
                    .unlockLevel(1)
                    .build();
            
            Skill fireball = Skill.builder()
                    .name("火球术")
                    .description("释放一个火球，对敌人造成火属性伤害")
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(20.0)
                    .damagePerLevel(8.0)
                    .cooldown(3)
                    .manaCost(10)
                    .skillType("攻击")
                    .element("火")
                    .unlockLevel(5)
                    .build();
            
            Skill waterShield = Skill.builder()
                    .name("水盾术")
                    .description("创造一个水盾，减少受到的伤害")
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(0.0)
                    .damagePerLevel(0.0)
                    .cooldown(10)
                    .manaCost(15)
                    .skillType("防御")
                    .element("水")
                    .unlockLevel(8)
                    .build();
            
            Skill earthSpike = Skill.builder()
                    .name("地刺术")
                    .description("从地面召唤尖刺，对敌人造成土属性伤害")
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(25.0)
                    .damagePerLevel(10.0)
                    .cooldown(5)
                    .manaCost(20)
                    .skillType("攻击")
                    .element("土")
                    .unlockLevel(12)
                    .build();
            
            Skill windSlash = Skill.builder()
                    .name("风刃术")
                    .description("释放锋利的风刃，对敌人造成风属性伤害")
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(15.0)
                    .damagePerLevel(7.0)
                    .cooldown(2)
                    .manaCost(8)
                    .skillType("攻击")
                    .element("风")
                    .unlockLevel(10)
                    .build();
            
            skillRepository.save(basicAttack);
            skillRepository.save(fireball);
            skillRepository.save(waterShield);
            skillRepository.save(earthSpike);
            skillRepository.save(windSlash);
        }
    }
}