package com.xiuxian.game.common.config;

import com.xiuxian.game.modules.skill.entity.Skill;
import com.xiuxian.game.modules.skill.mapper.SkillMapper;
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
    private final com.xiuxian.game.modules.player.mapper.UserMapper userMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        logger.info("寮€濮嬪垵濮嬪寲娓告垙鏁版嵁...");
        
        try {
            // 娣诲姞閲嶈瘯鏈哄埗锛屽皾璇曡繛鎺ユ暟鎹簱
            int retryCount = 0;
            int maxRetries = 5;
            while (retryCount < maxRetries) {
                try {
                    ensureSkillsTableExists();
                    break;
                } catch (Exception e) {
                    retryCount++;
                    logger.warn("鏁版嵁搴撹繛鎺ュけ璐ワ紝绗瑊}娆￠噸璇? {}", retryCount, e.getMessage());
                    if (retryCount >= maxRetries) {
                        throw new RuntimeException("鏁版嵁搴撹繛鎺ュけ璐ワ紝宸查噸璇?+maxRetries+"娆★紝璇锋鏌ユ暟鎹簱閰嶇疆鍜岀綉缁滆繛鎺?, e);
                    }
                    try {
                        Thread.sleep(5000); // 绛夊緟5绉掑悗閲嶈瘯
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("绾跨▼涓柇", ie);
                    }
                }
            }
            
            ensureUsersRoleColumn();
            ensureUsersForceChangeColumn();
            ensurePlayerSkillsDefaults();
            ensurePlayerItemsDefaults();
            ensureShopItemsDefaults();
            ensureAdminUser();
            // 鍒濆鍖栨妧鑳芥暟鎹?
            logger.info("鍒濆鍖栨妧鑳芥暟鎹?..");
            initializeDefaultSkills();
            logger.info("鎶€鑳芥暟鎹垵濮嬪寲瀹屾垚");
            // 鍒濆鍖栨妧鑳藉晢搴?
            logger.info("鍒濆鍖栨妧鑳藉晢搴?..");
            initializeSkillShop();
            logger.info("鎶€鑳藉晢搴楀垵濮嬪寲瀹屾垚");
            // 鍒濆鍖栦换鍔℃ā鏉?
            logger.info("鍒濆鍖栦换鍔℃ā鏉?..");
            initializeQuestTemplates();
            logger.info("浠诲姟妯℃澘鍒濆鍖栧畬鎴?);
            
            logger.info("娓告垙鏁版嵁鍒濆鍖栧叏閮ㄥ畬鎴愶紒");
        } catch (Exception e) {
            logger.error("娓告垙鏁版嵁鍒濆鍖栧け璐?, e);
            // 鍚姩涓嶄腑鏂細璁板綍閿欒浣嗕笉闃绘搴旂敤鍚姩
        }
    }

    /**
     * 鍒濆鍖栭粯璁ゆ妧鑳芥暟鎹?
     */
    private void initializeDefaultSkills() {
        long count = skillMapper.selectList(null).size();
        if (count == 0) {
            // 鍩虹鎶€鑳?
            Skill basicCultivation = Skill.builder()
                    .name("鍩虹鍔熸硶")
                    .description("鎻愬崌鍩虹淇偧閫熷害")
                    .level(1)
                    .maxLevel(100)
                    .baseDamage(0.05)
                    .damagePerLevel(0.01)
                    .cooldown(0)
                    .manaCost(0)
                    .skillType("cultivation")
                    .element("鏃?)
                    .unlockLevel(1)
                    .healthBonus(10) // 鏂板锛氱敓鍛藉€煎姞鎴?
                    .manaBonus(5)    // 鏂板锛氭硶鍔涘€煎姞鎴?
                    .attackBonus(2)  // 鏂板锛氭敾鍑诲姏鍔犳垚
                    .defenseBonus(1) // 鏂板锛氶槻寰″姏鍔犳垚
                    .speedBonus(1)   // 鏂板锛氶€熷害鍔犳垚
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Skill fireball = Skill.builder()
                    .name("鐏悆鏈?)
                    .description("鍩虹鐏郴鏀诲嚮娉曟湳")
                    .level(1)
                    .maxLevel(50)
                    .baseDamage(10.0)
                    .damagePerLevel(2.0)
                    .cooldown(5)
                    .manaCost(10)
                    .skillType("attack")
                    .element("鐏?)
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
                    .name("娌荤枟鏈?)
                    .description("鎭㈠鐢熷懡鍊肩殑娉曟湳")
                    .level(1)
                    .maxLevel(30)
                    .baseDamage(20.0)
                    .damagePerLevel(1.5)
                    .cooldown(8)
                    .manaCost(15)
                    .skillType("heal")
                    .element("鏈?)
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
                    .name("姘寸浘鏈?)
                    .description("鍒涢€犱竴涓按鐩撅紝鍑忓皯鍙楀埌鐨勪激瀹?)
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(0.0)
                    .damagePerLevel(0.0)
                    .cooldown(10)
                    .manaCost(15)
                    .skillType("闃插尽")
                    .element("姘?)
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
                    .name("鍦板埡鏈?)
                    .description("浠庡湴闈㈠彫鍞ゅ皷鍒猴紝瀵规晫浜洪€犳垚鍦熷睘鎬т激瀹?)
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(25.0)
                    .damagePerLevel(10.0)
                    .cooldown(5)
                    .manaCost(20)
                    .skillType("鏀诲嚮")
                    .element("鍦?)
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
                    .name("椋庡垉鏈?)
                    .description("閲婃斁閿嬪埄鐨勯鍒冿紝瀵规晫浜洪€犳垚椋庡睘鎬т激瀹?)
                    .level(1)
                    .maxLevel(10)
                    .baseDamage(15.0)
                    .damagePerLevel(7.0)
                    .cooldown(2)
                    .manaCost(8)
                    .skillType("鏀诲嚮")
                    .element("椋?)
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
     * 纭繚鎶€鑳借〃瀛樺湪锛圡ySQL锛?
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
            logger.info("妫€娴嬪苟鍒涘缓鎶€鑳借〃鎴愬姛锛堝涓嶅瓨鍦級");
        } catch (Exception e) {
            logger.warn("鎶€鑳借〃妫€娴?鍒涘缓澶辫触: {}", e.getMessage());
            throw new RuntimeException("鏁版嵁搴撹繛鎺ュけ璐ワ紝璇锋鏌ユ暟鎹簱閰嶇疆鍜岀綉缁滆繛鎺?, e);
        }
    }

    private void ensureUsersRoleColumn() {
        String alter = "ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER'";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(alter);
            logger.info("娣诲姞 users.role 鍒楁垚鍔?);
        } catch (Exception e) {
            logger.warn("users.role 鍒楀彲鑳藉凡瀛樺湪: {}", e.getMessage());
        }
    }

    private void ensureUsersForceChangeColumn() {
        String alter = "ALTER TABLE users ADD COLUMN must_change_password TINYINT(1) NOT NULL DEFAULT 0";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(alter);
            logger.info("娣诲姞 users.must_change_password 鍒楁垚鍔?);
        } catch (Exception e) {
            logger.warn("users.must_change_password 鍒楀彲鑳藉凡瀛樺湪: {}", e.getMessage());
        }
    }

    private void ensureAdminUser() {
        try {
            com.xiuxian.game.modules.player.entity.User admin = userMapper.selectByUsername("admin");
            if (admin == null) {
                com.xiuxian.game.modules.player.entity.User u = com.xiuxian.game.modules.player.entity.User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@xiuxian.com")
                        .role("ADMIN")
                        .mustChangePassword(false)
                        .build();
                userMapper.insert(u);
                logger.info("鍒涘缓榛樿绠＄悊鍛樻垚鍔?);
            }
        } catch (Exception e) {
            logger.warn("鍒涘缓榛樿绠＄悊鍛樺け璐? {}", e.getMessage());
        }
    }
    private void ensurePlayerSkillsDefaults() {
        String alter1 = "ALTER TABLE player_skills MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP";
        String alter2 = "ALTER TABLE player_skills MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(alter1);
            st.execute(alter2);
            logger.info("璋冩暣 player_skills 鏃堕棿鎴抽粯璁ゅ€兼垚鍔?);
        } catch (Exception e) {
            logger.warn("璋冩暣 player_skills 鏃堕棿鎴抽粯璁ゅ€煎け璐? {}", e.getMessage());
        }
    }

    private void ensurePlayerItemsDefaults() {
        String alter1 = "ALTER TABLE player_items MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP";
        String alter2 = "ALTER TABLE player_items MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(alter1);
            st.execute(alter2);
            logger.info("璋冩暣 player_items 鏃堕棿鎴抽粯璁ゅ€兼垚鍔?);
        } catch (Exception e) {
            logger.warn("璋冩暣 player_items 鏃堕棿鎴抽粯璁ゅ€煎け璐? {}", e.getMessage());
        }
    }

    private void ensureShopItemsDefaults() {
        String alter1 = "ALTER TABLE shop_items MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP";
        String alter2 = "ALTER TABLE shop_items MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(alter1);
            st.execute(alter2);
            logger.info("璋冩暣 shop_items 鏃堕棿鎴抽粯璁ゅ€兼垚鍔?);
        } catch (Exception e) {
            logger.warn("璋冩暣 shop_items 鏃堕棿鎴抽粯璁ゅ€煎け璐? {}", e.getMessage());
        }
    }
    
    /**
     * 鍒濆鍖栨妧鑳藉晢搴楁暟鎹?
     */
    private void initializeSkillShop() {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            // 妫€鏌kill_shop琛ㄦ槸鍚﹀瓨鍦?
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
            
            // 妫€鏌ユ槸鍚﹀凡鏈夋暟鎹?
            java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) as count FROM skill_shop");
            rs.next();
            int count = rs.getInt("count");
            
            if (count == 0) {
                // 鎻掑叆榛樿鎶€鑳藉晢搴楁暟鎹?
                String insertData = "INSERT INTO skill_shop (skill_id, price, required_level, available) VALUES " +
                        "(1, 500, 1, 1)," +  // 鍩虹鏀诲嚮
                        "(2, 1200, 5, 1)," + // 鐏悆鏈?
                        "(3, 1000, 3, 1)," + // 娌荤枟鏈?
                        "(4, 1600, 8, 1)," + // 姘寸浘鏈?
                        "(5, 1400, 12, 1)"  + // 鍦板埡鏈?
                        " ON DUPLICATE KEY UPDATE price=VALUES(price)";
                st.execute(insertData);
                logger.info("鎶€鑳藉晢搴楅粯璁ゆ暟鎹垵濮嬪寲鎴愬姛");
            }
        } catch (Exception e) {
            logger.warn("鍒濆鍖栨妧鑳藉晢搴楀け璐? {}", e.getMessage());
        }
    }
    
    /**
     * 鍒濆鍖栦换鍔℃ā鏉?
     */
    private void initializeQuestTemplates() {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            // 妫€鏌ユ槸鍚﹀凡鏈変换鍔℃暟鎹?
            java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) as count FROM quests WHERE type IN ('DAILY', 'WEEKLY', 'MONTHLY')");
            rs.next();
            int count = rs.getInt("count");
            
            if (count == 0) {
                // 鎻掑叆榛樿浠诲姟妯℃澘
                String insertQuests = "INSERT INTO quests (title, description, type, required_amount, reward_exp, reward_spirit_stones, reward_contribution_points, created_at, updated_at) VALUES " +
                        "('姣忔棩淇偧', '瀹屾垚涓€娆′慨鐐?, 'DAILY', 1, 100, 50, 10, NOW(), NOW())," +
                        "('姣忔棩鏀堕泦鐏电煶', '鑾峰緱100鐏电煶', 'DAILY', 100, 120, 80, 12, NOW(), NOW())," +
                        "('姣忓懆淇偧杩涘害', '绱淇偧300绉?, 'WEEKLY', 300, 800, 500, 50, NOW(), NOW())," +
                        "('姣忓懆鍗囩骇涓€娆?, '鎻愬崌1绾?, 'WEEKLY', 1, 1000, 600, 60, NOW(), NOW())," +
                        "('姣忔湀绐佺牬澧冪晫', '瀹屾垚10娆′慨鐐?, 'MONTHLY', 10, 3000, 2000, 200, NOW(), NOW())";
                st.execute(insertQuests);
                logger.info("浠诲姟妯℃澘榛樿鏁版嵁鍒濆鍖栨垚鍔?);
            }
        } catch (Exception e) {
            logger.warn("鍒濆鍖栦换鍔℃ā鏉垮け璐? {}", e.getMessage());
        }
    }
}

