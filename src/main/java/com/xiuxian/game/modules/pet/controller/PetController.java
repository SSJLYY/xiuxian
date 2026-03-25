package com.xiuxian.game.modules.pet.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.PetEvolutionResult;
import com.xiuxian.game.modules.player.entity.*;
import com.xiuxian.game.modules.combat.entity.*;
import com.xiuxian.game.modules.cultivation.entity.*;
import com.xiuxian.game.modules.pet.entity.*;
import com.xiuxian.game.modules.equipment.entity.*;
import com.xiuxian.game.modules.skill.entity.*;
import com.xiuxian.game.modules.quest.entity.*;
import com.xiuxian.game.modules.achievement.entity.*;
import com.xiuxian.game.modules.guild.entity.*;
import com.xiuxian.game.modules.ranking.entity.*;
import com.xiuxian.game.modules.auction.entity.*;
import com.xiuxian.game.modules.narrative.entity.*;
import com.xiuxian.game.modules.mail.entity.*;
import com.xiuxian.game.modules.shop.entity.*;
import com.xiuxian.game.modules.checkin.entity.*;
import com.xiuxian.game.modules.activity.entity.*;
import com.xiuxian.game.modules.giftcode.entity.*;
import com.xiuxian.game.modules.offline.entity.*;
import com.xiuxian.game.modules.map.entity.*;
import com.xiuxian.game.modules.announcement.entity.*;
import com.xiuxian.game.modules.vip.entity.*;
import com.xiuxian.game.modules.admin.entity.*;
import com.xiuxian.game.modules.pet.service.PetService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 宠物控制�?
 * 处理宠物相关的HTTP请求
 */
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PetController {

    private final PetService petService;
    private final PlayerService playerService;

    /**
     * 获取所有宠物模�?
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Pet>>> getAllPets() {
        try {
            List<Pet> pets = petService.getAllPets();
            return ResponseEntity.ok(ApiResponse.success("获取成功", pets));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取可捕获的宠物列表
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Pet>>> getAvailablePets() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<Pet> pets = petService.getAvailablePets(playerId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", pets));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取玩家的所有宠�?
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PlayerPet>>> getMyPets() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<PlayerPet> pets = petService.getPlayerPets(playerId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", pets));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取出战宠物
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PlayerPet>> getActivePet() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            PlayerPet pet = petService.getActivePet(playerId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", pet));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 捕获宠物
     */
    @PostMapping("/capture/{petId}")
    public ResponseEntity<ApiResponse<PlayerPet>> capturePet(@PathVariable Integer petId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            PlayerPet playerPet = petService.capturePet(playerId, petId);
            return ResponseEntity.ok(ApiResponse.success("捕获成功", playerPet));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 设置出战宠物
     */
    @PostMapping("/activate/{playerPetId}")
    public ResponseEntity<ApiResponse<Void>> setActivePet(@PathVariable Integer playerPetId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            petService.setActivePet(playerId, playerPetId);
            return ResponseEntity.ok(ApiResponse.success("设置成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 喂食宠物
     */
    @PostMapping("/feed/{playerPetId}")
    public ResponseEntity<ApiResponse<Void>> feedPet(@PathVariable Integer playerPetId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            petService.feedPet(playerId, playerPetId);
            return ResponseEntity.ok(ApiResponse.success("喂食成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 训练宠物
     */
    @PostMapping("/train/{playerPetId}")
    public ResponseEntity<ApiResponse<Void>> trainPet(
            @PathVariable Integer playerPetId,
            @RequestBody Map<String, String> request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            String trainingType = request.get("trainingType");
            if (trainingType == null || trainingType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("训练类型不能为空"));
            }
            petService.trainPet(playerId, playerPetId, trainingType);
            return ResponseEntity.ok(ApiResponse.success("训练成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 重命名宠�?
     */
    @PostMapping("/rename/{playerPetId}")
    public ResponseEntity<ApiResponse<Void>> renamePet(
            @PathVariable Integer playerPetId,
            @RequestBody Map<String, String> request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            String newNickname = request.get("nickname");
            petService.renamePet(playerId, playerPetId, newNickname);
            return ResponseEntity.ok(ApiResponse.success("重命名成�?, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 释放宠物
     */
    @DeleteMapping("/release/{playerPetId}")
    public ResponseEntity<ApiResponse<Void>> releasePet(@PathVariable Integer playerPetId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            petService.releasePet(playerId, playerPetId);
            return ResponseEntity.ok(ApiResponse.success("释放成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 锁定/解锁宠物
     */
    @PostMapping("/toggle-lock/{playerPetId}")
    public ResponseEntity<ApiResponse<Void>> toggleLockPet(@PathVariable Integer playerPetId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            petService.toggleLockPet(playerId, playerPetId);
            return ResponseEntity.ok(ApiResponse.success("操作成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取宠物训练记录
     */
    @GetMapping("/training-logs/{playerPetId}")
    public ResponseEntity<ApiResponse<List<PetTrainingLog>>> getTrainingLogs(
            @PathVariable Integer playerPetId,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            List<PetTrainingLog> logs = petService.getTrainingLogs(playerPetId, limit);
            return ResponseEntity.ok(ApiResponse.success("获取成功", logs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== 宠物进化相关API ====================

    /**
     * 检查宠物是否可以进�?
     */
    @GetMapping("/evolution/check/{playerPetId}")
    public ResponseEntity<ApiResponse<PetEvolutionResult>> checkEvolution(@PathVariable Integer playerPetId) {
        try {
            PetEvolutionResult result = petService.checkEvolution(playerPetId);
            return ResponseEntity.ok(ApiResponse.success(result.isSuccess() ? "可以进化" : "不可进化", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 执行宠物进化
     */
    @PostMapping("/evolution/evolve/{playerPetId}")
    public ResponseEntity<ApiResponse<PetEvolutionResult>> evolvePet(@PathVariable Integer playerPetId) {
        try {
            PetEvolutionResult result = petService.evolvePet(playerPetId);
            return ResponseEntity.ok(ApiResponse.success(result.isSuccess() ? "进化成功�? : "进化失败", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取宠物进化信息
     */
    @GetMapping("/evolution/info/{playerPetId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEvolutionInfo(@PathVariable Integer playerPetId) {
        try {
            Map<String, Object> info = petService.getPetEvolutionInfo(playerPetId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", info));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}


