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
 * 传说条目实体
 */
@TableName("lore_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoreEntry {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("lore_key")
    private String loreKey;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("lore_layer")
    private String loreLayer;

    @TableField("category")
    private String category;

    @TableField("related_npcs")
    private String relatedNpcs;

    @TableField("related_lore_keys")
    private String relatedLoreKeys;

    @TableField("discover_condition")
    private String discoverCondition;

    @TableField("min_realm")
    private String minRealm;

    @TableField("min_level")
    private Integer minLevel;

    @TableField("required_lore_keys")
    private String requiredLoreKeys;

    @TableField("icon")
    private String icon;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("active")
    private Boolean active;

    @TableField("created_at")
    private LocalDateTime createdAt;

    public enum LoreLayer {
        SURFACE("表面"),
        ENGAGED("参与"),
        DEEP("深层");

        private final String label;

        LoreLayer(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}

