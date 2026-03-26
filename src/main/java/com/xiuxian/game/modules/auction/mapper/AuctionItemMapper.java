package com.xiuxian.game.modules.auction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.auction.entity.AuctionItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface AuctionItemMapper extends BaseMapper<AuctionItem> {

    /**
     * 原子更新拍卖状态为SOLD（条件更新防止TOCTOU）
     * @return 影响行数，0表示状态已变更（被其他事务抢先）
     */
    @Update("UPDATE auction_items SET status = 'SOLD', buyer_id = #{buyerId}, sold_at = #{soldAt} " +
            "WHERE id = #{id} AND status = 'ON_SALE'")
    int claimAuctionItem(@Param("id") Long id, @Param("buyerId") Integer buyerId, @Param("soldAt") LocalDateTime soldAt);

    /**
     * 原子取消拍卖（条件更新防止TOCTOU）
     * @return 影响行数，0表示状态已变更
     */
    @Update("UPDATE auction_items SET status = 'CANCELLED' WHERE id = #{id} AND status = 'ON_SALE' AND seller_id = #{sellerId}")
    int cancelAuctionItem(@Param("id") Long id, @Param("sellerId") Integer sellerId);

    /**
     * 原子过期拍卖（条件更新防止与buyItem冲突）
     * @return 影响行数，0表示状态已变更
     */
    @Update("UPDATE auction_items SET status = 'EXPIRED' WHERE id = #{id} AND status = 'ON_SALE'")
    int expireAuctionItem(@Param("id") Long id);
}

