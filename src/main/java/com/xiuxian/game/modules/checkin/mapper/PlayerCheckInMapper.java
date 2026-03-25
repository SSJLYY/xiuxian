package com.xiuxian.game.modules.checkin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.checkin.entity.PlayerCheckIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 签到记录 Mapper
 */
@Mapper
public interface PlayerCheckInMapper extends BaseMapper<PlayerCheckIn> {

    /** 查询玩家指定日期的签到记�?*/
    @Select("SELECT * FROM player_check_ins WHERE player_id = #{playerId} AND DATE(check_in_date) = #{date} LIMIT 1")
    PlayerCheckIn findByPlayerAndDate(Integer playerId, LocalDate date);

    /** 查询玩家本月签到列表 */
    @Select("SELECT * FROM player_check_ins WHERE player_id = #{playerId} AND YEAR(check_in_date) = #{year} AND MONTH(check_in_date) = #{month} ORDER BY check_in_date")
    List<PlayerCheckIn> findByPlayerAndMonth(Integer playerId, int year, int month);

    /** 查询玩家最近一次签�?*/
    @Select("SELECT * FROM player_check_ins WHERE player_id = #{playerId} ORDER BY check_in_date DESC LIMIT 1")
    PlayerCheckIn findLatestByPlayer(Integer playerId);
}

