package com.xiuxian.game.modules.map.service;

import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.combat.entity.MapMonster;
import com.xiuxian.game.modules.combat.entity.Monster;
import com.xiuxian.game.modules.combat.service.CombatService;
import com.xiuxian.game.modules.map.entity.GameMap;
import com.xiuxian.game.modules.map.entity.PlayerMapProgress;
import com.xiuxian.game.modules.map.mapper.GameMapMapper;
import com.xiuxian.game.modules.map.mapper.PlayerMapProgressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameMapService {

    private final GameMapMapper gameMapMapper;
    private final CombatService combatService;
    private final PlayerMapProgressMapper playerMapProgressMapper;

    public List<GameMap> getAllMaps() {
        return gameMapMapper.selectAllActive();
    }

    public GameMap getMapById(Integer mapId) {
        GameMap map = gameMapMapper.selectById(mapId);
        if (map == null || Boolean.FALSE.equals(map.getActive())) {
            return null;
        }
        map.setMonsters(combatService.getMapMonsters(mapId));
        return map;
    }

    public PlayerMapProgress getCurrentMapProgress(Integer playerId) {
        return playerMapProgressMapper.selectCurrentMap(playerId);
    }

    public PlayerMapProgress getOfflineRewardProgress(Integer playerId) {
        PlayerMapProgress current = playerMapProgressMapper.selectCurrentMap(playerId);
        if (current != null && current.getOfflineStartAt() != null) {
            return current;
        }
        return playerMapProgressMapper.selectLatestOfflineProgress(playerId);
    }

    public List<GameMap> getMapsForPlayer(Integer playerId, int playerLevel, String playerRealm) {
        List<GameMap> allMaps = gameMapMapper.selectAllActive();
        List<PlayerMapProgress> playerProgress = playerMapProgressMapper.selectByPlayerId(playerId);
        Map<Integer, PlayerMapProgress> progressMap = playerProgress.stream()
                .collect(Collectors.toMap(
                        PlayerMapProgress::getMapId,
                        progress -> progress,
                        (existing, replacement) -> replacement));

        for (GameMap map : allMaps) {
            PlayerMapProgress progress = progressMap.get(map.getId());
            map.setUnlocked(progress != null && Boolean.TRUE.equals(progress.getIsUnlocked()));
            map.setCurrent(progress != null && Boolean.TRUE.equals(progress.getIsCurrent()));
        }
        return allMaps;
    }

    @Transactional
    public PlayerMapProgress enterMap(Integer playerId, Integer mapId, int playerLevel, String playerRealm) {
        GameMap map = getMapById(mapId);
        if (map == null) {
            throw new BusinessException(ErrorCode.MAP_NOT_FOUND);
        }
        if (!map.canEnter(playerLevel, playerRealm)) {
            throw new BusinessException(ErrorCode.MAP_REQUIREMENTS_NOT_MET);
        }

        PlayerMapProgress progress = playerMapProgressMapper.selectByPlayerAndMap(playerId, mapId);
        if (progress == null) {
            progress = buildInitialProgress(playerId, mapId);
            playerMapProgressMapper.insert(progress);
        } else if (!Boolean.TRUE.equals(progress.getIsUnlocked())) {
            playerMapProgressMapper.unlockMap(playerId, mapId);
            progress.setIsUnlocked(true);
        }

        playerMapProgressMapper.clearCurrentMap(playerId);
        progress.recordEnter();
        playerMapProgressMapper.updateById(progress);
        playerMapProgressMapper.setCurrentMap(playerId, mapId);

        PlayerMapProgress latestProgress = playerMapProgressMapper.selectByPlayerAndMap(playerId, mapId);
        log.info("playerId={} entered mapId={}", playerId, mapId);
        return latestProgress != null ? latestProgress : progress;
    }

    @Transactional
    public void leaveMap(Integer playerId) {
        PlayerMapProgress current = playerMapProgressMapper.selectCurrentMap(playerId);
        if (current == null) {
            return;
        }

        current.recordLeave();
        current.startOffline();
        playerMapProgressMapper.updateById(current);
        log.info("playerId={} left mapId={}", playerId, current.getMapId());
    }

    public GameMap getCurrentMap(Integer playerId) {
        PlayerMapProgress progress = playerMapProgressMapper.selectCurrentMap(playerId);
        if (progress == null) {
            return null;
        }
        return getMapById(progress.getMapId());
    }

    @Transactional
    public void initPlayerMapProgress(Integer playerId) {
        PlayerMapProgress existing = playerMapProgressMapper.selectByPlayerAndMap(playerId, 1);
        if (existing != null) {
            if (!Boolean.TRUE.equals(existing.getIsUnlocked())) {
                existing.setIsUnlocked(true);
            }
            existing.setIsCurrent(true);
            if (existing.getFirstEnterAt() == null) {
                existing.setFirstEnterAt(LocalDateTime.now());
            }
            existing.setLastEnterAt(LocalDateTime.now());
            if (existing.getTotalKills() == null) {
                existing.setTotalKills(0);
            }
            if (existing.getTotalTimeSpent() == null) {
                existing.setTotalTimeSpent(0);
            }
            playerMapProgressMapper.clearCurrentMap(playerId);
            playerMapProgressMapper.updateById(existing);
            return;
        }

        PlayerMapProgress progress = buildInitialProgress(playerId, 1);
        progress.setIsCurrent(true);
        progress.setFirstEnterAt(LocalDateTime.now());
        progress.setLastEnterAt(LocalDateTime.now());
        playerMapProgressMapper.clearCurrentMap(playerId);
        playerMapProgressMapper.insert(progress);
    }

    public MapEncounter generateEncounter(Integer playerId, Integer mapId, int playerLevel) {
        GameMap map = getMapById(mapId);
        if (map == null || map.getMonsters() == null || map.getMonsters().isEmpty()) {
            return null;
        }

        List<MapMonster> candidates = map.getMonsters();
        int totalWeight = candidates.stream().mapToInt(monster -> defaultInt(monster.getSpawnWeight())).sum();
        if (totalWeight <= 0) {
            log.warn("mapId={} has no valid encounter weights", mapId);
            return null;
        }

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        MapMonster selected = null;
        int currentWeight = 0;
        for (MapMonster monster : candidates) {
            currentWeight += defaultInt(monster.getSpawnWeight());
            if (roll < currentWeight) {
                selected = monster;
                break;
            }
        }
        if (selected == null) {
            selected = candidates.get(0);
        }

        Monster monster = combatService.getMonsterById(selected.getMonsterId());
        if (monster == null) {
            return null;
        }

        int mapRequiredLevel = map.getRequiredLevel() == null ? 1 : map.getRequiredLevel();
        int monsterLevel = selected.calculateLevel(playerLevel, mapRequiredLevel);
        double statMultiplier = selected.calculateStatMultiplier(playerLevel, mapRequiredLevel);
        if (Boolean.TRUE.equals(selected.getIsElite())) {
            statMultiplier *= 1.5;
        }

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

    public OfflineReward calculateOfflineReward(Integer playerId, PlayerMapProgress progress, GameMap map) {
        if (progress == null || map == null) {
            return null;
        }

        int hours = progress.calculateOfflineHours();
        if (hours <= 0) {
            return null;
        }

        int baseSpiritStones = defaultInt(map.getBaseSpiritStones()) * hours;
        int baseExp = calculateBaseExp(defaultInt(map.getRequiredLevel())) * hours;
        if (map.getExpModifier() != null) {
            baseExp = (int) (baseExp * map.getExpModifier().doubleValue());
        }

        int injuryCount = 0;
        int actualSpiritStones = baseSpiritStones;
        if (Boolean.TRUE.equals(map.getOfflineRisk())) {
            injuryCount = calculateInjuryCount(hours, defaultInt(map.getDangerLevel()));
            double lossRate = Math.pow(0.9, injuryCount);
            actualSpiritStones = (int) (baseSpiritStones * lossRate);
        }

        OfflineReward reward = new OfflineReward();
        reward.setHours(hours);
        reward.setBaseSpiritStones(baseSpiritStones);
        reward.setActualSpiritStones(actualSpiritStones);
        reward.setExp(baseExp);
        reward.setInjuryCount(injuryCount);
        return reward;
    }

    private PlayerMapProgress buildInitialProgress(Integer playerId, Integer mapId) {
        PlayerMapProgress progress = new PlayerMapProgress();
        progress.setPlayerId(playerId);
        progress.setMapId(mapId);
        progress.setIsUnlocked(true);
        progress.setIsCurrent(false);
        progress.setTotalKills(0);
        progress.setTotalTimeSpent(0);
        return progress;
    }

    private int calculateBaseExp(int mapLevel) {
        return 20 + mapLevel * 5;
    }

    private int calculateInjuryCount(int hours, int dangerLevel) {
        int injuryCount = 0;
        for (int i = 0; i < hours; i += 6) {
            double probability = 0.1 + dangerLevel * 0.05 + ((double) i / 6) * 0.05;
            if (ThreadLocalRandom.current().nextDouble() < probability) {
                injuryCount++;
            }
        }
        return injuryCount;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

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

    @lombok.Data
    public static class OfflineReward {
        private int hours;
        private int baseSpiritStones;
        private int actualSpiritStones;
        private int exp;
        private int injuryCount;
    }
}
