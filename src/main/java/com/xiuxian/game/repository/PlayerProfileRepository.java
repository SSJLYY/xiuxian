package com.xiuxian.game.repository;

import com.xiuxian.game.entity.PlayerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Integer> {

    /**
     * 根据用户ID查找玩家档案
     */
    Optional<PlayerProfile> findByUserId(Integer userId);

    /**
     * 根据用户名查找玩家档案
     */
    @Query("SELECT pp FROM PlayerProfile pp JOIN pp.user u WHERE u.username = :username")
    Optional<PlayerProfile> findByUsername(@Param("username") String username);

    /**
     * 根据用户名查找玩家档案（方法名查询）
     */
    @Query("SELECT pp FROM PlayerProfile pp JOIN pp.user u WHERE u.username = :username")
    Optional<PlayerProfile> findByUserUsername(@Param("username") String username);

    /**
     * 根据昵称查找玩家档案
     */
    Optional<PlayerProfile> findByNickname(String nickname);

    /**
     * 根据等级范围查找玩家档案
     */
    List<PlayerProfile> findByLevelBetween(Integer minLevel, Integer maxLevel);

    /**
     * 根据境界查找玩家档案
     */
    List<PlayerProfile> findByRealm(String realm);

    /**
     * 查找等级大于等于指定值的玩家档案
     */
    List<PlayerProfile> findByLevelGreaterThanEqual(Integer level);

    /**
     * 查找等级小于等于指定值的玩家档案
     */
    List<PlayerProfile> findByLevelLessThanEqual(Integer level);

    /**
     * 查找在线玩家（修复：使用JPA方法名查询替代HQL）
     */
    List<PlayerProfile> findByLastOnlineTimeAfter(LocalDateTime since);

    /**
     * 查找不活跃玩家（修复：使用JPA方法名查询）
     */
    List<PlayerProfile> findByLastOnlineTimeBefore(LocalDateTime cutoff);

    /**
     * 查找灵石数量大于指定值的玩家档案
     */
    List<PlayerProfile> findBySpiritStonesGreaterThan(Long minSpiritStones);

    /**
     * 根据修炼时间排序查找玩家档案
     */
    List<PlayerProfile> findByOrderByTotalCultivationTimeDesc();

    /**
     * 根据贡献点排序查找玩家档案
     */
    List<PlayerProfile> findByOrderByContributionPointsDesc();

    /**
     * 检查昵称是否已存在
     */
    boolean existsByNickname(String nickname);

    /**
     * 检查用户ID是否已有玩家档案
     */
    boolean existsByUserId(Integer userId);

    /**
     * 统计不同境界的玩家数量（修复：使用原生SQL）
     */
    @Query(value = "SELECT realm, COUNT(*) FROM player_profiles GROUP BY realm", nativeQuery = true)
    List<Object[]> countPlayersByRealm();

    /**
     * 获取玩家排行榜（按等级和修炼时间）
     */
    List<PlayerProfile> findByOrderByLevelDescTotalCultivationTimeDesc();

    /**
     * 获取财富排行榜（按灵石数量）
     */
    List<PlayerProfile> findByOrderBySpiritStonesDesc();

    /**
     * 根据多个用户ID批量查找玩家档案
     */
    List<PlayerProfile> findByUserIdIn(List<Integer> userIds);

    /**
     * 查找需要清理的长时间离线玩家档案（修复：使用参数化查询）
     */
    @Query("SELECT pp FROM PlayerProfile pp WHERE pp.lastOnlineTime < :cutoffTime")
    List<PlayerProfile> findInactivePlayers(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 统计玩家总数
     */
    @Query("SELECT COUNT(pp) FROM PlayerProfile pp")
    Long countTotalPlayers();

    /**
     * 获取平均玩家等级
     */
    @Query("SELECT AVG(pp.level) FROM PlayerProfile pp")
    Double getAverageLevel();
}