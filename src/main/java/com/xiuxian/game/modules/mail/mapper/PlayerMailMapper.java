package com.xiuxian.game.modules.mail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.mail.entity.PlayerMail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 玩家邮件Mapper
 */
@Mapper
public interface PlayerMailMapper extends BaseMapper<PlayerMail> {

    /**
     * 分页查询过期邮件ID（避免全表加载到内存）
     *
     * @param now    当前时间
     * @param limit  每批大小
     * @return 过期邮件ID列表
     */
    @Select("SELECT id FROM player_mails WHERE expire_at < #{now} LIMIT #{limit}")
    List<Long> selectExpiredMailIds(@Param("now") LocalDateTime now, @Param("limit") int limit);

    /**
     * 批量删除邮件（IN 子句）
     *
     * @param ids 邮件ID列表
     */
    @Delete("<script>" +
            "DELETE FROM player_mails WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    void deleteBatchByIds(@Param("ids") List<Long> ids);
}

