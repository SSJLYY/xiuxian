package com.xiuxian.game.modules.admin.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.skill.entity.SkillShopItem;
import com.xiuxian.game.modules.shop.entity.ShopItem;
import com.xiuxian.game.modules.player.entity.User;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.admin.service.AdminPlayerService;
import com.xiuxian.game.modules.shop.service.ShopService;
import com.xiuxian.game.modules.skill.service.SkillShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员综合控制器
 * 提供用户管理、商店管理等基础管理功能
 * 注意：复杂操作通过 Service 接口调用，不直接访问 Mapper
 *
 * @author shaun.sheng
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminPlayerService adminPlayerService;  // 玩家管理服务
    private final ShopService shopService;                // 商店服务
    private final SkillShopService skillShopService;      // 技能商店服务
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> listUsers() {
        return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", adminPlayerService.listAllUsers()));
    }

    @PostMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> setRole(@PathVariable Integer id, @RequestParam String role) {
        User u = adminPlayerService.updateUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("角色更新成功", u));
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestParam String newPassword) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        adminPlayerService.changeAdminPassword(auth.getName(), newPassword, passwordEncoder);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
    }

    @GetMapping("/shop/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ShopItem>>> listShopItems() {
        return ResponseEntity.ok(ApiResponse.success("获取商品列表成功", shopService.listAllItems()));
    }

    @PostMapping("/shop/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShopItem>> upsertShopItem(@RequestBody ShopItem item) {
        shopService.upsertShopItem(item);
        return ResponseEntity.ok(ApiResponse.success("商品保存成功", item));
    }

    @GetMapping("/shop/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SkillShopItem>>> listSkillShop() {
        return ResponseEntity.ok(ApiResponse.success("获取技能商品列表成功", skillShopService.listAvailable()));
    }

    @PostMapping("/shop/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SkillShopItem>> upsertSkillShop(@RequestBody SkillShopItem item) {
        skillShopService.upsertSkillShopItem(item);
        return ResponseEntity.ok(ApiResponse.success("技能商品保存成功", item));
    }
}
