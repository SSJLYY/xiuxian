package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "description")
    private String description;

    @TableField(value = "type")
    private String type; // 消耗品、材料、任务物品等

    @TableField(value = "quality")
    @Builder.Default
    private Integer quality = 1; // 1-普通, 2-精良, 3-稀有, 4-史诗, 5-传说

    @TableField(value = "stackable")
    @Builder.Default
    private Boolean stackable = true; // 是否可堆叠

    @TableField(value = "max_stack")
    @Builder.Default
    private Integer maxStack = 99; // 最大堆叠数量

    @TableField(value = "price")
    @Builder.Default
    private Integer price = 0; // 价格（灵石）

    @TableField(value = "sellable")
    @Builder.Default
    private Boolean sellable = true; // 是否可出售

    @TableField(value = "usable")
    @Builder.Default
    private Boolean usable = true; // 是否可使用

    @TableField(value = "effect")
    private String effect; // 使用效果描述

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    // 业务方法
    public boolean canStackWith(Item other) {
        return this.stackable && other.stackable &&
                this.id.equals(other.id) &&
                this.maxStack > 1;
    }

    public int getRemainingStackSpace() {
        return maxStack;
    }
}