package com.xiuxian.game.repository;

import com.xiuxian.game.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Integer> {
    List<Equipment> findByType(String type);
    List<Equipment> findByRequiredLevelLessThanEqual(Integer level);
    List<Equipment> findByQualityGreaterThanEqual(Integer quality);
}