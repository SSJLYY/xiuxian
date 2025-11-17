package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.SkillShopItem;
import com.xiuxian.game.service.SkillShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/skills")
@RequiredArgsConstructor
public class ShopController {

    private final SkillShopService skillShopService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SkillShopItem>>> list() {
        try {
            return ResponseEntity.ok(ApiResponse.success("获取技能商店成功", skillShopService.listAvailable()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{shopItemId}/buy")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> buy(@PathVariable Integer shopItemId) {
        try {
            skillShopService.buySkill(shopItemId);
            return ResponseEntity.ok(ApiResponse.success("购买成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/sell/{playerSkillId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> sell(@PathVariable Integer playerSkillId) {
        try {
            skillShopService.sellSkill(playerSkillId);
            return ResponseEntity.ok(ApiResponse.success("出售成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
