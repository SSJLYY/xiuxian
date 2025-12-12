package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.service.GiftCodeService;
import com.xiuxian.game.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/giftcode")
@RequiredArgsConstructor
public class GiftCodeController {

    private final GiftCodeService giftCodeService;
    private final PlayerService playerService;

    /**
     * 兑换礼包码
     */
    @PostMapping("/redeem")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Boolean> redeemGiftCode(@RequestParam String code) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            boolean result = giftCodeService.redeemGiftCode(playerId, code);
            return ApiResponse.success("兑换成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}