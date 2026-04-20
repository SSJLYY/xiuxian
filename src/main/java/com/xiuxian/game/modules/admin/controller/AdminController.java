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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员综合控制器
 *
 * <p>提供用户管理、商店管理等基础管理功能。</p>
 *
 * <p>注意：复杂操作通过 Service 接口调用，不直接访问 Mapper。</p>
 *
 * @author shaun.sheng
 * @version 1.0.0
 * @since 2024-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminPlayerService adminPlayerService;  // 玩家管理服务
    private final ShopService shopService;                // 商店服务
    private final SkillShopService skillShopService;      // 技能商店服务
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取所有用户列表
     *
     * <p>管理员查看所有用户列表。</p>
     *
     * @return 用户列表
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> listUsers() {
        log.debug("管理员获取用户列表");
        List<User> users = adminPlayerService.listAllUsers();
        log.debug("管理员获取用户列表成功: count={}", users.size());
        return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));
    }

    /**
     * 更新用户角色
     *
     * <p>管理员更新指定用户的角色。</p>
     *
     * @param id 用户ID
     * @param role 新角色
     * @return 更新后的用户信息
     */
    @PostMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> setRole(@PathVariable Integer id, @RequestParam String role) {
        log.info("管理员更新用户角色: userId={}, role={}", id, role);
        
        // 角色白名单校验，防止权限提升攻击
        if (role == null || (!role.equals("USER") && !role.equals("ADMIN") && !role.equals("VIP"))) {
            log.warn("非法的角色值：role={}", role);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "非法的角色值");
        }
        
        User u = adminPlayerService.updateUserRole(id, role);
        log.info("管理员更新用户角色成功: userId={}, role={}", id, role);
        return ResponseEntity.ok(ApiResponse.success("角色更新成功", u));
    }

    /**
     * 修改管理员密码
     *
     * <p>管理员修改自己的密码。</p>
     *
     * @param newPassword 新密码
     * @return 修改结果
     */
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestParam String newPassword) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        log.info("管理员修改密码: adminId={}", auth.getName());
        adminPlayerService.changeAdminPassword(auth.getName(), newPassword, passwordEncoder);
        log.info("管理员修改密码成功: adminId={}", auth.getName());
        return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
    }

    /**
     * 获取商店商品列表
     *
     * <p>管理员查看所有商店商品。</p>
     *
     * @return 商品列表
     */
    @GetMapping("/shop/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ShopItem>>> listShopItems() {
        log.debug("管理员获取商店商品列表");
        List<ShopItem> items = shopService.listAllItems();
        log.debug("管理员获取商店商品列表成功: count={}", items.size());
        return ResponseEntity.ok(ApiResponse.success("获取商品列表成功", items));
    }

    /**
     * 保存商店商品
     *
     * <p>管理员创建或更新商店商品。</p>
     *
     * @param item 商品信息
     * @return 保存后的商品
     */
    @PostMapping("/shop/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShopItem>> upsertShopItem(@RequestBody ShopItem item) {
        log.info("管理员保存商店商品: itemId={}, shopType={}", item.getId(), item.getShopType());
        shopService.upsertShopItem(item);
        log.info("管理员保存商店商品成功: itemId={}", item.getId());
        return ResponseEntity.ok(ApiResponse.success("商品保存成功", item));
    }

    /**
     * 获取技能商店商品列表
     *
     * <p>管理员查看所有技能商店商品。</p>
     *
     * @return 技能商品列表
     */
    @GetMapping("/shop/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SkillShopItem>>> listSkillShop() {
        log.debug("管理员获取技能商店商品列表");
        List<SkillShopItem> items = skillShopService.listAvailable();
        log.debug("管理员获取技能商店商品列表成功: count={}", items.size());
        return ResponseEntity.ok(ApiResponse.success("获取技能商品列表成功", items));
    }

    /**
     * 保存技能商店商品
     *
     * <p>管理员创建或更新技能商店商品。</p>
     *
     * @param item 技能商品信息
     * @return 保存后的技能商品
     */
    @PostMapping("/shop/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SkillShopItem>> upsertSkillShop(@RequestBody SkillShopItem item) {
        log.info("管理员保存技能商店商品: itemId={}", item.getId());
        skillShopService.upsertSkillShopItem(item);
        log.info("管理员保存技能商店商品成功: itemId={}", item.getId());
        return ResponseEntity.ok(ApiResponse.success("技能商品保存成功", item));
    }
}
