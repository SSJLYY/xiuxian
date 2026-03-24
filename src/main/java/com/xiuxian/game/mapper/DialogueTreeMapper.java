package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.DialogueTree;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DialogueTreeMapper extends BaseMapper<DialogueTree> {

    @Select("SELECT * FROM dialogue_trees WHERE npc_id = #{npcId} AND active = 1 ORDER BY priority DESC")
    List<DialogueTree> selectByNpcId(@Param("npcId") Integer npcId);

    @Select("SELECT * FROM dialogue_trees WHERE dialogue_key = #{dialogueKey}")
    DialogueTree selectByKey(@Param("dialogueKey") String dialogueKey);

    @Select("SELECT * FROM dialogue_trees WHERE dialogue_key = #{dialogueKey} AND active = 1")
    DialogueTree selectActiveByKey(@Param("dialogueKey") String dialogueKey);
}
