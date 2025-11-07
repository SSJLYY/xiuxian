package com.xiuxian.game.util;

import com.xiuxian.game.entity.PlayerProfile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class GameCalculator {

    private static final Map<String, BigDecimal> REALM_BONUS = Java8Compatibility.mapOf(
        "练气期", BigDecimal.ZERO,
        "筑基期", BigDecimal.valueOf(0.5),
        "金丹期", BigDecimal.ONE,
        "元婴期", BigDecimal.valueOf(2),
        "化神期", BigDecimal.valueOf(4),
        "合体期", BigDecimal.valueOf(8),
        "大乘期", BigDecimal.valueOf(16),
        "渡劫期", BigDecimal.valueOf(32)
    );

    public long calculateExpPerSecond(PlayerProfile player) {
        BigDecimal baseExp = BigDecimal.valueOf(10);
        BigDecimal realmBonus = REALM_BONUS.getOrDefault(player.getRealm(), BigDecimal.ZERO);
        BigDecimal cultivationSpeed = player.getCultivationSpeed();
        
        // 确保cultivationSpeed不为null，使用默认值1.0
        if (cultivationSpeed == null) {
            cultivationSpeed = BigDecimal.ONE;
        }

        return baseExp
            .multiply(BigDecimal.ONE.add(realmBonus))
            .multiply(cultivationSpeed)
            .longValue();
    }

    public long calculateSpiritStonesPerSecond(PlayerProfile player) {
        BigDecimal cultivationSpeed = player.getCultivationSpeed();
        
        // 确保cultivationSpeed不为null，使用默认值1.0
        if (cultivationSpeed == null) {
            cultivationSpeed = BigDecimal.ONE;
        }
        
        return Math.max(1, cultivationSpeed.longValue());
    }

    public void checkLevelUp(PlayerProfile player) {
        long expNeeded = player.getLevel() * 100L;
        while (player.getExp() >= expNeeded) {
            player.setExp(player.getExp() - expNeeded);
            player.setLevel(player.getLevel() + 1);
            updateRealm(player);
            expNeeded = player.getLevel() * 100L;
        }
    }

    private void updateRealm(PlayerProfile player) {
        int level = player.getLevel();
        if (level >= 2001) player.setRealm("渡劫期");
        else if (level >= 1501) player.setRealm("大乘期");
        else if (level >= 1001) player.setRealm("合体期");
        else if (level >= 701) player.setRealm("化神期");
        else if (level >= 401) player.setRealm("元婴期");
        else if (level >= 201) player.setRealm("金丹期");
        else if (level >= 101) player.setRealm("筑基期");
        else player.setRealm("练气期");
    }

    public long calculateOfflineRewards(PlayerProfile player, long offlineSeconds) {
        long maxOfflineTime = 24 * 60 * 60; // 24小时
        long effectiveTime = Math.min(offlineSeconds, maxOfflineTime);
        
        if (effectiveTime < 60) return 0; // 少于1分钟无奖励
        
        long expPerSecond = calculateExpPerSecond(player);
        return expPerSecond * effectiveTime;
    }

    public long calculateSkillUpgradeCost(int currentLevel) {
        return currentLevel * 100L;
    }

    public long calculateEquipmentEnhanceCost(int currentEnhanceLevel) {
        return (currentEnhanceLevel + 1) * 500L;
    }

    public int calculateMaxInventorySlots(int playerLevel) {
        return 20 + (playerLevel / 10) * 5; // 每10级增加5个格子
    }

    /**
     * 计算技能使用后获得的修炼经验
     */
    public long calculateSkillExpGain(int skillLevel, String skillType) {
        long baseExp = 10;
        
        // 根据技能类型调整经验获取
        switch (skillType) {
            case "攻击":
                return baseExp * skillLevel; // 攻击技能经验较多
            case "防御":
                return Math.round(baseExp * skillLevel * 0.8); // 防御技能经验稍少
            case "辅助":
                return Math.round(baseExp * skillLevel * 0.6); // 辅助技能经验最少
            default:
                return baseExp * skillLevel;
        }
    }

    /**
     * 计算技能伤害修正（基于玩家境界）
     */
    public double calculateRealmDamageBonus(String realm) {
        return REALM_BONUS.getOrDefault(realm, BigDecimal.ZERO).doubleValue();
    }

    /**
     * 计算技能效果修正（基于玩家等级）
     */
    public double calculateLevelEffectBonus(int playerLevel, int skillLevel) {
        // 玩家等级越高，技能效果越好
        double levelBonus = Math.min(playerLevel * 0.01, 2.0); // 最多200%加成
        double skillBonus = skillLevel * 0.05; // 每级技能增加5%效果
        
        return 1.0 + levelBonus + skillBonus;
    }

    /**
     * 计算技能冷却时间修正（基于玩家境界）
     */
    public double calculateRealmCooldownBonus(String realm) {
        // 高境界可以略微减少技能冷却时间
        BigDecimal bonus = REALM_BONUS.getOrDefault(realm, BigDecimal.ZERO);
        return Math.min(bonus.doubleValue() * 0.1, 0.5); // 最多减少50%冷却时间
    }
}
