package com.xiuxian.game.modules.narrative.service;

import com.xiuxian.game.modules.narrative.entity.OfflineNarrativeEvent;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.narrative.mapper.OfflineNarrativeEventMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.narrative.service.NarrativeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 离线事件叙事服务
 * 负责离线奇遇的触发、奖励发放和叙事文本生成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OfflineNarrativeService {

    private final OfflineNarrativeEventMapper offlineNarrativeEventMapper;
    private final PlayerService playerService; // 模块边界：通过PlayerService访问玩家数据
    private final NarrativeService narrativeService;
    private final LoreService loreService;

    /**
     * 检查并触发离线事件
     * 在玩家登录时调用
     * @return 触发的事件列表（可能为空）
     */
    @Transactional
    public List<OfflineEventResult> checkOfflineEvents(Integer playerId) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);
        if (player == null || player.getLastOnlineTime() == null) {
            return new ArrayList<>();
        }

        LocalDateTime now = LocalDateTime.now();
        long offlineHours = Duration.between(player.getLastOnlineTime(), now).toHours();

        if (offlineHours < 1) {
            return new ArrayList<>(); // 离线不足1小时不触发
        }

        List<OfflineNarrativeEvent> allEvents = offlineNarrativeEventMapper.selectAllActive();
        List<OfflineEventResult> triggered = new ArrayList<>();

        for (OfflineNarrativeEvent event : allEvents) {
            if (shouldTrigger(event, offlineHours, player)) {
                OfflineEventResult result = triggerEvent(event, playerId);
                if (result != null) {
                    triggered.add(result);
                }
            }
        }

        if (!triggered.isEmpty()) {
            log.info("玩家 {} 离线{}小时，触发{}个叙事事件", playerId, offlineHours, triggered.size());
        }

        return triggered;
    }

    /**
     * 判断事件是否应该触发
     */
    private boolean shouldTrigger(OfflineNarrativeEvent event, long offlineHours, PlayerProfile player) {
        // 离线时长检查
        if (offlineHours < event.getMinOfflineHours()) {
            return false;
        }
        if (event.getMaxOfflineHours() != null && offlineHours > event.getMaxOfflineHours()) {
            return false;
        }

        // 等级检查
        if (event.getMinLevel() != null && defaultInt(player.getLevel(), 1) < event.getMinLevel()) {
            return false;
        }

        // 境界检查
        if (event.getMinRealm() != null && !event.getMinRealm().isEmpty()) {
            if (!event.getMinRealm().equals(player.getRealm())) {
                return false;
            }
        }

        // 概率检查
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll > event.getProbability().doubleValue()) {
            return false;
        }

        return true;
    }

    /**
     * 触发事件：处理奖励、flag、传说发现
     */
    private OfflineEventResult triggerEvent(OfflineNarrativeEvent event, Integer playerId) {
        OfflineEventResult result = new OfflineEventResult();
        result.setEventKey(event.getEventKey());
        result.setTitle(event.getTitle());
        result.setNarrative(event.getNarrative());
        result.setRewards(new ArrayList<>());

        // 设置flag
        if (event.getSetFlag() != null && !event.getSetFlag().isEmpty()) {
            narrativeService.setFlag(playerId, event.getSetFlag(), "1", "离线事件: " + event.getEventKey());
            result.getRewards().add("解锁了新的剧情线索");
        }

        // NPC好感度变化
        if (event.getNpcRelationChange() != null && !event.getNpcRelationChange().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Integer> changes = om.readValue(event.getNpcRelationChange(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Integer>>() {});
                for (Map.Entry<String, Integer> entry : changes.entrySet()) {
                    int npcId = Integer.parseInt(entry.getKey());
                    narrativeService.changeNpcAffinity(playerId, npcId, entry.getValue());
                    result.getRewards().add("与NPC好感度发生了变化");
                }
            } catch (Exception e) {
                log.warn("解析NPC好感度变更失败: {}", event.getNpcRelationChange(), e);
            }
        }

        log.info("玩家 {} 触发离线事件: {}", playerId, event.getEventKey());
        return result;
    }

    @Data
    public static class OfflineEventResult {
        private String eventKey;
        private String title;
        private String narrative;
        private List<String> rewards;
    }
    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
