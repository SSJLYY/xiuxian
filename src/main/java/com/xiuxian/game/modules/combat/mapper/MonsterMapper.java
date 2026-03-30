package com.xiuxian.game.modules.combat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.modules.combat.entity.Monster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 怪物数据访问层
 *
 * <p>提供怪物的CRUD操作，继承MyBatis-Plus的BaseMapper。</p>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Mapper
public interface MonsterMapper extends BaseMapper<Monster> {
    
    /**
     * 随机获取指定等级以下的怪物
     *
     * @param maxLevel 最大等级
     * @return 随机怪物
     */
    @Select("SELECT * FROM monsters WHERE level <= #{maxLevel} ORDER BY RAND() LIMIT 1")
    Monster selectRandomByMaxLevel(Integer maxLevel);
    
    /**
     * 随机获取指定等级和类型的怪物
     *
     * @param level 等级
     * @param type 类型
     * @return 随机怪物
     */
    @Select("SELECT * FROM monsters WHERE level = #{level} AND type = #{type} ORDER BY RAND() LIMIT 1")
    Monster selectRandomByLevelAndType(Integer level, String type);
    
    /**
     * 获取指定等级范围内的怪物列表
     *
     * @param minLevel 最小等级
     * @param maxLevel 最大等级
     * @return 怪物列表
     */
    @Select("SELECT * FROM monsters WHERE level BETWEEN #{minLevel} AND #{maxLevel}")
    List<Monster> selectByLevelRange(Integer minLevel, Integer maxLevel);
}

