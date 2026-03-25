package com.xiuxian.game.common.util;

/**
 * 境界工具�?
 * 用于比较境界等级
 * 
 * @author LevelDesigner
 * @since 2026-03-23
 */
public class RealmUtil {
    
    // 境界等级定义（从低到高）
    private static final String[] REALMS = {
        "练气�?,
        "筑基�?, 
        "金丹�?,
        "元婴�?,
        "化神�?,
        "渡劫�?
    };
    
    /**
     * 比较两个境界
     * @return 负数：realm1 < realm2�?：相等，正数：realm1 > realm2
     */
    public static int compareRealm(String realm1, String realm2) {
        int index1 = getRealmIndex(realm1);
        int index2 = getRealmIndex(realm2);
        
        if (index1 == -1 || index2 == -1) {
            // 如果找不到，按字符串比较
            return realm1.compareTo(realm2);
        }
        
        return index1 - index2;
    }
    
    /**
     * 获取境界索引
     */
    private static int getRealmIndex(String realm) {
        if (realm == null) {
            return -1;
        }
        
        // 提取基础境界（去掉层数）
        String baseRealm = realm.replaceAll("[一二三四五六七八九十]+�?, "").trim();
        
        for (int i = 0; i < REALMS.length; i++) {
            if (REALMS[i].equals(baseRealm)) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * 检查realm1是否大于等于realm2
     */
    public static boolean isGreaterOrEqual(String realm1, String realm2) {
        return compareRealm(realm1, realm2) >= 0;
    }
    
    /**
     * 获取下一个境�?
     */
    public static String getNextRealm(String currentRealm) {
        int index = getRealmIndex(currentRealm);
        if (index >= 0 && index < REALMS.length - 1) {
            return REALMS[index + 1];
        }
        return null;
    }
    
    /**
     * 获取境界显示名称
     */
    public static String getRealmDisplayName(String realm) {
        if (realm == null) {
            return "未知境界";
        }
        return realm;
    }
}

