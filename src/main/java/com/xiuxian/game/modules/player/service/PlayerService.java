package com.xiuxian.game.modules.player.service;

import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.entity.PlayerItem;
import com.xiuxian.game.modules.player.entity.User;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.mapper.PlayerItemMapper;
import com.xiuxian.game.modules.player.mapper.UserMapper;
import com.xiuxian.game.modules.skill.service.SkillService;
import com.xiuxian.game.modules.quest.service.QuestProgressService;
import com.xiuxian.game.common.config.GameBalanceConfig;
import com.xiuxian.game.common.util.GameBalanceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;

/**
 * 鐜╁鏈嶅姟绫?
 * 璐熻矗鐜╁妗ｆ绠＄悊銆佷慨鐐肩郴缁熴€佸崌绾х郴缁熺瓑鏍稿績娓告垙閫昏緫
 *
 * 涓昏鍔熻兘锛?
 * - 鐜╁妗ｆ鍒涘缓鍜屾煡璇?
 * - 淇偧绯荤粺锛堝紑濮嬩慨鐐笺€佸仠姝慨鐐笺€佽绠楁敹鐩婏級
 * - 鍗囩骇绯荤粺锛堢粡楠岃绠椼€佺瓑绾ф彁鍗囥€佸鐣岀獊鐮达級
 * - 灞炴€х鐞嗭紙鍩虹灞炴€с€佽澶囧姞鎴愩€佹妧鑳藉姞鎴愶級
 * - 鏂版墜鐗╁搧鍙戞斁
 *
 * @author xiuxian
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private final PlayerProfileMapper playerProfileMapper;
    private final UserMapper userMapper;
    private final PlayerItemMapper playerItemMapper;
    private final com.xiuxian.game.modules.player.mapper.PlayerLoginLogMapper playerLoginLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final QuestProgressService questProgressService;
    private final SkillService skillService;
    private final GameBalanceConfig balance;
    private final GameBalanceUtils balanceUtils;

    /**
     * 鍒涘缓鏂扮帺瀹舵。妗?
     * 涓烘柊娉ㄥ唽鐢ㄦ埛鍒涘缓娓告垙瑙掕壊锛屽垵濮嬪寲鍩虹灞炴€у拰鏂版墜鐗╁搧
     * 
     * @param user 鐢ㄦ埛淇℃伅
     * @param nickname 鐜╁鏄电О锛屽鏋滀负null鍒欎娇鐢ㄧ敤鎴峰悕
     * @return 鍒涘缓鐨勭帺瀹舵。妗?
     * @throws RuntimeException 褰撳垱寤哄け璐ユ椂鎶涘嚭寮傚父
     */
    @Transactional
    public PlayerProfile createNewPlayer(User user, String nickname) {
        try {
            log.info("========== 鍒涘缓鏂扮帺瀹舵。妗?==========");
            log.info("鐢ㄦ埛ID: {}, 鐢ㄦ埛鍚? {}, 鏄电О: {}", user.getId(), user.getUsername(), nickname);

            // 1. 鏋勫缓鐜╁妗ｆ
            PlayerProfile playerProfile = PlayerProfile.builder()
                    .userId(user.getId())
                    .nickname(nickname != null ? nickname : user.getUsername())
                    // 鍒濆绛夌骇鍜岀粡楠?
                    .level(1)
                    .exp(balance.getPlayerInitial().getExp())
                    .expToNext(balanceUtils.calculateExpToNext(1))
                    .realm("缁冩皵鏈?)
                    .cultivationSpeed(BigDecimal.ONE)
                    // 鍒濆璧勬簮锛堜娇鐢℅DD浼樺寲鍊硷級
                    .spiritStones((long) balance.getPlayerInitial().getSpiritStones())
                    .cultivationPoints(0L)
                    .contributionPoints(0L)
                    .attributePoints(0)
                    .skillPoints(0)
                    // 鍒濆灞炴€э紙浣跨敤GDD浼樺寲鍊硷級
                    .attack(balance.getPlayerInitial().getAttack())
                    .defense(balance.getPlayerInitial().getDefense())
                    .health(balance.getPlayerInitial().getHealth())
                    .mana(balance.getPlayerInitial().getMana())
                    .speed(balance.getPlayerInitial().getSpeed())
                    // 淇偧鐘舵€?
                    .isCultivating(false)
                    .lastOnlineTime(LocalDateTime.now())
                    .totalCultivationTime(0L)
                    // 瑁呭鍔犳垚锛堝垵濮嬩负0锛?
                    .equipmentAttackBonus(0)
                    .equipmentDefenseBonus(0)
                    .equipmentHealthBonus(0)
                    .equipmentManaBonus(0)
                    .equipmentSpeedBonus(0)
                    .build();

            // 2. 淇濆瓨鍒版暟鎹簱
            playerProfileMapper.insert(playerProfile);
            PlayerProfile savedProfile = playerProfileMapper.selectById(playerProfile.getId());
            log.info("鐜╁妗ｆ淇濆瓨鎴愬姛: ID={}, 鏄电О={}, 绛夌骇={}, 澧冪晫={}", 
                    savedProfile.getId(), savedProfile.getNickname(), 
                    savedProfile.getLevel(), savedProfile.getRealm());

            // 3. 鍙戞斁鏂版墜鐗╁搧
            log.info("鍙戞斁鏂版墜鐗╁搧...");
            awardStarterItems(savedProfile.getId());

            // 4. 浠诲姟鍒濆鍖栫敱鍓嶇绗竴娆℃煡璇㈡椂鑷姩瑙﹀彂
            log.info("浠诲姟绯荤粺灏嗗湪棣栨鏌ヨ鏃惰嚜鍔ㄥ垵濮嬪寲");

            log.info("========== 鐜╁妗ｆ鍒涘缓瀹屾垚 ==========");
            return savedProfile;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("鍒涘缓鐜╁妗ｆ澶辫触: 鐢ㄦ埛鍚?{}", user.getUsername(), e);
            throw new BusinessException(ErrorCode.PLAYER_CREATE_FAILED);
        }
    }

    /**
     * 鏍规嵁ID鑾峰彇鐜╁妗ｆ
     */
    public PlayerProfile getPlayerProfileById(Integer playerId) {
        PlayerProfile profile = playerProfileMapper.selectById(playerId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
        return profile;
    }

    /**
     * 鑾峰彇褰撳墠鐧诲綍鐜╁鐨勬。妗?
     */
    public PlayerProfile getCurrentPlayerProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        String username = authentication.getName();
        log.info("鑾峰彇褰撳墠鐜╁妗ｆ: {}", username);

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        PlayerProfile profile = playerProfileMapper.selectByUserId(user.getId());
        if (profile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
        if (profile.getIsCultivating() == null) {
            profile.setIsCultivating(false);
        }
        List<PlayerItem> items = playerItemMapper.selectByPlayerId(profile.getId());
        if (items == null || items.isEmpty()) {
            awardStarterItems(profile.getId());
        }
        return profile;
    }

    /**
     * 鑾峰彇褰撳墠鐧诲綍鐜╁鐨処D
     */
    public Integer getCurrentPlayerId() {
        PlayerProfile profile = getCurrentPlayerProfile();
        return profile.getId();
    }

    /**
     * 寮€濮嬩慨鐐?
     * 鐜╁杩涘叆淇偧鐘舵€侊紝璁板綍寮€濮嬫椂闂?
     * 
     * @throws RuntimeException 褰撶帺瀹跺凡鍦ㄤ慨鐐间腑鎴栨搷浣滃け璐ユ椂鎶涘嚭寮傚父
     */
    @Transactional
    public void cultivate() {
        PlayerProfile profile = getCurrentPlayerProfile();
        log.info("鐜╁寮€濮嬩慨鐐? ID={}, 绛夌骇={}, 澧冪晫={}", profile.getId(), profile.getLevel(), profile.getRealm());

        if (profile.getIsCultivating() == null) {
            profile.setIsCultivating(false);
        }

        if (profile.getIsCultivating()) {
            log.info("鐜╁宸插湪淇偧涓紝蹇界暐閲嶅璇锋眰: ID={}", profile.getId());
            return;
        }

        profile.setIsCultivating(true);
        profile.setLastCultivationStart(LocalDateTime.now());
        playerProfileMapper.updateById(profile);

        log.info("鐜╁寮€濮嬩慨鐐兼垚鍔? ID={}, 寮€濮嬫椂闂?{}", profile.getId(), profile.getLastCultivationStart());
    }

    /**
     * 鍋滄淇偧
     * 缁撴潫淇偧鐘舵€侊紝璁＄畻淇偧鏀剁泭锛堢粡楠屻€佺伒鐭崇瓑锛夛紝妫€鏌ュ崌绾э紝鏇存柊浠诲姟杩涘害
     */
    @Transactional
    public void stopCultivate() {
        PlayerProfile profile = getCurrentPlayerProfile();
        log.info("鐜╁鍋滄淇偧: ID={}, 淇偧鐘舵€?{}", profile.getId(), profile.getIsCultivating());

        if (!profile.getIsCultivating()) {
            profile.setIsCultivating(false);
            playerProfileMapper.updateById(profile);
            log.info("鐜╁鏈湪淇偧涓紝蹇界暐鍋滄璇锋眰: ID={}", profile.getId());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = profile.getLastCultivationStart();

        if (startTime != null) {
            long cultivationTimeSeconds = java.time.Duration.between(startTime, now).getSeconds();
            long maxCultivationTime = 24 * 60 * 60;
            long actualCultivationTime = Math.min(cultivationTimeSeconds, maxCultivationTime);

            long cultivationTimeMinutes = actualCultivationTime / 60;
            long oldTotalTime = profile.getTotalCultivationTime() == null ? 0 : profile.getTotalCultivationTime();
            profile.setTotalCultivationTime(oldTotalTime + cultivationTimeMinutes);

            double baseExpPerSecond = 1.0;
            double cultivationSpeedMultiplier = profile.getCultivationSpeed().doubleValue();
            long expGained = (long) (actualCultivationTime * baseExpPerSecond * cultivationSpeedMultiplier);

            // 銆?026-03-24 浼樺寲銆戜娇鐢℅ameBalanceUtils璁＄畻鐏电煶鏀剁泭
            double cultivationHours = actualCultivationTime / 3600.0;
            long spiritStonesGained = balanceUtils.calculateCultivationSpiritStones(profile, cultivationHours);
            
            // 妫€鏌ョ伒鐭充笂闄愶紝瓒呭嚭閮ㄥ垎杞负淇偧鐐规暟
            long spiritStonesLimit = balanceUtils.calculateSpiritStonesLimit(profile.getRealm());
            long spiritStonesToAdd = Math.min(spiritStonesGained, spiritStonesLimit - profile.getSpiritStones());
            long overflowSpiritStones = spiritStonesGained - spiritStonesToAdd;
            
            if (overflowSpiritStones > 0) {
                profile.setCultivationPoints(profile.getCultivationPoints() + overflowSpiritStones);
                log.info("鐏电煶瓒呴檺锛寋}鐏电煶杞负淇偧鐐规暟", overflowSpiritStones);
            }

            profile.setExp(profile.getExp() + expGained);
            profile.setSpiritStones(profile.getSpiritStones() + spiritStonesToAdd);
            log.info("淇偧鏀剁泭: 鏃堕暱={}s, 缁忛獙+{}, 鐏电煶+{}", actualCultivationTime, expGained, spiritStonesGained);

            int oldLevel = profile.getLevel();
            checkLevelUp(profile);
            if (profile.getLevel() > oldLevel) {
                log.info("鐜╁鍗囩骇: {}绾?-> {}绾?, oldLevel, profile.getLevel());
            }

            try {
                questProgressService.updateQuestProgressByType(profile.getId(), com.xiuxian.game.modules.quest.entity.Quest.QuestType.DAILY, 1);
                questProgressService.updateQuestProgressByType(profile.getId(), com.xiuxian.game.modules.quest.entity.Quest.QuestType.WEEKLY, (int) actualCultivationTime);
                questProgressService.updateQuestProgressByType(profile.getId(), com.xiuxian.game.modules.quest.entity.Quest.QuestType.MONTHLY, 1);
            } catch (Exception qe) {
                log.warn("鏇存柊浠诲姟杩涘害澶辫触: {}", qe.getMessage());
            }
        } else {
            log.warn("淇偧寮€濮嬫椂闂翠负null锛屾棤娉曡绠楁敹鐩?);
        }

        profile.setIsCultivating(false);
        profile.setLastCultivationEnd(now);
        playerProfileMapper.updateById(profile);
        log.info("鐜╁鍋滄淇偧鎴愬姛: ID={}", profile.getId());
    }

    /**
     * 淇濆瓨鐜╁妗ｆ
     */
    @Transactional
    public void savePlayerProfile(PlayerProfile playerProfile) {
        playerProfileMapper.updateById(playerProfile);
        log.debug("淇濆瓨鐜╁妗ｆ鎴愬姛: ID={}", playerProfile.getId());
    }

    /**
     * 妫€鏌ュ苟澶勭悊鍗囩骇
     * 褰撶帺瀹剁粡楠岃揪鍒板崌绾ц姹傛椂锛岃嚜鍔ㄥ崌绾у苟鎻愬崌灞炴€?
     * 鏀寔杩炵画鍗囩骇锛屼絾闄愬埗鏈€澶?00娆′互闃叉鏃犻檺寰幆
     * 
     * @param profile 鐜╁妗ｆ
     */
    private void checkLevelUp(PlayerProfile profile) {
        // 闃叉鏃犻檺寰幆锛屾渶澶氬崌绾?00娆?
        int maxLevelUps = 100;
        int levelUps = 0;
        
        log.debug("寮€濮嬫鏌ュ崌绾? 褰撳墠绛夌骇={}, 褰撳墠缁忛獙={}, 鍗囩骇鎵€闇€={}", 
                profile.getLevel(), profile.getExp(), profile.getExpToNext());
        
        while (profile.getExp() >= profile.getExpToNext() && levelUps < maxLevelUps) {
            String oldRealm = profile.getRealm();
            int oldLevel = profile.getLevel();
            
            // 1. 鍗囩骇
            profile.setLevel(profile.getLevel() + 1);
            profile.setExp(profile.getExp() - profile.getExpToNext());
            profile.setExpToNext(profile.getExpToNext() * 2); // 涓嬩竴绾ф墍闇€缁忛獙缈诲€?
            
            // 2. 鍗囩骇灞炴€ф彁鍗?
            profile.setAttack(profile.getAttack() + 5);
            profile.setDefense(profile.getDefense() + 3);
            profile.setHealth(profile.getHealth() + 20);
            profile.setMana(profile.getMana() + 10);
            profile.setSpeed(profile.getSpeed() + 1);
            
            log.info("鐜╁鍗囩骇: {}绾?-> {}绾? 灞炴€ф彁鍗? 鏀诲嚮+5, 闃插尽+3, 鐢熷懡+20, 娉曞姏+10, 閫熷害+1", 
                    oldLevel, profile.getLevel());
            
            // 3. 鏇存柊澧冪晫
            updateRealm(profile);
            
            // 4. 澧冪晫绐佺牬濂栧姳锛堥澶栧睘鎬х偣鍜屾妧鑳界偣锛?
            if (!java.util.Objects.equals(oldRealm, profile.getRealm())) {
                int oldAttributePoints = profile.getAttributePoints() == null ? 0 : profile.getAttributePoints();
                int oldSkillPoints = profile.getSkillPoints() == null ? 0 : profile.getSkillPoints();
                
                profile.setAttributePoints(oldAttributePoints + 5);
                profile.setSkillPoints(oldSkillPoints + 1);
                
                log.info("澧冪晫绐佺牬: {} -> {}, 濂栧姳: 灞炴€х偣+5, 鎶€鑳界偣+1", 
                        oldRealm, profile.getRealm());
            }
            
            levelUps++;
        }
        
        if (levelUps > 0) {
            log.info("鍗囩骇瀹屾垚: 鍏卞崌绾}绾? 褰撳墠绛夌骇={}, 鍓╀綑缁忛獙={}, 涓嬬骇鎵€闇€={}", 
                    levelUps, profile.getLevel(), profile.getExp(), profile.getExpToNext());
        }
        
        if (levelUps >= maxLevelUps) {
            log.warn("鐜╁鍗囩骇娆℃暟杈惧埌涓婇檺({}娆?锛屽彲鑳藉瓨鍦ㄩ棶棰? ID={}, 褰撳墠缁忛獙={}", 
                    maxLevelUps, profile.getId(), profile.getExp());
        }
    }
    
    /**
     * 鏍规嵁绛夌骇鏇存柊澧冪晫
     * 澧冪晫鍒掑垎锛?
     * - 缁冩皵鏈? 1-100绾?
     * - 绛戝熀鏈? 101-200绾?
     * - 閲戜腹鏈? 201-400绾?
     * - 鍏冨┐鏈? 401-700绾?
     * - 鍖栫鏈? 701-1000绾?
     * - 鍚堜綋鏈? 1001-1500绾?
     * - 澶т箻鏈? 1501-2000绾?
     * - 娓″姭鏈? 2001绾т互涓?
     * 
     * @param profile 鐜╁妗ｆ
     */
    private void updateRealm(PlayerProfile profile) {
        int level = profile.getLevel();
        String newRealm;
        
        if (level >= 2001) {
            newRealm = "娓″姭鏈?;
        } else if (level >= 1501) {
            newRealm = "澶т箻鏈?;
        } else if (level >= 1001) {
            newRealm = "鍚堜綋鏈?;
        } else if (level >= 701) {
            newRealm = "鍖栫鏈?;
        } else if (level >= 401) {
            newRealm = "鍏冨┐鏈?;
        } else if (level >= 201) {
            newRealm = "閲戜腹鏈?;
        } else if (level >= 101) {
            newRealm = "绛戝熀鏈?;
        } else {
            newRealm = "缁冩皵鏈?;
        }
        
        profile.setRealm(newRealm);
    }

    /**
     * 鍙戞斁鏂版墜鐗╁搧
     * 涓烘柊鐜╁鍙戞斁鍒濆鐗╁搧锛屽寘鎷熀纭€瑁呭鍜屾秷鑰楀搧
     * 
     * @param playerId 鐜╁ID
     */
    private void awardStarterItems(Integer playerId) {
        try {
            log.info("涓虹帺瀹?{} 鍙戞斁鏂版墜鐗╁搧", playerId);
            
            // 鏂版墜鐗╁搧1: 鐤椾激涓?x1
            PlayerItem item1 = PlayerItem.builder()
                    .playerId(playerId)
                    .itemId(1)  // 鐤椾激涓?
                    .quantity(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            // 鏂版墜鐗╁搧2: 鍥炵伒涓?x5
            PlayerItem item2 = PlayerItem.builder()
                    .playerId(playerId)
                    .itemId(2)  // 鍥炵伒涓?
                    .quantity(5)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            playerItemMapper.insert(item1);
            playerItemMapper.insert(item2);
            
            log.info("鏂版墜鐗╁搧鍙戞斁鎴愬姛: 鐤椾激涓箈1, 鍥炵伒涓箈5");
        } catch (Exception e) {
            log.warn("鍙戞斁鏂版墜鐗╁搧澶辫触: {}", e.getMessage(), e);
            // 涓嶆姏鍑哄紓甯革紝鍏佽鐜╁鍒涘缓缁х画
        }
    }

    /**
     * 鑾峰彇鐜╁璇︾粏淇℃伅锛屽寘鍚墍鏈夊睘鎬у姞鎴?
     */
    public PlayerProfile getPlayerProfileWithBonuses(Integer playerId) {
        PlayerProfile profile = playerProfileMapper.selectById(playerId);
        if (profile == null) {
            throw new IllegalArgumentException("鐜╁涓嶅瓨鍦?);
        }
        
        // 璁＄畻鎶€鑳藉姞鎴?
        Map<String, Integer> skillBonuses = skillService.calculateSkillBonuses(playerId);
        
        // 璁剧疆鎶€鑳藉姞鎴愬埌鐜╁灞炴€т腑
        profile.setSkillHealthBonus(skillBonuses.get("health"));
        profile.setSkillManaBonus(skillBonuses.get("mana"));
        profile.setSkillAttackBonus(skillBonuses.get("attack"));
        profile.setSkillDefenseBonus(skillBonuses.get("defense"));
        profile.setSkillSpeedBonus(skillBonuses.get("speed"));
        
        return profile;
    }

    /**
     * 排行榜专用：查询最高等级的Top N玩家
     * 供RankingService使用，通过Service接口访问，遵守模块边界
     *
     * @param limit 返回条数
     * @return 按等级降序的玩家列表
     */
    public List<PlayerProfile> getTopPlayersByLevel(int limit) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlayerProfile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlayerProfile>()
                        .orderByDesc(PlayerProfile::getLevel)
                        .orderByDesc(PlayerProfile::getExp)
                        .last("LIMIT " + Math.min(limit, 100));
        return playerProfileMapper.selectList(wrapper);
    }

    /**
     * 排行榜专用：查询最多灵石的Top N玩家
     * 供RankingService使用
     *
     * @param limit 返回条数
     * @return 按灵石数降序的玩家列表
     */
    public List<PlayerProfile> getTopPlayersBySpiritStones(int limit) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlayerProfile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlayerProfile>()
                        .orderByDesc(PlayerProfile::getSpiritStones)
                        .last("LIMIT " + Math.min(limit, 100));
        return playerProfileMapper.selectList(wrapper);
    }

    /**
     * 排行榜专用：查询最高修炼速度的Top N玩家
     * 供RankingService使用
     *
     * @param limit 返回条数
     * @return 按修炼速度降序的玩家列表
     */
    public List<PlayerProfile> getTopPlayersByCultivationSpeed(int limit) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlayerProfile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlayerProfile>()
                        .orderByDesc(PlayerProfile::getCultivationSpeed)
                        .orderByDesc(PlayerProfile::getLevel)
                        .last("LIMIT " + Math.min(limit, 100));
        return playerProfileMapper.selectList(wrapper);
    }

    /**
     * 统计指定时间范围内新增玩家数（供Admin统计服务使用）
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 新增玩家数
     */
    public long countNewPlayers(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PlayerProfile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.between("created_at", startTime, endTime);
        return playerProfileMapper.selectCount(wrapper);
    }

    /**
     * 统计留存玩家数（供Admin统计服务使用）
     * 在createStart~createEnd期间创建，且在loginStart~loginEnd期间有登录的玩家数
     */
    public long countRetainedPlayers(java.time.LocalDateTime createStart, java.time.LocalDateTime createEnd,
                                      java.time.LocalDateTime loginStart, java.time.LocalDateTime loginEnd) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PlayerProfile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.between("created_at", createStart, createEnd)
               .between("last_login_at", loginStart, loginEnd);
        return playerProfileMapper.selectCount(wrapper);
    }

    /**
     * 统计活跃玩家数（供Admin统计服务使用）
     */
    public long countActivePlayers(java.time.LocalDateTime since) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PlayerProfile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.ge("last_login_at", since);
        return playerProfileMapper.selectCount(wrapper);
    }

    // ===================== 背包管理方法（供equipment模块InventoryService使用） =====================

    /**
     * 获取玩家所有背包物品（供InventoryService使用）
     */
    public List<PlayerItem> getPlayerItemsByPlayerId(Integer playerId) {
        return playerItemMapper.selectByPlayerId(playerId);
    }

    /**
     * 根据ID获取背包条目（供InventoryService使用）
     */
    public PlayerItem getPlayerItemById(Integer playerItemId) {
        return playerItemMapper.selectById(playerItemId);
    }

    /**
     * 根据玩家ID和物品ID获取背包条目（供InventoryService使用）
     */
    public PlayerItem getPlayerItemByPlayerAndItem(Integer playerId, Integer itemId) {
        return playerItemMapper.selectByPlayerIdAndItemId(playerId, itemId);
    }

    /**
     * 保存（插入或更新）背包条目（供InventoryService使用）
     */
    public void savePlayerItem(PlayerItem playerItem) {
        if (playerItem.getId() == null) {
            playerItemMapper.insert(playerItem);
        } else {
            playerItemMapper.updateById(playerItem);
        }
    }

    /**
     * 删除背包条目（供InventoryService使用）
     */
    public void deletePlayerItem(Integer playerItemId) {
        playerItemMapper.deleteById(playerItemId);
    }

    /**
     * 根据ID获取背包条目后更新（供InventoryService使用）
     */
    public void updatePlayerItem(PlayerItem playerItem) {
        playerItemMapper.updateById(playerItem);
    }

    // ===================== 反作弊/登录安全方法（供AntiFraudService使用） =====================

    /**
     * 查询玩家最近登录日志（供AntiFraudService使用）
     */
    public List<com.xiuxian.game.modules.player.entity.PlayerLoginLog> getRecentLoginLogs(
            Integer playerId, java.time.LocalDateTime since) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.xiuxian.game.modules.player.entity.PlayerLoginLog> qw =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.eq("player_id", playerId).ge("login_at", since);
        return playerLoginLogMapper.selectList(qw);
    }

    /**
     * 查询玩家某时间段内的登录次数（供AntiFraudService使用）
     */
    public long countRecentLogins(Integer playerId, java.time.LocalDateTime since) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.xiuxian.game.modules.player.entity.PlayerLoginLog> qw =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.eq("player_id", playerId).ge("login_at", since);
        return playerLoginLogMapper.selectCount(qw);
    }

    /**
     * 封禁用户（供AntiFraudService使用）
     */
    public void banUser(Integer userId, String status) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
    }

    /**
     * 根据ID获取用户（供AntiFraudService使用）
     */
    public User getUserById(Integer userId) {
        return userMapper.selectById(userId);
    }
}

