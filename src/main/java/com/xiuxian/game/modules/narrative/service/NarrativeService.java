package com.xiuxian.game.modules.narrative.service;

// narrative module entities
import com.xiuxian.game.modules.narrative.entity.DialogueNode;
import com.xiuxian.game.modules.narrative.entity.DialogueTree;
import com.xiuxian.game.modules.narrative.entity.Npc;
import com.xiuxian.game.modules.narrative.entity.NpcDailyDialogue;
import com.xiuxian.game.modules.narrative.entity.PlayerDialogueState;
import com.xiuxian.game.modules.narrative.entity.PlayerNarrativeFlag;
import com.xiuxian.game.modules.narrative.entity.PlayerNpcRelation;
// narrative module mappers (same module — OK)
import com.xiuxian.game.modules.narrative.mapper.DialogueNodeMapper;
import com.xiuxian.game.modules.narrative.mapper.DialogueTreeMapper;
import com.xiuxian.game.modules.narrative.mapper.NpcDailyDialogueMapper;
import com.xiuxian.game.modules.narrative.mapper.NpcMapper;
import com.xiuxian.game.modules.narrative.mapper.PlayerDialogueStateMapper;
import com.xiuxian.game.modules.narrative.mapper.PlayerNarrativeFlagMapper;
import com.xiuxian.game.modules.narrative.mapper.PlayerNpcRelationMapper;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 叙事服务 - 核心对话引擎
 * 负责对话树的加载、推进、分支选择、flag管理、好感度变更
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NarrativeService {

    private final DialogueTreeMapper dialogueTreeMapper;
    private final DialogueNodeMapper dialogueNodeMapper;
    private final PlayerDialogueStateMapper playerDialogueStateMapper;
    private final PlayerNarrativeFlagMapper playerNarrativeFlagMapper;
    private final PlayerNpcRelationMapper playerNpcRelationMapper;
    private final NpcMapper npcMapper;
    private final NpcDailyDialogueMapper npcDailyDialogueMapper;
    private final ObjectMapper objectMapper;
    private final PlayerService playerService;

    // ==================== 对话树操作 ====================

    /**
     * 获取NPC可用的对话树列表（含前置条件检查）
     */
    public List<DialogueTree> getAvailableDialogues(Integer playerId, Integer npcId) {
        List<DialogueTree> trees = dialogueTreeMapper.selectByNpcId(npcId);
        Set<String> playerFlags = getPlayerFlags(playerId);

        return trees.stream()
                .filter(tree -> meetsPrerequisites(tree, playerId, playerFlags))
                .collect(Collectors.toList());
    }

    /**
     * 开始/继续一个对话树
     * 返回当前节点的对话内容和可选选项
     */
    @Transactional
    public DialogueSceneData startOrContinueDialogue(Integer playerId, String dialogueKey) {
        DialogueTree tree = validateAndCheckPrerequisites(dialogueKey, playerId);
        PlayerDialogueState state = getOrCreateDialogueState(tree, playerId);
        String nodeKey = resolveStartingNode(state, tree);

        String npcName = getNpcName(tree.getNpcId());
        return buildSceneData(tree, nodeKey, playerId, npcName);
    }

    /**
     * 校验对话树存在性及前置条件
     */
    private DialogueTree validateAndCheckPrerequisites(String dialogueKey, Integer playerId) {
        DialogueTree tree = dialogueTreeMapper.selectActiveByKey(dialogueKey);
        if (tree == null) {
            throw new BusinessException(ErrorCode.DIALOGUE_NOT_FOUND);
        }

        Set<String> playerFlags = getPlayerFlags(playerId);
        if (!meetsPrerequisites(tree, playerId, playerFlags)) {
            throw new BusinessException(ErrorCode.DIALOGUE_PREREQUISITES_NOT_MET);
        }
        return tree;
    }

    /**
     * 获取或创建玩家对话状态
     */
    private PlayerDialogueState getOrCreateDialogueState(DialogueTree tree, Integer playerId) {
        PlayerDialogueState state = playerDialogueStateMapper.selectByPlayerAndTree(playerId, tree.getId());

        if (state == null) {
            return createNewDialogueState(tree, playerId);
        }
        if (!state.getIsCompleted()) {
            return state;
        }
        if (!tree.getIsRepeatable()) {
            throw new BusinessException(ErrorCode.DIALOGUE_ALREADY_COMPLETED);
        }
        return resetRepeatableDialogue(state, tree);
    }

    private PlayerDialogueState createNewDialogueState(DialogueTree tree, Integer playerId) {
        PlayerDialogueState state = PlayerDialogueState.builder()
                .playerId(playerId)
                .dialogueTreeId(tree.getId())
                .currentNodeKey(null)
                .isCompleted(false)
                .timesCompleted(0)
                .startedAt(LocalDateTime.now())
                .build();
        playerDialogueStateMapper.insert(state);
        return state;
    }

    private PlayerDialogueState resetRepeatableDialogue(PlayerDialogueState state, DialogueTree tree) {
        state.setCurrentNodeKey(null);
        state.setIsCompleted(false);
        state.setStartedAt(LocalDateTime.now());
        playerDialogueStateMapper.updateById(state);
        return state;
    }

    /**
     * 确定对话起始节点
     */
    private String resolveStartingNode(PlayerDialogueState state, DialogueTree tree) {
        String nodeKey = state.getCurrentNodeKey();
        if (nodeKey == null) {
            List<DialogueNode> roots = dialogueNodeMapper.selectRootNodes(tree.getId());
            if (roots.isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "对话树没有起始节点");
            }
            return roots.get(0).getNodeKey();
        }
        return nodeKey;
    }

    /**
     * 做出选择 / 推进对话
     */
    @Transactional
    public DialogueSceneData makeChoice(Integer playerId, String dialogueKey, String choiceNodeKey) {
        DialogueTree tree = dialogueTreeMapper.selectActiveByKey(dialogueKey);
        if (tree == null) {
            throw new BusinessException(ErrorCode.DIALOGUE_NOT_FOUND);
        }

        Set<String> playerFlags = getPlayerFlags(playerId);
        if (!meetsPrerequisites(tree, playerId, playerFlags)) {
            throw new BusinessException(ErrorCode.DIALOGUE_PREREQUISITES_NOT_MET);
        }

        PlayerDialogueState state = playerDialogueStateMapper.selectByPlayerAndTree(playerId, tree.getId());
        if (state == null || state.getIsCompleted()) {
            throw new BusinessException(ErrorCode.DIALOGUE_NOT_IN_PROGRESS);
        }

        String currentNodeKey = resolveStartingNode(state, tree);
        DialogueNode currentNode = dialogueNodeMapper.selectByTreeAndKey(tree.getId(), currentNodeKey);
        if (currentNode == null) {
            throw new BusinessException(ErrorCode.DIALOGUE_NOT_FOUND, "当前对话节点不存在");
        }
        if (!"choice".equals(currentNode.getNodeType())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前节点不可进行选择");
        }

        DialogueNode chosenNode = dialogueNodeMapper.selectByTreeAndKey(tree.getId(), choiceNodeKey);
        if (chosenNode == null) {
            throw new BusinessException(ErrorCode.DIALOGUE_NOT_FOUND, "对话节点不存在");
        }
        if (!currentNodeKey.equals(chosenNode.getParentNodeKey()) || !"choice".equals(chosenNode.getNodeType())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "非法的对话选项");
        }
        Map<Integer, Integer> npcRelations = getNpcAffinities(playerId);
        if (!meetsNodeConditions(chosenNode, playerFlags, npcRelations)) {
            throw new BusinessException(ErrorCode.DIALOGUE_PREREQUISITES_NOT_MET, "未满足该选项前置条件");
        }

        // 处理节点效果：flag、好感度
        processNodeEffects(playerId, chosenNode);

        // 记录选择tag
        if ("choice".equals(chosenNode.getNodeType())) {
            state.setLastChoiceTag(chosenNode.getNodeKey());
        }

        // 确定下一个节点
        String nextKey = resolveNextNode(tree, chosenNode, choiceNodeKey);

        String npcName = getNpcName(tree.getNpcId());

        // 对话结束
        if (nextKey == null || "end".equals(nextKey)) {
            handleDialogueCompletion(state, tree, dialogueKey, npcName);
            DialogueSceneData result = new DialogueSceneData();
            result.setDialogueKey(dialogueKey);
            result.setCompleted(true);
            result.setNpcName(npcName);
            return result;
        }

        // 继续对话
        state.setCurrentNodeKey(nextKey);
        playerDialogueStateMapper.updateById(state);
        return buildSceneData(tree, nextKey, playerId, npcName);
    }

    /**
     * 确定下一个对话节点
     */
    private String resolveNextNode(DialogueTree tree, DialogueNode chosenNode, String choiceNodeKey) {
        String nextKey = chosenNode.getNextNodeKey();

        // 如果是choice类型且没有直接nextKey，找它的子对话节点
        if ("choice".equals(chosenNode.getNodeType()) && nextKey == null) {
            List<DialogueNode> children = dialogueNodeMapper.selectChildrenByParent(tree.getId(), choiceNodeKey);
            if (!children.isEmpty()) {
                nextKey = children.get(0).getNodeKey();
            }
        }
        return nextKey;
    }

    /**
     * 处理对话完成逻辑
     */
    private void handleDialogueCompletion(PlayerDialogueState state, DialogueTree tree,
                                         String dialogueKey, String npcName) {
        state.setIsCompleted(true);
        state.setTimesCompleted(defaultInt(state.getTimesCompleted(), 0) + 1);
        state.setCompletedAt(LocalDateTime.now());
        playerDialogueStateMapper.updateById(state);

        // 首次完成时确保NPC关系记录存在
        Npc npc = npcMapper.selectById(tree.getNpcId());
        if (npc != null) {
            ensureNpcRelation(state.getPlayerId(), tree.getNpcId());
        }

        // 更新NPC互动计数
        updateNpcInteraction(state.getPlayerId(), tree.getNpcId());
    }

    /**
     * 构建场景数据（当前节点 + 可选选项）
     */
    private DialogueSceneData buildSceneData(DialogueTree tree, String nodeKey,
                                             Integer playerId, String npcName) {
        DialogueNode currentNode = dialogueNodeMapper.selectByTreeAndKey(tree.getId(), nodeKey);
        if (currentNode == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "对话节点丢失: " + nodeKey);
        }

        DialogueSceneData scene = new DialogueSceneData();
        scene.setDialogueKey(tree.getDialogueKey());
        scene.setNpcId(tree.getNpcId());
        scene.setNpcName(npcName);
        scene.setScene(tree.getScene());
        scene.setCompleted(false);

        // 当前节点
        DialogueSceneData.DialogueLine line = new DialogueSceneData.DialogueLine();
        line.setSpeaker(currentNode.getSpeaker());
        line.setText(currentNode.getText());
        line.setPortrait(currentNode.getPortrait());
        line.setNodeType(currentNode.getNodeType());
        scene.setCurrentLine(line);

        // 如果是choice节点，获取选项
        if ("choice".equals(currentNode.getNodeType())) {
            List<DialogueSceneData.ChoiceOption> choiceOptions = buildChoiceOptions(tree, nodeKey, playerId);
            scene.setChoices(choiceOptions);
        }

        return scene;
    }

    /**
     * 构建选项列表（含条件过滤）
     */
    private List<DialogueSceneData.ChoiceOption> buildChoiceOptions(DialogueTree tree, String nodeKey,
                                                                     Integer playerId) {
        List<DialogueNode> options = dialogueNodeMapper.selectChildrenByParent(tree.getId(), nodeKey);
        Set<String> playerFlags = getPlayerFlags(playerId);
        Map<Integer, Integer> npcRelations = getNpcAffinities(playerId);

        return options.stream()
                .filter(opt -> meetsNodeConditions(opt, playerFlags, npcRelations))
                .map(opt -> {
                    DialogueSceneData.ChoiceOption co = new DialogueSceneData.ChoiceOption();
                    co.setNodeKey(opt.getNodeKey());
                    co.setText(opt.getText());
                    co.setTag(opt.getNodeKey());
                    return co;
                })
                .collect(Collectors.toList());
    }

    /**
     * 处理节点效果（flag设置/清除、好感度变更）
     */
    private void processNodeEffects(Integer playerId, DialogueNode node) {
        // 设置flag
        if (node.getSetFlags() != null && !node.getSetFlags().isEmpty()) {
            try {
                List<String> flags = objectMapper.readValue(node.getSetFlags(), new TypeReference<List<String>>() {});
                for (String flag : flags) {
                    setFlag(playerId, flag, "1", "对话节点: " + node.getNodeKey());
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.warn("解析set_flags失败: {}", node.getSetFlags(), e);
            }
        }

        // 清除flag
        if (node.getClearFlags() != null && !node.getClearFlags().isEmpty()) {
            try {
                List<String> flags = objectMapper.readValue(node.getClearFlags(), new TypeReference<List<String>>() {});
                for (String flag : flags) {
                    clearFlag(playerId, flag);
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.warn("解析clear_flags失败: {}", node.getClearFlags(), e);
            }
        }

        // 好感度变更
        if (node.getSetReputation() != null && !node.getSetReputation().isEmpty()) {
            try {
                Map<String, Integer> changes = objectMapper.readValue(node.getSetReputation(),
                        new TypeReference<Map<String, Integer>>() {});
                for (Map.Entry<String, Integer> entry : changes.entrySet()) {
                    try {
                        Integer npcId = Integer.parseInt(entry.getKey());
                        changeNpcAffinity(playerId, npcId, entry.getValue());
                    } catch (NumberFormatException ex) {
                        log.warn("set_reputation中npcId格式错误: {}", entry.getKey(), ex);
                    }
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.warn("解析set_reputation失败: {}", node.getSetReputation(), e);
            }
        }
    }

    // ==================== Flag 系统 ====================

    public Set<String> getPlayerFlags(Integer playerId) {
        List<String> keys = playerNarrativeFlagMapper.selectFlagKeysByPlayerId(playerId);
        return new HashSet<>(keys);
    }

    public boolean hasFlag(Integer playerId, String flagKey) {
        return playerNarrativeFlagMapper.selectByPlayerAndKey(playerId, flagKey) != null;
    }

    @Transactional
    public void setFlag(Integer playerId, String flagKey, String value, String source) {
        PlayerNarrativeFlag existing = playerNarrativeFlagMapper.selectByPlayerAndKey(playerId, flagKey);
        if (existing != null) {
            existing.setFlagValue(value);
            existing.setSource(source);
            playerNarrativeFlagMapper.updateById(existing);
        } else {
            PlayerNarrativeFlag flag = PlayerNarrativeFlag.builder()
                    .playerId(playerId)
                    .flagKey(flagKey)
                    .flagValue(value)
                    .source(source)
                    .build();
            playerNarrativeFlagMapper.insert(flag);
        }
    }

    @Transactional
    public void clearFlag(Integer playerId, String flagKey) {
        PlayerNarrativeFlag flag = playerNarrativeFlagMapper.selectByPlayerAndKey(playerId, flagKey);
        if (flag != null) {
            playerNarrativeFlagMapper.deleteById(flag.getId());
        }
    }

    // ==================== 好感度系统 ====================

    public PlayerNpcRelation getNpcRelation(Integer playerId, Integer npcId) {
        return playerNpcRelationMapper.selectByPlayerAndNpc(playerId, npcId);
    }

    public List<PlayerNpcRelation> getAllNpcRelations(Integer playerId) {
        return playerNpcRelationMapper.selectByPlayerId(playerId);
    }

    @Transactional
    public void changeNpcAffinity(Integer playerId, Integer npcId, int change) {
        PlayerNpcRelation relation = ensureNpcRelation(playerId, npcId);
        int newAffinity = Math.max(-100, Math.min(100, defaultInt(relation.getAffinity(), 0) + change));
        relation.setAffinity(newAffinity);
        relation.setRelationshipLevel(PlayerNpcRelation.getRelationshipLevel(newAffinity));
        relation.setLastInteractAt(LocalDateTime.now());
        relation.setTotalInteractions(defaultInt(relation.getTotalInteractions(), 0) + 1);
        playerNpcRelationMapper.updateById(relation);

        if (log.isDebugEnabled()) {
            log.debug("NPC好感度变更: playerId={}, npcId={}, change={}, newAffinity={}",
                    playerId, npcId, change, newAffinity);
        }
    }

    @Transactional
    public PlayerNpcRelation ensureNpcRelation(Integer playerId, Integer npcId) {
        PlayerNpcRelation relation = playerNpcRelationMapper.selectByPlayerAndNpc(playerId, npcId);
        if (relation == null) {
            LocalDateTime now = LocalDateTime.now();
            playerNpcRelationMapper.insertIfAbsent(
                    playerId,
                    npcId,
                    0,
                    PlayerNpcRelation.LEVEL_STRANGER,
                    now,
                    now,
                    0,
                    now,
                    now);
            relation = playerNpcRelationMapper.selectByPlayerAndNpc(playerId, npcId);
        }
        return relation;
    }

    @Transactional
    public void updateNpcInteraction(Integer playerId, Integer npcId) {
        PlayerNpcRelation relation = ensureNpcRelation(playerId, npcId);
        relation.setLastInteractAt(LocalDateTime.now());
        relation.setTotalInteractions(defaultInt(relation.getTotalInteractions(), 0) + 1);
        playerNpcRelationMapper.updateById(relation);
    }

    private Map<Integer, Integer> getNpcAffinities(Integer playerId) {
        List<PlayerNpcRelation> relations = playerNpcRelationMapper.selectByPlayerId(playerId);
        return relations.stream()
                .collect(Collectors.toMap(PlayerNpcRelation::getNpcId, PlayerNpcRelation::getAffinity, (a, b) -> a));
    }

    // ==================== 日常对话 ====================

    /**
     * 获取NPC随机日常对话
     */
    public String getDailyDialogue(Integer playerId, Integer npcId) {
        List<NpcDailyDialogue> dialogues = npcDailyDialogueMapper.selectByNpcId(npcId);
        if (dialogues.isEmpty()) {
            return null;
        }

        Set<String> playerFlags = getPlayerFlags(playerId);
        List<NpcDailyDialogue> eligible = dialogues.stream()
                .filter(d -> meetsDailyConditions(d, playerId, playerFlags))
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            return null;
        }

        return eligible.get(ThreadLocalRandom.current().nextInt(eligible.size())).getText();
    }

    // ==================== 条件检查 ====================

    private boolean meetsPrerequisites(DialogueTree tree, Integer playerId, Set<String> playerFlags) {
        PlayerProfile player = playerService.getPlayerProfileById(playerId);

        if (tree.getMinLevel() != null && defaultInt(player.getLevel(), 1) < tree.getMinLevel()) {
            return false;
        }
        if (tree.getMaxLevel() != null && defaultInt(player.getLevel(), 1) > tree.getMaxLevel()) {
            return false;
        }
        if (tree.getRequiredRealm() != null && !tree.getRequiredRealm().trim().isEmpty()) {
            String requiredRealm = tree.getRequiredRealm().trim();
            if (player.getRealm() == null || !requiredRealm.equals(player.getRealm().trim())) {
                return false;
            }
        }
        // flag检查
        if (tree.getRequiredFlags() != null && !tree.getRequiredFlags().isEmpty()) {
            try {
                List<String> required = objectMapper.readValue(tree.getRequiredFlags(), new TypeReference<List<String>>() {});
                for (String flag : required) {
                    if (!playerFlags.contains(flag)) return false;
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.warn("解析required_flags失败: {}", tree.getRequiredFlags(), e);
            }
        }
        return true;
    }

    private boolean meetsNodeConditions(DialogueNode node, Set<String> playerFlags, Map<Integer, Integer> npcRelations) {
        if (node.getConditions() == null || node.getConditions().isEmpty()) {
            return true;
        }
        try {
            Map<String, Object> conditions = objectMapper.readValue(node.getConditions(),
                    new TypeReference<Map<String, Object>>() {});
            // min_relation: 需要对指定NPC的好感度
            if (conditions.containsKey("min_relation")) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> relReqs = objectMapper.convertValue(conditions.get("min_relation"),
                        new TypeReference<Map<String, Integer>>() {});
                for (Map.Entry<String, Integer> entry : relReqs.entrySet()) {
                    try {
                        int npcId = Integer.parseInt(entry.getKey());
                        int minRel = entry.getValue();
                        Integer current = npcRelations.getOrDefault(npcId, 0);
                        if (current < minRel) return false;
                    } catch (NumberFormatException ex) {
                        log.warn("conditions中min_relation的npcId格式错误: {}", entry.getKey());
                    }
                }
            }
            // flags: 需要拥有指定flag
            if (conditions.containsKey("flags")) {
                List<String> requiredFlags = objectMapper.convertValue(conditions.get("flags"),
                        new TypeReference<List<String>>() {});
                for (String flag : requiredFlags) {
                    if (!playerFlags.contains(flag)) return false;
                }
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.warn("解析conditions失败: {}", node.getConditions(), e);
        }
        return true;
    }

    private boolean meetsDailyConditions(NpcDailyDialogue dialogue, Integer playerId, Set<String> playerFlags) {
        if (dialogue.getConditions() == null || dialogue.getConditions().isEmpty()) {
            return true;
        }
        try {
            Map<String, Object> conditions = objectMapper.readValue(dialogue.getConditions(),
                    new TypeReference<Map<String, Object>>() {});
            // has_flag
            if (conditions.containsKey("has_flag")) {
                String requiredFlag = String.valueOf(conditions.get("has_flag")).trim();
                if (!requiredFlag.isEmpty() && !playerFlags.contains(requiredFlag)) {
                    return false;
                }
            }
            if (conditions.containsKey("flags")) {
                List<String> requiredFlags = objectMapper.convertValue(conditions.get("flags"),
                        new TypeReference<List<String>>() {});
                for (String flag : requiredFlags) {
                    if (flag != null && !playerFlags.contains(flag.trim())) {
                        return false;
                    }
                }
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.warn("解析日常对话条件失败: {}", dialogue.getConditions(), e);
        }
        return true;
    }

    private String getNpcName(Integer npcId) {
        if (npcId == null) return null;
        Npc npc = npcMapper.selectById(npcId);
        return npc != null ? npc.getName() : null;
    }

    // ==================== DTO ====================

    @Data
    public static class DialogueSceneData {
        private String dialogueKey;
        private Integer npcId;
        private String npcName;
        private String scene;
        private boolean completed;
        private DialogueLine currentLine;
        private List<ChoiceOption> choices;

        @Data
        public static class DialogueLine {
            private String speaker;
            private String text;
            private String portrait;
            private String nodeType;
        }

        @Data
        public static class ChoiceOption {
            private String nodeKey;
            private String text;
            private String tag;
        }
    }

    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}


