package com.xiuxian.game.common.config;

import com.xiuxian.game.modules.skill.entity.Skill;
import com.xiuxian.game.modules.skill.mapper.SkillMapper;
import lombok.RequiredArgsConstructor;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import org.springframework.beans.factory.annotation.Value;
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
    private final com.xiuxian.game.modules.player.mapper.UserMapper userMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Value("${admin.default.username:admin}")
    private String defaultAdminUsername;

    @Value("${admin.default.password:admin123}")
    private String defaultAdminPassword;

    @Value("${admin.default.email:admin@xiuxian.com}")
    private String defaultAdminEmail;

    @Override
    public void run(String... args) {
        logger.info("开始执行数据初始化...");
        
        try {
            // 确保技能表存在，最多重试5次
            int retryCount = 0;
            int maxRetries = 5;
            while (retryCount < maxRetries) {
                try {
                    ensureSkillsTableExists();
                    break;
                } catch (Exception e) {
                    retryCount++;
                    logger.warn("创建技能表失败，第{}次重试: {}", retryCount, e.getMessage());
                    if (retryCount >= maxRetries) {
                        throw new RuntimeException("创建技能表失败，已重试"+maxRetries+"次，请检查数据库连接", e);
                    }
                    try {
                        Thread.sleep(5000); // 等待5秒后重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("线程被中断", ie);
                    }
                }
            }
            
            ensureUsersRoleColumn();
            ensureUsersForceChangeColumn();
            ensurePlayerSkillsDefaults();
            ensurePlayerItemsDefaults();
            ensureShopItemsDefaults();
            ensureAdminUser();
            // 初始化默认技能数据
            logger.info("初始化默认技能数据...");
            initializeDefaultSkills();
            logger.info("默认技能数据初始化完成");
            // 初始化技能商店数据
            logger.info("初始化技能商店数据...");
            initializeSkillShop();
            logger.info("技能商店数据初始化完成");
            // 初始化任务模板数据
            logger.info("初始化任务模板数据...");
            initializeQuestTemplates();
            logger.info("任务模板数据初始化完成");
            
            logger.info("数据初始化全部完成");
        } catch (Exception e) {
            logger.error("数据初始化失败", e);
            // 不要抛出异常，避免影响应用启动
        }
    }

    /**
     * 初始化默认技能数据
     */
    private void initializeDefaultSkills() {
        long count = skillMapper.selectList(null).size();
        if (count == 0) {
            // 基础修炼
            Skill basicCultivation = Skill.builder()
                    .name("基础修炼")
                    .description("吸收天地灵气强身健体")
                    .level(1)
                    .maxLevel(100)
                    .baseDamage(0.05)
                    .damagePerLevel(0.01)
                    .cooldown(0)
                    .manaCost(0)
                    .skillType("cultivation")
                    .element("金")
                    .unlockLevel(1)
                    .healthBonus(10) // 每级增加的生命值
                    .manaBonus(5)    // 每级增加的法力值
                    .attackBonus(2)  // 每级增加的攻击力
                    .defenseBonus(1) // 每级增加的防御力
                    .speedBonus(1)   // 每级增加的速度值
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Skill fireball = Skill.builder()
                    .name("火球术")
                    .description("基础火系攻击技能")
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
                    .description("恢复自身生命值")
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
                    .name("水盾")
                    .description("凝聚水元素形成护盾抵挡伤害")
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(0.0)
                    .damagePerLevel(0.0)
                    .cooldown(10)
                    .manaCost(15)
                    .skillType("防御技能")
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
                    .name("土刺")
                    .description("从地下召唤土刺攻击敌人造成伤害")
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(25.0)
                    .damagePerLevel(10.0)
                    .cooldown(5)
                    .manaCost(20)
                    .skillType("攻击技能")
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
                    .name("风刃")
                    .description("凝聚风元素形成利刃快速攻击敌人造成伤害")
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(15.0)
                    .damagePerLevel(7.0)
                    .cooldown(2)
                    .manaCost(8)
                    .skillType("攻击技能")
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
     * 确保技能表存在，不存在则创建SQL语句
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
            logger.info("技能表检查完成，表已存在或已创建");
        } catch (Exception e) {
            logger.warn("创建技能表失败: {}", e.getMessage());
            throw new RuntimeException("创建技能表失败，可能是数据库连接问题", e);
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
            com.xiuxian.game.modules.player.entity.User admin = userMapper.selectByUsername(defaultAdminUsername);
            if (admin == null) {
                if ("admin123".equals(defaultAdminPassword)) {
                    logger.warn("使用默认管理员密码！请在生产环境中通过ADMIN_DEFAULT_PASSWORD环境变量配置强密码");
                }
                com.xiuxian.game.modules.player.entity.User u = com.xiuxian.game.modules.player.entity.User.builder()
                        .username(defaultAdminUsername)
                        .password(passwordEncoder.encode(defaultAdminPassword))
                        .email(defaultAdminEmail)
                        .role("ADMIN")
                        .mustChangePassword(false)
                        .build();
                userMapper.insert(u);
                logger.info("默认管理员账户创建成功");
            }
        } catch (Exception e) {
            logger.warn("创建默认管理员账户失败: {}", e.getMessage());
        }
    }
    private void ensurePlayerSkillsDefaults() {
        String alter1 = "ALTER TABLE player_skills MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP";
        String alter2 = "ALTER TABLE player_skills MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(alter1);
            st.execute(alter2);
            logger.info("修改表 player_skills 的时间戳字段默认值成功");
        } catch (Exception e) {
            logger.warn("修改表 player_skills 的时间戳字段失败: {}", e.getMessage());
        }
    }

    private void ensurePlayerItemsDefaults() {
        String alter1 = "ALTER TABLE player_items MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP";
        String alter2 = "ALTER TABLE player_items MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(alter1);
            st.execute(alter2);
            logger.info("修改表 player_items 的时间戳字段默认值成功");
        } catch (Exception e) {
            logger.warn("修改表 player_items 的时间戳字段失败: {}", e.getMessage());
        }
    }

    private void ensureShopItemsDefaults() {
        String alter1 = "ALTER TABLE shop_items MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP";
        String alter2 = "ALTER TABLE shop_items MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(alter1);
            st.execute(alter2);
            logger.info("修改表 shop_items 的时间戳字段默认值成功");
        } catch (Exception e) {
            logger.warn("修改表 shop_items 的时间戳字段失败: {}", e.getMessage());
        }
    }
    
    /**
     * 初始化技能商店数据
     */
    private void initializeSkillShop() {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            // 如果不存在则创建skill_shop表
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
            
            // 如果表为空则插入初始数据
            java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) as count FROM skill_shop");
            rs.next();
            int count = rs.getInt("count");
            
            if (count == 0) {
                // 插入默认技能商店数据
                String insertData = "INSERT INTO skill_shop (skill_id, price, required_level, available) VALUES " +
                        "(1, 500, 1, 1)," +  // 基础修炼
                        "(2, 1200, 5, 1)," + // 火球术
                        "(3, 1000, 3, 1)," + // 治疗术
                        "(4, 1600, 8, 1)," + // 水盾
                        "(5, 1400, 12, 1)"  + // 土刺
                        " ON DUPLICATE KEY UPDATE price=VALUES(price)";
                st.execute(insertData);
                logger.info("技能商店数据初始化完成");
            }
        } catch (Exception e) {
            logger.warn("初始化技能商店数据失败: {}", e.getMessage());
        }
    }
    
    /**
     * 初始化任务模板数据
     */
    private void initializeQuestTemplates() {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            // 如果表为空则插入初始任务模板
            java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) as count FROM quests WHERE type IN ('DAILY', 'WEEKLY', 'MONTHLY')");
            rs.next();
            int count = rs.getInt("count");
            
            if (count == 0) {
                // 插入默认任务模板
                String insertQuests = "INSERT INTO quests (title, description, type, required_amount, reward_exp, reward_spirit_stones, reward_contribution_points, created_at, updated_at) VALUES " +
                        "('每日签到', '每天登录游戏', 'DAILY', 1, 100, 50, 10, NOW(), NOW())," +
                        "('每日修炼经验', '获得100修炼经验', 'DAILY', 100, 120, 80, 12, NOW(), NOW())," +
                        "('每周累计签到奖励', '每周累计签到300次', 'WEEKLY', 300, 800, 500, 50, NOW(), NOW())," +
                        "('每周境界突破', '突破境界成功', 'WEEKLY', 1, 1000, 600, 60, NOW(), NOW())," +
                        "('每月累计完成任务', '完成10个任务', 'MONTHLY', 10, 3000, 2000, 200, NOW(), NOW())";
                st.execute(insertQuests);
                logger.info("任务模板数据初始化成功");
            }
        } catch (Exception e) {
            logger.warn("初始化任务模板数据失败: {}", e.getMessage());
        }
    }
}
