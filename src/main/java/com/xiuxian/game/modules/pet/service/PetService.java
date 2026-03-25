package com.xiuxian.game.modules.pet.service;

import com.xiuxian.game.dto.PetEvolutionResult;
// pet module entities (same module — OK)
import com.xiuxian.game.modules.pet.entity.Pet;
import com.xiuxian.game.modules.pet.entity.PetCombatBonus;
import com.xiuxian.game.modules.pet.entity.PetEvolution;
import com.xiuxian.game.modules.pet.entity.PetSkill;
import com.xiuxian.game.modules.pet.entity.PetTrainingLog;
import com.xiuxian.game.modules.pet.entity.PlayerPet;
import com.xiuxian.game.modules.pet.entity.PlayerPetEvolution;
import com.xiuxian.game.modules.pet.entity.PlayerPetSkill;
// pet module mappers (same module — OK)
import com.xiuxian.game.modules.pet.mapper.PetEvolutionMapper;
import com.xiuxian.game.modules.pet.mapper.PetMapper;
import com.xiuxian.game.modules.pet.mapper.PetSkillMapper;
import com.xiuxian.game.modules.pet.mapper.PetTrainingLogMapper;
import com.xiuxian.game.modules.pet.mapper.PlayerPetEvolutionMapper;
import com.xiuxian.game.modules.pet.mapper.PlayerPetMapper;
import com.xiuxian.game.modules.pet.mapper.PlayerPetSkillMapper;
// cross-module entities accessed via Service interfaces
import com.xiuxian.game.modules.player.entity.PlayerItem;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.shop.entity.Item;
// cross-module services (module boundary — access via Service, not Mapper)
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 瀹犵墿鏈嶅姟绫?
 * 璐熻矗瀹犵墿绯荤粺鐨勬墍鏈変笟鍔￠€昏緫
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PetService {

    private final PetMapper petMapper;
    private final PlayerPetMapper playerPetMapper;
    private final PetSkillMapper petSkillMapper;
    private final PlayerPetSkillMapper playerPetSkillMapper;
    private final PetTrainingLogMapper petTrainingLogMapper;
    private final PetEvolutionMapper petEvolutionMapper;
    private final PlayerPetEvolutionMapper playerPetEvolutionMapper;
    // 模块边界：通过Service访问跨模块数据，不直接注入跨模块Mapper
    private final PlayerService playerService;
    private final ItemService itemService;

    // 銆愪慨澶嶃€戜娇鐢?ThreadLocalRandom 鏇夸唬鍏变韩 Random 瀹炰緥銆?
    // PetService 鏄?Spring 鍗曚緥锛屾墍鏈夊苟鍙戣姹傚叡浜悓涓€瀹炰緥銆?
    // ThreadLocalRandom 姣忎釜绾跨▼鐙珛锛屾棤閿佺珵浜夛紝鎬ц兘鏇翠紭銆?
    private static ThreadLocalRandom rng() {
        return ThreadLocalRandom.current();
    }

    /**
     * 鑾峰彇鎵€鏈夊疇鐗╂ā鏉?
     */
    public List<Pet> getAllPets() {
        return petMapper.selectList(null);
    }

    /**
     * 鑾峰彇鐜╁鍙崟鑾风殑瀹犵墿鍒楄〃
     */
    public List<Pet> getAvailablePets(Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("鐜╁涓嶅瓨鍦?);
        }
        return petMapper.selectAvailablePets(player.getLevel());
    }

    /**
     * 鑾峰彇鐜╁鐨勬墍鏈夊疇鐗?
     */
    public List<PlayerPet> getPlayerPets(Integer playerId) {
        return playerPetMapper.selectByPlayerId(playerId);
    }

    /**
     * 鑾峰彇鐜╁鐨勫嚭鎴樺疇鐗?
     */
    public PlayerPet getActivePet(Integer playerId) {
        return playerPetMapper.selectActivePet(playerId);
    }
    
    /**
     * 鑾峰彇PlayerPetMapper锛堜緵鍏朵粬鏈嶅姟浣跨敤锛?
     */
    public PlayerPetMapper getPlayerPetMapper() {
        return playerPetMapper;
    }
    
    /**
     * GDD锛氭垬鍚庡疇鐗╅ケ椋熷害琛板噺
     * 姣忔鎴樻枟娑堣€梑alance.getPet().getCombatHungerCost()鐐归ケ椋熷害
     */
    @Transactional
    public void consumePetHungerAfterCombat(Integer playerId) {
        PlayerPet activePet = playerPetMapper.selectActivePet(playerId);
        if (activePet != null && activePet.getHunger() > 0) {
            int cost = balance.getPet().getCombatHungerCost();
            int newHunger = Math.max(0, activePet.getHunger() - cost);
            activePet.setHunger(newHunger);
            playerPetMapper.updateById(activePet);
            log.info("鎴樺悗瀹犵墿楗遍搴﹁"鍑? {} -> {}", newHunger + cost, newHunger);
        }
    }

    /**
     * GDD瀹犵墿鍙傛垬鏈哄埗锛氳绠楀疇鐗╂垬鏂楀鐩?
     * 
     * 璁捐鍘熷垯锛圙DD锛夛細
     * - 蹇犺瘹搴﹀奖鍝嶆妧鑳藉彂鍔ㄦ鐜囷細0-30:60%, 31-80:80%, 81-100:100%锛堣繕鏈?%鍏遍福脳2浼ゅ锛?
     * - 楗遍搴﹀奖鍝嶅弬鎴樻晥鏋滐細<20鏃堕檷浣?0%
     * - 瀹犵墿姣廚鍥炲悎鍙戝姩鎶€鑳斤紙N = 3 + 瀹犵墿閫熷害/10锛?
     * 
     * @return PetCombatBonus 鍖呭惈鎶€鑳藉彂鍔ㄦ鐜囥€佹妧鑳戒激瀹冲姞鎴愩€佹槸鍚﹁Е鍙戝叡楦?
     */
    public PetCombatBonus calculatePetCombatBonus(PlayerPet activePet) {
        if (activePet == null) {
            return null;
        }
        
        int loyalty = activePet.getLoyalty();
        int hunger = activePet.getHunger();
        
        // 蹇犺瘹搴﹀奖鍝嶆妧鑳藉彂鍔ㄦ鐜?
        double skillTriggerChance;
        if (loyalty <= 30) {
            skillTriggerChance = 0.60;
        } else if (loyalty <= 80) {
            skillTriggerChance = 0.80;
        } else {
            skillTriggerChance = 1.00; // 81-100: 100% + 5%鍏遍福
        }
        
        // 楗遍搴﹀奖鍝嶏紙<20鏃堕檷浣?0%鏁堟灉锛?
        double hungerFactor = (hunger < 20) ? 0.5 : 1.0;
        
        // 璁＄畻瀹犵墿鎶€鑳戒激瀹筹紙鍩轰簬瀹犵墿鍩虹灞炴€э級
        Pet pet = petMapper.selectById(activePet.getPetId());
        int basePetDamage = (pet != null) ? pet.getBaseAttack() : 10;
        int petLevel = activePet.getLevel();

        // 鎶€鑳戒激瀹?= 鍩虹浼ゅ 脳 鎶€鑳界瓑绾х郴鏁?脳 蹇犺瘹搴﹀洜瀛?
        double loyaltyFactor = (loyalty >= 81) ? 1.25 : (loyalty >= 51 ? 1.1 : 1.0);
        int skillDamage = (int)(basePetDamage * (1 + petLevel * 0.1) * loyaltyFactor * hungerFactor);

        // 鎶€鑳借Е鍙戦棿闅旓紙鍥炲悎锛?
        int petSpeed = (pet != null) ? pet.getBaseSpeed() : 10;
        int skillCooldown = 3 + petSpeed / 10;

        // 鏄惁瑙﹀彂鍏遍福锛堝繝璇氬害81-100鏃?%姒傜巼锛?
        boolean resonance = (loyalty >= 81) && (rng().nextDouble() < 0.05);
        if (resonance) {
            skillDamage *= 2;
        }
        
        return PetCombatBonus.builder()
                .petId(activePet.getPetId())
                .petName(activePet.getNickname() != null ? activePet.getNickname() : (pet != null ? pet.getName() : "鐏靛吔"))
                .skillTriggerChance(skillTriggerChance)
                .skillDamage(skillDamage)
                .skillCooldown(skillCooldown)
                .resonance(resonance)
                .hungerFactor(hungerFactor)
                .loyalty(loyalty)
                .hunger(hunger)
                .build();
    }
    
    /**
     * GDD锛氬疇鐗╅ケ椋熷害鑷劧琛板噺
     * 姣忓皬鏃惰"鍑?鐐癸紝璁粌娑堣€?0鐐癸紝鎴樻枟娑堣€?鐐?
     * 
     * @param playerId 鐜╁ID
     * @return 琛板噺鍚庣殑瀹犵墿鍒楄〃
     */
    @Transactional
    public List<PlayerPet> applyHungerDecay(Integer playerId) {
        List<PlayerPet> pets = playerPetMapper.selectByPlayerId(playerId);
        LocalDateTime now = LocalDateTime.now();
        
        for (PlayerPet pet : pets) {
            if (pet.getIsActive() == null || !pet.getIsActive()) {
                continue; // 鍙鐞哸ctive瀹犵墿
            }
            
            // 璁＄畻绂荤嚎鏃堕棿锛堝皬鏃讹級
            LocalDateTime lastUpdate = pet.getLastFeedTime() != null ? pet.getLastFeedTime() : now;
            long hoursSinceLastUpdate = java.time.Duration.between(lastUpdate, now).toHours();
            
            if (hoursSinceLastUpdate > 0) {
                int decay = (int)(hoursSinceLastUpdate * 2); // 姣忓皬鏃?鐐?
                int oldHunger = pet.getHunger();
                pet.setHunger(Math.max(0, oldHunger - decay));
                
                // 楗遍搴?鏃跺繝璇氬害涓嬮檷
                if (pet.getHunger() == 0 && pet.getLoyalty() > 0) {
                    pet.setLoyalty(Math.max(0, pet.getLoyalty() - (int)hoursSinceLastUpdate));
                }
                
                playerPetMapper.updateById(pet);
                log.info("瀹犵墿{}楗遍搴﹁"鍑? {} -> {}, 绂荤嚎{}灏忔椂", 
                    pet.getNickname(), oldHunger, pet.getHunger(), hoursSinceLastUpdate);
            }
        }
        return pets;
    }

    /**
     * 鎹曡幏瀹犵墿
     */
    @Transactional
    public PlayerPet capturePet(Integer playerId, Integer petId) {
        log.info("========== 鎹曡幏瀹犵墿 ==========");
        log.info("鐜╁ID: {}, 瀹犵墿ID: {}", playerId, petId);

        // 1. 楠岃瘉鐜╁
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("鐜╁涓嶅瓨鍦?);
        }

        // 2. 楠岃瘉瀹犵墿妯℃澘
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            throw new IllegalArgumentException("瀹犵墿涓嶅瓨鍦?);
        }

        // 3. 妫€鏌ョ瓑绾ц姹?
        if (player.getLevel() < pet.getUnlockLevel()) {
            throw new IllegalArgumentException("绛夌骇涓嶈冻锛岄渶瑕? + pet.getUnlockLevel() + "绾?);
        }

        // 4. 妫€鏌ユ槸鍚﹀凡鎷ユ湁
        int count = playerPetMapper.countByPlayerIdAndPetId(playerId, petId);
        if (count >= 3) {
            throw new IllegalArgumentException("鍚岀瀹犵墿鏈€澶氭嫢鏈?鍙?);
        }

        // 5. 璁＄畻鎹曡幏鎴愬姛鐜?
        double captureChance = pet.getCaptureRate().doubleValue();
        double roll = rng().nextDouble() * 100;
        
        log.info("鎹曡幏姒傜巼: {}%, 闅忔満鏁? {}", captureChance, roll);
        
        if (roll > captureChance) {
            log.info("鎹曡幏澶辫触");
            throw new IllegalArgumentException("鎹曡幏澶辫触锛岃鍐嶈瘯涓€娆?);
        }

        // 6. 鍒涘缓鐜╁瀹犵墿
        PlayerPet playerPet = PlayerPet.builder()
                .playerId(playerId)
                .petId(petId)
                .nickname(pet.getName())
                .level(1)
                .exp(0L)
                .expToNext(100L)
                .attack(pet.getBaseAttack())
                .defense(pet.getBaseDefense())
                .health(pet.getBaseHealth())
                .maxHealth(pet.getBaseHealth())
                .speed(pet.getBaseSpeed())
                .loyalty(50)
                .hunger(100)
                .isActive(false)
                .isLocked(false)
                .totalBattles(0)
                .totalWins(0)
                .capturedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        playerPetMapper.insert(playerPet);
        PlayerPet savedPet = playerPetMapper.selectById(playerPet.getId());

        // 7. 瀛︿範鍩虹鎶€鑳?
        PetSkill basicSkill = petSkillMapper.selectById(1); // 鎾曞挰鎶€鑳?
        if (basicSkill != null) {
            PlayerPetSkill playerPetSkill = PlayerPetSkill.builder()
                    .playerPetId(savedPet.getId())
                    .petSkillId(basicSkill.getId())
                    .skillLevel(1)
                    .learnedAt(LocalDateTime.now())
                    .build();
            playerPetSkillMapper.insert(playerPetSkill);
            log.info("瀹犵墿瀛︿範鍩虹鎶€鑳? {}", basicSkill.getName());
        }

        log.info("鎹曡幏鎴愬姛: 瀹犵墿ID={}, 鏄电О={}", savedPet.getId(), savedPet.getNickname());
        log.info("========== 鎹曡幏瀹犵墿瀹屾垚 ==========");
        
        return savedPet;
    }

    /**
     * 璁剧疆鍑烘垬瀹犵墿
     */
    @Transactional
    public void setActivePet(Integer playerId, Integer playerPetId) {
        log.info("璁剧疆鍑烘垬瀹犵墿: 鐜╁ID={}, 瀹犵墿ID={}", playerId, playerPetId);

        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("瀹犵墿涓嶅瓨鍦ㄦ垨涓嶅睘浜庤鐜╁");
        }

        // 鍙栨秷鎵€鏈夊嚭鎴樼姸鎬?
        playerPetMapper.deactivateAllPets(playerId);

        // 璁剧疆鏂扮殑鍑烘垬瀹犵墿
        playerPet.setIsActive(true);
        playerPetMapper.updateById(playerPet);

        log.info("鍑烘垬瀹犵墿璁剧疆鎴愬姛: {}", playerPet.getNickname());
    }

    /**
     * 鍠傞瀹犵墿
     */
    @Transactional
    public void feedPet(Integer playerId, Integer playerPetId) {
        log.info("鍠傞瀹犵墿: 鐜╁ID={}, 瀹犵墿ID={}", playerId, playerPetId);

        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("瀹犵墿涓嶅瓨鍦ㄦ垨涓嶅睘浜庤鐜╁");
        }

        // 妫€鏌ユ槸鍚﹂渶瑕佸杺椋?
        if (playerPet.getHunger() >= 90) {
            throw new IllegalArgumentException("瀹犵墿涓嶉タ锛屾棤闇€鍠傞");
        }

        // 鎭㈠楗遍搴?
        int oldHunger = playerPet.getHunger();
        playerPet.setHunger(Math.min(100, oldHunger + balance.getPet().getFeedingHungerRestore()));
        
        // 鎻愬崌蹇犺瘹搴?
        int oldLoyalty = playerPet.getLoyalty();
        playerPet.setLoyalty(Math.min(100, oldLoyalty + balance.getPet().getFeedingLoyaltyBoost()));
        
        playerPet.setLastFeedTime(LocalDateTime.now());
        playerPetMapper.updateById(playerPet);

        log.info("鍠傞鎴愬姛: 楗遍搴?{} -> {}, 蹇犺瘹搴?{} -> {}", 
                oldHunger, playerPet.getHunger(), oldLoyalty, playerPet.getLoyalty());
    }

    /**
     * 璁粌瀹犵墿
     */
    @Transactional
    public void trainPet(Integer playerId, Integer playerPetId, String trainingType) {
        log.info("========== 璁粌瀹犵墿 ==========");
        log.info("鐜╁ID: {}, 瀹犵墿ID: {}, 璁粌绫诲瀷: {}", playerId, playerPetId, trainingType);

        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("瀹犵墿涓嶅瓨鍦ㄦ垨涓嶅睘浜庤鐜╁");
        }

        // 妫€鏌ラケ椋熷害
        if (playerPet.getHunger() < 20) {
            throw new IllegalArgumentException("瀹犵墿澶タ浜嗭紝鏃犳硶璁粌");
        }

        // 娑堣€楅ケ椋熷害
        playerPet.setHunger(Math.max(0, playerPet.getHunger() - 10));

        // 鏍规嵁璁粌绫诲瀷鎻愬崌灞炴€?
        int improvementValue = 0;
        String attributeImproved = "";
        
        switch (trainingType) {
            case "鏀诲嚮":
                improvementValue = rng().nextInt(3) + 1;
                playerPet.setAttack(playerPet.getAttack() + improvementValue);
                attributeImproved = "attack";
                break;
            case "闃插尽":
                improvementValue = rng().nextInt(3) + 1;
                playerPet.setDefense(playerPet.getDefense() + improvementValue);
                attributeImproved = "defense";
                break;
            case "閫熷害":
                improvementValue = rng().nextInt(2) + 1;
                playerPet.setSpeed(playerPet.getSpeed() + improvementValue);
                attributeImproved = "speed";
                break;
            default:
                throw new IllegalArgumentException("鏃犳晥鐨勮缁冪被鍨?);
        }

        // 鑾峰緱缁忛獙
        int expGained = rng().nextInt(20) + 10;
        playerPet.setExp(playerPet.getExp() + expGained);

        // 妫€鏌ュ崌绾?
        checkPetLevelUp(playerPet);

        // 鎻愬崌蹇犺瘹搴?
        playerPet.setLoyalty(Math.min(100, playerPet.getLoyalty() + 2));
        
        playerPet.setLastTrainTime(LocalDateTime.now());
        playerPetMapper.updateById(playerPet);

        // 璁板綍璁粌鏃ュ織
        PetTrainingLog log = PetTrainingLog.builder()
                .playerPetId(playerPetId)
                .trainingType(trainingType)
                .expGained(expGained)
                .attributeImproved(attributeImproved)
                .improvementValue(improvementValue)
                .createdAt(LocalDateTime.now())
                .build();
        petTrainingLogMapper.insert(log);

        this.log.info("璁粌瀹屾垚: {}+{}, 缁忛獙+{}", trainingType, improvementValue, expGained);
        this.log.info("========== 璁粌瀹犵墿瀹屾垚 ==========");
    }

    /**
     * 妫€鏌ュ疇鐗╁崌绾?
     */
    private void checkPetLevelUp(PlayerPet playerPet) {
        int levelUps = 0;
        int maxLevelUps = 50;

        while (playerPet.getExp() >= playerPet.getExpToNext() && levelUps < maxLevelUps) {
            playerPet.setLevel(playerPet.getLevel() + 1);
            playerPet.setExp(playerPet.getExp() - playerPet.getExpToNext());
            playerPet.setExpToNext((long) (playerPet.getExpToNext() * 1.5));

            // 鍗囩骇灞炴€ф彁鍗?
            Pet petTemplate = petMapper.selectById(playerPet.getPetId());
            if (petTemplate != null) {
                double growthRate = petTemplate.getGrowthRate().doubleValue();
                playerPet.setAttack(playerPet.getAttack() + (int) (3 * growthRate));
                playerPet.setDefense(playerPet.getDefense() + (int) (2 * growthRate));
                playerPet.setMaxHealth(playerPet.getMaxHealth() + (int) (10 * growthRate));
                playerPet.setHealth(playerPet.getMaxHealth());
                playerPet.setSpeed(playerPet.getSpeed() + (int) (1 * growthRate));
            }

            levelUps++;
            log.info("瀹犵墿鍗囩骇: {}绾?-> {}绾?, playerPet.getLevel() - 1, playerPet.getLevel());
        }
    }

    /**
     * 閲嶅懡鍚嶅疇鐗?
     */
    @Transactional
    public void renamePet(Integer playerId, Integer playerPetId, String newNickname) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("瀹犵墿涓嶅瓨鍦ㄦ垨涓嶅睘浜庤鐜╁");
        }

        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("鏄电О涓嶈兘涓虹┖");
        }

        if (newNickname.length() > 20) {
            throw new IllegalArgumentException("鏄电О杩囬暱锛屾渶澶?0涓瓧绗?);
        }

        String oldNickname = playerPet.getNickname();
        playerPet.setNickname(newNickname.trim());
        playerPetMapper.updateById(playerPet);

        log.info("瀹犵墿閲嶅懡鍚? {} -> {}", oldNickname, newNickname);
    }

    /**
     * 閲婃斁瀹犵墿
     */
    @Transactional
    public void releasePet(Integer playerId, Integer playerPetId) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("瀹犵墿涓嶅瓨鍦ㄦ垨涓嶅睘浜庤鐜╁");
        }

        if (playerPet.getIsLocked()) {
            throw new IllegalArgumentException("瀹犵墿宸查攣瀹氾紝鏃犳硶閲婃斁");
        }

        if (playerPet.getIsActive()) {
            throw new IllegalArgumentException("鍑烘垬涓殑瀹犵墿鏃犳硶閲婃斁");
        }

        playerPetMapper.deleteById(playerPetId);
        log.info("閲婃斁瀹犵墿: ID={}, 鏄电О={}", playerPetId, playerPet.getNickname());
    }

    /**
     * 閿佸畾/瑙ｉ攣瀹犵墿
     */
    @Transactional
    public void toggleLockPet(Integer playerId, Integer playerPetId) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null || !playerPet.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("瀹犵墿涓嶅瓨鍦ㄦ垨涓嶅睘浜庤鐜╁");
        }

        playerPet.setIsLocked(!playerPet.getIsLocked());
        playerPetMapper.updateById(playerPet);

        log.info("瀹犵墿閿佸畾鐘舵€佸垏鎹? {}", playerPet.getIsLocked() ? "宸查攣瀹? : "宸茶В閿?);
    }

    /**
     * 鑾峰彇瀹犵墿鐨勮缁冭褰?
     */
    public List<PetTrainingLog> getTrainingLogs(Integer playerPetId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return petTrainingLogMapper.selectByPlayerPetId(playerPetId, limit);
    }

    // ==================== 瀹犵墿杩涘寲绯荤粺 ====================

    /**
     * GDD瀹犵墿杩涘寲绯荤粺锛氭鏌ュ疇鐗╂槸鍚﹀彲浠ヨ繘鍖?
     * 杩涘寲鏉′欢锛?
     * 1. 瀹犵墿绛夌骇杈惧埌杩涘寲闂ㄦ
     * 2. 蹇犺瘹搴?>= 80
     * 3. 鎷ユ湁鎵€闇€杩涘寲閬撳叿
     *
     * @param playerPetId 鐜╁瀹犵墿ID
     * @return 鍙繘鍖栫殑淇℃伅锛宯ull琛ㄧず涓嶅彲杩涘寲
     */
    public PetEvolutionResult checkEvolution(Integer playerPetId) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null) {
            return PetEvolutionResult.builder()
                    .success(false)
                    .message("瀹犵墿涓嶅瓨鍦?)
                    .build();
        }

        // 鑾峰彇褰撳墠杩涘寲闃舵
        int currentStage = 1;
        PlayerPetEvolution evolution = playerPetEvolutionMapper.selectByPlayerPetId(playerPetId);
        if (evolution != null) {
            currentStage = evolution.getCurrentStage();
        }

        // 鑾峰彇涓嬩竴涓彲杩涘寲闃舵
        PetEvolution nextEvolution = petEvolutionMapper.selectNextEvolution(playerPet.getPetId(), currentStage);
        if (nextEvolution == null) {
            return PetEvolutionResult.builder()
                    .success(false)
                    .message("璇ュ疇鐗╁凡杈惧埌鏈€楂樿繘鍖栭樁娈?)
                    .currentStage(currentStage)
                    .build();
        }

        // 妫€鏌ョ瓑绾ц姹?
        if (playerPet.getLevel() < nextEvolution.getRequiredLevel()) {
            return PetEvolutionResult.builder()
                    .success(false)
                    .message("瀹犵墿绛夌骇涓嶈冻锛岄渶瑕? + nextEvolution.getRequiredLevel() + "绾э紙褰撳墠" + playerPet.getLevel() + "绾э級")
                    .currentStage(currentStage)
                    .build();
        }

        // 妫€鏌ュ繝璇氬害瑕佹眰
        if (playerPet.getLoyalty() < 80) {
            return PetEvolutionResult.builder()
                    .success(false)
                    .message("瀹犵墿蹇犺瘹搴︿笉瓒筹紝闇€瑕?0鐐癸紙褰撳墠" + playerPet.getLoyalty() + "鐐癸級")
                    .currentStage(currentStage)
                    .build();
        }

        // 妫€鏌ヨ繘鍖栭亾鍏?
        if (nextEvolution.getRequiredItemId() != null) {
            PlayerItem item = playerService.getPlayerItemByPlayerAndItem(playerPet.getPlayerId(), nextEvolution.getRequiredItemId());
            int owned = (item != null) ? item.getQuantity() : 0;
            if (owned < nextEvolution.getRequiredItemQuantity()) {
                Item itemTemplate = itemService.getItemById(nextEvolution.getRequiredItemId());
                String itemName = (itemTemplate != null) ? itemTemplate.getName() : "杩涘寲閬撳叿";
                return PetEvolutionResult.builder()
                        .success(false)
                        .message("缂哄皯杩涘寲閬撳叿锛岄渶瑕? + itemName + "脳" + nextEvolution.getRequiredItemQuantity() + "锛堟嫢鏈? + owned + "锛?)
                        .currentStage(currentStage)
                        .build();
            }
        }

        // 鍙互杩涘寲
        return PetEvolutionResult.builder()
                .success(true)
                .message("鍙互杩涘寲锛?)
                .newName(nextEvolution.getEvolutionName())
                .currentStage(nextEvolution.getEvolutionStage())
                .newAbilityName(nextEvolution.getNewAbilityId() != null ? "鏂拌兘鍔? : null)
                .build();
    }

    /**
     * GDD瀹犵墿杩涘寲绯荤粺锛氭墽琛屽疇鐗╄繘鍖?
     *
     * @param playerPetId 鐜╁瀹犵墿ID
     * @return 杩涘寲缁撴灉
     */
    @Transactional
    public PetEvolutionResult evolvePet(Integer playerPetId) {
        log.info("========== 瀹犵墿杩涘寲 ==========");
        log.info("鐜╁瀹犵墿ID: {}", playerPetId);

        // 楠岃瘉瀹犵墿
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null) {
            return PetEvolutionResult.builder()
                    .success(false)
                    .message("瀹犵墿涓嶅瓨鍦?)
                    .build();
        }

        // 妫€鏌ユ槸鍚﹀彲浠ヨ繘鍖?
        PetEvolutionResult checkResult = checkEvolution(playerPetId);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }

        // 鑾峰彇褰撳墠杩涘寲闃舵
        int currentStage = 1;
        PlayerPetEvolution evolution = playerPetEvolutionMapper.selectByPlayerPetId(playerPetId);
        if (evolution != null) {
            currentStage = evolution.getCurrentStage();
        }

        // 鑾峰彇涓嬩竴涓繘鍖栭樁娈?
        PetEvolution nextEvolution = petEvolutionMapper.selectNextEvolution(playerPet.getPetId(), currentStage);
        if (nextEvolution == null) {
            return PetEvolutionResult.builder()
                    .success(false)
                    .message("娌℃湁鍙敤鐨勮繘鍖栭樁娈?)
                    .build();
        }

        // 鎵ｉ櫎杩涘寲閬撳叿
        if (nextEvolution.getRequiredItemId() != null) {
            PlayerItem item = playerService.getPlayerItemByPlayerAndItem(playerPet.getPlayerId(), nextEvolution.getRequiredItemId());
            if (item != null && item.getQuantity() >= nextEvolution.getRequiredItemQuantity()) {
                item.setQuantity(item.getQuantity() - nextEvolution.getRequiredItemQuantity());
                if (item.getQuantity() <= 0) {
                    playerService.deletePlayerItem(item.getId());
                } else {
                    playerService.updatePlayerItem(item);
                }
                log.info("娑堣€楄繘鍖栭亾鍏? {} 脳{}", nextEvolution.getRequiredItemId(), nextEvolution.getRequiredItemQuantity());
            }
        }

        // 搴旂敤杩涘寲鍔犳垚
        playerPet.setAttack(playerPet.getAttack() + nextEvolution.getAttackBonus());
        playerPet.setDefense(playerPet.getDefense() + nextEvolution.getDefenseBonus());
        playerPet.setMaxHealth(playerPet.getMaxHealth() + nextEvolution.getHealthBonus());
        playerPet.setHealth(playerPet.getMaxHealth()); // 婊¤
        playerPet.setSpeed(playerPet.getSpeed() + nextEvolution.getSpeedBonus());
        playerPet.setNickname(nextEvolution.getEvolutionName());

        // 鏇存柊鎴栧垱寤鸿繘鍖栬褰?
        if (evolution != null) {
            evolution.setCurrentStage(nextEvolution.getEvolutionStage());
            evolution.setEvolvedAt(LocalDateTime.now());
            playerPetEvolutionMapper.updateById(evolution);
        } else {
            evolution = PlayerPetEvolution.builder()
                    .playerPetId(playerPetId)
                    .currentStage(nextEvolution.getEvolutionStage())
                    .evolvedAt(LocalDateTime.now())
                    .build();
            playerPetEvolutionMapper.insert(evolution);
        }

        playerPetMapper.updateById(playerPet);

        log.info("瀹犵墿杩涘寲鎴愬姛: {} -> {}, 绛夌骇{}",
                playerPet.getNickname(), nextEvolution.getEvolutionName(), playerPet.getLevel());
        log.info("灞炴€ф彁鍗? 鏀诲嚮+{}, 闃插尽+{}, 鐢熷懡+{}, 閫熷害+{}",
                nextEvolution.getAttackBonus(), nextEvolution.getDefenseBonus(),
                nextEvolution.getHealthBonus(), nextEvolution.getSpeedBonus());
        log.info("========== 瀹犵墿杩涘寲瀹屾垚 ==========");

        return PetEvolutionResult.builder()
                .success(true)
                .message("杩涘寲鎴愬姛锛?)
                .newName(nextEvolution.getEvolutionName())
                .newLevel(playerPet.getLevel())
                .newAttack(playerPet.getAttack())
                .newDefense(playerPet.getDefense())
                .newHealth(playerPet.getMaxHealth())
                .newSpeed(playerPet.getSpeed())
                .currentStage(nextEvolution.getEvolutionStage())
                .build();
    }

    /**
     * 鑾峰彇瀹犵墿鐨勮繘鍖栦俊鎭?
     */
    public Map<String, Object> getPetEvolutionInfo(Integer playerPetId) {
        PlayerPet playerPet = playerPetMapper.selectById(playerPetId);
        if (playerPet == null) {
            return Map.of("error", "瀹犵墿涓嶅瓨鍦?);
        }

        // 鑾峰彇褰撳墠杩涘寲闃舵
        int currentStage = 1;
        PlayerPetEvolution evolution = playerPetEvolutionMapper.selectByPlayerPetId(playerPetId);
        if (evolution != null) {
            currentStage = evolution.getCurrentStage();
        }

        // 鑾峰彇鎵€鏈夎繘鍖栭樁娈?
        List<PetEvolution> allEvolutions = petEvolutionMapper.selectByPetId(playerPet.getPetId());

        // 鑾峰彇涓嬩竴涓彲杩涘寲闃舵
        PetEvolution nextEvolution = petEvolutionMapper.selectNextEvolution(playerPet.getPetId(), currentStage);

        return Map.of(
                "petId", playerPetId,
                "currentName", playerPet.getNickname(),
                "currentStage", currentStage,
                "currentLevel", playerPet.getLevel(),
                "currentLoyalty", playerPet.getLoyalty(),
                "allEvolutionStages", allEvolutions.size(),
                "hasNextEvolution", nextEvolution != null,
                "canEvolve", checkEvolution(playerPetId).isSuccess()
        );
    }

    // ===================== 供 AuctionService 使用的接口（模块边界规范） =====================

    /**
     * 根据宠物模板ID获取宠物信息（供 AuctionService 使用）
     */
    public Pet getPetById(Integer petId) {
        return petMapper.selectById(petId);
    }

    /**
     * 根据 PlayerPet 主键获取玩家宠物记录（供 AuctionService 使用）
     */
    public PlayerPet getPlayerPetById(Long playerPetId) {
        return playerPetMapper.selectById(playerPetId);
    }

    /**
     * 直接为玩家授予宠物（不做重复校验，供拍卖行成交回退使用）
     */
    @Transactional
    public PlayerPet grantPetDirectly(Integer playerId, Integer petId) {
        PlayerPet newPet = new PlayerPet();
        newPet.setPlayerId(playerId);
        newPet.setPetId(petId);
        newPet.setLevel(1);
        playerPetMapper.insert(newPet);
        return newPet;
    }

    /**
     * 删除玩家宠物记录（供 AuctionService 上架使用）
     */
    @Transactional
    public void deletePlayerPet(Long playerPetId) {
        playerPetMapper.deleteById(playerPetId);
    }
}