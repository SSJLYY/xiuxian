package com.xiuxian.game.modules.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiuxian.game.modules.shop.entity.Item;
import com.xiuxian.game.modules.shop.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 物品模板服务
 * 负责物品模板（Item表）的查询，供跨模块访问物品信息使用。
 * equipment/inventory 等模块通过此 Service 访问物品模板，禁止直接注入 ItemMapper。
 *
 * @author shaun.sheng
 */
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemMapper itemMapper;

    /**
     * 根据ID获取物品模板
     */
    public Item getItemById(Integer itemId) {
        return itemMapper.selectById(itemId);
    }

    /**
     * 批量获取物品模板（根据ID列表）
     */
    public List<Item> getItemsByIds(List<Integer> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<Item> wrapper = new QueryWrapper<>();
        wrapper.in("id", itemIds);
        return itemMapper.selectList(wrapper);
    }

    /**
     * 获取所有物品模板
     */
    public List<Item> getAllItems() {
        return itemMapper.selectList(null);
    }
}
