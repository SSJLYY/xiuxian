package com.xiuxian.game.modules.auction.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.dto.request.ListAuctionRequest;
import com.xiuxian.game.modules.auction.entity.AuctionItem;
import com.xiuxian.game.modules.auction.mapper.AuctionItemMapper;
import com.xiuxian.game.modules.equipment.entity.Equipment;
import com.xiuxian.game.modules.equipment.entity.PlayerEquipment;
import com.xiuxian.game.modules.equipment.service.EquipmentService;
import com.xiuxian.game.modules.mail.service.MailService;
import com.xiuxian.game.modules.pet.entity.Pet;
import com.xiuxian.game.modules.pet.entity.PlayerPet;
import com.xiuxian.game.modules.pet.service.PetService;
import com.xiuxian.game.modules.player.entity.PlayerItem;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.shop.entity.Item;
import com.xiuxian.game.modules.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuctionService extends ServiceImpl<AuctionItemMapper, AuctionItem> {

    private final AuctionItemMapper auctionItemMapper;
    private final EquipmentService equipmentService;
    private final PetService petService;
    private final ItemService itemService;
    private final MailService mailService;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerService playerService;

    @Transactional
    public AuctionItem listItem(Integer playerId, ListAuctionRequest request) {
        return listItem(
                playerId,
                request.getItemType(),
                request.getItemId(),
                request.getPlayerItemId(),
                request.getQuantity(),
                request.getPrice(),
                request.getDuration()
        );
    }

    @Transactional
    public AuctionItem listItem(
            Integer playerId,
            String itemType,
            Integer itemId,
            Long playerItemId,
            Integer quantity,
            Integer price,
            Integer duration
    ) {
        if (itemType == null || itemType.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "物品类型不能为空");
        }
        if (itemId == null || playerItemId == null || quantity == null || price == null || duration == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参数不完整");
        }
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "数量必须大于0");
        }
        if (price <= 0 || duration <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "价格和持续时间必须大于0");
        }

        validateAndDeductListingFee(playerId, price);
        consumeItemFromInventory(playerId, itemType, itemId, playerItemId, quantity);
        return createAuctionListing(playerId, itemType, itemId, playerItemId, quantity, price, duration);
    }

    private void validateAndDeductListingFee(Integer playerId, int price) {
        long fee = Math.max(1, Math.round(price * 0.05));
        PlayerProfile playerProfile = playerProfileMapper.selectByIdForUpdate(playerId);
        if (playerProfile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
        if (defaultLong(playerProfile.getSpiritStones()) < fee) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);
        }
        playerProfile.setSpiritStones(defaultLong(playerProfile.getSpiritStones()) - fee);
        playerService.savePlayerProfile(playerProfile);
    }

    private void consumeItemFromInventory(Integer playerId, String itemType, Integer itemId, Long playerItemId, Integer quantity) {
        switch (itemType.toUpperCase()) {
            case "ITEM":
                PlayerItem playerItem = playerService.getPlayerItemById(playerItemId.intValue());
                if (playerItem == null
                        || !playerItem.getPlayerId().equals(playerId)
                        || !Objects.equals(playerItem.getItemId(), itemId)
                        || Boolean.TRUE.equals(playerItem.getLocked())
                        || playerItem.getQuantity() < quantity) {
                    throw new BusinessException("物品不存在或不属于您");
                }
                if (playerItem.getQuantity() > quantity) {
                    playerItem.setQuantity(playerItem.getQuantity() - quantity);
                    playerService.updatePlayerItem(playerItem);
                } else {
                    playerService.deletePlayerItem(playerItemId.intValue());
                }
                break;

            case "EQUIPMENT":
                PlayerEquipment playerEquipment = equipmentService.getPlayerEquipmentById(playerItemId);
                if (playerEquipment == null
                        || !playerEquipment.getPlayerId().equals(playerId)
                        || !Objects.equals(playerEquipment.getEquipmentId(), itemId)) {
                    throw new BusinessException("装备不存在或不属于您");
                }
                equipmentService.deletePlayerEquipment(playerItemId);
                break;

            case "PET":
                PlayerPet playerPet = petService.getPlayerPetById(playerItemId);
                if (playerPet == null
                        || !playerPet.getPlayerId().equals(playerId)
                        || !Objects.equals(playerPet.getPetId(), itemId)) {
                    throw new BusinessException("宠物不存在或不属于您");
                }
                petService.deletePlayerPet(playerItemId);
                break;

            default:
                throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的物品类型");
        }
    }

    private AuctionItem createAuctionListing(
            Integer playerId,
            String itemType,
            Integer itemId,
            Long playerItemId,
            Integer quantity,
            Integer price,
            Integer duration
    ) {
        AuctionItem auctionItem = new AuctionItem();
        auctionItem.setSellerId(playerId);
        auctionItem.setItemType(itemType.toUpperCase());
        auctionItem.setItemId(itemId);
        auctionItem.setPlayerItemId(playerItemId);
        auctionItem.setQuantity(quantity);
        auctionItem.setPrice(price);
        auctionItem.setStatus("ON_SALE");
        auctionItem.setCreatedAt(LocalDateTime.now());
        auctionItem.setExpireAt(LocalDateTime.now().plusHours(duration));
        auctionItemMapper.insert(auctionItem);
        return auctionItem;
    }

    @Transactional
    public AuctionItem buyItem(Integer buyerId, Long auctionItemId) {
        AuctionItem auctionItem = auctionItemMapper.selectById(auctionItemId);
        if (auctionItem == null) {
            throw new BusinessException(ErrorCode.AUCTION_ITEM_NOT_FOUND);
        }
        if (!"ON_SALE".equals(auctionItem.getStatus())) {
            throw new BusinessException(ErrorCode.AUCTION_ITEM_SOLD);
        }
        if (auctionItem.getSellerId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.CANNOT_BUY_OWN_ITEM);
        }

        LocalDateTime now = LocalDateTime.now();
        int rows = auctionItemMapper.claimAuctionItem(auctionItemId, buyerId, now);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.AUCTION_ITEM_SOLD);
        }

        deductBuyerFunds(buyerId, auctionItem);
        paySellerProceeds(auctionItem);

        auctionItem.setStatus("SOLD");
        auctionItem.setBuyerId(buyerId);
        auctionItem.setSoldAt(now);

        addItemToBuyerInventory(buyerId, auctionItem);
        sendTransactionNotification(auctionItem);
        return auctionItem;
    }

    private void deductBuyerFunds(Integer buyerId, AuctionItem auctionItem) {
        PlayerProfile buyerProfile = playerProfileMapper.selectByIdForUpdate(buyerId);
        if (buyerProfile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
        if (defaultLong(buyerProfile.getSpiritStones()) < auctionItem.getPrice()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);
        }
        buyerProfile.setSpiritStones(defaultLong(buyerProfile.getSpiritStones()) - auctionItem.getPrice());
        playerService.savePlayerProfile(buyerProfile);
    }

    private void paySellerProceeds(AuctionItem auctionItem) {
        PlayerProfile sellerProfile = playerProfileMapper.selectByIdForUpdate(auctionItem.getSellerId());
        if (sellerProfile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
        long price = auctionItem.getPrice().longValue();
        long sellerProceeds = price * 9 / 10;
        sellerProfile.setSpiritStones(defaultLong(sellerProfile.getSpiritStones()) + sellerProceeds);
        playerService.savePlayerProfile(sellerProfile);
    }

    @Transactional
    public AuctionItem cancelAuction(Integer playerId, Long auctionItemId) {
        AuctionItem auctionItem = auctionItemMapper.selectById(auctionItemId);
        if (auctionItem == null) {
            throw new BusinessException(ErrorCode.AUCTION_ITEM_NOT_FOUND);
        }
        if (!auctionItem.getSellerId().equals(playerId)) {
            throw new BusinessException("只能取消自己的拍卖");
        }
        if (!"ON_SALE".equals(auctionItem.getStatus())) {
            throw new BusinessException(ErrorCode.AUCTION_ITEM_SOLD);
        }

        long originalFee = Math.max(1, Math.round(auctionItem.getPrice() * 0.05));
        long cancelFee = originalFee / 2;

        PlayerProfile playerProfile = playerProfileMapper.selectByIdForUpdate(playerId);
        if (playerProfile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
        if (defaultLong(playerProfile.getSpiritStones()) < cancelFee) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);
        }

        int rows = auctionItemMapper.cancelAuctionItem(auctionItemId, playerId);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.AUCTION_ITEM_SOLD);
        }

        playerProfile.setSpiritStones(defaultLong(playerProfile.getSpiritStones()) - cancelFee);
        playerService.savePlayerProfile(playerProfile);

        auctionItem.setStatus("CANCELLED");
        addItemToSellerInventory(playerId, auctionItem);
        return auctionItem;
    }

    public IPage<AuctionItem> getAuctionItems(int page, int size, String itemType, Integer minPrice, Integer maxPrice) {
        Page<AuctionItem> pageObj = new Page<>(page, size);
        QueryWrapper<AuctionItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ON_SALE");
        queryWrapper.gt("expire_at", LocalDateTime.now());

        if (itemType != null && !itemType.isEmpty()) {
            queryWrapper.eq("item_type", itemType);
        }
        if (minPrice != null) {
            queryWrapper.ge("price", minPrice);
        }
        if (maxPrice != null) {
            queryWrapper.le("price", maxPrice);
        }

        queryWrapper.orderByDesc("created_at");
        return auctionItemMapper.selectPage(pageObj, queryWrapper);
    }

    public List<AuctionItem> getPlayerAuctions(Integer playerId, String status) {
        QueryWrapper<AuctionItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("seller_id", playerId);
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("created_at");
        return auctionItemMapper.selectList(queryWrapper);
    }

    @Scheduled(fixedRate = 300000)
    public void processExpiredAuctions() {
        QueryWrapper<AuctionItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ON_SALE");
        queryWrapper.lt("expire_at", LocalDateTime.now());

        List<AuctionItem> expiredItems = auctionItemMapper.selectList(queryWrapper);
        for (AuctionItem item : expiredItems) {
            try {
                processOneExpiredAuction(item);
            } catch (Exception e) {
                log.error("处理过期拍卖物品失败，跳过该记录，auctionItemId={}", item.getId(), e);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void processOneExpiredAuction(AuctionItem item) {
        int rows = auctionItemMapper.expireAuctionItem(item.getId());
        if (rows == 0) {
            return;
        }

        item.setStatus("EXPIRED");
        returnItemToSellerViaMail(item);
    }

    private void addItemToBuyerInventory(Integer buyerId, AuctionItem auctionItem) {
        switch (auctionItem.getItemType().toUpperCase()) {
            case "ITEM":
                PlayerItem existingItem = playerService.getUnlockedPlayerItemByPlayerAndItem(buyerId, auctionItem.getItemId());

                if (existingItem != null) {
                    existingItem.setQuantity((existingItem.getQuantity() == null ? 0 : existingItem.getQuantity())
                            + (auctionItem.getQuantity() == null ? 0 : auctionItem.getQuantity()));
                    playerService.savePlayerItem(existingItem);
                } else {
                    PlayerItem newItem = new PlayerItem();
                    newItem.setPlayerId(buyerId);
                    newItem.setItemId(auctionItem.getItemId());
                    newItem.setQuantity(auctionItem.getQuantity());
                    playerService.savePlayerItem(newItem);
                }
                break;

            case "EQUIPMENT":
                equipmentService.grantEquipmentDirectly(buyerId, auctionItem.getItemId());
                break;

            case "PET":
                petService.grantPetDirectly(buyerId, auctionItem.getItemId());
                break;

            default:
                throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的物品类型: " + auctionItem.getItemType());
        }
    }

    private void addItemToSellerInventory(Integer playerId, AuctionItem auctionItem) {
        switch (auctionItem.getItemType().toUpperCase()) {
            case "ITEM":
                PlayerItem existingItem = playerService.getUnlockedPlayerItemByPlayerAndItem(playerId, auctionItem.getItemId());

                if (existingItem != null) {
                    existingItem.setQuantity((existingItem.getQuantity() == null ? 0 : existingItem.getQuantity())
                            + (auctionItem.getQuantity() == null ? 0 : auctionItem.getQuantity()));
                    playerService.savePlayerItem(existingItem);
                } else {
                    PlayerItem newItem = new PlayerItem();
                    newItem.setPlayerId(playerId);
                    newItem.setItemId(auctionItem.getItemId());
                    newItem.setQuantity(auctionItem.getQuantity());
                    playerService.savePlayerItem(newItem);
                }
                break;

            case "EQUIPMENT":
                equipmentService.grantEquipmentDirectly(playerId, auctionItem.getItemId());
                break;

            case "PET":
                petService.grantPetDirectly(playerId, auctionItem.getItemId());
                break;

            default:
                throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的物品类型: " + auctionItem.getItemType());
        }
    }

    private void returnItemToSellerViaMail(AuctionItem auctionItem) {
        String subject = "拍卖物品退回";
        String content = String.format("您的拍卖物品 %s 已过期未售出，现已退回给您。", getItemName(auctionItem));
        mailService.sendSystemMail(
                auctionItem.getSellerId(),
                subject,
                content,
                auctionItem.getItemType(),
                auctionItem.getItemId(),
                auctionItem.getQuantity()
        );
    }

    private void sendTransactionNotification(AuctionItem auctionItem) {
        String sellerSubject = "拍卖物品售出";
        String sellerContent = String.format(
                "您的拍卖物品 %s 已被其他玩家购买，获得灵石：%d。",
                getItemName(auctionItem),
                (long) auctionItem.getPrice() * 90 / 100
        );
        mailService.sendSystemMail(auctionItem.getSellerId(), sellerSubject, sellerContent, null, null, 0);

        String buyerSubject = "拍卖物品购买成功";
        String buyerContent = String.format(
                "您成功购买了物品 %s，花费灵石：%d。",
                getItemName(auctionItem),
                auctionItem.getPrice()
        );
        mailService.sendSystemMail(auctionItem.getBuyerId(), buyerSubject, buyerContent, null, null, 0);
    }

    private String getItemName(AuctionItem auctionItem) {
        String itemType = auctionItem.getItemType().toUpperCase();
        Integer itemId = auctionItem.getItemId();

        switch (itemType) {
            case "ITEM":
                Item item = itemService.getItemById(itemId);
                return item != null ? item.getName() : "未知物品[" + itemId + "]";
            case "EQUIPMENT":
                Equipment equipment = equipmentService.getEquipmentById(itemId);
                return equipment != null ? equipment.getName() : "未知装备[" + itemId + "]";
            case "PET":
                Pet pet = petService.getPetById(itemId);
                return pet != null ? pet.getName() : "未知宠物[" + itemId + "]";
            default:
                return "物品[" + itemId + "]";
        }
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
