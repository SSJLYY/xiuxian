package com.xiuxian.game.modules.shop.service;

import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.skill.entity.SkillShopItem;
import com.xiuxian.game.modules.shop.entity.ShopItem;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.shop.mapper.ShopItemMapper;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.skill.service.SkillShopService;
import com.xiuxian.game.modules.equipment.service.InventoryService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商店服务
 * 模块边界规范：通过各模块Service接口操作数据，禁止直接调用跨模块Mapper
 *
 * @author shaun.sheng
 */
@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopItemMapper shopItemMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerService playerService;      // 玩家数据
    private final InventoryService inventoryService; // 背包/物品操作
    private final SkillShopService skillShopService; // 技能商店查询

    public List<ShopItem> listItems(String shopType) {
        if (shopType == null || shopType.isEmpty()) {
            return shopItemMapper.selectAvailableItems();
        }
        return shopItemMapper.selectByShopType(shopType);
    }

    /**
     * 获取所有商品（管理端使用，不过滤类型）
     */
    public List<ShopItem> listAllItems() {
        return shopItemMapper.selectList(null);
    }

    public List<SkillShopItem> listSkillShop() {
        return skillShopService.getAvailableSkillShopItems();
    }

    @Transactional
    public void buyItem(Integer shopItemId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "购买数量必须大于0");
        }
        ShopItem item = shopItemMapper.selectById(shopItemId);
        if (item == null || !Boolean.TRUE.equals(item.getIsAvailable())) {
            throw new BusinessException(ErrorCode.SHOP_ITEM_NOT_AVAILABLE);
        }
        if (!item.hasStock(quantity)) {
            throw new BusinessException(ErrorCode.SHOP_ITEM_OUT_OF_STOCK);
        }

        PlayerProfile currentPlayer = playerService.getCurrentPlayerProfile();
        PlayerProfile profile = playerProfileMapper.selectByIdForUpdate(currentPlayer.getId());
        if (profile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }

        long needSpirit = (long) item.getPriceSpiritStones() * quantity;
        long needContribution = (long) item.getPriceContributionPoints() * quantity;

        if (needSpirit > 0) {
            if (defaultLong(profile.getSpiritStones()) < needSpirit) {
                throw new BusinessException(ErrorCode.SHOP_INSUFFICIENT_SPIRIT_STONES);
            }
            profile.setSpiritStones(defaultLong(profile.getSpiritStones()) - needSpirit);
        }
        if (needContribution > 0) {
            if (defaultLong(profile.getContributionPoints()) < needContribution) {
                throw new BusinessException(ErrorCode.SHOP_INSUFFICIENT_CONTRIBUTION);
            }
            profile.setContributionPoints(defaultLong(profile.getContributionPoints()) - needContribution);
        }

        // 扣除货币 — 通过PlayerService，遵守模块边界
        playerService.savePlayerProfile(profile);

        // 添加物品到背包 — 通过InventoryService，遵守模块边界
        if (item.getItemId() != null) {
            inventoryService.addItemToInventory(profile.getId(), item.getItemId(), quantity);
        }

        // 减少库存
        if (!item.isUnlimitedStock()) {
            int updatedRows = shopItemMapper.decreaseStockIfEnough(shopItemId, quantity);
            if (updatedRows == 0) {
                throw new BusinessException(ErrorCode.SHOP_ITEM_OUT_OF_STOCK);
            }
        }
    }

    @Transactional
    public void buySkill(Integer skillId) {
        // 查询技能商店 — 通过SkillShopService，遵守模块边界
        SkillShopItem ssi = skillShopService.getSkillShopItemBySkillId(skillId);

        if (ssi == null || !Boolean.TRUE.equals(ssi.getAvailable())) {
            throw new BusinessException(ErrorCode.SHOP_ITEM_NOT_AVAILABLE);
        }

        // 委托给技能商店服务统一处理扣费、等级校验和并发安全学习，避免重复扣费
        skillShopService.buySkill(ssi.getId());
    }

    /**
     * 管理员：新增或更新商店物品
     */
    public void upsertShopItem(ShopItem item) {
        if (item.getId() == null) {
            shopItemMapper.insert(item);
        } else {
            shopItemMapper.updateById(item);
        }
    }
    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
