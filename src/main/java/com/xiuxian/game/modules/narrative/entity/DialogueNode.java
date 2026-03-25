package com.xiuxian.game.modules.narrative.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 对话节点实体
 */
@TableName("dialogue_nodes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueNode {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("dialogue_tree_id")
    private Integer dialogueTreeId;

    @TableField("node_key")
    private String nodeKey;

    @TableField("node_type")
    private String nodeType;

    @TableField("speaker")
    private String speaker;

    @TableField("text")
    private String text;

    @TableField("portrait")
    private String portrait;

    @TableField("next_node_key")
    private String nextNodeKey;

    @TableField("parent_node_key")
    private String parentNodeKey;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("set_flags")
    private String setFlags;

    @TableField("clear_flags")
    private String clearFlags;

    @TableField("set_reputation")
    private String setReputation;

    @TableField("conditions")
    private String conditions;

    @TableField("on_complete_quest_id")
    private Integer onCompleteQuestId;

    @TableField("on_complete_flag")
    private String onCompleteFlag;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

