package com.xiuxian.game.modules.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.giftcode.entity.GiftCode;
import com.xiuxian.game.modules.giftcode.entity.GiftCodeUsage;
import com.xiuxian.game.modules.giftcode.service.GiftCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/giftcode")
@RequiredArgsConstructor
public class AdminGiftCodeController {

    private final GiftCodeService giftCodeService;

    /**
     * 创建礼包�?
     */
    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<GiftCode> createGiftCode(@RequestBody GiftCode giftCode) {
        try {
            GiftCode created = giftCodeService.createGiftCode(giftCode);
            return ApiResponse.success("创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取礼包码列�?
     */
    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<GiftCode>> getGiftCodes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            Page<GiftCode> pageObj = new Page<>(page, size);
            Page<GiftCode> result = giftCodeService.page(pageObj, new QueryWrapper<GiftCode>().orderByDesc("created_at"));
            return ApiResponse.success("获取成功", result.getRecords());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新礼包�?
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<GiftCode> updateGiftCode(@PathVariable Long id, @RequestBody GiftCode giftCode) {
        try {
            giftCode.setId(id);
            giftCodeService.updateById(giftCode);
            return ApiResponse.success("更新成功", giftCode);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除礼包�?
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteGiftCode(@PathVariable Long id) {
        try {
            giftCodeService.removeById(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取礼包码使用记�?
     */
    @GetMapping("/{id}/usage")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<GiftCodeUsage>> getGiftCodeUsage(@PathVariable Long id) {
        try {
            List<GiftCodeUsage> usageHistory = giftCodeService.getGiftCodeUsageHistory(id);
            return ApiResponse.success("获取成功", usageHistory);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
