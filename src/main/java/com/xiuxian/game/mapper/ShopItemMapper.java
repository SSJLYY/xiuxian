package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.ShopItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShopItemMapper extends BaseMapper<ShopItem> {
    
    /**
     * 根据商店类型查询商品
     */
    @Select("SELECT * FROM shop_items WHERE shop_type = #{shopType} AND is_available = true")
    List<ShopItem> selectByShopType(@Param("shopType") String shopType);
    
    /**
     * 查询所有可用商品
     */
    @Select("SELECT * FROM shop_items WHERE is_available = true")
    List<ShopItem> selectAvailableItems();
}
