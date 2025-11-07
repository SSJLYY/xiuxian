package com.xiuxian.game.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cultivation_logs")
public class CultivationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerProfile playerProfile;

    @Column(name = "exp_gained", nullable = false)
    private Long expGained;

    @Column(name = "spirit_stones_gained", nullable = false)
    private Integer spiritStonesGained;

    @Column(name = "cultivation_duration", nullable = false)
    private Long cultivationDuration;

    @Column(name = "is_offline", nullable = false)
    @Builder.Default
    private Boolean isOffline = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}