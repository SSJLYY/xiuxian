package com.xiuxian.game.modules.admin.controller;

import com.xiuxian.game.dto.response.AdminApiResponse;
import com.xiuxian.game.modules.combat.mapper.MonsterMapper;
import com.xiuxian.game.modules.equipment.mapper.EquipmentMapper;
import com.xiuxian.game.modules.pet.mapper.PetMapper;
import com.xiuxian.game.modules.shop.mapper.ItemMapper;
import com.xiuxian.game.modules.skill.mapper.SkillMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 内容管理控制器
 * 提供游戏内容统计查询功能
 *
 * @author shaun.sheng
 */
@RestController
@RequestMapping("/api/admin/content")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminContentController {

    private final ItemMapper itemMapper;
    private final EquipmentMapper equipmentMapper;
    private final SkillMapper skillMapper;
    private final PetMapper petMapper;
    private final MonsterMapper monsterMapper;

    /**
     * 获取内容统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminApiResponse> getContentStats() {
        try {
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("items", itemMapper.selectCount(null));
            stats.put("equipments", equipmentMapper.selectCount(null));
            stats.put("skills", skillMapper.selectCount(null));
            stats.put("pets", petMapper.selectCount(null));
            stats.put("monsters", monsterMapper.selectCount(null));

            return ResponseEntity.ok(AdminApiResponse.success("获取内容统计成功", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AdminApiResponse.error("获取内容统计失败: " + e.getMessage()));
        }
    }
}
