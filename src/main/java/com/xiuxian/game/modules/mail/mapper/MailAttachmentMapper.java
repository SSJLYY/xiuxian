package com.xiuxian.game.modules.mail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.mail.entity.MailAttachment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MailAttachmentMapper extends BaseMapper<MailAttachment> {

    @Delete("<script>" +
            "DELETE FROM mail_attachments WHERE mail_id IN " +
            "<foreach collection='mailIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    void deleteBatchByMailIds(@Param("mailIds") List<Long> mailIds);

    @Insert("<script>" +
            "INSERT INTO mail_attachments (mail_id, item_type, item_id, quantity) VALUES " +
            "<foreach collection='attachments' item='a' separator=','>" +
            "(#{a.mailId}, #{a.itemType}, #{a.itemId}, #{a.quantity})" +
            "</foreach>" +
            "</script>")
    int insertBatchSomeColumn(@Param("attachments") List<MailAttachment> attachments);
}

