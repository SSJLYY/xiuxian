package com.xiuxian.game.modules.pet.controller;

import com.xiuxian.game.dto.PetEvolutionResult;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.pet.entity.Pet;
import com.xiuxian.game.modules.pet.entity.PetTrainingLog;
import com.xiuxian.game.modules.pet.entity.PlayerPet;
import com.xiuxian.game.modules.pet.service.PetService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PetController {

    private static final String DEFAULT_TRAINING_TYPE = "普通训练";

    private final PetService petService;
    private final PlayerService playerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Pet>>> getAllPets() {
        try {
            List<Pet> pets = petService.getAllPets();
            return ResponseEntity.ok(ApiResponse.success("获取成功", pets));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

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

    @PostMapping("/train/{playerPetId}")
    public ResponseEntity<ApiResponse<Void>> trainPet(
            @PathVariable Integer playerPetId,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            String trainingType = request != null ? request.get("trainingType") : null;
            if (trainingType == null || trainingType.trim().isEmpty()) {
                trainingType = DEFAULT_TRAINING_TYPE;
            }
            petService.trainPet(playerId, playerPetId, trainingType);
            return ResponseEntity.ok(ApiResponse.success("训练成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/rename/{playerPetId}")
    public ResponseEntity<ApiResponse<Void>> renamePet(
            @PathVariable Integer playerPetId,
            @RequestBody Map<String, String> request) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            String newNickname = request.get("nickname");
            if (newNickname == null || newNickname.trim().isEmpty()) {
                newNickname = request.get("newName");
            }
            petService.renamePet(playerId, playerPetId, newNickname);
            return ResponseEntity.ok(ApiResponse.success("重命名成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/release/{playerPetId}")
    public ResponseEntity<ApiResponse<Void>> releasePet(@PathVariable Integer playerPetId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            petService.releasePet(playerId, playerPetId);
            return ResponseEntity.ok(ApiResponse.success("放生成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

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

    @GetMapping("/training-logs/{playerPetId}")
    public ResponseEntity<ApiResponse<List<PetTrainingLog>>> getTrainingLogs(
            @PathVariable Integer playerPetId,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<PetTrainingLog> logs = petService.getTrainingLogs(playerId, playerPetId, limit);
            return ResponseEntity.ok(ApiResponse.success("获取成功", logs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/evolution/check/{playerPetId}")
    public ResponseEntity<ApiResponse<PetEvolutionResult>> checkEvolution(@PathVariable Integer playerPetId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            PetEvolutionResult result = petService.checkEvolution(playerId, playerPetId);
            String message = result.isSuccess() ? "可以进化" : "不可进化";
            return ResponseEntity.ok(ApiResponse.success(message, result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/evolution/evolve/{playerPetId}")
    public ResponseEntity<ApiResponse<PetEvolutionResult>> evolvePet(@PathVariable Integer playerPetId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            PetEvolutionResult result = petService.evolvePet(playerId, playerPetId);
            String message = result.isSuccess() ? "进化成功" : "进化失败";
            return ResponseEntity.ok(ApiResponse.success(message, result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/evolution/info/{playerPetId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEvolutionInfo(@PathVariable Integer playerPetId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Map<String, Object> info = petService.getPetEvolutionInfo(playerId, playerPetId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", info));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
