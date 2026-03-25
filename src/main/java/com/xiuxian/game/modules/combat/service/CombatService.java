package com.xiuxian.game.modules.combat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiuxian.game.common.config.GameBalanceConfig;
import com.xiuxian.game.common.util.GameBalanceUtils;
import com.xiuxian.game.dto.response.CombatResult;
import com.xiuxian.game.dto.response.PetCombatBonus;
import com.xiuxian.game.modules.combat.entity.CombatLog;
import com.xiuxian.game.modules.combat.entity.Monster;
import com.xiuxian.game.modules.combat.mapper.CombatLogMapper;
import com.xiuxian.game.modules.combat.entity.MapMonster;
import com.xiuxian.game.modules.combat.mapper.MapMonsterMapper;
import com.xiuxian.game.modules.combat.mapper.MonsterMapper;
import com.xiuxian.game.modules.equipment.service.EquipmentService;
import com.xiuxian.game.modules.pet.entity.PlayerPet;
import com.xiuxian.game.modules.pet.service.PetService;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class CombatService {

    private final PlayerService playerService;       // 模块边界：通过PlayerService访问玩家数据
    private final MapMonsterMapper mapMonsterMapper;
    private final MonsterMapper monsterMapper;
    private final CombatLogMapper combatLogMapper;
    private final EquipmentService equipmentService;
    private final ObjectMapper objectMapper;
    private final PetService petService;             // GDD: 宠物参战机制
    private final GameBalanceConfig balance;
    private final GameBalanceUtils balanceUtils;

    /**
     * 鐢熸垚鎬墿
     */
    public Monster generateMonster(Integer playerLevel) {
        return generateMonsterByMap(playerLevel, 1); // 榛樿鍦板浘1
    }
    
    /**
     * 鏍规嵁鍦板浘鐢熸垚鎬墿
     * @param playerLevel 鐜╁绛夌骇
     * @param mapId 鍦板浘ID (1=鏂版墜鏉? 2=閲庡)
     */
    public Monster generateMonster(Integer playerLevel, Integer mapId) {
        return generateMonsterByMap(playerLevel, mapId != null ? mapId : 1);
    }
    
    private Monster generateMonsterByMap(Integer playerLevel, int mapId) {
        // 鏍规嵁鍦板浘ID璋冩暣鎬墿绛夌骇鑼冨洿
        int minLevel, maxLevel;
        String monsterType;
        
        switch (mapId) {
            case 1: // 鏂版墜鏉?- 绠€鍗曞湴鍥撅紙鎬墿绛夌骇浣庝簬鐜╁锛?
                minLevel = Math.max(1, playerLevel - 5);
                maxLevel = Math.max(1, playerLevel - 2);
                break;
            case 2: // 閲庡 - 涓瓑闅惧害鍦板浘
                minLevel = Math.max(1, playerLevel - 1);
                maxLevel = playerLevel + 1;
                break;
            default: // 榛樿鎯呭喌
                minLevel = Math.max(1, playerLevel - 2);
                maxLevel = playerLevel + 2;
                break;
        }
        
        // 鏍规嵁鍦板浘璋冩暣鎬墿绫诲瀷姒傜巼
        int typeRoll = rng().nextInt(100);
        if (mapId == 1) {
            // 鏂版墜鏉戯細95%鏅€氾紝5%绮捐嫳锛?%BOSS
            if (typeRoll < 95) {
                monsterType = "鏅€?;
            } else {
                monsterType = "绮捐嫳";
            }
        } else if (mapId == 2) {
            // 閲庡锛?0%鏅€氾紝25%绮捐嫳锛?%BOSS
            if (typeRoll < 70) {
                monsterType = "鏅€?;
            } else if (typeRoll < 95) {
                monsterType = "绮捐嫳";
            } else {
                monsterType = "BOSS";
            }
        } else {
            // 榛樿锛?0%鏅€氾紝25%绮捐嫳锛?%BOSS
            if (typeRoll < 70) {
                monsterType = "鏅€?;
            } else if (typeRoll < 95) {
                monsterType = "绮捐嫳";
            } else {
                monsterType = "BOSS";
            }
        }
        
        Monster monster = monsterMapper.selectRandomByLevelAndType(playerLevel, monsterType);
        
        // 濡傛灉鏁版嵁搴撴病鏈夊搴旀€墿锛屽皾璇曞湪绛夌骇鑼冨洿鍐呴殢鏈洪€夊彇锛屾渶鍚庢寜鏈€澶х瓑绾у厹搴?
        if (monster == null) {
            List<Monster> candidates = monsterMapper.selectByLevelRange(minLevel, maxLevel);
            if (candidates != null && !candidates.isEmpty()) {
                monster = candidates.get(rng().nextInt(candidates.size()));
            } else {
                monster = monsterMapper.selectRandomByMaxLevel(maxLevel);
            }
        }
        
        // 濡傛灉杩樻槸娌℃湁鎬墿锛岀敓鎴愪复鏃舵€墿
        if (monster == null) {
            int targetLevel = (minLevel + maxLevel) / 2;
            monster = generateTemporaryMonster(targetLevel, monsterType);
        }
        
        // 瀵逛簬鏂版墜鏉戯紝杩涗竴姝ラ檷浣庢€墿灞炴€э紙70%锛?
        if (mapId == 1) {
            monster = weakenMonster(monster, 0.7);
        }
        
        return monster;
    }
    
    /**
     * 鍓婂急鎬墿灞炴€?
     */
    private Monster weakenMonster(Monster monster, double factor) {
        Monster weakened = Monster.builder()
                .id(monster.getId())
                .name(monster.getName())
                .description(monster.getDescription())
                .level(monster.getLevel())
                .type(monster.getType())
                .health((int)(monster.getHealth() * factor))
                .attack((int)(monster.getAttack() * factor))
                .defense((int)(monster.getDefense() * factor))
                .speed(monster.getSpeed())
                .expReward(monster.getExpReward())
                .spiritStonesReward(monster.getSpiritStonesReward())
                .dropRate(monster.getDropRate())
                .dropEquipmentId(monster.getDropEquipmentId())
                .createdAt(monster.getCreatedAt())
                .updatedAt(monster.getUpdatedAt())
                .build();
        return weakened;
    }

    /**
     * 鐢熸垚涓存椂鎬墿锛堝綋鏁版嵁搴撴病鏈夊搴旀€墿鏃讹級- 浼樺寲骞宠　鎬?
     */
    private Monster generateTemporaryMonster(Integer level, String type) {
        String[] normalNames = {"閲庣嫾", "灞辫醇", "濡栨€?, "閭慨", "鎭剁伒"};
        String[] eliteNames = {"鐙傛毚閲庣嫾", "灞辫醇澶寸洰", "鍗冨勾濡栨€?, "閭亾闀胯€?, "鍘夐"};
        String[] bossNames = {"鐙肩帇", "瀵ㄤ富", "濡栫帇", "閭亾鎶ゆ硶", "楝肩帇"};
        
        String[] names = type.equals("BOSS") ? bossNames : 
                        type.equals("绮捐嫳") ? eliteNames : normalNames;
        String name = names[rng().nextInt(names.length)];
        
        double typeMultiplier = type.equals("BOSS") ? 2.5 : 
                               type.equals("绮捐嫳") ? 1.3 : 0.8;
        
        // 浼樺寲灞炴€ц绠楋紝闄嶄綆鎬墿寮哄害
        return Monster.builder()
                .name(name)
                .description("绛夌骇" + level + "鐨? + type + "鎬墿")
                .level(level)
                .type(type)
                .health((int)(80 + level * 15 * typeMultiplier))
                .attack((int)(8 + level * 2 * typeMultiplier))
                .defense((int)(3 + level * 1 * typeMultiplier))
                .speed(10 + level / 2)
                .expReward((int)(50 + level * 10 * typeMultiplier))
                .spiritStonesReward((int)(10 + level * 2 * typeMultiplier))
                .dropRate(type.equals("BOSS") ? 50 : type.equals("绮捐嫳") ? 20 : 10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 鎴樻枟涓婚€昏緫
     *
     * <p>銆怭2-9 閲嶆瀯銆戣繑鍥炲€肩敱 {@code Map<String, Object>} 鏀逛负寮虹被鍨?{@link CombatResult}锛?
     * 娑堥櫎寮辩被鍨嬪甫鏉ョ殑閿悕鎷煎啓椋庨櫓鍜岃繍琛屾椂 ClassCastException 闅愭偅銆?/p>
     */
    @Transactional
    public CombatResult startCombat(Integer playerId, Monster monster) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("鐜╁涓嶅瓨鍦?);
        }

        // 鑾峰彇鐜╁鎬诲睘鎬э紙鍩虹+瑁呭鍔犳垚锛?
        int playerHealth = player.getHealth() + player.getEquipmentHealthBonus();
        int playerAttack = player.getAttack() + player.getEquipmentAttackBonus();
        int playerDefense = player.getDefense() + player.getEquipmentDefenseBonus();
        int playerSpeed = player.getSpeed() + player.getEquipmentSpeedBonus();

        int monsterHealth = monster.getHealth();
        int monsterAttack = monster.getAttack();
        int monsterDefense = monster.getDefense();
        int monsterSpeed = monster.getSpeed();

        // GDD鏂版墜淇濇姢锛氬墠3鍦烘垬鏂楋紝鎬墿灞炴€ч檷浣?0%锛岀'淇濋鎴樺繀鑳?
        // 缁熻鐜╁鎴樻枟娆℃暟锛堜粠combat_logs琛級
        long battleCount = combatLogMapper.countByPlayerId(playerId);
        boolean isNewPlayer = battleCount < balance.getCombat().getNewbieBattleProtection();
        
        if (isNewPlayer) {
            double factor = balance.getCombat().getNewbieMonsterWeakFactor();
            monsterHealth = (int)(monsterHealth * factor);
            monsterAttack = (int)(monsterAttack * factor);
            monsterDefense = (int)(monsterDefense * factor);
            battleLog.add("馃専 鏂版墜淇濇姢涓紒鎬墿灞炴€ч檷浣? + (int)((1-factor)*100) + "%锛屽姪浣犺交鏉捐幏鑳滐紒");
        }

        List<String> battleLog = new ArrayList<>();
        battleLog.add("鈿旓笍 鎴樻枟寮€濮嬶紒" + player.getNickname() + " VS " + monster.getName());

        int currentPlayerHealth = playerHealth;
        int currentMonsterHealth = monsterHealth;
        int rounds = 0;
        final int maxRounds = balance.getCombat().getMaxRounds();

        // 銆?026-03-24 浼樺寲銆戜娇鐢℅ameBalanceUtils璁＄畻閫熷害浼樺娍
        int playerSpeedActions = balanceUtils.calculateSpeedAdvantageActions(playerSpeed, monsterSpeed);
        int monsterSpeedActions = balanceUtils.calculateSpeedAdvantageActions(monsterSpeed, playerSpeed);
        
        if (playerHasSpeedAdvantage) {
            battleLog.add("馃殌 閫熷害浼樺娍锛佷綘鐨勯€熷害鏄€墿鐨? + String.format("%.1f", speedRatio) + "鍊嶏紝姣忓洖鍚堝彲琛屽姩" + 
                (speedRatio >= 2.0 ? "3娆? : "2娆?) + "锛?);
        } else if (monsterHasSpeedAdvantage) {
            battleLog.add("鈿狅笍 閫熷害鍔ｅ娍锛佹€墿閫熷害杩滈珮浜庝綘锛屽綋蹇冿紒");
        }

        // GDD瀹犵墿鍙傛垬鏈哄埗锛氳幏鍙栧嚭鎴樺疇鐗╁苟璁＄畻鎴樻枟澧炵泭
        PetCombatBonus petBonus = petService.calculatePetCombatBonus(
            petService.getActivePet(playerId));
        
        if (petBonus != null && petBonus.isEligible()) {
            battleLog.add("馃惥 鐏靛吔鍔╂垬锛佷綘鐨? + petBonus.getPetName() + "鍑嗗鍙傛垬锛?);
            if (petBonus.getHunger() < 20) {
                battleLog.add("鈿狅笍 璀﹀憡锛氬疇鐗╅ゥ楗匡紝鎴樻枟鏁堟灉闄嶄綆50%锛?);
            }
        } else if (petBonus != null) {
            battleLog.add("馃挙 瀹犵墿鍥犻ゥ楗挎棤娉曞弬鎴橈紝蹇幓鍠傞鍚э紒");
            petBonus = null; // 鏃犳硶鍙傛垬
        }

        // 鎴樻枟寰幆
        while (currentPlayerHealth > 0 && currentMonsterHealth > 0 && rounds < maxRounds) {
            rounds++;
            
            // GDD锛氶€熷害浼樺娍鏃讹紝鐜╁鍙幏寰楅澶栬鍔?
            // 閫熷害姣?=2.0: 鐜╁3娆¤鍔?vs 鎬墿1娆?
            // 閫熷害姣?=1.5: 鐜╁2娆¤鍔?vs 鎬墿1娆?
            // 姝ｅ父: 浜ゆ浛杩涜
            
            int playerActionsThisRound = 1;
            int monsterActionsThisRound = 1;
            
            if (speedRatio >= 2.0) {
                playerActionsThisRound = 3;
                monsterActionsThisRound = 1;
            } else if (speedRatio >= 1.5) {
                playerActionsThisRound = 2;
                monsterActionsThisRound = 1;
            } else if (1.0 / speedRatio >= 2.0) {
                playerActionsThisRound = 1;
                monsterActionsThisRound = 3;
            } else if (1.0 / speedRatio >= 1.5) {
                playerActionsThisRound = 1;
                monsterActionsThisRound = 2;
            }
            
            // 鏍规嵁閫熷害鍐冲畾鍏堝悗鎵?
            boolean playerFirst = playerSpeed >= monsterSpeed;

            if (playerFirst) {
                // 鐜╁琛屽姩
                for (int i = 0; i < playerActionsThisRound && currentMonsterHealth > 0; i++) {
                    int damage = calculateDamage(playerAttack, monsterDefense, player.getLevel(), 
                        monster.getLevel(), playerSpeed, monsterSpeed, true, battleLog);
                    currentMonsterHealth -= damage;
                    String actionMarker = playerActionsThisRound > 1 ? "銆愯繛鍑? + (i+1) + "銆? : "";
                    battleLog.add("绗? + rounds + "鍥炲悎: " + actionMarker + player.getNickname() + 
                        "閫犳垚浜? + damage + "鐐逛激瀹?);
                }
                
                // GDD瀹犵墿鍙傛垬锛氭瘡N鍥炲悎瀹犵墿鍙戝姩鎶€鑳?
                if (petBonus != null && rounds % petBonus.getSkillCooldown() == 0) {
                    // 妫€鏌ユ槸鍚﹁Е鍙戞妧鑳?
                    if (rng().nextDouble() < petBonus.getSkillTriggerChance()) {
                        int petDamage = petBonus.getSkillDamage();
                        currentMonsterHealth -= petDamage;
                        String resonanceMsg = petBonus.isResonance() ? "銆愬叡楦ｇ垎鍙戙€? : "";
                        battleLog.add("馃惥 " + resonanceMsg + petBonus.getPetName() + 
                            "鍙戝姩鐏靛吔鎶€鑳斤紒閫犳垚浜? + petDamage + "鐐逛激瀹筹紒");
                    } else {
                        battleLog.add("馃惥 " + petBonus.getPetName() + "鍑嗗鍙戝姩鎶€鑳斤紝浣嗚繕鏈噯澶囧ソ...");
                    }
                }
                
                if (currentMonsterHealth <= 0) break;

                // 鎬墿琛屽姩
                for (int i = 0; i < monsterActionsThisRound && currentPlayerHealth > 0; i++) {
                    int monsterDamage = calculateDamage(monsterAttack, playerDefense, 
                        monster.getLevel(), player.getLevel(), monsterSpeed, playerSpeed, false, battleLog);
                    currentPlayerHealth -= monsterDamage;
                    String actionMarker = monsterActionsThisRound > 1 ? "銆愯繛鍑? + (i+1) + "銆? : "";
                    battleLog.add("绗? + rounds + "鍥炲悎: " + actionMarker + monster.getName() + 
                        "閫犳垚浜? + monsterDamage + "鐐逛激瀹?);
                }
            } else {
                // 鎬墿鍏堣鍔?
                for (int i = 0; i < monsterActionsThisRound && currentPlayerHealth > 0; i++) {
                    int monsterDamage = calculateDamage(monsterAttack, playerDefense, 
                        monster.getLevel(), player.getLevel(), monsterSpeed, playerSpeed, false, battleLog);
                    currentPlayerHealth -= monsterDamage;
                    String actionMarker = monsterActionsThisRound > 1 ? "銆愯繛鍑? + (i+1) + "銆? : "";
                    battleLog.add("绗? + rounds + "鍥炲悎: " + actionMarker + monster.getName() + 
                        "閫犳垚浜? + monsterDamage + "鐐逛激瀹?);
                }
                
                if (currentPlayerHealth <= 0) break;

                // 鐜╁琛屽姩
                for (int i = 0; i < playerActionsThisRound && currentMonsterHealth > 0; i++) {
                    int damage = calculateDamage(playerAttack, monsterDefense, player.getLevel(), 
                        monster.getLevel(), playerSpeed, monsterSpeed, true, battleLog);
                    currentMonsterHealth -= damage;
                    String actionMarker = playerActionsThisRound > 1 ? "銆愯繛鍑? + (i+1) + "銆? : "";
                    battleLog.add("绗? + rounds + "鍥炲悎: " + actionMarker + player.getNickname() + 
                        "閫犳垚浜? + damage + "鐐逛激瀹?);
                }
                
                // GDD瀹犵墿鍙傛垬锛氭瘡N鍥炲悎瀹犵墿鍙戝姩鎶€鑳?
                if (petBonus != null && rounds % petBonus.getSkillCooldown() == 0) {
                    if (rng().nextDouble() < petBonus.getSkillTriggerChance()) {
                        int petDamage = petBonus.getSkillDamage();
                        currentMonsterHealth -= petDamage;
                        String resonanceMsg = petBonus.isResonance() ? "銆愬叡楦ｇ垎鍙戙€? : "";
                        battleLog.add("馃惥 " + resonanceMsg + petBonus.getPetName() + 
                            "鍙戝姩鐏靛吔鎶€鑳斤紒閫犳垚浜? + petDamage + "鐐逛激瀹筹紒");
                    } else {
                        battleLog.add("馃惥 " + petBonus.getPetName() + "鍑嗗鍙戝姩鎶€鑳斤紝浣嗚繕鏈噯澶囧ソ...");
                    }
                }
            }
        }

        // GDD锛氭垬鏂楃粨鏉熷悗锛屽疇鐗╅ケ椋熷害鍑忓皯锛堟瘡娆℃垬鏂楁秷鑰?鐐癸級
        if (petBonus != null) {
            petService.consumePetHungerAfterCombat(playerId);
            PlayerPet activePet = petService.getActivePet(playerId);
            if (activePet != null && activePet.getHunger() < 20) {
                battleLog.add("馃挙 鎴樺悗瀹犵墿楗ラタ鍔犲墽锛屽綋鍓嶉ケ椋熷害锛? + activePet.getHunger() + "锛屽揩鍘诲杺椋熷惂锛?);
            }
        }

        boolean playerWon = currentMonsterHealth <= 0;
        String result = playerWon ? "WIN" : "LOSE";

        long expGained = 0;
        long spiritStonesGained = 0;
        Integer droppedEquipmentId = null;

        if (playerWon) {
            battleLog.add("鎴樻枟鑳滃埄锛?);
            expGained = calculateExpReward(monster, player.getLevel());
            spiritStonesGained = calculateSpiritStonesReward(monster, player.getLevel());

            // 瑁呭鎺夎惤妫€鏌?
            if (rng().nextInt(100) < monster.getDropRate() && monster.getDropEquipmentId() != null) {
                droppedEquipmentId = monster.getDropEquipmentId();
                try {
                    equipmentService.acquireEquipment(droppedEquipmentId, playerId);
                    battleLog.add("鑾峰緱瑁呭鎺夎惤锛?);
                } catch (Exception e) {
                    log.warn("瑁呭鎺夎惤澶辫触: {}", e.getMessage());
                }
            }

            // 鏇存柊鐜╁缁忛獙鍜岀伒鐭?
            player.setExp(player.getExp() + expGained);
            player.setSpiritStones(player.getSpiritStones() + spiritStonesGained);

            // 鍗囩骇妫€鏌?
            int levelUps = 0;
            while (player.getExp() >= player.getExpToNext() && levelUps < 100) {
                player.setExp(player.getExp() - player.getExpToNext());
                player.setLevel(player.getLevel() + 1);
                player.setExpToNext((long)(player.getExpToNext() * 1.5));
                player.setHealth(player.getHealth() + 10);
                player.setMana(player.getMana() + 5);
                player.setAttack(player.getAttack() + 2);
                player.setDefense(player.getDefense() + 1);
                player.setAttributePoints(player.getAttributePoints() + 5);
                levelUps++;
            }
            if (levelUps > 0) {
                battleLog.add("鎭枩鍗囩骇锛佸綋鍓嶇瓑绾э細" + player.getLevel());
            }

            playerService.savePlayerProfile(player);
            battleLog.add("鑾峰緱缁忛獙锛? + expGained + "锛岀伒鐭筹細" + spiritStonesGained);
        } else {
            battleLog.add("鎴樻枟澶辫触...");
            long lostSpiritStones = spiritStonesGained / 10;
            if (player.getSpiritStones() >= lostSpiritStones && lostSpiritStones > 0) {
                player.setSpiritStones(player.getSpiritStones() - lostSpiritStones);
                playerService.savePlayerProfile(player);
                battleLog.add("鎹熷け鐏电煶锛? + lostSpiritStones);
            }
        }

        // 鎸佷箙鍖栨垬鏂楁棩蹇?
        saveCombatLog(playerId, monster, result, rounds, expGained, spiritStonesGained, droppedEquipmentId, battleLog);

        return CombatResult.builder()
                .result(result)
                .rounds(rounds)
                .totalBattles(1)
                .wins(playerWon ? 1 : 0)
                .losses(playerWon ? 0 : 1)
                .winRate(playerWon ? 1.0 : 0.0)
                .averageRounds(rounds)
                .totalExpGained(expGained)
                .totalSpiritStonesGained(spiritStonesGained)
                .droppedEquipmentId(droppedEquipmentId)
                .battleLog(battleLog)
                .monsterName(monster.getName())
                .monsterLevel(monster.getLevel())
                .monsterType(monster.getType())
                .playerLevel(player.getLevel())
                .playerExp(player.getExp())
                .playerSpiritStones(player.getSpiritStones())
                .build();
    }

    /**
     * 璁＄畻浼ゅ - GDD浼樺寲鍏紡 v2
     * 鍖呭惈锛氶槻寰＄巼鏈哄埗銆佹毚鍑荤郴缁熴€侀€熷害浼樺娍
     * 
     * GDD璁捐鍘熷垯锛?
     * - 闃插尽鐜?= defense / (defense + attackerLevel * 10)锛岃闃插尽鏈夌湡瀹炰环鍊?
     * - 鏆村嚮鐜囬粯璁?%锛屾毚鍑讳激瀹?.8鍊?
     * - 閫熷害>瀵规柟1.5鍊嶆椂鑾峰緱棰濆琛屽姩鏈轰細
     */
    private int calculateDamage(int attack, int defense, int attackerLevel, int defenderLevel, 
                                 int attackerSpeed, int defenderSpeed, boolean isPlayerAttacking, 
                                 List<String> battleLog) {
        // 1. 绛夌骇鍘嬪埗锛堥檷浣庡奖鍝嶏紝淇濇寔骞宠　锛?
        double levelFactor = 1.0 + (attackerLevel - defenderLevel) * 0.03;
        levelFactor = Math.max(0.7, Math.min(1.3, levelFactor));
        
        // 2. GDD闃插尽鐜囧叕寮忥細璁╅槻寰″睘鎬ф湁鎰忎箟
        // 闃插尽鐜?= defense / (defense + attackerLevel * 10)
        double defenseRate = defense / (defense + attackerLevel * 10.0);
        defenseRate = Math.max(0, Math.min(0.7, defenseRate)); // 鏈€澶氬噺浼?0%
        
        // 3. 鍩虹浼ゅ
        int baseDamage = (int)(attack * levelFactor);
        
        // 4. 搴旂敤闃插尽鐜?
        int finalDamage = (int)(baseDamage * (1 - defenseRate));
        
        // 5. 鏆村嚮鏈哄埗锛圙DD鏂板锛?
        // 榛樿鏆村嚮鐜?%锛岄珮椋庨櫓鐜╁/鐗瑰畾鎶€鑳藉彲鎻愬崌
        double critChance = 0.05; 
        boolean isCrit = rng().nextDouble() < critChance;
        
        if (isCrit) {
            finalDamage = (int)(finalDamage * 1.8); // 鏆村嚮浼ゅ1.8鍊?
            if (battleLog != null) {
                battleLog.add((isPlayerAttacking ? "銆愭毚鍑汇€? : "銆愭€墿鏆村嚮銆?) + 
                    (isPlayerAttacking ? "浣? : "鎬墿") + "瑙﹀彂浜嗘毚鍑伙紒浼ゅ澶у箙鎻愬崌锛?);
            }
        }
        
        // 6. 闅忔満娉㈠姩 卤15%
        int variance = (int)(finalDamage * 0.15);
        if (variance > 0) {
            finalDamage += rng().nextInt(variance * 2 + 1) - variance;
        }
        
        // 7. 淇濊瘉鏈€灏忎激瀹筹紙鑷冲皯閫犳垚鏀诲嚮鍔涚殑10%锛?
        int minDamage = Math.max(1, attack / 10);
        return Math.max(minDamage, finalDamage);
    }
    
    /**
     * 绠€鍖栫殑浼ゅ璁＄畻锛堝吋瀹规棫璋冪敤锛?
     */
    private int calculateDamage(int attack, int defense, int attackerLevel, int defenderLevel) {
        return calculateDamage(attack, defense, attackerLevel, defenderLevel, 10, 10, true, null);
    }

    /**
     * 璁＄畻缁忛獙濂栧姳
     */
    private long calculateExpReward(Monster monster, int playerLevel) {
        long baseExp = monster.getExpReward();
        int levelDiff = playerLevel - monster.getLevel();
        if (levelDiff > 5) {
            baseExp = (long)(baseExp * 0.1);
        } else if (levelDiff > 0) {
            baseExp = (long)(baseExp * (1 - levelDiff * 0.1));
        } else if (levelDiff < -5) {
            baseExp = (long)(baseExp * 2.0);
        }
        return Math.max(1, baseExp);
    }

    /**
     * 璁＄畻鐏电煶濂栧姳
     * GDD璁捐锛氭垬鏂楃伒鐭?= (10 + 鎬墿绛夌骇 脳 2) 脳 鎬墿绫诲瀷鍊嶇巼
     *   鏅€氭€? 脳1.0
     *   绮捐嫳鎬? 脳2.5
     *   BOSS:   脳6.0
     */
    private long calculateSpiritStonesReward(Monster monster, int playerLevel) {
        int monsterLevel = monster.getLevel();
        String monsterType = monster.getType();

        // 璁＄畻鍩虹鐏电煶
        long baseReward = 10 + monsterLevel * 2;

        // 搴旂敤鎬墿绫诲瀷鍊嶇巼
        double typeMultiplier;
        if ("BOSS".equals(monsterType)) {
            typeMultiplier = 6.0;
        } else if ("绮捐嫳".equals(monsterType)) {
            typeMultiplier = 2.5;
        } else {
            typeMultiplier = 1.0; // 鏅€氭€?
        }

        // 绛夌骇宸儵缃氾細鐜╁姣旀€墿楂樺お澶氾紝濂栧姳闄嶄綆
        int levelDiff = playerLevel - monsterLevel;
        double levelFactor = 1.0;
        if (levelDiff > 10) {
            levelFactor = 0.5; // 楂?0绾ф儵缃?0%
        } else if (levelDiff > 5) {
            levelFactor = 0.75; // 楂?绾ф儵缃?5%
        } else if (levelDiff < -5) {
            levelFactor = 1.25; // 浣?绾у鍔?25%锛堥闄╄ˉ鍋匡級
        }

        long finalReward = (long)(baseReward * typeMultiplier * levelFactor);
        log.debug("鐏电煶璁＄畻: 鍩虹={}, 绫诲瀷鍊嶇巼={}, 绛夌骇鍥犲瓙={}, 鏈€缁?{}",
                baseReward, typeMultiplier, levelFactor, finalReward);

        return Math.max(1, finalReward);
    }

    /**
     * 鎸佷箙鍖栨垬鏂楁棩蹇楋紙鎶藉彇澶嶇敤锛?
     */
    private void saveCombatLog(Integer playerId, Monster monster, String result, int rounds,
                                long expGained, long spiritStonesGained,
                                Integer droppedEquipmentId, List<String> battleLog) {
        String battleDetailsJson = "[]";
        try {
            battleDetailsJson = objectMapper.writeValueAsString(battleLog);
        } catch (JsonProcessingException e) {
            log.error("鎴樻枟鏃ュ織搴忓垪鍖栧け璐?, e);
        }

        CombatLog combatLog = CombatLog.builder()
                .playerId(playerId)
                .monsterId(monster.getId())
                .result(result)
                .rounds(rounds)
                .expGained((int) Math.min(expGained, Integer.MAX_VALUE))
                .spiritStonesGained((int) Math.min(spiritStonesGained, Integer.MAX_VALUE))
                .equipmentDropped(droppedEquipmentId)
                .battleDetails(battleDetailsJson)
                .createdAt(LocalDateTime.now())
                .build();
        combatLogMapper.insert(combatLog);
    }

    public Monster getMonsterById(Integer monsterId) {
        if (monsterId == null) return null;
        try {
            return monsterMapper.selectById(monsterId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 鑾峰彇鎴樻枟鍘嗗彶
     */
    public List<CombatLog> getCombatHistory(Integer playerId, Integer limit) {
        return combatLogMapper.selectRecentByPlayerId(playerId, limit != null ? limit : 10);
    }
    
    /**
     * 鎵归噺鎴樻枟
     *
     * <p>銆怭1-6 淇銆戝師瀹炵幇鍏堣皟鐢?{@code startCombat()} 鍐欏叆涓€娆℃暟鎹簱锛屽啀鐢ㄥ樊鍊间慨姝ｏ紝
     * 瀛樺湪涓ゆ鍐欏叆涔嬮棿鍙戠敓宕╂簝瀵艰嚧鏁版嵁涓嶄竴鑷寸殑椋庨櫓銆?/p>
     *
     * <p>鏂板疄鐜帮細鐩存帴鍦ㄦ湰鏂规硶鍐呭畬鎴愭墍鏈夎绠楋紝鍙啓涓€娆℃暟鎹簱锛屼繚璇佸師瀛愭€с€?
     * 鎵归噺鎴樻枟鍩轰簬鍗曟鎴樻枟缁撴灉鎺ㄧ畻锛堣儨鍒欑瓑姣旀斁澶э紝璐ュ垯涓嶇粰濂栧姳锛夛紝閫昏緫涓庡師鐗堜竴鑷淬€?/p>
     *
     * @param playerId    鐜╁ID
     * @param playerLevel 鐜╁绛夌骇锛堢敤浜庣敓鎴愭€墿锛?
     * @param mapId       鍦板浘ID
     * @param times       鎴樻枟娆℃暟锛堜笂闄?00锛?
     * @return 鎴樻枟姹囨€荤粨鏋?
     */
    @Transactional
    public CombatResult batchCombat(Integer playerId, Integer playerLevel, Integer mapId, int times) {
        log.info("鎵归噺鎴樻枟寮€濮? playerId={}, times={}, mapId={}", playerId, times, mapId);

        // 涓婇檺淇濇姢
        int actualTimes = Math.min(times, 100);

        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("鐜╁涓嶅瓨鍦?);
        }

        // 鐢熸垚鎬墿
        Monster monster = generateMonster(playerLevel, mapId);
        log.debug("鐢熸垚鎬墿: {}(Lv.{} {})", monster.getName(), monster.getLevel(), monster.getType());

        // --- 鍦ㄤ笉鍐欏簱鐨勬儏鍐典笅妯℃嫙涓€娆℃垬鏂楋紝寰楀埌鍩虹缁撴灉 ---
        SingleCombatOutcome outcome = simulateSingleCombat(player, monster);

        int wins = 0;
        long totalExpGained = 0;
        long totalSpiritStonesGained = 0;
        List<String> battleLog = outcome.battleLog;

        if (outcome.playerWon) {
            // 鑳滃埄锛氬€嶆暟鏀惧ぇ濂栧姳锛屼竴娆℃€у啓搴?
            wins = actualTimes;
            totalExpGained = (long) outcome.expGained * actualTimes;
            totalSpiritStonesGained = (long) outcome.spiritStonesGained * actualTimes;

            player.setExp(player.getExp() + totalExpGained);
            player.setSpiritStones(player.getSpiritStones() + totalSpiritStonesGained);

            // 鍗囩骇妫€鏌?
            int levelUps = 0;
            while (player.getExp() >= player.getExpToNext() && levelUps < 200) {
                player.setExp(player.getExp() - player.getExpToNext());
                player.setLevel(player.getLevel() + 1);
                player.setExpToNext((long)(player.getExpToNext() * 1.5));
                player.setHealth(player.getHealth() + 10);
                player.setMana(player.getMana() + 5);
                player.setAttack(player.getAttack() + 2);
                player.setDefense(player.getDefense() + 1);
                player.setAttributePoints(player.getAttributePoints() + 5);
                levelUps++;
            }
            if (levelUps > 0) {
                log.debug("鐜╁鍗囩骇 {} 娆★紝褰撳墠绛夌骇: {}", levelUps, player.getLevel());
            }

            // 涓€娆″啓搴擄紙鍘熷瓙鎬т繚璇侊級
            playerService.savePlayerProfile(player);
        }
        // 澶辫触锛氫笉鏇存柊鐜╁鏁版嵁锛屾棤闇€鍐欏簱

        // 鎸佷箙鍖栦竴鏉′唬琛ㄦ€ф垬鏂楁棩蹇?
        saveCombatLog(playerId, monster,
                outcome.playerWon ? "WIN" : "LOSE",
                outcome.rounds,
                totalExpGained, totalSpiritStonesGained,
                null, battleLog);

        log.info("鎵归噺鎴樻枟瀹屾垚: wins={}/{}, exp={}, stones={}", wins, actualTimes, totalExpGained, totalSpiritStonesGained);

        return CombatResult.builder()
                .totalBattles(actualTimes)
                .wins(wins)
                .losses(actualTimes - wins)
                .winRate(wins > 0 ? 1.0 : 0.0)
                .averageRounds((double) outcome.rounds)
                .totalExpGained(totalExpGained)
                .totalSpiritStonesGained(totalSpiritStonesGained)
                .battleLog(battleLog)
                .monsterName(monster.getName())
                .monsterLevel(monster.getLevel())
                .monsterType(monster.getType())
                .playerLevel(player.getLevel())
                .playerExp(player.getExp())
                .playerSpiritStones(player.getSpiritStones())
                .build();
    }

    // =====================================================================
    // 鍐呴儴杈呭姪锛氫笉鍐欏簱鐨勫崟娆℃垬鏂楁ā鎷燂紙鐢ㄤ簬 batchCombat 璁＄畻锛?
    // =====================================================================

    /** 鍗曟鎴樻枟妯℃嫙缁撴灉锛堝唴閮ㄤ娇鐢級 */
    private static class SingleCombatOutcome {
        boolean playerWon;
        int rounds;
        long expGained;
        long spiritStonesGained;
        List<String> battleLog;
    }

    /**
     * 妯℃嫙涓€娆℃垬鏂楀苟杩斿洖缁撴灉锛屼笉鍐欐暟鎹簱
     * batchCombat 鐢ㄦ鏂规硶鑾峰彇鍩虹鏁版嵁锛屽啀缁熶竴涔樹互鍊嶆暟鍚庝竴娆″啓搴?
     */
    private SingleCombatOutcome simulateSingleCombat(PlayerProfile player, Monster monster) {
        int playerAttack = player.getAttack() + player.getEquipmentAttackBonus();
        int playerDefense = player.getDefense() + player.getEquipmentDefenseBonus();
        int playerSpeed = player.getSpeed() + player.getEquipmentSpeedBonus();

        int currentPlayerHealth = player.getHealth() + player.getEquipmentHealthBonus();
        int currentMonsterHealth = monster.getHealth();

        List<String> log = new ArrayList<>();
        log.add("鎴樻枟寮€濮嬶紒" + player.getNickname() + " VS " + monster.getName());

        int rounds = 0;
        boolean playerFirst = playerSpeed >= monster.getSpeed();

        while (currentPlayerHealth > 0 && currentMonsterHealth > 0 && rounds < 50) {
            rounds++;
            if (playerFirst) {
                int dmg = calculateDamage(playerAttack, monster.getDefense(), player.getLevel(), monster.getLevel());
                currentMonsterHealth -= dmg;
                log.add("绗? + rounds + "鍥炲悎: " + player.getNickname() + "閫犳垚浜? + dmg + "鐐逛激瀹?);
                if (currentMonsterHealth <= 0) break;
                int mDmg = calculateDamage(monster.getAttack(), playerDefense, monster.getLevel(), player.getLevel());
                currentPlayerHealth -= mDmg;
                log.add("绗? + rounds + "鍥炲悎: " + monster.getName() + "閫犳垚浜? + mDmg + "鐐逛激瀹?);
            } else {
                int mDmg = calculateDamage(monster.getAttack(), playerDefense, monster.getLevel(), player.getLevel());
                currentPlayerHealth -= mDmg;
                log.add("绗? + rounds + "鍥炲悎: " + monster.getName() + "閫犳垚浜? + mDmg + "鐐逛激瀹?);
                if (currentPlayerHealth <= 0) break;
                int dmg = calculateDamage(playerAttack, monster.getDefense(), player.getLevel(), monster.getLevel());
                currentMonsterHealth -= dmg;
                log.add("绗? + rounds + "鍥炲悎: " + player.getNickname() + "閫犳垚浜? + dmg + "鐐逛激瀹?);
            }
        }

        SingleCombatOutcome result = new SingleCombatOutcome();
        result.playerWon = currentMonsterHealth <= 0;
        result.rounds = rounds;
        result.battleLog = log;

        if (result.playerWon) {
            result.expGained = calculateExpReward(monster, player.getLevel());
            result.spiritStonesGained = calculateSpiritStonesReward(monster, player.getLevel());
            log.add("鎴樻枟鑳滃埄锛佽幏寰楃粡楠岋細" + result.expGained + "锛岀伒鐭筹細" + result.spiritStonesGained);
        } else {
            log.add("鎴樻枟澶辫触...");
        }

        return result;
    }

    // ===================== Map module interface (module boundary) =====================

    /**
     * Get map monster list by map ID (for GameMapService)
     */
    public List<MapMonster> getMapMonsters(Integer mapId) {
        return mapMonsterMapper.selectByMapId(mapId);
    }

    /**
     * Get monster template by ID (for GameMapService)
     */
    public Monster getMonsterById(Integer monsterId) {
        return monsterMapper.selectById(monsterId);
    }
}



