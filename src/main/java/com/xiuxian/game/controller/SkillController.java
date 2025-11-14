package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.PlayerSkill;
import com.xiuxian.game.entity.Skill;
import com.xiuxian.game.service.PlayerService;
import com.xiuxian.game.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse<List<PlayerSkill>>> getPlayerSkills() {
        try {
            PlayerProfile player = getCurrentPlayerProfile();
            List<PlayerSkill> skills = skillService.getPlayerSkills(player.getId());
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
}
