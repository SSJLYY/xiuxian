package com.xiuxian.game.modules.skill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.skill.entity.PlayerSkillComboRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 玩家技能连招记录Mapper
 */
@Mapper
public interface PlayerSkillComboRecordMapper extends BaseMapper<PlayerSkillComboRecord> {

    /**
     * 获取玩家最近N次技能使用记录（按时间倒序）
     *
     * @param playerId 玩家ID
     * @param limit    限制数量
     * @return 技能使用记录列表
     */
    @Select("SELECT * FROM player_skill_combo_records WHERE player_id = #{playerId} ORDER BY used_at DESC LIMIT #{limit}")
    List<PlayerSkillComboRecord> findRecentRecords(@Param("playerId") Integer playerId, @Param("limit") int limit);

    /**
     * 删除玩家超过指定时间的旧记录
     *
     * @param playerId   玩家ID
     * @param beforeTime 时间阈值
     */
    @Delete("DELETE FROM player_skill_combo_records WHERE player_id = #{playerId} AND used_at < #{beforeTime}")
    void deleteOldRecords(@Param("playerId") Integer playerId, @Param("beforeTime") LocalDateTime beforeTime);
}
