package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.SkillShopItem;
import com.xiuxian.game.entity.ShopItem;
import com.xiuxian.game.entity.User;
import com.xiuxian.game.exception.BusinessException;
import com.xiuxian.game.exception.ErrorCode;
import com.xiuxian.game.mapper.SkillShopMapper;
import com.xiuxian.game.mapper.ShopItemMapper;
import com.xiuxian.game.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理控制器
 * 提供用户管理、商店管理等后台功能
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final ShopItemMapper shopItemMapper;
    private final SkillShopMapper skillShopMapper;
    // 【修复】注入 Spring 托管的 PasswordEncoder Bean，不要 new BCryptPasswordEncoder()
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> listUsers() {
        return ResponseEntity.ok(ApiResponse.success("获取用户成功", userMapper.selectList(null)));
    }

    @PostMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> setRole(@PathVariable Integer id, @RequestParam String role) {
        User u = userMapper.selectById(id);
        if (u == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        u.setRole(role);
        userMapper.updateById(u);
        return ResponseEntity.ok(ApiResponse.success("角色更新成功", u));
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestParam String newPassword) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        User u = userMapper.selectByUsername(auth.getName());
        if (u == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        u.setPassword(passwordEncoder.encode(newPassword));
        u.setMustChangePassword(false);
        userMapper.updateById(u);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
    }

    @GetMapping("/shop/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ShopItem>>> listShopItems() {
        return ResponseEntity.ok(ApiResponse.success("获取商店商品成功", shopItemMapper.selectList(null)));
    }

    @PostMapping("/shop/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShopItem>> upsertShopItem(@RequestBody ShopItem item) {
        if (item.getId() == null) {
            shopItemMapper.insert(item);
        } else {
            shopItemMapper.updateById(item);
        }
        return ResponseEntity.ok(ApiResponse.success("保存成功", item));
    }

    @GetMapping("/shop/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SkillShopItem>>> listSkillShop() {
        return ResponseEntity.ok(ApiResponse.success("获取技能商店成功", skillShopMapper.selectList(null)));
    }

    @PostMapping("/shop/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SkillShopItem>> upsertSkillShop(@RequestBody SkillShopItem item) {
        if (item.getId() == null) {
            skillShopMapper.insert(item);
        } else {
            skillShopMapper.updateById(item);
        }
        return ResponseEntity.ok(ApiResponse.success("保存成功", item));
    }
}
