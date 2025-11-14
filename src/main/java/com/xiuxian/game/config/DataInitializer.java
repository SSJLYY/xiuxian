package com.xiuxian.game.config;

import com.xiuxian.game.entity.Skill;
import com.xiuxian.game.mapper.SkillMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final SkillMapper skillMapper;

    @Override
    public void run(String... args) {
        logger.info("开始初始化游戏数据...");
        
        try {
            // 初始化技能数据
            logger.info("初始化技能数据...");
            initializeDefaultSkills();
            logger.info("技能数据初始化完成");
            
            logger.info("游戏数据初始化全部完成！");
        } catch (Exception e) {
            logger.error("游戏数据初始化失败", e);
            throw new RuntimeException("数据初始化失败", e);
        }
    }

    /**
     * 初始化默认技能数据
     */
    private void initializeDefaultSkills() {
        long count = skillMapper.selectList(null).size();
        if (count == 0) {
            // 基础技能
            Skill basicAttack = Skill.builder()
                    .name("基础攻击")
                    .description("基础的攻击技能，对敌人造成物理伤害")
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(10.0)
                    .damagePerLevel(5.0)
                    .cooldown(0)
                    .manaCost(0)
                    .skillType("攻击")
                    .element("无")
                    .unlockLevel(1)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            Skill fireball = Skill.builder()
                    .name("火球术")
                    .description("释放一个火球，对敌人造成火属性伤害")
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(20.0)
                    .damagePerLevel(8.0)
                    .cooldown(3)
                    .manaCost(10)
                    .skillType("攻击")
                    .element("火")
                    .unlockLevel(5)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            Skill waterShield = Skill.builder()
                    .name("水盾术")
                    .description("创造一个水盾，减少受到的伤害")
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(0.0)
                    .damagePerLevel(0.0)
                    .cooldown(10)
                    .manaCost(15)
                    .skillType("防御")
                    .element("水")
                    .unlockLevel(8)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            Skill earthSpike = Skill.builder()
                    .name("地刺术")
                    .description("从地面召唤尖刺，对敌人造成土属性伤害")
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(25.0)
                    .damagePerLevel(10.0)
                    .cooldown(5)
                    .manaCost(20)
                    .skillType("攻击")
                    .element("土")
                    .unlockLevel(12)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            Skill windSlash = Skill.builder()
                    .name("风刃术")
                    .description("释放锋利的风刃，对敌人造成风属性伤害")
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(15.0)
                    .damagePerLevel(7.0)
                    .cooldown(2)
                    .manaCost(8)
                    .skillType("攻击")
                    .element("风")
                    .unlockLevel(10)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            skillMapper.insert(basicAttack);
            skillMapper.insert(fireball);
            skillMapper.insert(waterShield);
            skillMapper.insert(earthSpike);
            skillMapper.insert(windSlash);
        }
    }
}
