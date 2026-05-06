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
 * 宗门服务类
 *
 * <p>提供宗门管理功能，包括宗门创建、加入、退出、捐献等</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>创建宗门 - 玩家创建新的宗门，成为宗门门主</li>
 *   <li>申请加入 - 玩家申请加入宗门</li>
 *   <li>处理申请 - 宗门管理员审批加入申请</li>
 *   <li>退出宗门 - 玩家退出当前宗门</li>
 *   <li>宗门捐献 - 玩家向宗门捐献灵石</li>
 *   <li>获取宗门列表 - 分页获取所有宗门</li>
 *   <li>获取宗门详情 - 获取指定宗门的详细信息</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuildService {

    private final GuildMapper guildMapper;
    private final GuildMemberMapper guildMemberMapper;
    private final GuildApplicationMapper guildApplicationMapper;
    private final PlayerService playerService; // 模块边界：通过PlayerService访问玩家数据

    /**
     * 创建宗门
     *
     * <p>玩家创建新的宗门，成为宗门门主</p>
     *
     * <p>创建流程：</p>
     * <ol>
     *   <li>校验宗门创建参数</li>
     *   <li>校验玩家状态并扣除创建费用</li>
     *   <li>创建宗门实体</li>
     *   <li>添加创建者为宗主</li>
     * </ol>
     *
     * @param playerId 玩家ID
     * @param guildName 宗门名称
     * @param description 宗门描述
     * @throws BusinessException 当参数错误、玩家状态异常或费用不足时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void createGuild(Integer playerId, String guildName, String description) {
        log.info("创建宗门: playerId={}, guildName={}", playerId, guildName);
        
        // 参数校验
        validateGuildCreation(playerId, guildName, description);
        
        // 校验玩家状态 + 扣费
        validatePlayerAndDeductFee(playerId);
        
        // 创建宗门实体
        Guild guild = buildGuildEntity(guildName, description, playerId);
        guildMapper.insert(guild);
        
        // 添加创建者为宗主
        addGuildLeader(guild.getId(), playerId);
        
        log.info("宗门创建成功: guildId={}, guildName={}", guild.getId(), guildName);
    }
    
    /**
     * 校验宗门创建参数
     *
     * <p>校验宗门名称、描述等参数是否合法</p>
     *
     * @param playerId 玩家ID
     * @param guildName 宗门名称
     * @param description 宗门描述
     * @throws BusinessException 当参数不合法时抛出
     */
    private void validateGuildCreation(Integer playerId, String guildName, String description) {
        if (guildName == null || guildName.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "宗门名称不能为空");
        }
        if (guildName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "宗门名称不能超过20个字符");
        }
        if (description != null && description.length() > 200) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "宗门简介不能超过200个字符");
        }
        
        GuildMember existingMember = guildMemberMapper.selectOne(
                new QueryWrapper<GuildMember>().eq("player_id", playerId));
        if (existingMember != null) {
            throw new BusinessException(ErrorCode.GUILD_ALREADY_JOINED);
        }
        
        Guild existingGuild = guildMapper.selectOne(
                new QueryWrapper<Guild>().eq("guild_name", guildName.trim()));
        if (existingGuild != null) {
            throw new BusinessException(ErrorCode.GUILD_NAME_EXISTS);
        }
    }
    
    /**
     * 校验玩家状态并扣除创建费用
     *
     * <p>校验玩家等级是否满足要求，并扣除创建宗门所需的灵石</p>
     *
     * @param playerId 玩家ID
     * @return 玩家档案
     * @throws BusinessException 当玩家不存在、等级不足或灵石不足时抛出
     */
    private void validatePlayerAndDeductFee(Integer playerId) {
        PlayerProfile profile = playerService.getPlayerProfileById(playerId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
        if (defaultInt(profile.getLevel(), 1) < 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "需要达到20级才能创建宗门");
        }
        final long CREATE_COST = 10000L;
        if (defaultLong(profile.getSpiritStones()) < CREATE_COST) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES, "创建宗门需要" + CREATE_COST + "灵石");
        }
        profile.setSpiritStones(defaultLong(profile.getSpiritStones()) - CREATE_COST);
        playerService.savePlayerProfile(profile);
    }
    
    /**
     * 构建宗门实体
     *
     * <p>根据传入的参数构建宗门实体对象</p>
     *
     * @param guildName 宗门名称
     * @param description 宗门描述
     * @param playerId 创建者ID
     * @return 宗门实体
     */
    private Guild buildGuildEntity(String guildName, String description, Integer playerId) {
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
        return guild;
    }
    
    /**
     * 添加创建者为宗主
     *
     * <p>将创建者添加为宗门的宗主</p>
     *
     * @param guildId 宗门ID
     * @param playerId 创建者ID
     */
    private void addGuildLeader(Integer guildId, Integer playerId) {
        GuildMember member = new GuildMember();
        member.setGuildId(guildId);
        member.setPlayerId(playerId);
        member.setRole("LEADER");
        member.setContribution(0);
        member.setJoinedAt(LocalDateTime.now());
        guildMemberMapper.insert(member);
    }

    /**
     * 申请加入宗门
     *
     * <p>玩家申请加入指定宗门，需要宗门管理员审批</p>
     *
     * @param playerId 玩家ID
     * @param guildId 宗门ID
     * @throws BusinessException 当玩家已申请、等级不足或宗门不存在时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyToGuild(Integer playerId, Long guildId) {
        log.info("玩家申请加入宗门: playerId={}, guildId={}", playerId, guildId);

        GuildMember existingMember = guildMemberMapper.selectOne(
                new QueryWrapper<GuildMember>().eq("player_id", playerId));
        if (existingMember != null) {
            throw new BusinessException(ErrorCode.GUILD_ALREADY_JOINED);
        }

        Guild guild = guildMapper.selectById(guildId);
        if (guild == null) {
            throw new BusinessException(ErrorCode.GUILD_NOT_FOUND);
        }
        
        // 模块边界：通过PlayerService访问玩家数据
        GuildApplication existingApp = guildApplicationMapper.selectOne(
                new QueryWrapper<GuildApplication>()
                        .eq("player_id", playerId)
                        .eq("guild_id", guildId.intValue())  // 转换为Integer
                        .eq("status", "PENDING"));
        if (existingApp != null) {
            throw new BusinessException(ErrorCode.GUILD_APPLICATION_EXISTS);
        }
        
        // 模块边界：通过PlayerService访问玩家数据
        PlayerProfile profile = playerService.getPlayerProfileById(playerId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
        
        if (defaultInt(profile.getLevel(), 1) < 10) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "需要达到10级才能加入宗门");
        }
        
        // 创建申请
        GuildApplication application = new GuildApplication();
        application.setGuildId(guildId.intValue());  // 转换为Integer
        application.setPlayerId(playerId);
        application.setStatus("PENDING");
        application.setAppliedAt(LocalDateTime.now());
        
        guildApplicationMapper.insert(application);
        
        log.info("宗门申请已提交: applicationId={}, guildId={}, playerId={}", 
                application.getId(), guildId, playerId);
    }

    /**
     * 处理宗门申请
     *
     * <p>宗门管理员审批玩家的加入申请</p>
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>校验申请是否存在</li>
     *   <li>校验申请状态</li>
     *   <li>校验处理者权限</li>
     *   <li>如果批准，添加成员并增加成员计数</li>
     *   <li>如果拒绝，更新申请状态</li>
     * </ol>
     *
     * @param applicationId 申请ID
     * @param handlerId 处理者ID
     * @param approved 是否批准
     * @throws BusinessException 当申请不存在、已处理或权限不足时抛出
     */
    @Transactional(rollbackFor = Exception.class)
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
        
        // 模块边界：通过PlayerService访问玩家数据
        GuildMember handler = guildMemberMapper.selectOne(
                new QueryWrapper<GuildMember>()
                        .eq("guild_id", application.getGuildId())
                        .eq("player_id", handlerId));
        
        if (handler == null || (!"LEADER".equals(handler.getRole()) && !"OFFICER".equals(handler.getRole()))) {
            throw new BusinessException(ErrorCode.GUILD_NO_PERMISSION);
        }
        
        if (approved) {
            GuildMember existingMember = guildMemberMapper.selectOne(
                    new QueryWrapper<GuildMember>().eq("player_id", application.getPlayerId()));
            if (existingMember != null) {
                throw new BusinessException(ErrorCode.GUILD_ALREADY_JOINED);
            }

            // 检查成员上限（通过原子操作判断，而非先查后改）
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
            
            // 原子增加成员计数（防止并发加入导致计数不准确）
            int rows = guildMapper.incrementMemberCount(application.getGuildId());
            if (rows == 0) {
                // 宗门已满（并发冲突）
                guildMemberMapper.deleteById(member.getId());
                throw new BusinessException(ErrorCode.GUILD_FULL);
            }
            
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
     * 退出宗门
     *
     * <p>玩家退出当前所在的宗门</p>
     *
     * <p>退出流程：</p>
     * <ol>
     *   <li>校验玩家是否为宗门成员</li>
     *   <li>校验玩家是否为宗主（宗主不能退出）</li>
     *   <li>删除成员记录</li>
     *   <li>减少宗门成员计数</li>
     * </ol>
     *
     * @param playerId 玩家ID
     * @throws BusinessException 当玩家不是宗门成员或为宗主时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void leaveGuild(Integer playerId) {
        log.info("退出宗门: playerId={}", playerId);
        
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
        
        // 原子减少成员计数（防止并发退出导致计数不准确）
        guildMapper.decrementMemberCount(member.getGuildId());
        
        log.info("退出宗门成功: playerId={}, guildId={}", playerId, member.getGuildId());
    }

    /**
     * 宗门捐献
     *
     * <p>玩家向宗门捐献灵石，增加宗门资金和玩家贡献</p>
     *
     * <p>捐献流程：</p>
     * <ol>
     *   <li>校验玩家是否为宗门成员</li>
     *   <li>扣除玩家灵石</li>
     *   <li>增加宗门资金</li>
     *   <li>增加玩家贡献</li>
     * </ol>
     *
     * @param playerId 玩家ID
     * @param amount 捐献金额
     * @throws BusinessException 当玩家不是宗门成员或灵石不足时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void donate(Integer playerId, Integer amount) {
        log.info("宗门捐献: playerId={}, amount={}", playerId, amount);

        if (amount == null || amount <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "捐献金额必须大于0");
        }
        
        GuildMember member = guildMemberMapper.selectOne(
                new QueryWrapper<GuildMember>().eq("player_id", playerId));
        
        if (member == null) {
            throw new BusinessException(ErrorCode.GUILD_NOT_MEMBER);
        }
        
        // 扣除玩家灵石
        PlayerProfile profile = playerService.getPlayerProfileById(playerId);
        if (defaultLong(profile.getSpiritStones()) < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SPIRIT_STONES);
        }
        
        profile.setSpiritStones(defaultLong(profile.getSpiritStones()) - amount);
        playerService.savePlayerProfile(profile);
        
        // 原子增加宗门资金（防止并发捐献导致资金不一致）
        guildMapper.addGuildFunds(member.getGuildId(), amount);
        
        // 原子增加成员贡献（防止并发捐献导致贡献不一致）
        guildMemberMapper.addContribution(member.getId(), amount);
        
        log.info("宗门捐献成功: playerId={}, amount={}, guildId={}", 
                playerId, amount, member.getGuildId());
    }

    /**
     * 获取宗门列表
     *
     * <p>分页获取所有宗门列表，按等级和经验降序排列</p>
     *
     * @param page 页码
     * @param size 每页数量
     * @return 宗门分页列表
     */
    public IPage<Guild> getGuildList(int page, int size) {
        IPage<Guild> pageObj = PageUtil.createPage(page, size);
        QueryWrapper<Guild> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("level", "exp");
        
        return guildMapper.selectPage(pageObj, wrapper);
    }

    /**
     * 获取宗门详情
     *
     * <p>获取指定宗门的详细信息</p>
     *
     * @param guildId 宗门ID
     * @return 宗门信息
     * @throws BusinessException 当宗门不存在时抛出
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
     *
     * <p>获取指定宗门的所有成员列表，按贡献降序排列</p>
     *
     * @param guildId 宗门ID
     * @return 成员列表
     */
    public List<GuildMember> getGuildMembers(Long guildId) {
        return guildMemberMapper.selectList(
                new QueryWrapper<GuildMember>()
                        .eq("guild_id", guildId)
                        .orderByDesc("contribution"));
    }

    /**
     * 获取玩家的宗门信息
     *
     * <p>获取当前玩家所在的宗门信息</p>
     *
     * @param playerId 玩家ID
     * @return 宗门信息，如果玩家不在任何宗门则返回null
     */
    public Guild getPlayerGuild(Integer playerId) {
        GuildMember member = guildMemberMapper.selectOne(
                new QueryWrapper<GuildMember>().eq("player_id", playerId));
        
        if (member == null) {
            return null;
        }
        
        return guildMapper.selectById(member.getGuildId());
    }
    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}

