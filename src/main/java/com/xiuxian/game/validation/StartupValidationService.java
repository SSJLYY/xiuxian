package com.xiuxian.game.validation;

import com.xiuxian.game.modules.player.entity.*;
import com.xiuxian.game.modules.combat.entity.*;
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
import com.xiuxian.game.modules.cultivation.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Service that performs comprehensive type validation during application startup
 * Implements Requirements 3.1, 3.2, 3.4, 3.5
 */
@Service
public class StartupValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(StartupValidationService.class);
    
    @Autowired
    private DataConsistencyValidator dataConsistencyValidator;
    
    @Autowired
    private ValidationConfig validationConfig;
    
    // List of all entity classes to validate
    private static final List<Class<?>> ENTITY_CLASSES = Arrays.asList(
            User.class,
            PlayerProfile.class,
            Item.class,
            PlayerItem.class,
            Quest.class,
            PlayerQuest.class,
            Skill.class,
            PlayerSkill.class,
            Equipment.class,
            PlayerEquipment.class,
            Achievement.class,
            PlayerAchievement.class,
            Guild.class,
            GuildMember.class,
            ShopItem.class,
            Monster.class,
            Pet.class,
            PlayerPet.class
    );
    
    /**
     * Perform startup validation when application is ready
     * This implements Property 8: Runtime Validation
     */
    @EventListener(ApplicationReadyEvent.class)
    public void performStartupValidation() {
        if (!validationConfig.isEnableStartupValidation()) {
            logger.info("Startup validation is disabled");
            return;
        }
        
        logger.info("Starting comprehensive data consistency validation...");
        
        try {
            List<TypeValidationResult> results = dataConsistencyValidator.validateAllEntities(ENTITY_CLASSES);
            
            // Generate and log summary
            ValidationSummary summary = ValidationUtils.generateSummary(results);
            ValidationUtils.logSummary(summary);
            
            // Log detailed results for entities with issues (if enabled)
            if (validationConfig.isEnableDetailedLogging()) {
                for (TypeValidationResult result : results) {
                    if (!result.isValid() || result.hasWarnings()) {
                        logDetailedResult(result);
                    }
                }
            }
            
            // Determine if we should fail startup
            boolean hasCriticalErrors = ValidationUtils.hasCriticalErrors(results);
            
            if (hasCriticalErrors && validationConfig.isFailOnCriticalMismatches()) {
                String errorMessage = "Critical type mismatches detected. Application startup failed.";
                logger.error(errorMessage);
                
                // Collect all critical mismatches
                List<TypeMismatch> criticalMismatches = ValidationUtils.getAllCriticalMismatches(results);
                
                throw new ValidationException(errorMessage, criticalMismatches);
            }
            
            // Log success confirmation (Requirement 3.5)
            if (summary.isSuccessful()) {
                logger.info("SUCCESS: All data types are consistent across layers");
                logger.info("SUCCESS: Startup validation completed successfully");
            } else if (hasCriticalErrors) {
                logger.error("ERROR: Critical validation errors detected but startup continues (failOnCriticalMismatches=false)");
            } else {
                logger.warn("WARNING: Startup validation completed with warnings. Review the issues above.");
            }
            
        } catch (ValidationException e) {
            // Re-throw validation exceptions to fail startup
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during startup validation: {}", e.getMessage(), e);
            throw new ValidationException("Startup validation failed due to unexpected error", 
                    Arrays.asList(), e);
        }
    }
    
    /**
     * Log detailed validation results for entities with issues
     */
    private void logDetailedResult(TypeValidationResult result) {
        logger.info("--- Entity: {} (Table: {}) ---", result.getEntityName(), result.getTableName());
        
        if (!result.isValid()) {
            logger.error("ERROR: Validation FAILED");
            for (TypeMismatch mismatch : result.getMismatches()) {
                logger.error("  - {}", mismatch.toString());
            }
        } else {
            logger.info("SUCCESS: Validation PASSED");
        }
        
        if (result.hasWarnings()) {
            for (String warning : result.getWarnings()) {
                logger.warn("  WARNING: {}", warning);
            }
        }
    }
    
    /**
     * Manually trigger validation (for testing purposes)
     */
    public List<TypeValidationResult> validateNow() {
        return dataConsistencyValidator.validateAllEntities(ENTITY_CLASSES);
    }
}
