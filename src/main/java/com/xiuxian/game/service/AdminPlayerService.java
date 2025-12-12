package com.xiuxian.game.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.User;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPlayerService {

    private final PlayerProfileMapper playerProfileMapper;
    private final UserMapper userMapper;

    /**
     * 获取玩家列表（支持分页和搜索）
     *
     * @param page     页码
     * @param size     每页大小
     * @param nickname 昵称搜索关键词
     * @param userId   用户ID搜索
     * @return 玩家分页列表
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
     * 根据ID获取玩家详情
     *
     * @param playerId 玩家ID
     * @return 玩家详情
     */
    public PlayerProfile getPlayerDetail(Integer playerId) {
        return playerProfileMapper.selectById(playerId);
    }

    /**
     * 更新玩家属性
     *
     * @param playerId 玩家ID
     * @param profile  更新的玩家信息
     * @return 更新后的玩家信息
     */
    public PlayerProfile updatePlayerProfile(Integer playerId, PlayerProfile profile) {
        PlayerProfile existingProfile = playerProfileMapper.selectById(playerId);
        if (existingProfile == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        // 只更新允许修改的字段
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
     * 封禁/解封玩家
     *
     * @param userId   用户ID
     * @param ban      true为封禁，false为解封
     * @param reason   封禁原因
     * @return 更新后的用户信息
     */
    public User banPlayer(Integer userId, boolean ban, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        user.setRole(ban ? "BANNED" : "USER");
        if (ban && reason != null && !reason.isEmpty()) {
            // 可以将封禁原因存储在备注字段或其他地方
        }
        userMapper.updateById(user);
        return user;
    }

    /**
     * 删除玩家（谨慎操作）
     *
     * @param playerId 玩家ID
     * @return 是否删除成功
     */
    public boolean deletePlayer(Integer playerId) {
        PlayerProfile profile = playerProfileMapper.selectById(playerId);
        if (profile == null) {
            throw new IllegalArgumentException("玩家不存在");
        }

        // 删除玩家相关信息（注意：这会级联删除相关数据）
        return playerProfileMapper.deleteById(playerId) > 0;
    }

    /**
     * 发放奖励给玩家
     *
     * @param playerId 玩家ID
     * @param spiritStones 灵石数量
     * @param exp 经验值
     * @return 更新后的玩家信息
     */
    public PlayerProfile grantReward(Integer playerId, Long spiritStones, Long exp) {
        PlayerProfile profile = playerProfileMapper.selectById(playerId);
        if (profile == null) {
            throw new IllegalArgumentException("玩家不存在");
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
}