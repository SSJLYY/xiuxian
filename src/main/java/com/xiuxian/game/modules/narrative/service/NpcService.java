package com.xiuxian.game.modules.narrative.service;

import com.xiuxian.game.modules.narrative.entity.Npc;
import com.xiuxian.game.modules.narrative.entity.PlayerNpcRelation;
import com.xiuxian.game.modules.narrative.mapper.NpcMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * NPC管理服务
 * 负责NPC信息查询和NPC关系摘要
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NpcService {

    private final NpcMapper npcMapper;
    private final NarrativeService narrativeService;

    /**
     * 获取所有NPC列表（按玩家等级过滤）
     */
    public List<Npc> getAllNpcs(Integer playerLevel) {
        if (playerLevel == null || playerLevel < 1) {
            return npcMapper.selectAllActive();
        }
        return npcMapper.selectByLevel(playerLevel);
    }

    /**
     * 获取NPC详情（含关系信息）
     */
    public NpcDetailVo getNpcDetail(Integer npcId, Integer playerId) {
        Npc npc = npcMapper.selectById(npcId);
        if (npc == null || Boolean.FALSE.equals(npc.getActive())) {
            return null;
        }

        NpcDetailVo vo = new NpcDetailVo();
        vo.setId(npc.getId());
        vo.setName(npc.getName());
        vo.setTitle(npc.getTitle());
        vo.setFaction(npc.getFaction());
        vo.setRoleType(npc.getRoleType());
        vo.setDescription(npc.getDescription());
        vo.setPersonalityTraits(npc.getPersonalityTraits());
        vo.setLocation(npc.getLocation());

        // 获取玩家与NPC的关系
        PlayerNpcRelation relation = narrativeService.getNpcRelation(playerId, npcId);
        if (relation != null) {
            vo.setAffinity(relation.getAffinity());
            vo.setRelationshipLevel(relation.getRelationshipLevel());
            vo.setTotalInteractions(relation.getTotalInteractions());
            vo.setFirstMet(relation.getFirstMetAt());
        } else {
            vo.setAffinity(0);
            vo.setRelationshipLevel(PlayerNpcRelation.LEVEL_STRANGER);
            vo.setTotalInteractions(0);
        }

        // 获取今日日常对话
        String dailyLine = narrativeService.getDailyDialogue(playerId, npcId);
        vo.setDailyDialogue(dailyLine);

        return vo;
    }

    /**
     * 获取玩家所有NPC关系摘要
     */
    public List<NpcRelationSummary> getNpcRelationSummaries(Integer playerId) {
        List<PlayerNpcRelation> relations = narrativeService.getAllNpcRelations(playerId);
        java.util.Map<Integer, Npc> npcMap = npcMapper.selectBatchIds(
                relations.stream().map(PlayerNpcRelation::getNpcId).distinct().collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(Npc::getId, npc -> npc, (a, b) -> a));
        return relations.stream().map(r -> {
            NpcRelationSummary summary = new NpcRelationSummary();
            summary.setNpcId(r.getNpcId());
            Npc npc = npcMap.get(r.getNpcId());
            summary.setNpcName(npc != null ? npc.getName() : "未知");
            summary.setTitle(npc != null ? npc.getTitle() : "");
            summary.setFaction(npc != null ? npc.getFaction() : "");
            summary.setAffinity(r.getAffinity());
            summary.setRelationshipLevel(r.getRelationshipLevel());
            summary.setTotalInteractions(r.getTotalInteractions());
            return summary;
        }).collect(Collectors.toList());
    }

    @Data
    public static class NpcDetailVo {
        private Integer id;
        private String name;
        private String title;
        private String faction;
        private String roleType;
        private String description;
        private String personalityTraits;
        private String location;
        private Integer affinity;
        private String relationshipLevel;
        private Integer totalInteractions;
        private Object firstMet;
        private String dailyDialogue;
    }

    @Data
    public static class NpcRelationSummary {
        private Integer npcId;
        private String npcName;
        private String title;
        private String faction;
        private Integer affinity;
        private String relationshipLevel;
        private Integer totalInteractions;
    }
}

