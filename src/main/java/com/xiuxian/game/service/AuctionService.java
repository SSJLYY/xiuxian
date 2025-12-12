package com.xiuxian.game.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiuxian.game.entity.*;
import com.xiuxian.game.mapper.*;
import com.xiuxian.game.exception.BusinessException;
import com.xiuxian.game.exception.ErrorCode;
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
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerItemMapper playerItemMapper;
    private final PlayerEquipmentMapper playerEquipmentMapper;
    private final PlayerPetMapper playerPetMapper;
    private final ItemMapper itemMapper;
    private final EquipmentMapper equipmentMapper;
    private final PetMapper petMapper;
    private final MailService mailService;
    private final PlayerService playerService;
    
    /**
     * 上架物品到拍卖行
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
        // 检查参数
        if (price <= 0 || duration <= 0) {
            throw new BusinessException("价格和持续时间必须大于0");
        }
        
        // 扣除手续费 (假设为售价的5%)
        long fee = Math.max(1, price / 20);
        PlayerProfile playerProfile = playerProfileMapper.selectById(playerId);
        if (playerProfile.getSpiritStones() < fee) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);
        }
        
        // 检查物品是否存在且属于玩家
        switch (itemType.toUpperCase()) {
            case "ITEM":
                PlayerItem playerItem = playerItemMapper.selectById(playerItemId);
                if (playerItem == null || !playerItem.getPlayerId().equals(playerId) || 
                    playerItem.getItemId() != itemId || playerItem.getQuantity() < quantity) {
                    throw new BusinessException("物品不存在或不属于您");
                }
                // 更新玩家物品数量
                if (playerItem.getQuantity() > quantity) {
                    playerItem.setQuantity(playerItem.getQuantity() - quantity);
                    playerItemMapper.updateById(playerItem);
                } else {
                    playerItemMapper.deleteById(playerItemId);
                }
                break;
                
            case "EQUIPMENT":
                PlayerEquipment playerEquipment = playerEquipmentMapper.selectById(playerItemId);
                if (playerEquipment == null || !playerEquipment.getPlayerId().equals(playerId) || 
                    playerEquipment.getEquipmentId() != itemId) {
                    throw new BusinessException("装备不存在或不属于您");
                }
                // 删除玩家装备
                playerEquipmentMapper.deleteById(playerItemId);
                break;
                
            case "PET":
                PlayerPet playerPet = playerPetMapper.selectById(playerItemId);
                if (playerPet == null || !playerPet.getPlayerId().equals(playerId) || 
                    playerPet.getPetId() != itemId) {
                    throw new BusinessException("宠物不存在或不属于您");
                }
                // 删除玩家宠物
                playerPetMapper.deleteById(playerItemId);
                break;
                
            default:
                throw new BusinessException("不支持的物品类型");
        }
        
        // 扣除手续费
        playerProfile.setSpiritStones(playerProfile.getSpiritStones() - fee);
        playerProfileMapper.updateById(playerProfile);
        
        // 创建拍卖物品
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
        
        // 检查买家灵石是否足够
        PlayerProfile buyerProfile = playerProfileMapper.selectById(buyerId);
        if (buyerProfile.getSpiritStones() < auctionItem.getPrice()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);
        }
        
        // 扣除买家灵石
        buyerProfile.setSpiritStones(buyerProfile.getSpiritStones() - auctionItem.getPrice());
        playerProfileMapper.updateById(buyerProfile);
        
        // 增加卖家灵石 (扣除10%平台费)
        long sellerProceeds = (long)auctionItem.getPrice() * 90 / 100;
        PlayerProfile sellerProfile = playerProfileMapper.selectById(auctionItem.getSellerId());
        sellerProfile.setSpiritStones(sellerProfile.getSpiritStones() + sellerProceeds);
        playerProfileMapper.updateById(sellerProfile);
        
        // 更新拍卖物品状态
        auctionItem.setStatus("SOLD");
        auctionItem.setBuyerId(buyerId);
        auctionItem.setSoldAt(LocalDateTime.now());
        auctionItemMapper.updateById(auctionItem);
        
        // 将物品添加到买家背包
        addItemToBuyerInventory(buyerId, auctionItem);
        
        // 发送邮件通知买卖双方
        sendTransactionNotification(auctionItem);
        
        return auctionItem;
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
        
        // 扣除取消手续费 (假设为原手续费的一半)
        long originalFee = Math.max(1, (long)auctionItem.getPrice() / 20);
        long cancelFee = originalFee / 2;
        
        PlayerProfile playerProfile = playerProfileMapper.selectById(playerId);
        if (playerProfile.getSpiritStones() < cancelFee) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);
        }
        
        // 扣除取消手续费
        playerProfile.setSpiritStones(playerProfile.getSpiritStones() - cancelFee);
        playerProfileMapper.updateById(playerProfile);
        
        // 更新拍卖物品状态
        auctionItem.setStatus("CANCELLED");
        auctionItemMapper.updateById(auctionItem);
        
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
    @Scheduled(fixedRate = 300000) // 每5分钟检查一次
    @Transactional
    public void processExpiredAuctions() {
        // 查找已过期但仍在拍卖中的物品
        QueryWrapper<AuctionItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ON_SALE");
        queryWrapper.lt("expire_at", LocalDateTime.now());
        
        List<AuctionItem> expiredItems = auctionItemMapper.selectList(queryWrapper);
        
        for (AuctionItem item : expiredItems) {
            item.setStatus("EXPIRED");
            auctionItemMapper.updateById(item);
            
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
                playerItemMapper.insert(newItem);
                break;
                
            case "EQUIPMENT":
                // 添加装备到买家背包
                PlayerEquipment newEquipment = new PlayerEquipment();
                newEquipment.setPlayerId(buyerId);
                newEquipment.setEquipmentId(auctionItem.getItemId());
                playerEquipmentMapper.insert(newEquipment);
                break;
                
            case "PET":
                // 添加宠物到买家
                PlayerPet newPet = new PlayerPet();
                newPet.setPlayerId(buyerId);
                newPet.setPetId(auctionItem.getItemId());
                newPet.setLevel(1);
                playerPetMapper.insert(newPet);
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
                playerItemMapper.insert(newItem);
                break;
                
            case "EQUIPMENT":
                // 添加装备到卖家背包
                PlayerEquipment newEquipment = new PlayerEquipment();
                newEquipment.setPlayerId(playerId);
                newEquipment.setEquipmentId(auctionItem.getItemId());
                playerEquipmentMapper.insert(newEquipment);
                break;
                
            case "PET":
                // 添加宠物到卖家
                PlayerPet newPet = new PlayerPet();
                newPet.setPlayerId(playerId);
                newPet.setPetId(auctionItem.getItemId());
                newPet.setLevel(1);
                playerPetMapper.insert(newPet);
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
                Item item = itemMapper.selectById(itemId);
                return item != null ? item.getName() : "未知物品[" + itemId + "]";
            case "EQUIPMENT":
                Equipment equipment = equipmentMapper.selectById(itemId);
                return equipment != null ? equipment.getName() : "未知装备[" + itemId + "]";
            case "PET":
                Pet pet = petMapper.selectById(itemId);
                return pet != null ? pet.getName() : "未知宠物[" + itemId + "]";
            default:
                return "物品[" + itemId + "]";
        }
    }
}