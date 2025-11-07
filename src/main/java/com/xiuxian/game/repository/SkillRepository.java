package com.xiuxian.game.repository;

import com.xiuxian.game.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Integer> {
    List<Skill> findByUnlockLevelLessThanEqual(Integer playerLevel);
    List<Skill> findBySkillType(String skillType);
    List<Skill> findByElement(String element);
}