package com.xiuxian.game.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestType type;

    @Column(nullable = false)
    private Integer requiredAmount;

    @Column(nullable = false)
    private Integer rewardExp;

    @Column(nullable = false)
    private Integer rewardSpiritStones;

    @Column(nullable = false)
    private Integer rewardContributionPoints;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum QuestType {
        DAILY, // 日常任务
        WEEKLY, // 周常任务
        MAIN, // 主线任务
        SIDE // 支线任务
    }
}