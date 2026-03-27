package com.xiuxian.game.modules.guild.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.guild.entity.Guild;
import com.xiuxian.game.modules.guild.entity.GuildMember;
import com.xiuxian.game.modules.guild.service.GuildService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宗门控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/guild")
@RequiredArgsConstructor
public class GuildController {

    private final GuildService guildService;
    private final PlayerService playerService;

    /**
     * 创建宗门
     */
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> createGuild(@RequestBody CreateGuildRequest request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            guildService.createGuild(playerId, request.getGuildName(), request.getDescription());
            return ApiResponse.success("宗门创建成功", null);
        } catch (Exception e) {
            log.error("创建宗门失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 申请加入宗门
     */
    @PostMapping("/apply/{guildId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> applyToGuild(@PathVariable Long guildId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            guildService.applyToGuild(playerId, guildId);
            return ApiResponse.success("申请已提交", null);
        } catch (Exception e) {
            log.error("申请加入宗门失败: guildId={}", guildId, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 处理宗门申请
     */
    @PostMapping("/application/{applicationId}/handle")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> handleApplication(
            @PathVariable Long applicationId,
            @RequestBody HandleApplicationRequest request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            guildService.handleApplication(applicationId, playerId, request.isApproved());
            return ApiResponse.success("处理成功", null);
        } catch (Exception e) {
            log.error("处理宗门申请失败: applicationId={}", applicationId, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 退出宗门
     */
    @PostMapping("/leave")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> leaveGuild() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            guildService.leaveGuild(playerId);
            return ApiResponse.success("已退出宗门", null);
        } catch (Exception e) {
            log.error("退出宗门失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 宗门捐献
     */
    @PostMapping("/donate")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> donate(@RequestBody DonateRequest request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            guildService.donate(playerId, request.getAmount());
            return ApiResponse.success("捐献成功", null);
        } catch (Exception e) {
            log.error("宗门捐献失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取宗门列表
     */
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> getGuildList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            IPage<Guild> guildPage = guildService.getGuildList(page, size);

            Map<String, Object> result = new HashMap<>();
            result.put("guilds", guildPage.getRecords());
            result.put("total", guildPage.getTotal());
            result.put("page", guildPage.getCurrent());
            result.put("size", guildPage.getSize());
            result.put("pages", guildPage.getPages());

            return ApiResponse.success("获取成功", result);
        } catch (Exception e) {
            log.error("获取宗门列表失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取宗门详情
     */
    @GetMapping("/{guildId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> getGuildDetail(@PathVariable Long guildId) {
        try {
            Guild guild = guildService.getGuildById(guildId);
            List<GuildMember> members = guildService.getGuildMembers(guildId);

            Map<String, Object> result = new HashMap<>();
            result.put("guild", guild);
            result.put("members", members);

            return ApiResponse.success("获取成功", result);
        } catch (Exception e) {
            log.error("获取宗门详情失败: guildId={}", guildId, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取我的宗门
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Guild> getMyGuild() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Guild guild = guildService.getPlayerGuild(playerId);
            return ApiResponse.success("获取成功", guild);
        } catch (Exception e) {
            log.error("获取我的宗门失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @Data
    public static class CreateGuildRequest {
        private String guildName;
        private String description;
    }

    @Data
    public static class HandleApplicationRequest {
        private boolean approved;
    }

    @Data
    public static class DonateRequest {
        private Integer amount;
    }
}
