package com.xiuxian.game.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.entity.User;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.mapper.UserMapper;
import com.xiuxian.game.modules.player.service.AccountSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;

/**
 * 管理端玩家管理服务（admin 聚合层，务实例外允许直接使用玩家 Mapper）
 *
 * @author shaun.sheng
 */
@Service
@RequiredArgsConstructor
public class AdminPlayerService {

    private final PlayerProfileMapper playerProfileMapper;
    private final UserMapper userMapper;
    private final AccountSecurityService accountSecurityService;

    /**
     * 分页查询玩家列表，支持按昵称和用户ID过滤
     *
     * @param page     页码（从1开始）
     * @param size     每页大小
     * @param nickname 昵称模糊搜索（可为null）
     * @param userId   用户ID精确搜索（可为null）
     * @return 玩家档案分页结果
     */
    public Page<PlayerProfile> getPlayerList(int page, int size, String nickname, Integer userId) {
        Page<PlayerProfile> pageObj = new Page<>(page, size);
        QueryWrapper<PlayerProfile> queryWrapper = new QueryWrapper<>();

        if (nickname != null && !nickname.isEmpty()) {
            queryWrapper.like("nickname", nickname);
        }

        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }

        queryWrapper.orderByDesc("created_at");
        return playerProfileMapper.selectPage(pageObj, queryWrapper);
    }

    /**
     * 根据玩家ID获取详情
     *
     * @param playerId 玩家ID
     * @return 玩家档案，不存在返回null
     */
    public PlayerProfile getPlayerDetail(Integer playerId) {
        return playerProfileMapper.selectById(playerId);
    }

    /**
     * 更新玩家档案信息
     *
     * @param playerId 玩家ID
     * @param profile  新的档案数据
     * @return 更新后的档案
     */
    public PlayerProfile updatePlayerProfile(Integer playerId, PlayerProfile profile) {
        PlayerProfile existingProfile = playerProfileMapper.selectById(playerId);
        if (existingProfile == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在: playerId=" + playerId);
        }

        existingProfile.setNickname(profile.getNickname());
        existingProfile.setLevel(profile.getLevel());
        existingProfile.setRealm(profile.getRealm());
        existingProfile.setSpiritStones(profile.getSpiritStones());
        existingProfile.setAttack(profile.getAttack());
        existingProfile.setDefense(profile.getDefense());
        existingProfile.setHealth(profile.getHealth());
        existingProfile.setMana(profile.getMana());
        existingProfile.setSpeed(profile.getSpeed());
        existingProfile.setUpdatedAt(LocalDateTime.now());

        playerProfileMapper.updateById(existingProfile);
        return existingProfile;
    }

    /**
     * 封禁或解封玩家账号
     *
     * @param userId 用户ID
     * @param ban    true=封禁，false=解封
     * @param reason 封禁原因（可为null）
     * @return 更新后的用户对象
     */
    public User banPlayer(Integer userId, boolean ban, String reason) {
        if (ban) {
            accountSecurityService.banAccount(userId, reason);
        } else {
            accountSecurityService.unbanAccount(userId);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户不存在: userId=" + userId);
        }
        return user;
    }

    /**
     * 删除玩家档案
     *
     * @param playerId 玩家ID
     * @return 是否删除成功
     */
    public boolean deletePlayer(Integer playerId) {
        PlayerProfile profile = playerProfileMapper.selectById(playerId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在: playerId=" + playerId);
        }

        return playerProfileMapper.deleteById(playerId) > 0;
    }

    /**
     * 给玩家发放奖励（灵石和经验）
     *
     * @param playerId     玩家ID
     * @param spiritStones 发放灵石数量（可为null）
     * @param exp          发放经验值（可为null）
     * @return 更新后的玩家档案
     */
    public PlayerProfile grantReward(Integer playerId, Long spiritStones, Long exp) {
        PlayerProfile profile = playerProfileMapper.selectById(playerId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "玩家不存在: playerId=" + playerId);
        }

        if (spiritStones != null && spiritStones > 0) {
            profile.setSpiritStones(profile.getSpiritStones() + spiritStones);
        }

        if (exp != null && exp > 0) {
            profile.setExp(profile.getExp() + exp);
        }

        profile.setUpdatedAt(LocalDateTime.now());
        playerProfileMapper.updateById(profile);
        return profile;
    }

    /**
     * 获取所有用户列表
     *
     * @return 用户列表
     */
    public List<User> listAllUsers() {
        return userMapper.selectList(null);
    }

    /**
     * 修改用户角色
     *
     * @param userId 用户ID
     * @param role   新角色
     * @return 更新后的用户
     */
    public User updateUserRole(Integer userId, String role) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户不存在: userId=" + userId);
        }
        if ("admin".equalsIgnoreCase(u.getUsername())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "默认管理员角色不可修改");
        }
        u.setRole(role);
        userMapper.updateById(u);
        return u;
    }

    /**
     * 修改管理员密码
     *
     * @param username        管理员用户名
     * @param newPassword     新密码（明文）
     * @param passwordEncoder 密码加密器
     */
    public void changeAdminPassword(String username, String newPassword,
                                    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        User u = userMapper.selectByUsername(username);
        if (u == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "管理员不存在: username=" + username);
        }
        if (newPassword == null || newPassword.length() < 8
                || !newPassword.matches(".*[a-zA-Z].*")
                || !newPassword.matches(".*\\d.*")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码必须至少8位，且包含字母和数字");
        }
        u.setPassword(passwordEncoder.encode(newPassword));
        u.setMustChangePassword(false);
        userMapper.updateById(u);
    }
}
