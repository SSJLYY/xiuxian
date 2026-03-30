package com.xiuxian.game.modules.giftcode.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.giftcode.service.GiftCodeService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 礼包码控制器
 *
 * <p>提供礼包码兑换相关的REST API接口</p>
 *
 * <p>主要接口：</p>
 * <ul>
 *   <li>POST /api/giftcode/redeem - 兑换礼包码</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/giftcode")
@RequiredArgsConstructor
public class GiftCodeController {

    private final GiftCodeService giftCodeService;
    private final PlayerService playerService;

    /**
     * 兑换礼包码
     *
     * <p>玩家使用礼包码领取奖励</p>
     *
     * @param code 礼包码
     * @return 兑换结果
     */
    @PostMapping("/redeem")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Boolean> redeemGiftCode(@RequestParam String code) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家兑换礼包码, playerId={}, code={}", playerId, code);
            boolean result = giftCodeService.redeemGiftCode(playerId, code);
            log.info("礼包码兑换成功, playerId={}, code={}", playerId, code);
            return ApiResponse.success("兑换成功", result);
        } catch (Exception e) {
            log.error("礼包码兑换失败, code={}, error={}", code, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
