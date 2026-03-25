package com.xiuxian.game.modules.narrative.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.narrative.entity.DialogueNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DialogueNodeMapper extends BaseMapper<DialogueNode> {

    @Select("SELECT * FROM dialogue_nodes WHERE dialogue_tree_id = #{treeId} ORDER BY id ASC")
    List<DialogueNode> selectByTreeId(@Param("treeId") Integer treeId);

    @Select("SELECT * FROM dialogue_nodes WHERE dialogue_tree_id = #{treeId} AND node_key = #{nodeKey}")
    DialogueNode selectByTreeAndKey(@Param("treeId") Integer treeId, @Param("nodeKey") String nodeKey);

    @Select("SELECT * FROM dialogue_nodes WHERE dialogue_tree_id = #{treeId} AND parent_node_key = #{parentKey} ORDER BY sort_order ASC")
    List<DialogueNode> selectChildrenByParent(@Param("treeId") Integer treeId, @Param("parentKey") String parentKey);

    @Select("SELECT * FROM dialogue_nodes WHERE dialogue_tree_id = #{treeId} AND parent_node_key IS NULL ORDER BY sort_order ASC")
    List<DialogueNode> selectRootNodes(@Param("treeId") Integer treeId);
}

