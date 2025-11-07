package com.xiuxian.game.service;

import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.User;
import com.xiuxian.game.repository.PlayerProfileRepository;
import com.xiuxian.game.repository.UserRepository;
import com.xiuxian.game.util.GameCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerProfileRepository playerProfileRepository;
    private final UserRepository userRepository;
    private final GameCalculator gameCalculator;
    private final SkillService skillService;

    public PlayerProfile getCurrentPlayerProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return playerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("玩家资料不存在"));
    }

    @Transactional
    public void cultivate() {
        PlayerProfile player = getCurrentPlayerProfile();
        
        // 使用GameCalculator计算收益
        long expGain = gameCalculator.calculateExpPerSecond(player);
        long spiritStonesGain = gameCalculator.calculateSpiritStonesPerSecond(player);
        
        player.setExp(player.getExp() + expGain);
        player.setSpiritStones(player.getSpiritStones() + spiritStonesGain);
        player.setCultivationPoints(player.getCultivationPoints() + 1);
        
        // 更新总修炼时间
        player.setTotalCultivationTime(player.getTotalCultivationTime() + 1);
        
        // 检查是否可以升级
        gameCalculator.checkLevelUp(player);
        
        playerProfileRepository.save(player);
    }
    


    @Transactional
    public PlayerProfile savePlayerProfile(PlayerProfile player) {
        return playerProfileRepository.save(player);
    }

    @Transactional
    public PlayerProfile updatePlayerProfile(PlayerProfile player) {
        return playerProfileRepository.save(player);
    }

    /**
     * 创建新玩家并自动初始化基础技能
     */
    @Transactional
    public PlayerProfile createNewPlayer(User user) {
        PlayerProfile player = PlayerProfile.builder()
                .user(user)
                .level(1)
                .exp(0L)
                .spiritStones(100L) // 新玩家赠送100灵石
                .cultivationPoints(0L)
                .totalCultivationTime(0L)
                .cultivationSpeed(java.math.BigDecimal.ONE)
                .realm("练气期")
                .build();
        
        PlayerProfile savedPlayer = playerProfileRepository.save(player);
        
        // 初始化基础技能（直接调用 SkillService 的方法）
        skillService.initializePlayerSkills(savedPlayer); // 为玩家分配基础技能
        
        return savedPlayer;
    }
}