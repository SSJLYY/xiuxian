package com.xiuxian.game.repository;

import com.xiuxian.game.entity.Item;
import com.xiuxian.game.entity.PlayerItem;
import com.xiuxian.game.entity.PlayerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerItemRepository extends JpaRepository<PlayerItem, Integer> {

    /**
     * 根据玩家查找所有物品
     */
    List<PlayerItem> findByPlayer(PlayerProfile player);

    /**
     * 根据玩家和物品查找特定物品
     */
    Optional<PlayerItem> findByPlayerAndItem(PlayerProfile player, Item item);

    /**
     * 根据玩家ID和物品ID查找特定物品（修复：使用参数化查询）
     */
    @Query("SELECT pi FROM PlayerItem pi WHERE pi.player.id = :playerId AND pi.item.id = :itemId")
    Optional<PlayerItem> findByPlayerIdAndItemId(@Param("playerId") Integer playerId,
                                                 @Param("itemId") Integer itemId);

    /**
     * 根据玩家和物品类型查找物品
     */
    @Query("SELECT pi FROM PlayerItem pi WHERE pi.player = :player AND pi.item.type = :itemType")
    List<PlayerItem> findByPlayerAndItemType(@Param("player") PlayerProfile player,
                                             @Param("itemType") String itemType);

    /**
     * 根据玩家和物品品质查找物品
     */
    @Query("SELECT pi FROM PlayerItem pi WHERE pi.player = :player AND pi.item.quality = :quality")
    List<PlayerItem> findByPlayerAndItemQuality(@Param("player") PlayerProfile player,
                                                @Param("quality") Integer quality);

    /**
     * 查找玩家可使用的物品
     */
    List<PlayerItem> findByPlayerAndItemUsableTrue(PlayerProfile player);

    /**
     * 查找玩家可出售的物品
     */
    List<PlayerItem> findByPlayerAndItemSellableTrue(PlayerProfile player);

    /**
     * 查找玩家可堆叠的物品（数量未满）
     */
    @Query("SELECT pi FROM PlayerItem pi WHERE pi.player = :player AND pi.item.stackable = true AND pi.quantity < pi.item.maxStack")
    List<PlayerItem> findStackableItemsByPlayer(@Param("player") PlayerProfile player);

    /**
     * 根据数量范围查找物品
     */
    List<PlayerItem> findByPlayerAndQuantityBetween(PlayerProfile player, Integer minQuantity, Integer maxQuantity);

    /**
     * 查找数量大于指定值的物品
     */
    List<PlayerItem> findByPlayerAndQuantityGreaterThan(PlayerProfile player, Integer quantity);

    /**
     * 统计玩家物品总数
     */
    @Query("SELECT COUNT(pi) FROM PlayerItem pi WHERE pi.player = :player")
    Long countByPlayer(@Param("player") PlayerProfile player);

    /**
     * 统计玩家物品总数量（考虑堆叠）
     */
    @Query("SELECT SUM(pi.quantity) FROM PlayerItem pi WHERE pi.player = :player")
    Long sumQuantityByPlayer(@Param("player") PlayerProfile player);

    /**
     * 统计玩家不同类型物品的数量（修复：使用原生SQL）
     */
    @Query(value = "SELECT i.type, SUM(pi.quantity) FROM player_items pi JOIN items i ON pi.item_id = i.id WHERE pi.player_id = :playerId GROUP BY i.type", nativeQuery = true)
    List<Object[]> countItemsByType(@Param("playerId") Integer playerId);

    /**
     * 统计玩家不同品质物品的数量（修复：使用原生SQL）
     */
    @Query(value = "SELECT i.quality, SUM(pi.quantity) FROM player_items pi JOIN items i ON pi.item_id = i.id WHERE pi.player_id = :playerId GROUP BY i.quality", nativeQuery = true)
    List<Object[]> countItemsByQuality(@Param("playerId") Integer playerId);

    /**
     * 查找玩家最近获得的物品
     */
    List<PlayerItem> findByPlayerOrderByCreatedAtDesc(PlayerProfile player);

    /**
     * 查找玩家最近更新的物品
     */
    List<PlayerItem> findByPlayerOrderByUpdatedAtDesc(PlayerProfile player);

    /**
     * 根据物品名称模糊搜索
     */
    @Query("SELECT pi FROM PlayerItem pi WHERE pi.player = :player AND LOWER(pi.item.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<PlayerItem> findByPlayerAndItemNameContaining(@Param("player") PlayerProfile player,
                                                       @Param("name") String name);

    /**
     * 根据物品描述模糊搜索
     */
    @Query("SELECT pi FROM PlayerItem pi WHERE pi.player = :player AND LOWER(pi.item.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<PlayerItem> findByPlayerAndItemDescriptionContaining(@Param("player") PlayerProfile player,
                                                              @Param("description") String description);
}