package com.xiuxian.game.modules.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.common.util.PageUtil;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.giftcode.entity.GiftCode;
import com.xiuxian.game.modules.giftcode.entity.GiftCodeUsage;
import com.xiuxian.game.modules.giftcode.service.GiftCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/api/admin/giftcode")
@RequiredArgsConstructor
@Validated
public class AdminGiftCodeController {

    private final GiftCodeService giftCodeService;

    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GiftCode>> createGiftCode(@Valid @RequestBody GiftCode giftCode) {
        try {
            GiftCode created = giftCodeService.createGiftCode(giftCode);
            return ResponseEntity.ok(ApiResponse.success("礼品码创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GiftCode>>> getGiftCodes(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        try {
            Page<GiftCode> pageObj = PageUtil.createPage(page, size);
            Page<GiftCode> result = giftCodeService.page(pageObj, new QueryWrapper<GiftCode>().orderByDesc("created_at"));
            return ResponseEntity.ok(ApiResponse.success("获取礼品码列表成功", result.getRecords()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GiftCode>> updateGiftCode(@PathVariable Long id, @Valid @RequestBody GiftCode giftCode) {
        try {
            GiftCode updated = giftCodeService.updateGiftCode(id, giftCode);
            return ResponseEntity.ok(ApiResponse.success("礼品码更新成功", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteGiftCode(@PathVariable Long id) {
        try {
            giftCodeService.deleteGiftCode(id);
            return ResponseEntity.ok(ApiResponse.success("礼品码删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/usage")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GiftCodeUsage>>> getGiftCodeUsage(@PathVariable Long id) {
        try {
            List<GiftCodeUsage> usageHistory = giftCodeService.getGiftCodeUsageHistory(id);
            return ResponseEntity.ok(ApiResponse.success("获取使用记录成功", usageHistory));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
