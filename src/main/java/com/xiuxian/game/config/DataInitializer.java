package com.xiuxian.game.config;

import com.xiuxian.game.entity.Skill;
import com.xiuxian.game.mapper.SkillMapper;
import lombok.RequiredArgsConstructor;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
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
    private final DataSource dataSource;
    private final com.xiuxian.game.mapper.UserMapper userMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        logger.info("开始初始化游戏数据...");
        
        try {
            // 添加重试机制，尝试连接数据库
            int retryCount = 0;
            int maxRetries = 5;
            while (retryCount < maxRetries) {
                try {
                    ensureSkillsTableExists();
                    break;
                } catch (Exception e) {
                    retryCount++;
                    logger.warn("数据库连接失败，第{}次重试: {}", retryCount, e.getMessage());
                    if (retryCount >= maxRetries) {
                        throw new RuntimeException("数据库连接失败，已重试"+maxRetries+"次，请检查数据库配置和网络连接", e);
                    }
                    try {
                        Thread.sleep(5000); // 等待5秒后重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("线程中断", ie);
                    }
                }
            }
            
            ensureUsersRoleColumn();
            ensureUsersForceChangeColumn();
            ensurePlayerSkillsDefaults();
            ensurePlayerItemsDefaults();
            ensureShopItemsDefaults();
            ensureAdminUser();
            // 初始化技能数据
            logger.info("初始化技能数据...");
            initializeDefaultSkills();
            logger.info("技能数据初始化完成");
            // 初始化技能商店
            logger.info("初始化技能商店...");
            initializeSkillShop();
            logger.info("技能商店初始化完成");
            // 初始化任务模板
            logger.info("初始化任务模板...");
            initializeQuestTemplates();
            logger.info("任务模板初始化完成");
            
            logger.info("游戏数据初始化全部完成！");
        } catch (Exception e) {
            logger.error("游戏数据初始化失败", e);
            // 启动不中断：记录错误但不阻止应用启动
        }
    }

    /**
     * 初始化默认技能数据
     */
    private void initializeDefaultSkills() {
        long count = skillMapper.selectList(null).size();
        if (count == 0) {
            // 基础技能
            Skill basicCultivation = Skill.builder()
                    .name("基础功法")
                    .description("提升基础修炼速度")
                    .level(1)
                    .maxLevel(100)
                    .baseDamage(0.05)
                    .damagePerLevel(0.01)
                    .cooldown(0)
                    .manaCost(0)
                    .skillType("cultivation")
                    .element("无")
                    .unlockLevel(1)
                    .healthBonus(10) // 新增：生命值加成
                    .manaBonus(5)    // 新增：法力值加成
                    .attackBonus(2)  // 新增：攻击力加成
                    .defenseBonus(1) // 新增：防御力加成
                    .speedBonus(1)   // 新增：速度加成
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Skill fireball = Skill.builder()
                    .name("火球术")
                    .description("基础火系攻击法术")
                    .level(1)
                    .maxLevel(50)
                    .baseDamage(10.0)
                    .damagePerLevel(2.0)
                    .cooldown(5)
                    .manaCost(10)
                    .skillType("attack")
                    .element("火")
                    .unlockLevel(5)
                    .healthBonus(0)
                    .manaBonus(10)
                    .attackBonus(5)
                    .defenseBonus(0)
                    .speedBonus(2)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Skill heal = Skill.builder()
                    .name("治疗术")
                    .description("恢复生命值的法术")
                    .level(1)
                    .maxLevel(30)
                    .baseDamage(20.0)
                    .damagePerLevel(1.5)
                    .cooldown(8)
                    .manaCost(15)
                    .skillType("heal")
                    .element("木")
                    .unlockLevel(3)
                    .healthBonus(20)
                    .manaBonus(15)
                    .attackBonus(0)
                    .defenseBonus(3)
                    .speedBonus(0)
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
                    .healthBonus(30)
                    .manaBonus(10)
                    .attackBonus(0)
                    .defenseBonus(8)
                    .speedBonus(0)
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
                    .healthBonus(0)
                    .manaBonus(0)
                    .attackBonus(10)
                    .defenseBonus(5)
                    .speedBonus(0)
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
                    .healthBonus(0)
                    .manaBonus(5)
                    .attackBonus(7)
                    .defenseBonus(0)
                    .speedBonus(5)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            skillMapper.insert(basicCultivation);
            skillMapper.insert(fireball);
            skillMapper.insert(heal);
            skillMapper.insert(waterShield);
            skillMapper.insert(earthSpike);
            skillMapper.insert(windSlash);
        }
    }

    /**
     * 确保技能表存在（MySQL）
     */
    private void ensureSkillsTableExists() {
        String ddl = "CREATE TABLE IF NOT EXISTS skills (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255)," +
                "description TEXT," +
                "level INT," +
                "max_level INT," +
                "base_damage DOUBLE," +
                "damage_per_level DOUBLE," +
                "cooldown INT," +
                "mana_cost INT," +
                "skill_type VARCHAR(50)," +
                "element VARCHAR(50)," +
                "unlock_level INT," +
                "required_spirit_stones INT," +
                "health_bonus INT DEFAULT 0," +
                "mana_bonus INT DEFAULT 0," +
                "attack_bonus INT DEFAULT 0," +
                "defense_bonus INT DEFAULT 0," +
                "speed_bonus INT DEFAULT 0," +
                "icon VARCHAR(255)," +
                "animation VARCHAR(255)," +
                "active TINYINT(1)," +
                "created_at DATETIME," +
                "updated_at DATETIME" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(ddl);
            logger.info("检测并创建技能表成功（如不存在）");
        } catch (Exception e) {
            logger.warn("技能表检测/创建失败: {}", e.getMessage());
            throw new RuntimeException("数据库连接失败，请检查数据库配置和网络连接", e);
        }
    }

    private void ensureUsersRoleColumn() {
        String alter = "ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER'";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(alter);
            logger.info("添加 users.role 列成功");
        } catch (Exception e) {
            logger.warn("users.role 列可能已存在: {}", e.getMessage());
        }
    }

    private void ensureUsersForceChangeColumn() {
        String alter = "ALTER TABLE users ADD COLUMN must_change_password TINYINT(1) NOT NULL DEFAULT 0";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(alter);
            logger.info("添加 users.must_change_password 列成功");
        } catch (Exception e) {
            logger.warn("users.must_change_password 列可能已存在: {}", e.getMessage());
        }
    }

    private void ensureAdminUser() {
        try {
            com.xiuxian.game.entity.User admin = userMapper.selectByUsername("admin");
            if (admin == null) {
                com.xiuxian.game.entity.User u = com.xiuxian.game.entity.User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@xiuxian.com")
                        .role("ADMIN")
                        .mustChangePassword(false)
                        .build();
                userMapper.insert(u);
                logger.info("创建默认管理员成功");
            }
        } catch (Exception e) {
            logger.warn("创建默认管理员失败: {}", e.getMessage());
        }
    }
    private void ensurePlayerSkillsDefaults() {
        String alter1 = "ALTER TABLE player_skills MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP";
        String alter2 = "ALTER TABLE player_skills MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(alter1);
            st.execute(alter2);
            logger.info("调整 player_skills 时间戳默认值成功");
        } catch (Exception e) {
            logger.warn("调整 player_skills 时间戳默认值失败: {}", e.getMessage());
        }
    }

    private void ensurePlayerItemsDefaults() {
        String alter1 = "ALTER TABLE player_items MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP";
        String alter2 = "ALTER TABLE player_items MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(alter1);
            st.execute(alter2);
            logger.info("调整 player_items 时间戳默认值成功");
        } catch (Exception e) {
            logger.warn("调整 player_items 时间戳默认值失败: {}", e.getMessage());
        }
    }

    private void ensureShopItemsDefaults() {
        String alter1 = "ALTER TABLE shop_items MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP";
        String alter2 = "ALTER TABLE shop_items MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(alter1);
            st.execute(alter2);
            logger.info("调整 shop_items 时间戳默认值成功");
        } catch (Exception e) {
            logger.warn("调整 shop_items 时间戳默认值失败: {}", e.getMessage());
        }
    }
    
    /**
     * 初始化技能商店数据
     */
    private void initializeSkillShop() {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            // 检查skill_shop表是否存在
            String checkTable = "CREATE TABLE IF NOT EXISTS skill_shop (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "skill_id INT NOT NULL," +
                    "price BIGINT NOT NULL," +
                    "required_level INT NOT NULL DEFAULT 1," +
                    "available TINYINT(1) NOT NULL DEFAULT 1," +
                    "created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "KEY idx_skill_id (skill_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            st.execute(checkTable);
            
            // 检查是否已有数据
            java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) as count FROM skill_shop");
            rs.next();
            int count = rs.getInt("count");
            
            if (count == 0) {
                // 插入默认技能商店数据
                String insertData = "INSERT INTO skill_shop (skill_id, price, required_level, available) VALUES " +
                        "(1, 500, 1, 1)," +  // 基础攻击
                        "(2, 1200, 5, 1)," + // 火球术
                        "(3, 1000, 3, 1)," + // 治疗术
                        "(4, 1600, 8, 1)," + // 水盾术
                        "(5, 1400, 12, 1)"  + // 地刺术
                        " ON DUPLICATE KEY UPDATE price=VALUES(price)";
                st.execute(insertData);
                logger.info("技能商店默认数据初始化成功");
            }
        } catch (Exception e) {
            logger.warn("初始化技能商店失败: {}", e.getMessage());
        }
    }
    
    /**
     * 初始化任务模板
     */
    private void initializeQuestTemplates() {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            // 检查是否已有任务数据
            java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) as count FROM quests WHERE type IN ('DAILY', 'WEEKLY', 'MONTHLY')");
            rs.next();
            int count = rs.getInt("count");
            
            if (count == 0) {
                // 插入默认任务模板
                String insertQuests = "INSERT INTO quests (title, description, type, required_amount, reward_exp, reward_spirit_stones, reward_contribution_points, created_at, updated_at) VALUES " +
                        "('每日修炼', '完成一次修炼', 'DAILY', 1, 100, 50, 10, NOW(), NOW())," +
                        "('每日收集灵石', '获得100灵石', 'DAILY', 100, 120, 80, 12, NOW(), NOW())," +
                        "('每周修炼进度', '累计修炼300秒', 'WEEKLY', 300, 800, 500, 50, NOW(), NOW())," +
                        "('每周升级一次', '提升1级', 'WEEKLY', 1, 1000, 600, 60, NOW(), NOW())," +
                        "('每月突破境界', '完成10次修炼', 'MONTHLY', 10, 3000, 2000, 200, NOW(), NOW())";
                st.execute(insertQuests);
                logger.info("任务模板默认数据初始化成功");
            }
        } catch (Exception e) {
            logger.warn("初始化任务模板失败: {}", e.getMessage());
        }
    }
}