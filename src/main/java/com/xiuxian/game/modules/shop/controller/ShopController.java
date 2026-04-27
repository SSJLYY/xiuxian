package com.xiuxian.game.modules.shop.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.shop.entity.ShopItem;
import com.xiuxian.game.modules.shop.service.ShopService;
import com.xiuxian.game.modules.skill.entity.SkillShopItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ShopItem>>> listItems(@RequestParam(required = false) String type) {
        try {
            return ResponseEntity.ok(ApiResponse.success("获取商品成功", shopService.listItems(type)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/items/{id}/buy")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> buyItem(@PathVariable Integer id,
                                                     @RequestParam(required = false) Integer quantity,
                                                     @RequestBody(required = false) Map<String, Integer> request) {
        try {
            int resolvedQuantity = quantity != null ? quantity : 1;
            if (quantity == null && request != null) {
                if (request.get("quantity") != null) {
                    resolvedQuantity = request.get("quantity");
                } else if (request.get("count") != null) {
                    resolvedQuantity = request.get("count");
                }
            }
            shopService.buyItem(id, resolvedQuantity);
            return ResponseEntity.ok(ApiResponse.success("购买成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/skills")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SkillShopItem>>> listSkills() {
        try {
            return ResponseEntity.ok(ApiResponse.success("获取技能商店成功", shopService.listSkillShop()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/skills/{skillId}/buy")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> buySkill(@PathVariable Integer skillId) {
        try {
            shopService.buySkill(skillId);
            return ResponseEntity.ok(ApiResponse.success("购买技能成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
