package com.xiuxian.game.modules.auction.service;

import com.xiuxian.game.modules.auction.entity.AuctionItem;
import com.xiuxian.game.modules.auction.mapper.AuctionItemMapper;
import com.xiuxian.game.modules.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuctionExpirationService {

    private final AuctionItemMapper auctionItemMapper;
    private final MailService mailService;

    @Transactional(rollbackFor = Exception.class)
    public void processOneExpiredAuction(AuctionItem item, String itemName) {
        int rows = auctionItemMapper.expireAuctionItem(item.getId());
        if (rows == 0) {
            return;
        }

        item.setStatus("EXPIRED");
        mailService.sendSystemMail(
                item.getSellerId(),
                "拍卖物品退回",
                String.format("您的拍卖物品 %s 已过期未售出，现已退回给您。", itemName),
                item.getItemType(),
                item.getItemId(),
                item.getQuantity()
        );
    }
}
