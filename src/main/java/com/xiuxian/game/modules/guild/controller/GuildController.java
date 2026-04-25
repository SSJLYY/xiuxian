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
 *
 * <p>提供宗门相关的REST API接口，包括宗门创建、加入、退出、捐献等功能</p>
 *
 * <p>主要接口：</p>
 * <ul>
 *   <li>POST /api/guild/create - 创建宗门</li>
 *   <li>POST /api/guild/apply/{guildId} - 申请加入宗门</li>
 *   <li>POST /api/guild/application/{applicationId}/handle - 处理宗门申请</li>
 *   <li>POST /api/guild/leave - 退出宗门</li>
 *   <li>POST /api/guild/donate - 宗门捐献</li>
 *   <li>GET /api/guild/list - 获取宗门列表</li>
 *   <li>GET /api/guild/{guildId} - 获取宗门详情</li>
 *   <li>GET /api/guild/my - 获取我的宗门</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
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
     *
     * <p>玩家创建新的宗门，成为宗门门主</p>
     *
     * @param request 创建宗门请求，包含宗门名称和描述
     * @return 创建结果
     */
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> createGuild(@RequestBody CreateGuildRequest request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家创建宗门, playerId={}, guildName={}", playerId, request.getGuildName());
            guildService.createGuild(playerId, request.getGuildName(), request.getDescription());
            log.info("宗门创建成功, playerId={}, guildName={}", playerId, request.getGuildName());
            return ApiResponse.success("宗门创建成功", null);
        } catch (Exception e) {
            log.error("创建宗门失败, guildName={}, error={}", request.getGuildName(), e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 申请加入宗门
     *
     * <p>玩家申请加入指定宗门，需要宗门管理员审批</p>
     *
     * @param guildId 宗门ID
     * @return 申请结果
     */
    @PostMapping("/apply/{guildId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> applyToGuild(@PathVariable Long guildId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家申请加入宗门, playerId={}, guildId={}", playerId, guildId);
            guildService.applyToGuild(playerId, guildId);
            log.info("宗门申请提交成功, playerId={}, guildId={}", playerId, guildId);
            return ApiResponse.success("申请已提交", null);
        } catch (Exception e) {
            log.error("申请加入宗门失败, guildId={}, error={}", guildId, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 处理宗门申请
     *
     * <p>宗门管理员审批玩家的加入申请</p>
     *
     * @param applicationId 申请ID
     * @param request 处理请求，包含是否批准
     * @return 处理结果
     */
    @PostMapping("/application/{applicationId}/handle")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> handleApplication(
            @PathVariable Long applicationId,
            @RequestBody HandleApplicationRequest request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("处理宗门申请, playerId={}, applicationId={}, approved={}",
                    playerId, applicationId, request.isApproved());
            guildService.handleApplication(applicationId, playerId, request.isApproved());
            log.info("宗门申请处理成功, applicationId={}, approved={}", applicationId, request.isApproved());
            return ApiResponse.success("处理成功", null);
        } catch (Exception e) {
            log.error("处理宗门申请失败, applicationId={}, error={}", applicationId, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 退出宗门
     *
     * <p>玩家退出当前所在的宗门</p>
     *
     * @return 退出结果
     */
    @PostMapping("/leave")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> leaveGuild() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家退出宗门, playerId={}", playerId);
            guildService.leaveGuild(playerId);
            log.info("玩家退出宗门成功, playerId={}", playerId);
            return ApiResponse.success("已退出宗门", null);
        } catch (Exception e) {
            log.error("退出宗门失败, error={}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 宗门捐献
     *
     * <p>玩家向宗门捐献灵石，增加宗门资金</p>
     *
     * @param request 捐献请求，包含捐献金额
     * @return 捐献结果
     */
    @PostMapping("/donate")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> donate(@RequestBody DonateRequest request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家宗门捐献, playerId={}, amount={}", playerId, request.getAmount());
            guildService.donate(playerId, request.getAmount());
            log.info("宗门捐献成功, playerId={}, amount={}", playerId, request.getAmount());
            return ApiResponse.success("捐献成功", null);
        } catch (Exception e) {
            log.error("宗门捐献失败, amount={}, error={}", request.getAmount(), e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取宗门列表
     *
     * <p>分页获取所有宗门列表</p>
     *
     * @param page 页码，默认第1页
     * @param size 每页数量，默认20条
     * @return 宗门列表
     */
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> getGuildList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("获取宗门列表, page={}, size={}", page, size);
            IPage<Guild> guildPage = guildService.getGuildList(page, size);

            Map<String, Object> result = new HashMap<>();
            result.put("guilds", guildPage.getRecords());
            result.put("total", guildPage.getTotal());
            result.put("page", guildPage.getCurrent());
            result.put("size", guildPage.getSize());
            result.put("pages", guildPage.getPages());

            log.info("获取宗门列表成功, total={}, page={}", guildPage.getTotal(), page);
            return ApiResponse.success("获取成功", result);
        } catch (Exception e) {
            log.error("获取宗门列表失败, page={}, size={}, error={}", page, size, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取宗门详情
     *
     * <p>获取指定宗门的详细信息和成员列表</p>
     *
     * @param guildId 宗门ID
     * @return 宗门详情和成员列表
     */
    @GetMapping("/{guildId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> getGuildDetail(@PathVariable Long guildId) {
        try {
            log.info("获取宗门详情, guildId={}", guildId);
            Guild guild = guildService.getGuildById(guildId);
            List<GuildMember> members = guildService.getGuildMembers(guildId);

            Map<String, Object> result = new HashMap<>();
            result.put("guild", guild);
            result.put("members", members);

            log.info("获取宗门详情成功, guildId={}, memberCount={}", guildId, members.size());
            return ApiResponse.success("获取成功", result);
        } catch (Exception e) {
            log.error("获取宗门详情失败, guildId={}, error={}", guildId, e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取我的宗门
     *
     * <p>获取当前玩家所在的宗门信息</p>
     *
     * @return 宗门信息
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Guild> getMyGuild() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("获取我的宗门, playerId={}", playerId);
            Guild guild = guildService.getPlayerGuild(playerId);
            log.info("获取我的宗门成功, playerId={}, guildId={}", playerId, guild != null ? guild.getId() : null);
            return ApiResponse.success("获取成功", guild);
        } catch (Exception e) {
            log.error("获取我的宗门失败, error={}", e.getMessage(), e);
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
