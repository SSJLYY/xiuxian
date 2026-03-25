package com.xiuxian.game.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.entity.User;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.mapper.UserMapper;
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
     * 鑾峰彇鐜╁鍒楄〃锛堟敮鎸佸垎椤靛拰鎼滅储锛?
     *
     * @param page     椤电爜
     * @param size     姣忛〉澶у皬
     * @param nickname 鏄电О鎼滅储鍏抽敭璇?
     * @param userId   鐢ㄦ埛ID鎼滅储
     * @return 鐜╁鍒嗛〉鍒楄〃
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
     * 鏍规嵁ID鑾峰彇鐜╁璇︽儏
     *
     * @param playerId 鐜╁ID
     * @return 鐜╁璇︽儏
     */
    public PlayerProfile getPlayerDetail(Integer playerId) {
        return playerProfileMapper.selectById(playerId);
    }

    /**
     * 鏇存柊鐜╁灞炴€?
     *
     * @param playerId 鐜╁ID
     * @param profile  鏇存柊鐨勭帺瀹朵俊鎭?
     * @return 鏇存柊鍚庣殑鐜╁淇℃伅
     */
    public PlayerProfile updatePlayerProfile(Integer playerId, PlayerProfile profile) {
        PlayerProfile existingProfile = playerProfileMapper.selectById(playerId);
        if (existingProfile == null) {
            throw new IllegalArgumentException("鐜╁涓嶅瓨鍦?);
        }

        // 鍙洿鏂板厑璁镐慨鏀圭殑瀛楁
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
     * 灏佺/瑙ｅ皝鐜╁
     *
     * @param userId   鐢ㄦ埛ID
     * @param ban      true涓哄皝绂侊紝false涓鸿В灏?
     * @param reason   灏佺鍘熷洜
     * @return 鏇存柊鍚庣殑鐢ㄦ埛淇℃伅
     */
    public User banPlayer(Integer userId, boolean ban, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("鐢ㄦ埛涓嶅瓨鍦?);
        }

        user.setRole(ban ? "BANNED" : "USER");
        if (ban && reason != null && !reason.isEmpty()) {
            // 鍙互灏嗗皝绂佸師鍥犲瓨鍌ㄥ湪澶囨敞瀛楁鎴栧叾浠栧湴鏂?
        }
        userMapper.updateById(user);
        return user;
    }

    /**
     * 鍒犻櫎鐜╁锛堣皑鎱庢搷浣滐級
     *
     * @param playerId 鐜╁ID
     * @return 鏄惁鍒犻櫎鎴愬姛
     */
    public boolean deletePlayer(Integer playerId) {
        PlayerProfile profile = playerProfileMapper.selectById(playerId);
        if (profile == null) {
            throw new IllegalArgumentException("鐜╁涓嶅瓨鍦?);
        }

        // 鍒犻櫎鐜╁鐩稿叧淇℃伅锛堟敞鎰忥細杩欎細绾ц仈鍒犻櫎鐩稿叧鏁版嵁锛?
        return playerProfileMapper.deleteById(playerId) > 0;
    }

    /**
     * 鍙戞斁濂栧姳缁欑帺瀹?
     *
     * @param playerId 鐜╁ID
     * @param spiritStones 鐏电煶鏁伴噺
     * @param exp 缁忛獙鍊?
     * @return 鏇存柊鍚庣殑鐜╁淇℃伅
     */
    public PlayerProfile grantReward(Integer playerId, Long spiritStones, Long exp) {
        PlayerProfile profile = playerProfileMapper.selectById(playerId);
        if (profile == null) {
            throw new IllegalArgumentException("鐜╁涓嶅瓨鍦?);
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
     * 列出所有用户（AdminController用）
     */
    public List<User> listAllUsers() {
        return userMapper.selectList(null);
    }

    /**
     * 更新用户角色（AdminController用）
     */
    public User updateUserRole(Integer userId, String role) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        u.setRole(role);
        userMapper.updateById(u);
        return u;
    }

    /**
     * 修改管理员密码（AdminController用）
     */
    public void changeAdminPassword(String username, String newPassword,
                                    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        User u = userMapper.selectByUsername(username);
        if (u == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        u.setPassword(passwordEncoder.encode(newPassword));
        u.setMustChangePassword(false);
        userMapper.updateById(u);
    }
}
