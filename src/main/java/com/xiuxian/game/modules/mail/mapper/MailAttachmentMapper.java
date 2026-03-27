package com.xiuxian.game.modules.mail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.mail.entity.MailAttachment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MailAttachmentMapper extends BaseMapper<MailAttachment> {

    /**
     * 批量删除指定邮件ID的附件（IN 子句）
     *
     * @param mailIds 邮件ID列表
     */
    @Delete("<script>" +
            "DELETE FROM mail_attachments WHERE mail_id IN " +
            "<foreach collection='mailIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    void deleteBatchByMailIds(@Param("mailIds") List<Long> mailIds);
}

