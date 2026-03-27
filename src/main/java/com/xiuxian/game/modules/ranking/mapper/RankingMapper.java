package com.xiuxian.game.modules.ranking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.ranking.entity.Ranking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

@Mapper
public interface RankingMapper extends BaseMapper<Ranking> {

    /**
     * 批量插入排行榜记录（性能优化：避免循环insert）
     * 注意：playerName 和 realm 是 @TableField(exist=false)，不写入DB。
     *
     * @param rankings 排行榜列表
     */
    @Insert("<script>" +
            "INSERT INTO rankings (player_id, ranking_type, rank, score, updated_at) VALUES " +
            "<foreach collection='list' item='r' separator=','>" +
            "(#{r.playerId}, #{r.rankingType}, #{r.rank}, #{r.score}, #{r.updatedAt})" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("list") List<Ranking> rankings);
}

