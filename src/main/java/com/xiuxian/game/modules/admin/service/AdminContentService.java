package com.xiuxian.game.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.common.util.PageUtil;
import com.xiuxian.game.modules.combat.entity.Monster;
import com.xiuxian.game.modules.combat.mapper.MonsterMapper;
import com.xiuxian.game.modules.equipment.entity.Equipment;
import com.xiuxian.game.modules.equipment.mapper.EquipmentMapper;
import com.xiuxian.game.modules.pet.entity.Pet;
import com.xiuxian.game.modules.pet.mapper.PetMapper;
import com.xiuxian.game.modules.shop.entity.Item;
import com.xiuxian.game.modules.shop.mapper.ItemMapper;
import com.xiuxian.game.modules.skill.entity.Skill;
import com.xiuxian.game.modules.skill.mapper.SkillMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminContentService {

    private final ItemMapper itemMapper;
    private final EquipmentMapper equipmentMapper;
    private final SkillMapper skillMapper;
    private final PetMapper petMapper;
    private final MonsterMapper monsterMapper;

    public Page<Item> getItemList(int page, int size, String name) {
        Page<Item> pageObj = PageUtil.createPage(page, size);
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like("name", name.trim());
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
        int insertedRows = itemMapper.insert(item);
        if (insertedRows == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建物品失败");
        }
        return item;
    }

    public Item updateItem(Integer id, Item item) {
        ensureExists(itemMapper.selectById(id), "物品不存在，id=" + id);
        item.setId(id);
        item.setUpdatedAt(LocalDateTime.now());
        int updatedRows = itemMapper.updateById(item);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "物品不存在，id=" + id);
        }
        return itemMapper.selectById(id);
    }

    public boolean deleteItem(Integer id) {
        int deletedRows = itemMapper.deleteById(id);
        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "物品不存在，id=" + id);
        }
        return true;
    }

    public Page<Equipment> getEquipmentList(int page, int size, String name) {
        Page<Equipment> pageObj = PageUtil.createPage(page, size);
        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like("name", name.trim());
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
        int insertedRows = equipmentMapper.insert(equipment);
        if (insertedRows == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建装备失败");
        }
        return equipment;
    }

    public Equipment updateEquipment(Integer id, Equipment equipment) {
        ensureExists(equipmentMapper.selectById(id), "装备不存在，id=" + id);
        equipment.setId(id);
        equipment.setUpdatedAt(LocalDateTime.now());
        int updatedRows = equipmentMapper.updateById(equipment);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "装备不存在，id=" + id);
        }
        return equipmentMapper.selectById(id);
    }

    public boolean deleteEquipment(Integer id) {
        int deletedRows = equipmentMapper.deleteById(id);
        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "装备不存在，id=" + id);
        }
        return true;
    }

    public Page<Skill> getSkillList(int page, int size, String name) {
        Page<Skill> pageObj = PageUtil.createPage(page, size);
        QueryWrapper<Skill> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like("name", name.trim());
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
        int insertedRows = skillMapper.insert(skill);
        if (insertedRows == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建技能失败");
        }
        return skill;
    }

    public Skill updateSkill(Integer id, Skill skill) {
        ensureExists(skillMapper.selectById(id), "技能不存在，id=" + id);
        skill.setId(id);
        skill.setUpdatedAt(LocalDateTime.now());
        int updatedRows = skillMapper.updateById(skill);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "技能不存在，id=" + id);
        }
        return skillMapper.selectById(id);
    }

    public boolean deleteSkill(Integer id) {
        int deletedRows = skillMapper.deleteById(id);
        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "技能不存在，id=" + id);
        }
        return true;
    }

    public Page<Pet> getPetList(int page, int size, String name) {
        Page<Pet> pageObj = PageUtil.createPage(page, size);
        QueryWrapper<Pet> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like("name", name.trim());
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
        int insertedRows = petMapper.insert(pet);
        if (insertedRows == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建宠物失败");
        }
        return pet;
    }

    public Pet updatePet(Integer id, Pet pet) {
        ensureExists(petMapper.selectById(id), "宠物不存在，id=" + id);
        pet.setId(id);
        pet.setUpdatedAt(LocalDateTime.now());
        int updatedRows = petMapper.updateById(pet);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "宠物不存在，id=" + id);
        }
        return petMapper.selectById(id);
    }

    public boolean deletePet(Integer id) {
        int deletedRows = petMapper.deleteById(id);
        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "宠物不存在，id=" + id);
        }
        return true;
    }

    public Page<Monster> getMonsterList(int page, int size, String name) {
        Page<Monster> pageObj = PageUtil.createPage(page, size);
        QueryWrapper<Monster> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like("name", name.trim());
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
        int insertedRows = monsterMapper.insert(monster);
        if (insertedRows == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建怪物失败");
        }
        return monster;
    }

    public Monster updateMonster(Integer id, Monster monster) {
        ensureExists(monsterMapper.selectById(id), "怪物不存在，id=" + id);
        monster.setId(id);
        monster.setUpdatedAt(LocalDateTime.now());
        int updatedRows = monsterMapper.updateById(monster);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "怪物不存在，id=" + id);
        }
        return monsterMapper.selectById(id);
    }

    public boolean deleteMonster(Integer id) {
        int deletedRows = monsterMapper.deleteById(id);
        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "怪物不存在，id=" + id);
        }
        return true;
    }

    public Map<String, Long> getContentStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("items", itemMapper.selectCount(null));
        stats.put("equipments", equipmentMapper.selectCount(null));
        stats.put("skills", skillMapper.selectCount(null));
        stats.put("pets", petMapper.selectCount(null));
        stats.put("monsters", monsterMapper.selectCount(null));
        return stats;
    }

    private void ensureExists(Object entity, String message) {
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, message);
        }
    }
}
