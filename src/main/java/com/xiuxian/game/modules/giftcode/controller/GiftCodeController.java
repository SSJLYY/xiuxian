package com.xiuxian.game.modules.giftcode.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.giftcode.service.GiftCodeService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/giftcode")
@RequiredArgsConstructor
public class GiftCodeController {

    private final GiftCodeService giftCodeService;
    private final PlayerService playerService;

    @PostMapping("/redeem")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Boolean> redeemGiftCode(@RequestParam(required = false) String code,
                                               @RequestBody(required = false) RedeemGiftCodeRequest requestBody) {
        String resolvedCode = firstNonBlank(code, requestBody == null ? null : requestBody.getCode());
        if (resolvedCode == null) {
            return ApiResponse.error("礼包码不能为空");
        }

        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家兑换礼包码: playerId={}, code={}", playerId, resolvedCode);
            boolean result = giftCodeService.redeemGiftCode(playerId, resolvedCode);
            return ApiResponse.success("兑换成功", result);
        } catch (Exception e) {
            log.error("礼包码兑换失败: code={}, error={}", resolvedCode, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<GiftCodeService.PlayerGiftCodeRecord>> getMyGiftCodes() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            return ApiResponse.success("获取兑换记录成功", giftCodeService.getPlayerGiftCodeHistory(playerId));
        } catch (Exception e) {
            log.error("获取玩家礼包码兑换记录失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/available")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<GiftCodeService.AvailableGiftCodeInfo>> getAvailableGiftCodes() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            return ApiResponse.success("获取可用礼包码成功", giftCodeService.getAvailableGiftCodes(playerId));
        } catch (Exception e) {
            log.error("获取可用礼包码失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    @Data
    public static class RedeemGiftCodeRequest {
        private String code;
    }
}
