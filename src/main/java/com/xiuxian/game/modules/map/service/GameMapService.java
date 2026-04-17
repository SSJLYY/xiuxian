package com.xiuxian.game.modules.map.service;

import com.xiuxian.game.modules.map.entity.GameMap;
import com.xiuxian.game.modules.combat.entity.MapMonster;
import com.xiuxian.game.modules.combat.entity.Monster;
import com.xiuxian.game.modules.map.entity.PlayerMapProgress;
import com.xiuxian.game.modules.map.mapper.GameMapMapper;
import com.xiuxian.game.modules.combat.service.CombatService;
import com.xiuxian.game.modules.map.mapper.PlayerMapProgressMapper;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 游戏地图服务
 * 
 * @author LevelDesigner
 * @since 2026-03-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameMapService {
    
    private final GameMapMapper gameMapMapper;
    private final CombatService combatService; // module boundary: access combat data via CombatService
    private final PlayerMapProgressMapper playerMapProgressMapper;
    
    /**
     * 获取所有激活的地图
     */
    public List<GameMap> getAllMaps() {
        return gameMapMapper.selectAllActive();
    }
    
    /**
     * 根据ID获取地图
     */
    public GameMap getMapById(Integer mapId) {
        GameMap map = gameMapMapper.selectById(mapId);
        if (map != null) {
            // 加载怪物配置
            List<MapMonster> monsters = combatService.getMapMonsters(mapId);
            map.setMonsters(monsters);
        }
        return map;
    }
    
    /**
     * 获取玩家视角的地图列表（包含解锁状态）
     */
    public List<GameMap> getMapsForPlayer(Integer playerId, int playerLevel, String playerRealm) {
        List<GameMap> allMaps = gameMapMapper.selectAllActive();
        List<PlayerMapProgress> playerProgress = playerMapProgressMapper.selectByPlayerId(playerId);
        
        // 构建进度Map，使用mergeFunction处理重复的mapId（取最后一个）
        java.util.Map<Integer, PlayerMapProgress> progressMap = playerProgress.stream()
            .collect(Collectors.toMap(
                PlayerMapProgress::getMapId, 
                p -> p,
                (existing, replacement) -> replacement // 如果有重复key，保留后来的
            ));
        
        // 设置解锁状态和当前状态
        for (GameMap map : allMaps) {
            PlayerMapProgress progress = progressMap.get(map.getId());
            if (progress != null) {
                map.setUnlocked(progress.getIsUnlocked());
                map.setCurrent(progress.getIsCurrent());
            } else {
                map.setUnlocked(false);
                map.setCurrent(false);
            }
        }
        
        return allMaps;
    }
    
    /**
     * 进入地图
     */
    @Transactional
    public PlayerMapProgress enterMap(Integer playerId, Integer mapId, int playerLevel, String playerRealm) {
        GameMap map = getMapById(mapId);
        if (map == null) {
            throw new BusinessException(ErrorCode.MAP_NOT_FOUND);
        }

        // 检查进入条件
        if (!map.canEnter(playerLevel, playerRealm)) {
            throw new BusinessException(ErrorCode.MAP_REQUIREMENTS_NOT_MET);
        }
        
        // 获取或创建玩家进度
        PlayerMapProgress progress = playerMapProgressMapper.selectByPlayerAndMap(playerId, mapId);
        if (progress == null) {
            progress = new PlayerMapProgress();
            progress.setPlayerId(playerId);
            progress.setMapId(mapId);
            progress.setIsUnlocked(true);
            progress.setTotalKills(0);
            progress.setTotalTimeSpent(0);
            playerMapProgressMapper.insert(progress);
        } else if (!progress.getIsUnlocked()) {
            // 解锁地图
            playerMapProgressMapper.unlockMap(playerId, mapId);
            progress.setIsUnlocked(true);
        }
        
        // 清除其他地图的当前标记
        playerMapProgressMapper.clearCurrentMap(playerId);
        
        // 设置当前地图
        playerMapProgressMapper.setCurrentMap(playerId, mapId);
        progress.setIsCurrent(true);
        progress.recordEnter();
        
        // 更新记录
        playerMapProgressMapper.updateById(progress);
        
        log.info("玩家 {} 进入地图 {}", playerId, map.getName());
        return progress;
    }
    
    /**
     * 离开当前地图
     */
    @Transactional
    public void leaveMap(Integer playerId) {
        PlayerMapProgress current = playerMapProgressMapper.selectCurrentMap(playerId);
        if (current != null) {
            current.recordLeave();
            playerMapProgressMapper.updateById(current);
            log.info("玩家 {} 离开地图 {}", playerId, current.getMapId());
        }
    }
    
    /**
     * 获取玩家当前所在地图
     */
    public GameMap getCurrentMap(Integer playerId) {
        PlayerMapProgress progress = playerMapProgressMapper.selectCurrentMap(playerId);
        if (progress == null) {
            return null;
        }
        return getMapById(progress.getMapId());
    }
    
    /**
     * 初始化新玩家的地图进度
     */
    @Transactional
    public void initPlayerMapProgress(Integer playerId) {
        // 解锁起始地图（青云镇）
        PlayerMapProgress progress = new PlayerMapProgress();
        progress.setPlayerId(playerId);
        progress.setMapId(1); // 青云镇
        progress.setIsUnlocked(true);
        progress.setIsCurrent(true);
        progress.setFirstEnterAt(LocalDateTime.now());
        progress.setLastEnterAt(LocalDateTime.now());
        progress.setTotalKills(0);
        progress.setTotalTimeSpent(0);
        playerMapProgressMapper.insert(progress);
        
        log.info("初始化玩家 {} 的地图进度", playerId);
    }
    
    /**
     * 生成遭遇战
     */
    public MapEncounter generateEncounter(Integer playerId, Integer mapId, int playerLevel) {
        GameMap map = getMapById(mapId);
        if (map == null || map.getMonsters() == null || map.getMonsters().isEmpty()) {
            return null;
        }
        
        // 根据权重随机选择怪物
        List<MapMonster> candidates = map.getMonsters();
        int totalWeight = candidates.stream().mapToInt(MapMonster::getSpawnWeight).sum();
        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        
        MapMonster selected = null;
        int currentWeight = 0;
        for (MapMonster monster : candidates) {
            currentWeight += monster.getSpawnWeight();
            if (roll < currentWeight) {
                selected = monster;
                break;
            }
        }
        
        if (selected == null) {
            selected = candidates.get(0);
        }
        
        // 加载怪物详细信息
        Monster monster = combatService.getMonsterById(selected.getMonsterId());
        if (monster == null) {
            return null;
        }
        
        // 计算怪物等级和属性
        int monsterLevel = selected.calculateLevel(playerLevel, map.getRequiredLevel());
        double statMultiplier = selected.calculateStatMultiplier(playerLevel, map.getRequiredLevel());
        
        // 应用精英怪倍率
        if (selected.getIsElite()) {
            statMultiplier *= 1.5;
        }
        
        // 创建遭遇
        MapEncounter encounter = new MapEncounter();
        encounter.setMapId(mapId);
        encounter.setMapName(map.getName());
        encounter.setSceneText(selected.getEncounterText());
        encounter.setMonster(monster);
        encounter.setMonsterLevel(monsterLevel);
        encounter.setStatMultiplier(statMultiplier);
        encounter.setIsElite(selected.getIsElite());
        
        return encounter;
    }
    
    /**
     * 计算离线收益
     */
    public OfflineReward calculateOfflineReward(Integer playerId, PlayerMapProgress progress, GameMap map) {
        int hours = progress.calculateOfflineHours();
        if (hours <= 0) {
            return null;
        }
        
        // 基础收益
        int baseSpiritStones = map.getBaseSpiritStones() * hours;
        int baseExp = calculateBaseExp(map.getRequiredLevel()) * hours;
        
        // 应用经验倍率
        baseExp = (int)(baseExp * map.getExpModifier().doubleValue());
        
        // 危险区风险计算
        int injuryCount = 0;
        int actualSpiritStones = baseSpiritStones;
        
        if (map.getOfflineRisk()) {
            injuryCount = calculateInjuryCount(hours, map.getDangerLevel());
            // 每次受伤损失10%灵石
            double lossRate = Math.pow(0.9, injuryCount);
            actualSpiritStones = (int)(baseSpiritStones * lossRate);
        }
        
        OfflineReward reward = new OfflineReward();
        reward.setHours(hours);
        reward.setBaseSpiritStones(baseSpiritStones);
        reward.setActualSpiritStones(actualSpiritStones);
        reward.setExp(baseExp);
        reward.setInjuryCount(injuryCount);
        
        return reward;
    }
    
    /**
     * 计算基础经验
     */
    private int calculateBaseExp(int mapLevel) {
        return 20 + mapLevel * 5;
    }
    
    /**
     * 计算受伤次数
     */
    private int calculateInjuryCount(int hours, int dangerLevel) {
        int injuryCount = 0;
        
        // 根据离线时长和危险等级计算受伤概率
        for (int i = 0; i < hours; i += 6) {
            double baseProbability = 0.1; // 基础10%
            double dangerMultiplier = dangerLevel * 0.05; // 危险等级加成
            double timeMultiplier = (i / 6) * 0.05; // 时间加成
            
            double probability = baseProbability + dangerMultiplier + timeMultiplier;
            
            if (ThreadLocalRandom.current().nextDouble() < probability) {
                injuryCount++;
            }
        }
        
        return injuryCount;
    }
    
    /**
     * 内部类：遭遇战
     */
    @lombok.Data
    public static class MapEncounter {
        private Integer mapId;
        private String mapName;
        private String sceneText;
        private Monster monster;
        private int monsterLevel;
        private double statMultiplier;
        private Boolean isElite;
    }
    
    /**
     * 内部类：离线收益
     */
    @lombok.Data
    public static class OfflineReward {
        private int hours;
        private int baseSpiritStones;
        private int actualSpiritStones;
        private int exp;
        private int injuryCount;
    }
}


