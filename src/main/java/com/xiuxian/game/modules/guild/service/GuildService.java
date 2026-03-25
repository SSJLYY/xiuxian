package com.xiuxian.game.modules.guild.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiuxian.game.modules.guild.entity.Guild;
import com.xiuxian.game.modules.guild.entity.GuildApplication;
import com.xiuxian.game.modules.guild.entity.GuildMember;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.guild.mapper.GuildApplicationMapper;
import com.xiuxian.game.modules.guild.mapper.GuildMapper;
import com.xiuxian.game.modules.guild.mapper.GuildMemberMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.common.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 宗门服务�?
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuildService {

    private final GuildMapper guildMapper;
    private final GuildMemberMapper guildMemberMapper;
    private final GuildApplicationMapper guildApplicationMapper;
    private final PlayerService playerService; // 妯″潡杈圭晫锛氶€氳繃PlayerService璁块棶鐜╁鏁版嵁

    /**
     * 创建宗门
     */
    @Transactional(rollbackFor = Exception.class)
    public void createGuild(Integer playerId, String guildName, String description) {
        log.info("创建宗门: playerId={}, guildName={}", playerId, guildName);
        
        // 参数校验
        if (guildName == null || guildName.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "宗门名称不能为空");
        }
        
        if (guildName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "宗门名称不能超过20个字�?);
        }
        
        if (description != null && description.length() > 200) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "宗门简介不能超�?00个字�?);
        }
        
        // 检查玩家是否已加入宗门
        GuildMember existingMember = guildMemberMapper.selectOne(
                new QueryWrapper<GuildMember>().eq("player_id", playerId));
        if (existingMember != null) {
            throw new BusinessException(ErrorCode.GUILD_ALREADY_JOINED);
        }
        
        // 检查宗门名称是否已存在
        Guild existingGuild = guildMapper.selectOne(
                new QueryWrapper<Guild>().eq("guild_name", guildName.trim()));
        if (existingGuild != null) {
            throw new BusinessException(ErrorCode.GUILD_NAME_EXISTS);
        }
        
        // 检查玩家等级是否满足创建条件（假设需�?0级）
        PlayerProfile profile = playerService.getPlayerProfileById(playerId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
        
        if (profile.getLevel() < 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "需要达�?0级才能创建宗�?);
        }
        
        // 检查玩家灵石是否足够（创建宗门需�?0000灵石�?
        final long CREATE_COST = 10000L;
        if (profile.getSpiritStones() < CREATE_COST) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES, "创建宗门需�? + CREATE_COST + "灵石");
        }
        
        // 扣除创建费用
        profile.setSpiritStones(profile.getSpiritStones() - CREATE_COST);
        playerService.savePlayerProfile(profile);
        
        // 创建宗门
        Guild guild = new Guild();
        guild.setGuildName(guildName.trim());
        guild.setDescription(description != null ? description.trim() : "");
        guild.setLeaderId(playerId);
        guild.setLevel(1);
        guild.setExp(0L);
        guild.setExpToNext(1000L);
        guild.setMemberCount(1);
        guild.setMaxMembers(20);
        guild.setGuildFunds(0L);
        guild.setCreatedAt(LocalDateTime.now());
        guild.setUpdatedAt(LocalDateTime.now());
        
        guildMapper.insert(guild);
        
        // 添加创建者为宗主
        GuildMember member = new GuildMember();
        member.setGuildId(guild.getId());
        member.setPlayerId(playerId);
        member.setRole("LEADER");
        member.setContribution(0);
        member.setJoinedAt(LocalDateTime.now());
        
        guildMemberMapper.insert(member);
        
        log.info("宗门创建成功: guildId={}, guildName={}, cost={}", guild.getId(), guildName, CREATE_COST);
    }

    /**
     * 申请加入宗门
     * @param playerId 玩家ID
     * @param guildId 宗门ID
     */
    @Transactional
    public void applyToGuild(Integer playerId, Long guildId) {
        log.info("玩家申请加入宗门: playerId={}, guildId={}", playerId, guildId);
        
        // 检查是否已申请�?
        GuildApplication existingApp = guildApplicationMapper.selectOne(
                new QueryWrapper<GuildApplication>()
                        .eq("player_id", playerId)
                        .eq("guild_id", guildId.intValue())  // 转换为Integer
                        .eq("status", "PENDING"));
        if (existingApp != null) {
            throw new BusinessException(ErrorCode.GUILD_APPLICATION_EXISTS);
        }
        
        // 检查玩家等级是否满足要求（假设需�?0级）
        PlayerProfile profile = playerService.getPlayerProfileById(playerId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
        
        if (profile.getLevel() < 10) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "需要达�?0级才能加入宗�?);
        }
        
        // 创建申请
        GuildApplication application = new GuildApplication();
        application.setGuildId(guildId.intValue());  // 转换为Integer
        application.setPlayerId(playerId);
        application.setStatus("PENDING");
        application.setAppliedAt(LocalDateTime.now());
        
        guildApplicationMapper.insert(application);
        
        log.info("宗门申请已提�? applicationId={}, guildId={}, playerId={}", 
                application.getId(), guildId, playerId);
    }

    /**
     * 处理宗门申请
     */
    @Transactional
    public void handleApplication(Long applicationId, Integer handlerId, boolean approved) {
        log.info("处理宗门申请: applicationId={}, handlerId={}, approved={}", 
                applicationId, handlerId, approved);
        
        GuildApplication application = guildApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(ErrorCode.GUILD_APPLICATION_NOT_FOUND);
        }
        
        if (!"PENDING".equals(application.getStatus())) {
            throw new BusinessException(ErrorCode.GUILD_APPLICATION_ALREADY_HANDLED);
        }
        
        // 检查处理者权�?
        GuildMember handler = guildMemberMapper.selectOne(
                new QueryWrapper<GuildMember>()
                        .eq("guild_id", application.getGuildId())
                        .eq("player_id", handlerId));
        
        if (handler == null || (!"LEADER".equals(handler.getRole()) && !"OFFICER".equals(handler.getRole()))) {
            throw new BusinessException(ErrorCode.GUILD_NO_PERMISSION);
        }
        
        if (approved) {
            // 检查宗门是否已�?
            Guild guild = guildMapper.selectById(application.getGuildId());
            if (guild.getMemberCount() >= guild.getMaxMembers()) {
                throw new BusinessException(ErrorCode.GUILD_FULL);
            }
            
            // 添加成员
            GuildMember member = new GuildMember();
            member.setGuildId(application.getGuildId());
            member.setPlayerId(application.getPlayerId());
            member.setRole("MEMBER");
            member.setContribution(0);
            member.setJoinedAt(LocalDateTime.now());
            
            guildMemberMapper.insert(member);
            
            // 更新宗门成员�?
            guild.setMemberCount(guild.getMemberCount() + 1);
            guildMapper.updateById(guild);
            
            application.setStatus("APPROVED");
        } else {
            application.setStatus("REJECTED");
        }
        
        application.setHandledBy(handlerId);
        application.setHandledAt(LocalDateTime.now());
        guildApplicationMapper.updateById(application);
        
        log.info("宗门申请处理完成: applicationId={}, status={}", applicationId, application.getStatus());
    }

    /**
     * 退出宗�?
     */
    @Transactional
    public void leaveGuild(Integer playerId) {
        log.info("退出宗�? playerId={}", playerId);
        
        GuildMember member = guildMemberMapper.selectOne(
                new QueryWrapper<GuildMember>().eq("player_id", playerId));
        
        if (member == null) {
            throw new BusinessException(ErrorCode.GUILD_NOT_MEMBER);
        }
        
        if ("LEADER".equals(member.getRole())) {
            throw new BusinessException(ErrorCode.GUILD_LEADER_CANNOT_LEAVE);
        }
        
        // 删除成员
        guildMemberMapper.deleteById(member.getId());
        
        // 更新宗门成员�?
        Guild guild = guildMapper.selectById(member.getGuildId());
        guild.setMemberCount(guild.getMemberCount() - 1);
        guildMapper.updateById(guild);
        
        log.info("退出宗门成�? playerId={}, guildId={}", playerId, member.getGuildId());
    }

    /**
     * 宗门捐献
     */
    @Transactional
    public void donate(Integer playerId, Integer amount) {
        log.info("宗门捐献: playerId={}, amount={}", playerId, amount);
        
        GuildMember member = guildMemberMapper.selectOne(
                new QueryWrapper<GuildMember>().eq("player_id", playerId));
        
        if (member == null) {
            throw new BusinessException(ErrorCode.GUILD_NOT_MEMBER);
        }
        
        // 扣除玩家灵石
        PlayerProfile profile = playerService.getPlayerProfileById(playerId);
        if (profile.getSpiritStones() < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);
        }
        
        profile.setSpiritStones(profile.getSpiritStones() - amount);
        playerService.savePlayerProfile(profile);
        
        // 增加宗门资金和成员贡�?
        Guild guild = guildMapper.selectById(member.getGuildId());
        guild.setGuildFunds(guild.getGuildFunds() + amount);
        guildMapper.updateById(guild);
        
        member.setContribution(member.getContribution() + amount);
        guildMemberMapper.updateById(member);
        
        log.info("宗门捐献成功: playerId={}, amount={}, contribution={}", 
                playerId, amount, member.getContribution());
    }

    /**
     * 获取宗门列表
     */
    public IPage<Guild> getGuildList(int page, int size) {
        IPage<Guild> pageObj = PageUtil.createPage(page, size);
        QueryWrapper<Guild> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("level", "exp");
        
        return guildMapper.selectPage(pageObj, wrapper);
    }

    /**
     * 获取宗门详情
     */
    public Guild getGuildById(Long guildId) {
        Guild guild = guildMapper.selectById(guildId);
        if (guild == null) {
            throw new BusinessException(ErrorCode.GUILD_NOT_FOUND);
        }
        return guild;
    }

    /**
     * 获取宗门成员列表
     */
    public List<GuildMember> getGuildMembers(Long guildId) {
        return guildMemberMapper.selectList(
                new QueryWrapper<GuildMember>()
                        .eq("guild_id", guildId)
                        .orderByDesc("contribution"));
    }

    /**
     * 获取玩家的宗门信�?
     */
    public Guild getPlayerGuild(Integer playerId) {
        GuildMember member = guildMemberMapper.selectOne(
                new QueryWrapper<GuildMember>().eq("player_id", playerId));
        
        if (member == null) {
            return null;
        }
        
        return guildMapper.selectById(member.getGuildId());
    }
}

