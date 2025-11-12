package com.xiuxian.game.repository;

import com.xiuxian.game.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Integer> {
    List<Equipment> findByType(String type);
    List<Equipment> findByRequiredLevelLessThanEqual(Integer level);
    List<Equipment> findByQualityGreaterThanEqual(Integer quality);

    // 添加一些常用的查询方法
    Optional<Equipment> findByName(String name);
    List<Equipment> findByLevel(Integer level);
    List<Equipment> findByAttackBonusGreaterThan(Integer attackBonus);
    List<Equipment> findByDefenseBonusGreaterThan(Integer defenseBonus);
    List<Equipment> findByTypeAndRequiredLevelLessThanEqual(String type, Integer level);
    List<Equipment> findByTypeAndQualityGreaterThanEqual(String type, Integer quality);

    // 根据属性范围查询
    List<Equipment> findByAttackBonusBetween(Integer minAttack, Integer maxAttack);
    List<Equipment> findByRequiredLevelBetween(Integer minLevel, Integer maxLevel);
}