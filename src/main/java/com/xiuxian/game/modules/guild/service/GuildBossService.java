package com.xiuxian.game.modules.guild.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// guild module entities (same module -- OK)
import com.xiuxian.game.modules.guild.entity.GuildBoss;
import com.xiuxian.game.modules.guild.entity.GuildBossChallenge;
import com.xiuxian.game.modules.guild.entity.GuildMember;
// guild module mappers (same module -- OK)
import com.xiuxian.game.modules.guild.mapper.GuildBossChallengeMapper;
import com.xiuxian.game.modules.guild.mapper.GuildBossMapper;
import com.xiuxian.game.modules.guild.mapper.GuildMemberMapper;
// cross-module entities accessed via Service interfaces
import com.xiuxian.game.modules.player.entity.PlayerProfile;
// cross-module services (module boundary)
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.common.util.LogUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 宗门BOSS服务
 * 负责BOSS刷新、协作挑战、伤害统计、奖励分配
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GuildBossService {

    private final GuildBossMapper guildBossMapper;
    private final GuildBossChallengeMapper challengeMapper;
    private final GuildMemberMapper guildMemberMapper;
    // module boundary: access player data via PlayerService
    private final PlayerService playerService;

    /** 每人每天最多挑战次数 */
    private static final int MAX_DAILY_ATTEMPTS = 5;
    /** BOSS每周一刷新 */
    private static final int BOSS_RESPAWN_DAYS = 7;

    // ==================== BOSS 预设配置 ====================
    private static final List<BossTemplate> BOSS_TEMPLATES = Arrays.asList(
        new BossTemplate("妖皇魃影", "远古妖皇的残魂，凶悍无比",  5,  500_000L, 2800, 400, 5000, 8000),
        new BossTemplate("血魔尸王", "以怨气凝聚的不死尸王",     10, 2_000_000L, 6000, 900, 12000, 20000),
        new BossTemplate("冥狱火龙", "来自冥狱的上古火龙",       15, 8_000_000L, 12000, 1800, 30000, 50000),
        new BossTemplate("太虚古魔", "太虚时代留存的上古大魔",   20, 30_000_000L, 25000, 3500, 80000, 120000)
    );

    /**
     * 获取当前宗门BOSS（若未刷新则自动生成）
     */
    public GuildBossVO getCurrentBoss(Integer playerId) {
        Integer guildId = getPlayerGuildId(playerId);

        GuildBoss boss = guildBossMapper.findAliveByGuildId(guildId);
        if (boss == null) {
            boss = spawnBoss(guildId);
        }

        GuildBossChallenge myRecord = challengeMapper.findByBossAndPlayer(boss.getId(), playerId);
        List<GuildBossChallenge> allChallenges = challengeMapper.findByBossIdOrderByDamage(boss.getId());

        return buildBossVO(boss, myRecord, allChallenges, playerId);
    }

    /**
     * 挑战宗门BOSS
     */
    @Transactional
    public ChallengeResult challengeBoss(Integer playerId) {
        Integer guildId = getPlayerGuildId(playerId);
        GuildBoss boss = guildBossMapper.findAliveByGuildId(guildId);

        if (boss == null) {
            boss = spawnBoss(guildId);
        }
        if ("DEFEATED".equals(boss.getStatus())) {
            throw new BusinessException(ErrorCode.GUILD_BOSS_ALREADY_DEFEATED);
        }

        // 检查每日次数
        GuildBossChallenge record = challengeMapper.findByBossAndPlayer(boss.getId(), playerId);
        if (record != null) {
            int todayAttempts = getTodayAttempts(record);
            if (todayAttempts >= MAX_DAILY_ATTEMPTS) {
                throw new BusinessException(ErrorCode.GUILD_BOSS_DAILY_LIMIT_REACHED);
            }
        }

        // 计算伤害（基于玩家战斗属性）
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        long damage = calculateDamage(player, boss);

        // 扣除BOSS生命值
        boss.setCurrentHealth(Math.max(0, boss.getCurrentHealth() - damage));
        boolean bossDefeated = boss.getCurrentHealth() <= 0;
        if (bossDefeated) {
            boss.setStatus("DEFEATED");
            boss.setDefeatedAt(LocalDateTime.now());
        }
        guildBossMapper.updateById(boss);

        // 更新挑战记录
        updateChallengeRecord(boss.getId(), playerId, damage, record);

        // BOSS被击败时分配奖励
        if (bossDefeated) {
            distributeRewards(boss);
        }

        log.info("[GuildBoss] 玩家{}挑战BOSS{}造成{}伤害, 剩余血量{}", playerId, boss.getId(), damage, boss.getCurrentHealth());
        LogUtils.logBusiness("GUILD_BOSS", "挑战BOSS", "playerId", playerId, "damage", damage, "bossDefeated", bossDefeated);

        return ChallengeResult.builder()
                .damage(damage)
                .bossDefeated(bossDefeated)
                .bossRemainingHealth(boss.getCurrentHealth())
                .bossMaxHealth(boss.getMaxHealth())
                .remainingAttempts(MAX_DAILY_ATTEMPTS - getTodayAttempts(record) - 1)
                .build();
    }

    /**
     * 领取BOSS击败奖励
     */
    @Transactional
    public Map<String, Object> claimReward(Integer playerId) {
        Integer guildId = getPlayerGuildId(playerId);

        // 找最近击败的BOSS
        LambdaQueryWrapper<GuildBoss> bossQuery = new LambdaQueryWrapper<GuildBoss>()
                .eq(GuildBoss::getGuildId, guildId)
                .eq(GuildBoss::getStatus, "DEFEATED")
                .orderByDesc(GuildBoss::getDefeatedAt)
                .last("LIMIT 1");
        GuildBoss boss = guildBossMapper.selectOne(bossQuery);

        if (boss == null) throw new BusinessException(ErrorCode.GUILD_BOSS_NOT_DEFEATED);

        GuildBossChallenge record = challengeMapper.findByBossAndPlayer(boss.getId(), playerId);
        if (record == null || record.getDamageDealt() == 0) {
            throw new BusinessException(ErrorCode.GUILD_BOSS_NO_CONTRIBUTION);
        }
        if (Boolean.TRUE.equals(record.getRewardClaimed())) {
            throw new BusinessException(ErrorCode.GUILD_BOSS_REWARD_CLAIMED);
        }

        // 发放奖励
        int stones = record.getPersonalRewardStones() != null ? record.getPersonalRewardStones() : 0;
        int exp = (int) (boss.getRewardExp() * getDamageRatio(boss.getId(), playerId));

        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        player.setSpiritStones(player.getSpiritStones() + stones);
        player.setExp(player.getExp() + exp);
        playerService.savePlayerProfile(player);

        record.setRewardClaimed(true);
        challengeMapper.updateById(record);

        log.info("[GuildBoss] 玩家{}领取BOSS奖励: 灵石+{}, 经验+{}", playerId, stones, exp);

        Map<String, Object> result = new HashMap<>();
        result.put("spiritStones", stones);
        result.put("exp", exp);
        result.put("message", "成功击败" + boss.getName() + "！获得奖励");
        return result;
    }

    // ==================== 私有方法 ====================

    private GuildBoss spawnBoss(Integer guildId) {
        // 根据宗门成员平均等级选择BOSS模板
        double avgLevel = getGuildAvgLevel(guildId);
        BossTemplate template = selectBossTemplate(avgLevel);

        GuildBoss boss = new GuildBoss();
        boss.setGuildId(guildId);
        boss.setName(template.name);
        boss.setDescription(template.description);
        boss.setLevel(template.level);
        boss.setMaxHealth(template.maxHealth);
        boss.setCurrentHealth(template.maxHealth);
        boss.setAttack(template.attack);
        boss.setDefense(template.defense);
        boss.setStatus("ALIVE");
        boss.setRewardSpiritStones(template.rewardStones);
        boss.setRewardExp(template.rewardExp);
        boss.setSpawnedAt(LocalDateTime.now());
        boss.setNextSpawnAt(LocalDateTime.now().plusDays(BOSS_RESPAWN_DAYS));
        guildBossMapper.insert(boss);
        log.info("[GuildBoss] 宗门{}刷新BOSS: {}", guildId, template.name);
        return boss;
    }

    private long calculateDamage(PlayerProfile player, GuildBoss boss) {
        int attack = player.getAttack() != null ? player.getAttack() : 100;
        int defense = boss.getDefense() != null ? boss.getDefense() : 50;
        // 基础伤害 = 攻击 - 防御/2，带随机浮动±10%
        double base = Math.max(attack - defense / 2.0, attack * 0.1);
        double random = 0.9 + ThreadLocalRandom.current().nextDouble() * 0.2;
        // 连招技能加成：10%暴击概率 × 2倍
        boolean crit = ThreadLocalRandom.current().nextDouble() < 0.10;
        return Math.round(base * random * (crit ? 2.0 : 1.0));
    }

    private void updateChallengeRecord(Integer bossId, Integer playerId, long damage, GuildBossChallenge existing) {
        if (existing == null) {
            existing = new GuildBossChallenge();
            existing.setBossId(bossId);
            existing.setPlayerId(playerId);
            existing.setDamageDealt(damage);
            existing.setTodayAttempts(1);
            existing.setLastChallengeAt(LocalDateTime.now());
            existing.setRewardClaimed(false);
            challengeMapper.insert(existing);
        } else {
            existing.setDamageDealt(existing.getDamageDealt() + damage);
            existing.setTodayAttempts(getTodayAttempts(existing) + 1);
            existing.setLastChallengeAt(LocalDateTime.now());
            challengeMapper.updateById(existing);
        }
    }

    private void distributeRewards(GuildBoss boss) {
        List<GuildBossChallenge> all = challengeMapper.findByBossIdOrderByDamage(boss.getId());
        long totalDamage = all.stream().mapToLong(GuildBossChallenge::getDamageDealt).sum();
        if (totalDamage == 0) return;

        for (GuildBossChallenge c : all) {
            double ratio = (double) c.getDamageDealt() / totalDamage;
            int personalStones = (int) (boss.getRewardSpiritStones() * ratio);
            c.setPersonalRewardStones(personalStones);
            challengeMapper.updateById(c);
        }
    }

    private double getDamageRatio(Integer bossId, Integer playerId) {
        List<GuildBossChallenge> all = challengeMapper.findByBossIdOrderByDamage(bossId);
        long total = all.stream().mapToLong(GuildBossChallenge::getDamageDealt).sum();
        long mine = all.stream().filter(c -> c.getPlayerId().equals(playerId))
                .mapToLong(GuildBossChallenge::getDamageDealt).sum();
        return total > 0 ? (double) mine / total : 0;
    }

    private int getTodayAttempts(GuildBossChallenge record) {
        if (record == null) return 0;
        if (record.getLastChallengeAt() == null) return 0;
        // 如果最后挑战时间是今天，返回累计次数；否则返回0
        if (record.getLastChallengeAt().toLocalDate().equals(java.time.LocalDate.now())) {
            return record.getTodayAttempts() != null ? record.getTodayAttempts() : 0;
        }
        return 0;
    }

    private Integer getPlayerGuildId(Integer playerId) {
        LambdaQueryWrapper<GuildMember> query = new LambdaQueryWrapper<GuildMember>()
                .eq(GuildMember::getPlayerId, playerId);
        GuildMember member = guildMemberMapper.selectOne(query);
        if (member == null) throw new BusinessException(ErrorCode.GUILD_NOT_MEMBER);
        return member.getGuildId();
    }

    private double getGuildAvgLevel(Integer guildId) {
        LambdaQueryWrapper<GuildMember> query = new LambdaQueryWrapper<GuildMember>()
                .eq(GuildMember::getGuildId, guildId);
        List<GuildMember> members = guildMemberMapper.selectList(query);
        if (members.isEmpty()) return 5;
        return members.stream()
                .mapToInt(m -> {
                    PlayerProfile p = playerService.getPlayerProfileById(m.getPlayerId());
                    return p != null && p.getLevel() != null ? p.getLevel() : 1;
                })
                .average().orElse(5);
    }

    private BossTemplate selectBossTemplate(double avgLevel) {
        for (int i = BOSS_TEMPLATES.size() - 1; i >= 0; i--) {
            if (avgLevel >= BOSS_TEMPLATES.get(i).level - 3) {
                return BOSS_TEMPLATES.get(i);
            }
        }
        return BOSS_TEMPLATES.get(0);
    }

    private GuildBossVO buildBossVO(GuildBoss boss, GuildBossChallenge myRecord,
                                    List<GuildBossChallenge> all, Integer playerId) {
        long totalDamage = all.stream().mapToLong(GuildBossChallenge::getDamageDealt).sum();
        int rank = 1;
        long myDamage = 0;
        if (myRecord != null) {
            myDamage = myRecord.getDamageDealt();
            rank = (int) all.stream().filter(c -> c.getDamageDealt() > myDamage).count() + 1;
        }

        // 构建伤害排行
        List<Map<String, Object>> damageRanking = new ArrayList<>();
        for (int i = 0; i < Math.min(all.size(), 10); i++) {
            GuildBossChallenge c = all.get(i);
            Map<String, Object> entry = new HashMap<>();
            entry.put("rank", i + 1);
            entry.put("playerId", c.getPlayerId());
            PlayerProfile p = playerService.getPlayerProfileById(c.getPlayerId());
            entry.put("playerName", p != null ? p.getNickname() : "未知");
            entry.put("damage", c.getDamageDealt());
            entry.put("ratio", totalDamage > 0 ? String.format("%.1f%%", c.getDamageDealt() * 100.0 / totalDamage) : "0%");
            damageRanking.add(entry);
        }

        return GuildBossVO.builder()
                .id(boss.getId())
                .name(boss.getName())
                .description(boss.getDescription())
                .level(boss.getLevel())
                .maxHealth(boss.getMaxHealth())
                .currentHealth(boss.getCurrentHealth())
                .status(boss.getStatus())
                .healthPercent((int) (boss.getCurrentHealth() * 100 / boss.getMaxHealth()))
                .rewardSpiritStones(boss.getRewardSpiritStones())
                .rewardExp(boss.getRewardExp())
                .spawnedAt(boss.getSpawnedAt())
                .nextSpawnAt(boss.getNextSpawnAt())
                .defeatedAt(boss.getDefeatedAt())
                .myDamage(myDamage)
                .myDamageRatio(totalDamage > 0 ? String.format("%.1f%%", myDamage * 100.0 / totalDamage) : "0%")
                .myRank(rank)
                .myTodayAttempts(getTodayAttempts(myRecord))
                .maxDailyAttempts(MAX_DAILY_ATTEMPTS)
                .remainingAttempts(MAX_DAILY_ATTEMPTS - getTodayAttempts(myRecord))
                .canClaimReward("DEFEATED".equals(boss.getStatus()) && myRecord != null
                        && myRecord.getDamageDealt() > 0 && !Boolean.TRUE.equals(myRecord.getRewardClaimed()))
                .rewardClaimed(myRecord != null && Boolean.TRUE.equals(myRecord.getRewardClaimed()))
                .totalParticipants(all.size())
                .totalDamage(totalDamage)
                .damageRanking(damageRanking)
                .build();
    }

    // ==================== VO & DTO ====================

    @Data
    @lombok.Builder
    public static class GuildBossVO {
        private Integer id;
        private String name;
        private String description;
        private Integer level;
        private Long maxHealth;
        private Long currentHealth;
        private String status;
        private Integer healthPercent;
        private Integer rewardSpiritStones;
        private Integer rewardExp;
        private LocalDateTime spawnedAt;
        private LocalDateTime nextSpawnAt;
        private LocalDateTime defeatedAt;
        // 玩家个人数据
        private Long myDamage;
        private String myDamageRatio;
        private Integer myRank;
        private Integer myTodayAttempts;
        private Integer maxDailyAttempts;
        private Integer remainingAttempts;
        private Boolean canClaimReward;
        private Boolean rewardClaimed;
        // 宗门整体
        private Integer totalParticipants;
        private Long totalDamage;
        private List<Map<String, Object>> damageRanking;
    }

    @Data
    @lombok.Builder
    public static class ChallengeResult {
        private Long damage;
        private Boolean bossDefeated;
        private Long bossRemainingHealth;
        private Long bossMaxHealth;
        private Integer remainingAttempts;
    }

    /** BOSS模板（内部配置） */
    @Data
    private static class BossTemplate {
        final String name, description;
        final int level, attack, defense;
        final long maxHealth;
        final int rewardStones, rewardExp;

        BossTemplate(String n, String d, int lv, long hp, int atk, int def, int stones, int exp) {
            name = n; description = d; level = lv; maxHealth = hp;
            attack = atk; defense = def; rewardStones = stones; rewardExp = exp;
        }
    }
}
