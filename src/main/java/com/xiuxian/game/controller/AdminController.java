package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.SkillShopItem;
import com.xiuxian.game.entity.ShopItem;
import com.xiuxian.game.entity.User;
import com.xiuxian.game.mapper.SkillShopMapper;
import com.xiuxian.game.mapper.ShopItemMapper;
import com.xiuxian.game.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final ShopItemMapper shopItemMapper;
    private final SkillShopMapper skillShopMapper;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> listUsers() {
        try {
            return ResponseEntity.ok(ApiResponse.success("获取用户成功", userMapper.selectList(null)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> setRole(@PathVariable Integer id, @RequestParam String role) {
        try {
            User u = userMapper.selectById(id);
            if (u == null) throw new RuntimeException("用户不存在");
            u.setRole(role);
            userMapper.updateById(u);
            return ResponseEntity.ok(ApiResponse.success("角色更新成功", u));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestParam String newPassword) {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) throw new RuntimeException("未登录");
            User u = userMapper.selectByUsername(auth.getName());
            if (u == null) throw new RuntimeException("用户不存在");
            org.springframework.security.crypto.password.PasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            u.setPassword(encoder.encode(newPassword));
            u.setMustChangePassword(false);
            userMapper.updateById(u);
            return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/shop/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ShopItem>>> listShopItems() {
        try {
            return ResponseEntity.ok(ApiResponse.success("获取商店商品成功", shopItemMapper.selectList(null)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/shop/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShopItem>> upsertShopItem(@RequestBody ShopItem item) {
        try {
            if (item.getId() == null) shopItemMapper.insert(item); else shopItemMapper.updateById(item);
            return ResponseEntity.ok(ApiResponse.success("保存成功", item));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/shop/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SkillShopItem>>> listSkillShop() {
        try {
            return ResponseEntity.ok(ApiResponse.success("获取技能商店成功", skillShopMapper.selectList(null)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/shop/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SkillShopItem>> upsertSkillShop(@RequestBody SkillShopItem item) {
        try {
            if (item.getId() == null) skillShopMapper.insert(item); else skillShopMapper.updateById(item);
            return ResponseEntity.ok(ApiResponse.success("保存成功", item));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}