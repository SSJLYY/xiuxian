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

@TableName("quests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quest {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "title")
    private String title;

    @TableField(value = "description")
    private String description;

    @TableField(value = "type")
    private String type;

    @TableField(value = "required_amount")
    private Integer requiredAmount;

    @TableField(value = "reward_exp")
    private Integer rewardExp;

    @TableField(value = "reward_spirit_stones")
    private Integer rewardSpiritStones;

    @TableField(value = "reward_contribution_points")
    private Integer rewardContributionPoints;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    public enum QuestType {
        DAILY, // 日常任务
        WEEKLY, // 周常任务
        MAIN, // 主线任务
        SIDE // 支线任务
    }
}
