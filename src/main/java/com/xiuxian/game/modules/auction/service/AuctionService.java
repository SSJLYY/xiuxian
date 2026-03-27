package com.xiuxian.game.modules.auction.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// auction entities via Service









import com.xiuxian.game.modules.auction.entity.AuctionItem;











import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.entity.PlayerItem;
import com.xiuxian.game.modules.equipment.entity.Equipment;
import com.xiuxian.game.modules.equipment.entity.PlayerEquipment;
import com.xiuxian.game.modules.pet.entity.Pet;
import com.xiuxian.game.modules.pet.entity.PlayerPet;
import com.xiuxian.game.modules.shop.entity.Item;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.equipment.service.EquipmentService;
import com.xiuxian.game.modules.pet.service.PetService;
import com.xiuxian.game.modules.shop.service.ItemService;
import com.xiuxian.game.modules.mail.service.MailService;









import com.xiuxian.game.modules.auction.mapper.AuctionItemMapper;











import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.dto.request.ListAuctionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuctionService extends ServiceImpl<AuctionItemMapper, AuctionItem> {
    
    private final AuctionItemMapper auctionItemMapper;


    private final EquipmentService equipmentService;
    private final PetService petService;
    private final ItemService itemService;


    private final MailService mailService;
    private final PlayerService playerService;
    
    /**
     * 上架物品到拍卖行（DTO 版本，推荐使用）
     */
    @Transactional
    public AuctionItem listItem(Integer playerId, ListAuctionRequest request) {
        return listItem(playerId, request.getItemType(), request.getItemId(),
                request.getPlayerItemId(), request.getQuantity(), request.getPrice(), request.getDuration());
    }
    
    /**
     * 上架物品到拍卖行（多参数版本，兼容旧调用方）
     * @param playerId 玩家ID
     * @param itemType 物品类型 (ITEM/EQUIPMENT/PET)
     * @param itemId 物品ID
     * @param playerItemId 玩家物品ID
     * @param quantity 数量
     * @param price 价格
     * @param duration 持续时间(小时)
     * @return 拍卖物品
     */
    @Transactional
    public AuctionItem listItem(Integer playerId, String itemType, Integer itemId, Long playerItemId, 
                               Integer quantity, Integer price, Integer duration) {
        // 参数校验
        if (price <= 0 || duration <= 0) {
            throw new BusinessException("价格和持续时间必须大于0");
        }
        
        // 手续费校验 + 扣费
        validateAndDeductListingFee(playerId, price);
        
        // 物品校验 + 从背包移除
        consumeItemFromInventory(playerId, itemType, itemId, playerItemId, quantity);
        
        // 创建拍卖记录
        return createAuctionListing(playerId, itemType, itemId, playerItemId, quantity, price, duration);
    }
    
    /**
     * 校验手续费并扣除
     */
    private void validateAndDeductListingFee(Integer playerId, int price) {
        long fee = Math.max(1, price / 20);
        PlayerProfile playerProfile = playerService.getPlayerProfileById(playerId);
        if (playerProfile.getSpiritStones() < fee) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);
        }
        playerProfile.setSpiritStones(playerProfile.getSpiritStones() - fee);
        playerService.savePlayerProfile(playerProfile);
    }
    
    /**
     * 校验物品归属并从玩家背包移除
     */
    private void consumeItemFromInventory(Integer playerId, String itemType, Integer itemId, Long playerItemId, Integer quantity) {
        switch (itemType.toUpperCase()) {
            case "ITEM":
                PlayerItem playerItem = playerService.getPlayerItemById(playerItemId.intValue());
                if (playerItem == null || !playerItem.getPlayerId().equals(playerId) || 
                    playerItem.getItemId() != itemId || playerItem.getQuantity() < quantity) {
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
                if (playerEquipment == null || !playerEquipment.getPlayerId().equals(playerId) || 
                    playerEquipment.getEquipmentId() != itemId) {
                    throw new BusinessException("装备不存在或不属于您");
                }
                equipmentService.deletePlayerEquipment(playerItemId);
                break;
                
            case "PET":
                PlayerPet playerPet = petService.getPlayerPetById(playerItemId);
                if (playerPet == null || !playerPet.getPlayerId().equals(playerId) || 
                    playerPet.getPetId() != itemId) {
                    throw new BusinessException("宠物不存在或不属于您");
                }
                petService.deletePlayerPet(playerItemId);
                break;
                
            default:
                throw new BusinessException("不支持的物品类型");
        }
    }
    
    /**
     * 创建拍卖记录
     */
    private AuctionItem createAuctionListing(Integer playerId, String itemType, Integer itemId, 
                                             Long playerItemId, Integer quantity, Integer price, Integer duration) {
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
    
    /**
     * 购买拍卖物品
     * @param buyerId 买家ID
     * @param auctionItemId 拍卖物品ID
     * @return 拍卖物品
     */
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
        
        // 扣除买家灵石
        deductBuyerFunds(buyerId, auctionItem);
        
        // 支付卖家（扣除10%平台费）
        paySellerProceeds(auctionItem);
        
        // 原子更新拍卖状态（防止TOCTOU）
        LocalDateTime now = LocalDateTime.now();
        int rows = auctionItemMapper.claimAuctionItem(auctionItemId, buyerId, now);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.AUCTION_ITEM_SOLD);
        }
        
        auctionItem.setStatus("SOLD");
        auctionItem.setBuyerId(buyerId);
        auctionItem.setSoldAt(now);
        
        // 交付物品 + 发送通知
        addItemToBuyerInventory(buyerId, auctionItem);
        sendTransactionNotification(auctionItem);
        
        return auctionItem;
    }
    
    /**
     * 扣除买家灵石
     */
    private void deductBuyerFunds(Integer buyerId, AuctionItem auctionItem) {
        PlayerProfile buyerProfile = playerService.getPlayerProfileById(buyerId);
        if (buyerProfile.getSpiritStones() < auctionItem.getPrice()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);
        }
        buyerProfile.setSpiritStones(buyerProfile.getSpiritStones() - auctionItem.getPrice());
        playerService.savePlayerProfile(buyerProfile);
    }
    
    /**
     * 支付卖家收益
     */
    private void paySellerProceeds(AuctionItem auctionItem) {
        long sellerProceeds = (long) auctionItem.getPrice() * 90 / 100;
        PlayerProfile sellerProfile = playerService.getPlayerProfileById(auctionItem.getSellerId());
        sellerProfile.setSpiritStones(sellerProfile.getSpiritStones() + sellerProceeds);
        playerService.savePlayerProfile(sellerProfile);
    }
    
    /**
     * 取消拍卖
     * @param playerId 玩家ID
     * @param auctionItemId 拍卖物品ID
     * @return 拍卖物品
     */
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
        
        // 扣除取消手续费(假设为原手续费的一半)
        long originalFee = Math.max(1, (long)auctionItem.getPrice() / 20);
        long cancelFee = originalFee / 2;
        
        PlayerProfile playerProfile = playerService.getPlayerProfileById(playerId);
        if (playerProfile.getSpiritStones() < cancelFee) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);
        }
        
        // 扣除取消手续费
        playerProfile.setSpiritStones(playerProfile.getSpiritStones() - cancelFee);
        playerService.savePlayerProfile(playerProfile);
        
        // 原子更新拍卖状态（防止TOCTOU）
        int rows = auctionItemMapper.cancelAuctionItem(auctionItemId, playerId);
        if (rows == 0) {
            // 并发冲突：物品已被购买或过期
            throw new BusinessException(ErrorCode.AUCTION_ITEM_SOLD);
        }
        
        auctionItem.setStatus("CANCELLED");
        
        // 将物品退还给卖家
        addItemToSellerInventory(playerId, auctionItem);
        
        return auctionItem;
    }
    
    /**
     * 获取拍卖物品列表
     * @param page 页码
     * @param size 每页大小
     * @param itemType 物品类型过滤
     * @param minPrice 最低价格过滤
     * @param maxPrice 最高价格过滤
     * @return 拍卖物品分页列表
     */
    public IPage<AuctionItem> getAuctionItems(int page, int size, String itemType, Integer minPrice, Integer maxPrice) {
        Page<AuctionItem> pageObj = new Page<>(page, size);
        QueryWrapper<AuctionItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ON_SALE");
        queryWrapper.gt("expire_at", LocalDateTime.now()); // 只显示未过期的
        
        if (itemType != null && !itemType.isEmpty()) {
            queryWrapper.eq("item_type", itemType);
        }
        if (minPrice != null) {
            queryWrapper.ge("price", minPrice);
        }
        if (maxPrice != null) {
            queryWrapper.le("price", maxPrice);
        }
        
        queryWrapper.orderByDesc("created_at"); // 按创建时间倒序排列
        return auctionItemMapper.selectPage(pageObj, queryWrapper);
    }
    
    /**
     * 获取玩家的拍卖物品列表
     * @param playerId 玩家ID
     * @param status 状态过滤
     * @return 拍卖物品列表
     */
    public List<AuctionItem> getPlayerAuctions(Integer playerId, String status) {
        QueryWrapper<AuctionItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("seller_id", playerId);
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("created_at");
        return auctionItemMapper.selectList(queryWrapper);
    }
    
    /**
     * 处理过期的拍卖物品
     */
    @Scheduled(fixedRate = 300000) // 5分钟检查一次
    @Transactional
    public void processExpiredAuctions() {
        // 查找已过期但仍在拍卖中的物品
        QueryWrapper<AuctionItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ON_SALE");
        queryWrapper.lt("expire_at", LocalDateTime.now());
        
        List<AuctionItem> expiredItems = auctionItemMapper.selectList(queryWrapper);
        
        for (AuctionItem item : expiredItems) {
            // 原子更新过期状态（防止与buyItem并发冲突）
            int rows = auctionItemMapper.expireAuctionItem(item.getId());
            if (rows == 0) {
                // 并发冲突：物品在检查期间已被购买或取消
                continue;
            }
            
            item.setStatus("EXPIRED");
            // 将物品退还给卖家邮箱
            returnItemToSellerViaMail(item);
        }
    }
    
    /**
     * 将物品添加到买家背包
     * @param buyerId 买家ID
     * @param auctionItem 拍卖物品
     */
    private void addItemToBuyerInventory(Integer buyerId, AuctionItem auctionItem) {
        switch (auctionItem.getItemType().toUpperCase()) {
            case "ITEM":
                // 添加物品到买家背包
                PlayerItem newItem = new PlayerItem();
                newItem.setPlayerId(buyerId);
                newItem.setItemId(auctionItem.getItemId());
                newItem.setQuantity(auctionItem.getQuantity());
                playerService.savePlayerItem(newItem);
                break;
                
            case "EQUIPMENT":
                // 添加装备到买家背包
                PlayerEquipment newEquipment = new PlayerEquipment();
                newEquipment.setPlayerId(buyerId);
                newEquipment.setEquipmentId(auctionItem.getItemId());
                equipmentService.grantEquipmentDirectly(newEquipment.getPlayerId(), newEquipment.getEquipmentId());
                break;
                
            case "PET":
                // 添加宠物到买家
                PlayerPet newPet = new PlayerPet();
                newPet.setPlayerId(buyerId);
                newPet.setPetId(auctionItem.getItemId());
                newPet.setLevel(1);
                petService.grantPetDirectly(newPet.getPlayerId(), newPet.getPetId());
                break;
        }
    }
    
    /**
     * 将物品退还给卖家背包
     * @param playerId 卖家ID
     * @param auctionItem 拍卖物品
     */
    private void addItemToSellerInventory(Integer playerId, AuctionItem auctionItem) {
        switch (auctionItem.getItemType().toUpperCase()) {
            case "ITEM":
                // 添加物品到卖家背包
                PlayerItem newItem = new PlayerItem();
                newItem.setPlayerId(playerId);
                newItem.setItemId(auctionItem.getItemId());
                newItem.setQuantity(auctionItem.getQuantity());
                playerService.savePlayerItem(newItem);
                break;
                
            case "EQUIPMENT":
                // 添加装备到卖家背包
                PlayerEquipment newEquipment = new PlayerEquipment();
                newEquipment.setPlayerId(playerId);
                newEquipment.setEquipmentId(auctionItem.getItemId());
                equipmentService.grantEquipmentDirectly(newEquipment.getPlayerId(), newEquipment.getEquipmentId());
                break;
                
            case "PET":
                // 添加宠物到卖家
                PlayerPet newPet = new PlayerPet();
                newPet.setPlayerId(playerId);
                newPet.setPetId(auctionItem.getItemId());
                newPet.setLevel(1);
                petService.grantPetDirectly(newPet.getPlayerId(), newPet.getPetId());
                break;
        }
    }
    
    /**
     * 通过邮件将过期物品退还给卖家
     * @param auctionItem 拍卖物品
     */
    private void returnItemToSellerViaMail(AuctionItem auctionItem) {
        String subject = "拍卖物品退回";
        String content = String.format("您的拍卖物品 %s 已过期未售出，已退回给您。", getItemName(auctionItem));
        
        // 发送邮件给卖家
        mailService.sendSystemMail(auctionItem.getSellerId(), subject, content, 
                                  auctionItem.getItemType(), auctionItem.getItemId(), auctionItem.getQuantity());
    }
    
    /**
     * 发送交易通知邮件
     * @param auctionItem 拍卖物品
     */
    private void sendTransactionNotification(AuctionItem auctionItem) {
        // 发送给卖家
        String sellerSubject = "拍卖物品售出";
        String sellerContent = String.format("您的拍卖物品 %s 已被玩家购买，获得灵石：%d。", 
                                          getItemName(auctionItem), 
                                          (long)auctionItem.getPrice() * 90 / 100);
        mailService.sendSystemMail(auctionItem.getSellerId(), sellerSubject, sellerContent, null, null, 0);
        
        // 发送给买家
        String buyerSubject = "拍卖物品购买成功";
        String buyerContent = String.format("您成功购买了物品 %s，花费灵石：%d。", 
                                         getItemName(auctionItem), 
                                         auctionItem.getPrice());
        mailService.sendSystemMail(auctionItem.getBuyerId(), buyerSubject, buyerContent, null, null, 0);
    }
    
    /**
     * 获取物品名称
     * @param auctionItem 拍卖物品
     * @return 物品名称
     */
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
}

