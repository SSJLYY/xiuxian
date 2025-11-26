package com.xiuxian.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiuxian.game.entity.Monster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MonsterMapper extends BaseMapper<Monster> {
    
    @Select("SELECT * FROM monsters WHERE level <= #{maxLevel} ORDER BY RAND() LIMIT 1")
    Monster selectRandomByMaxLevel(Integer maxLevel);
    
    @Select("SELECT * FROM monsters WHERE level = #{level} AND type = #{type} ORDER BY RAND() LIMIT 1")
    Monster selectRandomByLevelAndType(Integer level, String type);
    
    @Select("SELECT * FROM monsters WHERE level BETWEEN #{minLevel} AND #{maxLevel}")
    List<Monster> selectByLevelRange(Integer minLevel, Integer maxLevel);
}
