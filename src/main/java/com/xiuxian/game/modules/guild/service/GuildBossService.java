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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 仙盟BOSS战斗管理类
 *
 * <p>提供仙盟BOSS的生成、挑战、奖励领取等核心功能</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>获取仙盟BOSS信息 - 获取当前仙盟的BOSS状态</li>
 *   <li>挑战仙盟BOSS - 玩家挑战BOSS造成伤害</li>
 *   <li>领取BOSS击杀奖励 - BOSS被击败后领取奖励</li>
 *   <li>BOSS生成 - 根据仙盟平均等级生成匹配的BOSS</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
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

    /** 每日最大挑战次数 */
    private static final int MAX_DAILY_ATTEMPTS = 5;
    /** BOSS复活周期（天） */
    private static final int BOSS_RESPAWN_DAYS = 7;
    private final Map<Integer, Object> bossSpawnLocks = new ConcurrentHashMap<>();

    // ==================== BOSS 模板定义 ====================
    private static final List<BossTemplate> BOSS_TEMPLATES = Arrays.asList(
        new BossTemplate("青牛兽", "筑基境界精英魔兽",  5,  500_000L, 2800, 400, 5000, 8000),
        new BossTemplate("修罗牛魔王", "金丹境界凶猛魔兽",     10, 2_000_000L, 6000, 900, 12000, 20000),
        new BossTemplate("九幽冥凤", "元婴境界远古神兽",       15, 8_000_000L, 12000, 1800, 30000, 50000),
        new BossTemplate("万妖狼王", "化神境界绝世妖王",   20, 30_000_000L, 25000, 3500, 80000, 120000)
    );

    /**
     * 获取仙盟BOSS信息
     *
     * <p>获取当前玩家所在仙盟的BOSS信息，包括BOSS状态、血量、伤害排名等</p>
     *
     * @param playerId 玩家ID
     * @return BOSS信息VO
     * @throws BusinessException 当玩家不是仙盟成员时抛出
     */
    public GuildBossVO getCurrentBoss(Integer playerId) {
        Integer guildId = getPlayerGuildId(playerId);
        GuildBoss boss = getBossForView(guildId);

        GuildBossChallenge myRecord = challengeMapper.findByBossAndPlayer(boss.getId(), playerId);
        List<GuildBossChallenge> allChallenges = challengeMapper.findByBossIdOrderByDamage(boss.getId());

        return buildBossVO(boss, myRecord, allChallenges, playerId);
    }

    /**
     * 挑战仙盟BOSS
     *
     * <p>玩家挑战仙盟BOSS，造成伤害并可能击败BOSS</p>
     *
     * <p>挑战流程：</p>
     * <ol>
     *   <li>校验玩家是否为仙盟成员</li>
     *   <li>校验每日挑战次数</li>
     *   <li>计算伤害并扣减BOSS血量</li>
     *   <li>更新挑战记录</li>
     *   <li>如果BOSS被击败，分发奖励</li>
     * </ol>
     *
     * @param playerId 玩家ID
     * @return 挑战结果
     * @throws BusinessException 当玩家不是仙盟成员或挑战次数已满时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public ChallengeResult challengeBoss(Integer playerId) {
        Integer guildId = getPlayerGuildId(playerId);
        GuildBoss boss = getOrCreateBoss(guildId);

        // 校验挑战次数
        GuildBossChallenge record = challengeMapper.findByBossAndPlayer(boss.getId(), playerId);
        validateChallengeAttempts(record);

        // 计算伤害 + 原子扣减BOSS血量
        long damage = calculateDamage(playerService.getPlayerProfileById(playerId), boss);
        applyDamageToBoss(boss, damage);

        // 重新查询BOSS获取最新状态
        boss = guildBossMapper.selectBossById(boss.getId());
        boolean bossDefeated = "DEFEATED".equals(boss.getStatus());

        // 更新挑战记录
        updateChallengeRecord(boss.getId(), playerId, damage, record);

        // BOSS死亡时分发奖励
        if (bossDefeated) {
            GuildBoss latestBoss = guildBossMapper.selectBossById(boss.getId());
            if (latestBoss != null && latestBoss.getDefeatedAt() != null
                    && latestBoss.getDefeatedAt().equals(boss.getDefeatedAt())) {
                distributeRewards(latestBoss);
            }
        }

        log.info("[GuildBoss] 玩家{}挑战BOSS{}造成伤害{}，BOSS剩余血量{}", playerId, boss.getId(), damage, boss.getCurrentHealth());
        LogUtils.logBusiness("GUILD_BOSS", "挑战仙盟BOSS", "playerId", playerId, "damage", damage, "bossDefeated", bossDefeated);

        return ChallengeResult.builder()
                .damage(damage)
                .bossDefeated(bossDefeated)
                .bossRemainingHealth(boss.getCurrentHealth())
                .bossMaxHealth(boss.getMaxHealth())
                .remainingAttempts(Math.max(0, MAX_DAILY_ATTEMPTS - getTodayAttempts(record) - 1))
                .build();
    }

    /**
     * 获取或创建BOSS
     *
     * <p>获取当前仙盟的BOSS，如果不存在则创建新的BOSS</p>
     *
     * @param guildId 仙盟ID
     * @return BOSS实体
     * @throws BusinessException 当BOSS已被击败时抛出
     */
    private GuildBoss getOrCreateBoss(Integer guildId) {
        synchronized (getBossLock(guildId)) {
            GuildBoss boss = guildBossMapper.findAliveByGuildId(guildId);
            if (boss != null) {
                return boss;
            }

            GuildBoss latestBoss = guildBossMapper.findLatestByGuildId(guildId);
            if (isBossRespawnPending(latestBoss)) {
                throw new BusinessException(ErrorCode.GUILD_BOSS_ALREADY_DEFEATED, "宗门BOSS尚未刷新");
            }

            return spawnBoss(guildId);
        }
    }

    /**
     * 校验每日挑战次数
     *
     * <p>校验玩家今日挑战次数是否已达到上限</p>
     *
     * @param record 挑战记录
     * @throws BusinessException 当挑战次数已满时抛出
     */
    private void validateChallengeAttempts(GuildBossChallenge record) {
        if (record != null && getTodayAttempts(record) >= MAX_DAILY_ATTEMPTS) {
            throw new BusinessException(ErrorCode.GUILD_BOSS_DAILY_LIMIT_REACHED);
        }
    }

    /**
     * 原子扣减BOSS血量
     *
     * <p>使用原子操作扣减BOSS血量，防止并发问题</p>
     *
     * @param boss BOSS实体
     * @param damage 伤害值
     * @throws BusinessException 当BOSS已被击败时抛出
     */
    private void applyDamageToBoss(GuildBoss boss, long damage) {
        LocalDateTime now = LocalDateTime.now();
        int rows = guildBossMapper.atomicDamage(boss.getId(), damage, now);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.GUILD_BOSS_ALREADY_DEFEATED);
        }
    }

    /**
     * 领取BOSS击杀奖励
     *
     * <p>当仙盟BOSS被击败后，玩家领取击杀奖励</p>
     *
     * <p>领取流程：</p>
     * <ol>
     *   <li>校验BOSS是否已被击败</li>
     *   <li>校验玩家是否有贡献</li>
     *   <li>校验奖励是否已领取</li>
     *   <li>发放个人奖励（灵石和经验）</li>
     *   <li>更新领取状态</li>
     * </ol>
     *
     * @param playerId 玩家ID
     * @return 奖励内容
     * @throws BusinessException 当BOSS未被击败、无贡献或奖励已领取时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> claimReward(Integer playerId) {
        Integer guildId = getPlayerGuildId(playerId);

        // 查询已击杀的仙盟BOSS
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

        if (challengeMapper.markRewardClaimed(record.getId()) == 0) {
            throw new BusinessException(ErrorCode.GUILD_BOSS_REWARD_CLAIMED);
        }

        // 发放个人奖励
        int stones = record.getPersonalRewardStones() != null ? record.getPersonalRewardStones() : 0;
        int exp = (int) (boss.getRewardExp() * getDamageRatio(boss.getId(), playerId));

        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        player.setSpiritStones(player.getSpiritStones() + stones);
        player.setExp(player.getExp() + exp);
        playerService.applyLevelUpsWithoutCommit(player, 100);
        playerService.savePlayerProfile(player);

        log.info("[GuildBoss] 玩家{}领取仙盟BOSS奖励: 灵石+{}, 经验+{}", playerId, stones, exp);

        Map<String, Object> result = new HashMap<>();
        result.put("spiritStones", stones);
        result.put("exp", exp);
        result.put("message", "恭喜击杀" + boss.getName() + "获得灵石奖励");
        return result;
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 生成BOSS
     *
     * <p>根据仙盟平均等级生成匹配的BOSS</p>
     *
     * @param guildId 仙盟ID
     * @return 生成的BOSS实体
     */
    private GuildBoss spawnBoss(Integer guildId) {
        // 根据仙盟平均等级生成匹配的BOSS
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
        log.info("[GuildBoss] 仙盟{}召唤BOSS: {}", guildId, template.name);
        return boss;
    }

    /**
     * 计算伤害
     *
     * <p>根据玩家属性和BOSS属性计算伤害值</p>
     *
     * @param player 玩家档案
     * @param boss BOSS实体
     * @return 伤害值
     */
    private long calculateDamage(PlayerProfile player, GuildBoss boss) {
        int attack = player.getAttack() != null ? player.getAttack() : 100;
        int defense = boss.getDefense() != null ? boss.getDefense() : 50;
        // 基础伤害 = 攻击 - 防御/2（最低为攻击的10%）
        double base = Math.max(attack - defense / 2.0, attack * 0.1);
        double random = 0.9 + ThreadLocalRandom.current().nextDouble() * 0.2;
        // 有10%概率暴击，暴击伤害翻2倍
        boolean crit = ThreadLocalRandom.current().nextDouble() < 0.10;
        return Math.round(base * random * (crit ? 2.0 : 1.0));
    }

    /**
     * 更新挑战记录
     *
     * <p>更新或创建玩家的挑战记录</p>
     *
     * @param bossId BOSS ID
     * @param playerId 玩家ID
     * @param damage 伤害值
     * @param existing 现有记录
     */
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

    /**
     * 分发奖励
     *
     * <p>当BOSS被击败后，根据伤害比例分发奖励给所有参与者</p>
     *
     * @param boss BOSS实体
     */
    private void distributeRewards(GuildBoss boss) {
        synchronized (this) {
        List<GuildBossChallenge> all = challengeMapper.findByBossIdOrderByDamage(boss.getId());
        if (all.isEmpty()) return;
        long totalDamage = all.stream().mapToLong(GuildBossChallenge::getDamageDealt).sum();
        if (totalDamage == 0) return;

        if (all.stream().anyMatch(c -> c.getPersonalRewardStones() != null && c.getPersonalRewardStones() > 0)) {
            return;
        }

        // 计算各参与者奖励
        for (GuildBossChallenge c : all) {
            double ratio = (double) c.getDamageDealt() / totalDamage;
            int personalStones = (int) (boss.getRewardSpiritStones() * ratio);
            c.setPersonalRewardStones(personalStones);
        }
        // 批量更新，替代循环 updateById（避免N次DB往返）
        challengeMapper.batchUpdateRewardStones(all);
        }
    }

    private GuildBoss getBossForView(Integer guildId) {
        synchronized (getBossLock(guildId)) {
            GuildBoss boss = guildBossMapper.findAliveByGuildId(guildId);
            if (boss != null) {
                return boss;
            }

            GuildBoss latestBoss = guildBossMapper.findLatestByGuildId(guildId);
            if (isBossRespawnPending(latestBoss)) {
                return latestBoss;
            }

            return spawnBoss(guildId);
        }
    }

    private boolean isBossRespawnPending(GuildBoss boss) {
        return boss != null && boss.getNextSpawnAt() != null && boss.getNextSpawnAt().isAfter(LocalDateTime.now());
    }

    private Object getBossLock(Integer guildId) {
        return bossSpawnLocks.computeIfAbsent(guildId, id -> new Object());
    }

    /**
     * 获取伤害比例
     *
     * <p>计算玩家伤害占总伤害的比例</p>
     *
     * @param bossId BOSS ID
     * @param playerId 玩家ID
     * @return 伤害比例
     */
    private double getDamageRatio(Integer bossId, Integer playerId) {
        List<GuildBossChallenge> all = challengeMapper.findByBossIdOrderByDamage(bossId);
        long total = all.stream().mapToLong(GuildBossChallenge::getDamageDealt).sum();
        long mine = all.stream().filter(c -> c.getPlayerId().equals(playerId))
                .mapToLong(GuildBossChallenge::getDamageDealt).sum();
        return total > 0 ? (double) mine / total : 0;
    }

    /**
     * 获取今日挑战次数
     *
     * <p>获取玩家今日的挑战次数，如果跨天则重置为0</p>
     *
     * @param record 挑战记录
     * @return 今日挑战次数
     */
    private int getTodayAttempts(GuildBossChallenge record) {
        if (record == null) return 0;
        if (record.getLastChallengeAt() == null) return 0;
        // 挑战日期是今天则返回当日次数，否则返回0（新的一天重置次数）
        if (record.getLastChallengeAt().toLocalDate().equals(java.time.LocalDate.now())) {
            return record.getTodayAttempts() != null ? record.getTodayAttempts() : 0;
        }
        return 0;
    }

    /**
     * 获取玩家仙盟ID
     *
     * <p>获取玩家所在的仙盟ID</p>
     *
     * @param playerId 玩家ID
     * @return 仙盟ID
     * @throws BusinessException 当玩家不是仙盟成员时抛出
     */
    private Integer getPlayerGuildId(Integer playerId) {
        LambdaQueryWrapper<GuildMember> query = new LambdaQueryWrapper<GuildMember>()
                .eq(GuildMember::getPlayerId, playerId);
        GuildMember member = guildMemberMapper.selectOne(query);
        if (member == null) throw new BusinessException(ErrorCode.GUILD_NOT_MEMBER);
        return member.getGuildId();
    }

    /**
     * 获取仙盟平均等级
     *
     * <p>获取仙盟所有成员的平均等级</p>
     *
     * @param guildId 仙盟ID
     * @return 平均等级
     */
    private double getGuildAvgLevel(Integer guildId) {
        LambdaQueryWrapper<GuildMember> query = new LambdaQueryWrapper<GuildMember>()
                .eq(GuildMember::getGuildId, guildId);
        List<GuildMember> members = guildMemberMapper.selectList(query);
        if (members.isEmpty()) return 5;

        // 批量加载玩家信息（避免N+1查询）
        List<Integer> playerIds = members.stream().map(GuildMember::getPlayerId).distinct().collect(Collectors.toList());
        Map<Integer, PlayerProfile> playerMap = playerService.getPlayerProfilesByIds(playerIds);

        return members.stream()
                .mapToInt(m -> {
                    PlayerProfile p = playerMap.get(m.getPlayerId());
                    return p != null && p.getLevel() != null ? p.getLevel() : 1;
                })
                .average().orElse(5);
    }

    /**
     * 选择BOSS模板
     *
     * <p>根据平均等级选择合适的BOSS模板</p>
     *
     * @param avgLevel 平均等级
     * @return BOSS模板
     */
    private BossTemplate selectBossTemplate(double avgLevel) {
        for (int i = BOSS_TEMPLATES.size() - 1; i >= 0; i--) {
            if (avgLevel >= BOSS_TEMPLATES.get(i).level - 3) {
                return BOSS_TEMPLATES.get(i);
            }
        }
        return BOSS_TEMPLATES.get(0);
    }

    /**
     * 构建BOSS VO
     *
     * <p>构建BOSS信息VO对象</p>
     *
     * @param boss BOSS实体
     * @param myRecord 玩家挑战记录
     * @param all 所有挑战记录
     * @param playerId 玩家ID
     * @return BOSS信息VO
     */
    private GuildBossVO buildBossVO(GuildBoss boss, GuildBossChallenge myRecord,
                                    List<GuildBossChallenge> all, Integer playerId) {
        long totalDamage = all.stream().mapToLong(GuildBossChallenge::getDamageDealt).sum();
        int rank = calculatePlayerRank(myRecord, all);
        long myDamage = (myRecord != null) ? myRecord.getDamageDealt() : 0;

        List<Map<String, Object>> damageRanking = buildDamageRanking(all, totalDamage);
        int todayAttempts = getTodayAttempts(myRecord);

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
                .myTodayAttempts(todayAttempts)
                .maxDailyAttempts(MAX_DAILY_ATTEMPTS)
                .remainingAttempts(MAX_DAILY_ATTEMPTS - todayAttempts)
                .canClaimReward("DEFEATED".equals(boss.getStatus()) && myRecord != null
                        && myRecord.getDamageDealt() > 0 && !Boolean.TRUE.equals(myRecord.getRewardClaimed()))
                .rewardClaimed(myRecord != null && Boolean.TRUE.equals(myRecord.getRewardClaimed()))
                .totalParticipants(all.size())
                .totalDamage(totalDamage)
                .damageRanking(damageRanking)
                .build();
    }

    /**
     * 计算玩家排名
     *
     * <p>根据伤害值计算玩家在仙盟中的排名</p>
     *
     * @param myRecord 玩家挑战记录
     * @param all 所有挑战记录
     * @return 排名
     */
    private int calculatePlayerRank(GuildBossChallenge myRecord, List<GuildBossChallenge> all) {
        if (myRecord == null) return 0;
        final long myDamage = myRecord.getDamageDealt();
        return (int) all.stream().filter(c -> c.getDamageDealt() > myDamage).count() + 1;
    }

    /**
     * 构建伤害排行榜
     *
     * <p>构建伤害排行榜，批量加载玩家信息避免N+1查询</p>
     *
     * @param all 所有挑战记录
     * @param totalDamage 总伤害
     * @return 排行榜列表
     */
    private List<Map<String, Object>> buildDamageRanking(List<GuildBossChallenge> all, long totalDamage) {
        int rankLimit = Math.min(all.size(), 10);
        List<GuildBossChallenge> topChallenges = all.subList(0, rankLimit);
        List<Integer> playerIds = topChallenges.stream()
                .map(GuildBossChallenge::getPlayerId).distinct().collect(Collectors.toList());
        Map<Integer, PlayerProfile> playerMap = playerService.getPlayerProfilesByIds(playerIds);

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (int i = 0; i < rankLimit; i++) {
            GuildBossChallenge c = topChallenges.get(i);
            Map<String, Object> entry = new HashMap<>();
            entry.put("rank", i + 1);
            entry.put("playerId", c.getPlayerId());
            PlayerProfile p = playerMap.get(c.getPlayerId());
            entry.put("playerName", p != null ? p.getNickname() : "未知玩家");
            entry.put("damage", c.getDamageDealt());
            entry.put("ratio", totalDamage > 0 ? String.format("%.1f%%", c.getDamageDealt() * 100.0 / totalDamage) : "0%");
            ranking.add(entry);
        }
        return ranking;
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
        // 个人伤害统计
        private Long myDamage;
        private String myDamageRatio;
        private Integer myRank;
        private Integer myTodayAttempts;
        private Integer maxDailyAttempts;
        private Integer remainingAttempts;
        private Boolean canClaimReward;
        private Boolean rewardClaimed;
        // 仙盟总体数据
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

    /** BOSS模板数据 */
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
