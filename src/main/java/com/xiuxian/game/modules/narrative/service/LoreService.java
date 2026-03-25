package com.xiuxian.game.modules.narrative.service;

import com.xiuxian.game.modules.narrative.entity.LoreEntry;
import com.xiuxian.game.modules.narrative.entity.PlayerLoreCollection;
import com.xiuxian.game.modules.narrative.mapper.LoreEntryMapper;
import com.xiuxian.game.modules.narrative.mapper.PlayerLoreCollectionMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 传说系统服务
 * 负责传说条目的发现、查询和收集进度
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoreService {

    private final LoreEntryMapper loreEntryMapper;
    private final PlayerLoreCollectionMapper playerLoreCollectionMapper;

    /**
     * 获取所有传说条目（标记哪些已被玩家发现）
     */
    public List<LoreVo> getAllLoreEntries(Integer playerId) {
        List<LoreEntry> allEntries = loreEntryMapper.selectAllActive();
        Set<Integer> discoveredIds = getDiscoveredIds(playerId);

        return allEntries.stream()
                .filter(entry -> entry.getLoreLayer().equals("表面") || discoveredIds.contains(entry.getId()))
                .map(entry -> toLoreVo(entry, discoveredIds.contains(entry.getId())))
                .collect(Collectors.toList());
    }

    /**
     * 获取玩家已发现的传说条目
     */
    public List<LoreVo> getDiscoveredLore(Integer playerId) {
        List<PlayerLoreCollection> collections = playerLoreCollectionMapper.selectByPlayerId(playerId);
        return collections.stream().map(c -> {
            LoreEntry entry = loreEntryMapper.selectById(c.getLoreEntryId());
            if (entry == null) return null;
            return toLoreVo(entry, true);
        }).filter(vo -> vo != null).collect(Collectors.toList());
    }

    /**
     * 获取传说收集进度
     */
    public LoreProgressVo getLoreProgress(Integer playerId) {
        List<LoreEntry> allEntries = loreEntryMapper.selectAllActive();
        Set<Integer> discoveredIds = getDiscoveredIds(playerId);

        LoreProgressVo progress = new LoreProgressVo();
        progress.setTotalCount(allEntries.size());
        progress.setDiscoveredCount(discoveredIds.size());

        // 按层级统计
        long surfaceTotal = allEntries.stream().filter(e -> "表面".equals(e.getLoreLayer())).count();
        long engagedTotal = allEntries.stream().filter(e -> "参与".equals(e.getLoreLayer())).count();
        long deepTotal = allEntries.stream().filter(e -> "深层".equals(e.getLoreLayer())).count();

        long surfaceDiscovered = allEntries.stream()
                .filter(e -> "表面".equals(e.getLoreLayer()) && discoveredIds.contains(e.getId())).count();
        long engagedDiscovered = allEntries.stream()
                .filter(e -> "参与".equals(e.getLoreLayer()) && discoveredIds.contains(e.getId())).count();
        long deepDiscovered = allEntries.stream()
                .filter(e -> "深层".equals(e.getLoreLayer()) && discoveredIds.contains(e.getId())).count();

        progress.setSurfaceDiscovered(surfaceDiscovered);
        progress.setSurfaceTotal(surfaceTotal);
        progress.setEngagedDiscovered(engagedDiscovered);
        progress.setEngagedTotal(engagedTotal);
        progress.setDeepDiscovered(deepDiscovered);
        progress.setDeepTotal(deepTotal);

        return progress;
    }

    /**
     * 发现一条传说
     */
    @Transactional
    public boolean discoverLore(Integer playerId, String loreKey, String source) {
        LoreEntry entry = loreEntryMapper.selectByKey(loreKey);
        if (entry == null) {
            log.warn("传说条目不存在: {}", loreKey);
            return false;
        }

        PlayerLoreCollection existing = playerLoreCollectionMapper.selectByPlayerAndLore(playerId, entry.getId());
        if (existing != null) {
            return false; // 已经发现
        }

        PlayerLoreCollection collection = PlayerLoreCollection.builder()
                .playerId(playerId)
                .loreEntryId(entry.getId())
                .discoveredAt(LocalDateTime.now())
                .source(source)
                .build();
        playerLoreCollectionMapper.insert(collection);

        log.info("玩家 {} 发现传说: {} (来源: {})", playerId, loreKey, source);
        return true;
    }

    private Set<Integer> getDiscoveredIds(Integer playerId) {
        return new HashSet<>(playerLoreCollectionMapper.selectDiscoveredIds(playerId));
    }

    private LoreVo toLoreVo(LoreEntry entry, boolean discovered) {
        LoreVo vo = new LoreVo();
        vo.setId(entry.getId());
        vo.setLoreKey(entry.getLoreKey());
        vo.setTitle(entry.getTitle());
        vo.setLoreLayer(entry.getLoreLayer());
        vo.setCategory(entry.getCategory());
        vo.setDiscoverCondition(entry.getDiscoverCondition());
        vo.setIcon(entry.getIcon());
        vo.setDiscovered(discovered);
        if (discovered) {
            vo.setContent(entry.getContent());
        } else {
            vo.setContent("??? 未发现 ???");
        }
        return vo;
    }

    @Data
    public static class LoreVo {
        private Integer id;
        private String loreKey;
        private String title;
        private String content;
        private String loreLayer;
        private String category;
        private String discoverCondition;
        private String icon;
        private boolean discovered;
    }

    @Data
    public static class LoreProgressVo {
        private int totalCount;
        private int discoveredCount;
        private long surfaceTotal;
        private long surfaceDiscovered;
        private long engagedTotal;
        private long engagedDiscovered;
        private long deepTotal;
        private long deepDiscovered;
    }
}

