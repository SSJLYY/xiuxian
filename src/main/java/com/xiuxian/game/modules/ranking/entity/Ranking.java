package com.xiuxian.game.modules.ranking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 排行榜实体类
 */
@Data
@TableName("rankings")
public class Ranking {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String rankingType; // LEVEL/SPIRIT_STONES/COMBAT_POWER/PET
    
    private Integer playerId;
    
    @TableField(exist = false)
    private String playerName;
    
    @TableField(value = "`rank`")
    private Integer rank;
    
    private Long score;
    
    @TableField(exist = false)
    private String realm;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

