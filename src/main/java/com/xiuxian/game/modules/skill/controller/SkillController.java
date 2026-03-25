package com.xiuxian.game.modules.skill.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.skill.entity.PlayerSkill;
import com.xiuxian.game.modules.skill.entity.Skill;
import com.xiuxian.game.modules.skill.entity.SkillCombo;
import com.xiuxian.game.dto.SkillComboResult;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.dto.response.SkillResponse;
import com.xiuxian.game.modules.skill.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.features.skills.enabled", havingValue = "true")
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;
    private final PlayerService playerService;

    /**
     * 获取当前登录用户的PlayerProfile
     */
    private PlayerProfile getCurrentPlayerProfile() {
        return playerService.getCurrentPlayerProfile();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Skill>>> getAllSkills() {
        try {
            List<Skill> skills = skillService.getAllSkills();
            return ResponseEntity.ok(ApiResponse.success("获取成功", skills));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Skill>>> getAvailableSkills() {
        try {
            PlayerProfile player = getCurrentPlayerProfile();
            List<Skill> skills = skillService.getAvailableSkills(player.getId());
            return ResponseEntity.ok(ApiResponse.success("获取成功", skills));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/player")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getPlayerSkills() {
        try {
            PlayerProfile player = getCurrentPlayerProfile();
            List<SkillResponse> skills = skillService.getPlayerSkillDetails(player.getId());
            return ResponseEntity.ok(ApiResponse.success("获取成功", skills));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/equipped")
    public ResponseEntity<ApiResponse<List<PlayerSkill>>> getEquippedSkills() {
        try {
            PlayerProfile player = getCurrentPlayerProfile();
            List<PlayerSkill> skills = skillService.getEquippedSkills(player.getId());
            return ResponseEntity.ok(ApiResponse.success("获取成功", skills));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/learn/{skillId}")
    public ResponseEntity<ApiResponse<PlayerSkill>> learnSkill(@PathVariable Integer skillId) {
        try {
            PlayerProfile player = getCurrentPlayerProfile();
            PlayerSkill playerSkill = skillService.learnSkill(skillId, player.getId());
            return ResponseEntity.ok(ApiResponse.success("学习成功", playerSkill));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{playerSkillId}/upgrade")
    public ResponseEntity<ApiResponse<PlayerSkill>> upgradeSkill(@PathVariable Integer playerSkillId) {
        try {
            PlayerProfile player = getCurrentPlayerProfile();
            PlayerSkill playerSkill = skillService.upgradeSkill(playerSkillId, player.getId());
            return ResponseEntity.ok(ApiResponse.success("升级成功", playerSkill));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/equip/{playerSkillId}/{slotNumber}")
    public ResponseEntity<ApiResponse<PlayerSkill>> equipSkill(
            @PathVariable Integer playerSkillId,
            @PathVariable Integer slotNumber) {
        try {
            PlayerProfile player = getCurrentPlayerProfile();
            PlayerSkill playerSkill = skillService.equipSkill(playerSkillId, slotNumber, player.getId());
            return ResponseEntity.ok(ApiResponse.success("装备成功", playerSkill));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/unequip/{playerSkillId}")
    public ResponseEntity<ApiResponse<PlayerSkill>> unequipSkill(@PathVariable Integer playerSkillId) {
        try {
            PlayerProfile player = getCurrentPlayerProfile();
            PlayerSkill playerSkill = skillService.unequipSkill(playerSkillId, player.getId());
            return ResponseEntity.ok(ApiResponse.success("卸下成功", playerSkill));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{playerSkillId}/use")
    public ResponseEntity<ApiResponse<String>> useSkill(@PathVariable Integer playerSkillId) {
        try {
            // 使用技能后增加经验
            skillService.addSkillExperience(playerSkillId, 10);
            return ResponseEntity.ok(ApiResponse.success("技能使用成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{playerSkillId}/upgrade-by-points")
    public ResponseEntity<ApiResponse<PlayerSkill>> upgradeSkillByPoints(@PathVariable Integer playerSkillId) {
        try {
            PlayerProfile player = getCurrentPlayerProfile();
            PlayerSkill updated = skillService.upgradeSkillByPoints(playerSkillId, player.getId());
            return ResponseEntity.ok(ApiResponse.success("技能点升级成功", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{playerSkillId}/damage")
    public ResponseEntity<ApiResponse<Double>> calculateSkillDamage(@PathVariable Integer playerSkillId) {
        try {
            // 获取玩家技能实例
            PlayerProfile player = getCurrentPlayerProfile();
            List<PlayerSkill> playerSkills = skillService.getPlayerSkills(player.getId());
            PlayerSkill targetSkill = playerSkills.stream()
                    .filter(ps -> ps.getId().equals(playerSkillId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("玩家技能不存在"));
            
            double damage = skillService.calculateSkillDamage(targetSkill);
            return ResponseEntity.ok(ApiResponse.success("计算成功", damage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{playerSkillId}/cooldown")
    public ResponseEntity<ApiResponse<Integer>> getSkillCooldown(@PathVariable Integer playerSkillId) {
        try {
            // 获取玩家技能实例
            PlayerProfile player = getCurrentPlayerProfile();
            List<PlayerSkill> playerSkills = skillService.getPlayerSkills(player.getId());
            PlayerSkill targetSkill = playerSkills.stream()
                    .filter(ps -> ps.getId().equals(playerSkillId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("玩家技能不存在"));
            
            int cooldown = skillService.getSkillCooldown(targetSkill);
            return ResponseEntity.ok(ApiResponse.success("获取成功", cooldown));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{playerSkillId}/mana-cost")
    public ResponseEntity<ApiResponse<Integer>> getSkillManaCost(@PathVariable Integer playerSkillId) {
        try {
            // 获取玩家技能实例
            PlayerProfile player = getCurrentPlayerProfile();
            List<PlayerSkill> playerSkills = skillService.getPlayerSkills(player.getId());
            PlayerSkill targetSkill = playerSkills.stream()
                    .filter(ps -> ps.getId().equals(playerSkillId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("玩家技能不存在"));
            
            int manaCost = skillService.getSkillManaCost(targetSkill);
            return ResponseEntity.ok(ApiResponse.success("获取成功", manaCost));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== 技能连招相关API ====================

    /**
     * 获取玩家可用的连招列表
     */
    @GetMapping("/combos/available")
    public ResponseEntity<ApiResponse<List<SkillCombo>>> getAvailableCombos() {
        try {
            PlayerProfile player = getCurrentPlayerProfile();
            List<SkillCombo> combos = skillService.getAvailableCombos(player.getId());
            return ResponseEntity.ok(ApiResponse.success("获取成功", combos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取所有激活的连招
     */
    @GetMapping("/combos/all")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllCombos() {
        try {
            List<SkillCombo> combos = skillService.getAllActiveCombos();
            List<Map<String, Object>> comboInfos = combos.stream()
                    .map(skillService::getComboInfo)
                    .toList();
            return ResponseEntity.ok(ApiResponse.success("获取成功", comboInfos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取玩家的连招统计
     */
    @GetMapping("/combos/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getComboStats() {
        try {
            PlayerProfile player = getCurrentPlayerProfile();
            Map<String, Object> stats = skillService.getPlayerComboStats(player.getId());
            return ResponseEntity.ok(ApiResponse.success("获取成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 检测技能使用后是否触发连招（用于战斗系统）
     */
    @PostMapping("/combos/check")
    public ResponseEntity<ApiResponse<SkillComboResult>> checkCombo(
            @RequestParam Integer skillId,
            @RequestParam(defaultValue = "0") Integer baseDamage) {
        try {
            PlayerProfile player = getCurrentPlayerProfile();
            SkillComboResult result = skillService.checkAndTriggerCombo(player.getId(), skillId, baseDamage);
            return ResponseEntity.ok(ApiResponse.success(
                    result.isTriggered() ? "连招触发！" : "未触发连招", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
