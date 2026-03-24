package com.xiuxian.game.service;

import com.xiuxian.game.entity.PlayerItem;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.PlayerSkill;
import com.xiuxian.game.entity.ShopItem;
import com.xiuxian.game.entity.SkillShopItem;
import com.xiuxian.game.exception.BusinessException;
import com.xiuxian.game.exception.ErrorCode;
import com.xiuxian.game.mapper.PlayerItemMapper;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.mapper.PlayerSkillMapper;
import com.xiuxian.game.mapper.ShopItemMapper;
import com.xiuxian.game.mapper.SkillShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopItemMapper shopItemMapper;
    private final SkillShopMapper skillShopMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerItemMapper playerItemMapper;
    private final PlayerSkillMapper playerSkillMapper;
    private final PlayerService playerService;

    public List<ShopItem> listItems(String shopType) {
        if (shopType == null || shopType.isEmpty()) {
            return shopItemMapper.selectAvailableItems();
        }
        return shopItemMapper.selectByShopType(shopType);
    }

    public List<SkillShopItem> listSkillShop() {
        return skillShopMapper.selectAvailable();
    }

    @Transactional
    public void buyItem(Integer shopItemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("购买数量必须大于0");
        }
        ShopItem item = shopItemMapper.selectById(shopItemId);
        if (item == null || !Boolean.TRUE.equals(item.getIsAvailable())) {
            throw new BusinessException(ErrorCode.SHOP_ITEM_NOT_AVAILABLE);
        }
        if (!item.hasStock(quantity)) {
            throw new BusinessException(ErrorCode.SHOP_ITEM_OUT_OF_STOCK);
        }

        PlayerProfile profile = playerService.getCurrentPlayerProfile();

        long needSpirit = (long) item.getPriceSpiritStones() * quantity;
        long needContribution = (long) item.getPriceContributionPoints() * quantity;

        if (needSpirit > 0) {
            if (profile.getSpiritStones() < needSpirit) {
                throw new BusinessException(ErrorCode.SHOP_INSUFFICIENT_SPIRIT_STONES);
            }
            profile.setSpiritStones(profile.getSpiritStones() - needSpirit);
        }
        if (needContribution > 0) {
            if (profile.getContributionPoints() < needContribution) {
                throw new BusinessException(ErrorCode.SHOP_INSUFFICIENT_CONTRIBUTION);
            }
            profile.setContributionPoints(profile.getContributionPoints() - needContribution);
        }

        playerProfileMapper.updateById(profile);

        if (item.getItemId() != null) {
            PlayerItem existing = playerItemMapper.selectByPlayerIdAndItemId(profile.getId(), item.getItemId());
            if (existing == null) {
                PlayerItem pi = PlayerItem.builder()
                        .playerId(profile.getId())
                        .itemId(item.getItemId())
                        .quantity(quantity)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                playerItemMapper.insert(pi);
            } else {
                existing.setQuantity(existing.getQuantity() + quantity);
                existing.setUpdatedAt(LocalDateTime.now());
                playerItemMapper.updateById(existing);
            }
        }

        item.decreaseStock(quantity);
        shopItemMapper.updateById(item);
    }

    @Transactional
    public void buySkill(Integer skillId) {
        PlayerProfile profile = playerService.getCurrentPlayerProfile();

        // 查询技能商店中是否有该技能
        SkillShopItem ssi = skillShopMapper.selectById(skillId);
        if (ssi == null) {
            // 如果使用skillId查不到，尝试通过skill_id查找
            List<SkillShopItem> allItems = skillShopMapper.selectAvailable();
            ssi = allItems.stream()
                    .filter(item -> item.getSkillId().equals(skillId))
                    .findFirst()
                    .orElse(null);
        }

        if (ssi == null || !Boolean.TRUE.equals(ssi.getAvailable())) {
            throw new BusinessException(ErrorCode.SHOP_ITEM_NOT_AVAILABLE);
        }
        if (profile.getLevel() < ssi.getRequiredLevel()) {
            throw new BusinessException(ErrorCode.SHOP_SKILL_LEVEL_NOT_ENOUGH);
        }
        if (profile.getSpiritStones() < ssi.getPrice()) {
            throw new BusinessException(ErrorCode.SHOP_INSUFFICIENT_SPIRIT_STONES);
        }

        // 检查是否已经拥有该技能
        PlayerSkill existing = playerSkillMapper.selectByPlayerIdAndSkillId(profile.getId(), skillId);
        if (existing != null) {
            throw new BusinessException(ErrorCode.SHOP_SKILL_ALREADY_OWNED);
        }

        // 扣除灵石
        profile.setSpiritStones(profile.getSpiritStones() - ssi.getPrice());
        playerProfileMapper.updateById(profile);

        // 学习技能
        PlayerSkill ps = PlayerSkill.builder()
                .playerId(profile.getId())
                .skillId(skillId)
                .level(1)
                .experience(0)
                .equipped(false)
                .slotNumber(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        playerSkillMapper.insert(ps);
    }
}
