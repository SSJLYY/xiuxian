package com.xiuxian.game.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.entity.*;
import com.xiuxian.game.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminContentService {

    private final ItemMapper itemMapper;
    private final EquipmentMapper equipmentMapper;
    private final SkillMapper skillMapper;
    private final PetMapper petMapper;
    private final MonsterMapper monsterMapper;

    // 物品管理
    public Page<Item> getItemList(int page, int size, String name) {
        Page<Item> pageObj = new Page<>(page, size);
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        
        queryWrapper.orderByAsc("id");
        return itemMapper.selectPage(pageObj, queryWrapper);
    }

    public Item getItemById(Integer id) {
        return itemMapper.selectById(id);
    }

    public Item createItem(Item item) {
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.insert(item);
        return item;
    }

    public Item updateItem(Integer id, Item item) {
        Item existingItem = itemMapper.selectById(id);
        if (existingItem == null) {
            throw new IllegalArgumentException("物品不存在");
        }
        
        item.setId(id);
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);
        return item;
    }

    public boolean deleteItem(Integer id) {
        return itemMapper.deleteById(id) > 0;
    }

    // 装备管理
    public Page<Equipment> getEquipmentList(int page, int size, String name) {
        Page<Equipment> pageObj = new Page<>(page, size);
        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        
        queryWrapper.orderByAsc("id");
        return equipmentMapper.selectPage(pageObj, queryWrapper);
    }

    public Equipment getEquipmentById(Integer id) {
        return equipmentMapper.selectById(id);
    }

    public Equipment createEquipment(Equipment equipment) {
        equipment.setCreatedAt(LocalDateTime.now());
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentMapper.insert(equipment);
        return equipment;
    }

    public Equipment updateEquipment(Integer id, Equipment equipment) {
        Equipment existingEquipment = equipmentMapper.selectById(id);
        if (existingEquipment == null) {
            throw new IllegalArgumentException("装备不存在");
        }
        
        equipment.setId(id);
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentMapper.updateById(equipment);
        return equipment;
    }

    public boolean deleteEquipment(Integer id) {
        return equipmentMapper.deleteById(id) > 0;
    }

    // 技能管理
    public Page<Skill> getSkillList(int page, int size, String name) {
        Page<Skill> pageObj = new Page<>(page, size);
        QueryWrapper<Skill> queryWrapper = new QueryWrapper<>();
        
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        
        queryWrapper.orderByAsc("id");
        return skillMapper.selectPage(pageObj, queryWrapper);
    }

    public Skill getSkillById(Integer id) {
        return skillMapper.selectById(id);
    }

    public Skill createSkill(Skill skill) {
        skill.setCreatedAt(LocalDateTime.now());
        skill.setUpdatedAt(LocalDateTime.now());
        skillMapper.insert(skill);
        return skill;
    }

    public Skill updateSkill(Integer id, Skill skill) {
        Skill existingSkill = skillMapper.selectById(id);
        if (existingSkill == null) {
            throw new IllegalArgumentException("技能不存在");
        }
        
        skill.setId(id);
        skill.setUpdatedAt(LocalDateTime.now());
        skillMapper.updateById(skill);
        return skill;
    }

    public boolean deleteSkill(Integer id) {
        return skillMapper.deleteById(id) > 0;
    }

    // 宠物管理
    public Page<Pet> getPetList(int page, int size, String name) {
        Page<Pet> pageObj = new Page<>(page, size);
        QueryWrapper<Pet> queryWrapper = new QueryWrapper<>();
        
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        
        queryWrapper.orderByAsc("id");
        return petMapper.selectPage(pageObj, queryWrapper);
    }

    public Pet getPetById(Integer id) {
        return petMapper.selectById(id);
    }

    public Pet createPet(Pet pet) {
        pet.setCreatedAt(LocalDateTime.now());
        pet.setUpdatedAt(LocalDateTime.now());
        petMapper.insert(pet);
        return pet;
    }

    public Pet updatePet(Integer id, Pet pet) {
        Pet existingPet = petMapper.selectById(id);
        if (existingPet == null) {
            throw new IllegalArgumentException("宠物不存在");
        }
        
        pet.setId(id);
        pet.setUpdatedAt(LocalDateTime.now());
        petMapper.updateById(pet);
        return pet;
    }

    public boolean deletePet(Integer id) {
        return petMapper.deleteById(id) > 0;
    }

    // 怪物管理
    public Page<Monster> getMonsterList(int page, int size, String name) {
        Page<Monster> pageObj = new Page<>(page, size);
        QueryWrapper<Monster> queryWrapper = new QueryWrapper<>();
        
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        
        queryWrapper.orderByAsc("id");
        return monsterMapper.selectPage(pageObj, queryWrapper);
    }

    public Monster getMonsterById(Integer id) {
        return monsterMapper.selectById(id);
    }

    public Monster createMonster(Monster monster) {
        monster.setCreatedAt(LocalDateTime.now());
        monster.setUpdatedAt(LocalDateTime.now());
        monsterMapper.insert(monster);
        return monster;
    }

    public Monster updateMonster(Integer id, Monster monster) {
        Monster existingMonster = monsterMapper.selectById(id);
        if (existingMonster == null) {
            throw new IllegalArgumentException("怪物不存在");
        }
        
        monster.setId(id);
        monster.setUpdatedAt(LocalDateTime.now());
        monsterMapper.updateById(monster);
        return monster;
    }

    public boolean deleteMonster(Integer id) {
        return monsterMapper.deleteById(id) > 0;
    }

    // 获取所有内容类型的统计信息
    public java.util.Map<String, Long> getContentStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        
        stats.put("items", itemMapper.selectCount(null));
        stats.put("equipments", equipmentMapper.selectCount(null));
        stats.put("skills", skillMapper.selectCount(null));
        stats.put("pets", petMapper.selectCount(null));
        stats.put("monsters", monsterMapper.selectCount(null));
        
        return stats;
    }
}