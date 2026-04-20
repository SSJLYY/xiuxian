/**
 * 日志脱敏工具类
 * 
 * 用于对日志中的敏感信息进行脱敏处理，防止敏感数据泄露
 * 
 * 脱敏规则：
 * - 密码：统一显示为 ***
 * - Token：仅显示前 8 位 + ...
 * - 手机号：显示前 3 位 + 中间 4 位隐藏 + 后 4 位
 * - 邮箱：显示前 2 位 + ***@域名
 * - 身份证号：显示前 3 位 + 中间隐藏 + 后 4 位
 * - 银行卡号：显示前 4 位 + 中间隐藏 + 后 4 位
 * 
 * @author xiuxian-game-team
 * @version 1.0
 */
public class LogMasker {

    private static final String MASK = "***";
    private static final String PARTIAL_MASK = "****";

    /**
     * 密码脱敏
     * 
     * @param password 密码
     * @return 脱敏后的字符串
     */
    public static String maskPassword(String password) {
        return password != null ? MASK : "null";
    }

    /**
     * Token 脱敏（显示前 8 位）
     * 
     * @param token Token 字符串
     * @return 脱敏后的字符串
     */
    public static String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return MASK;
        }
        if (token.length() <= 8) {
            return MASK;
        }
        return token.substring(0, 8) + "...";
    }

    /**
     * 手机号脱敏
     * 格式：138****1234
     * 
     * @param phone 手机号
     * @return 脱敏后的字符串
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return MASK;
        }
        if (phone.length() < 7) {
            return MASK;
        }
        return phone.substring(0, 3) + PARTIAL_MASK + phone.substring(phone.length() - 4);
    }

    /**
     * 邮箱脱敏
     * 格式：ab***@example.com
     * 
     * @param email 邮箱地址
     * @return 脱敏后的字符串
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return MASK;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return MASK;
        }
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);
        
        if (localPart.length() <= 2) {
            return MASK + domainPart;
        }
        return localPart.substring(0, 2) + MASK + domainPart;
    }

    /**
     * 身份证号脱敏
     * 格式：110***********1234
     * 
     * @param idCard 身份证号
     * @return 脱敏后的字符串
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return MASK;
        }
        if (idCard.length() < 7) {
            return MASK;
        }
        return idCard.substring(0, 3) + "********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 银行卡号脱敏
     * 格式：6222***********1234
     * 
     * @param cardNumber 银行卡号
     * @return 脱敏后的字符串
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return MASK;
        }
        if (cardNumber.length() < 8) {
            return MASK;
        }
        // 移除空格和连字符
        String cleaned = cardNumber.replaceAll("[\\s-]", "");
        if (cleaned.length() < 8) {
            return MASK;
        }
        return cleaned.substring(0, 4) + "************" + cleaned.substring(cleaned.length() - 4);
    }

    /**
     * 姓名脱敏
     * 格式：张*三、李**（两个字显示第一个，三个字显示首尾）
     * 
     * @param name 姓名
     * @return 脱敏后的字符串
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return MASK;
        }
        if (name.length() == 1) {
            return MASK;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "**" + name.charAt(name.length() - 1);
    }

    /**
     * 地址脱敏
     * 格式：北京市海淀区***
     * 
     * @param address 地址
     * @return 脱敏后的字符串
     */
    public static String maskAddress(String address) {
        if (address == null || address.isEmpty()) {
            return MASK;
        }
        if (address.length() <= 6) {
            return MASK;
        }
        return address.substring(0, 6) + "***";
    }

    /**
     * IP 地址脱敏
     * 格式：192.168.1.*
     * 
     * @param ip IP 地址
     * @return 脱敏后的字符串
     */
    public static String maskIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return MASK;
        }
        int lastDotIndex = ip.lastIndexOf('.');
        if (lastDotIndex <= 0) {
            return MASK;
        }
        return ip.substring(0, lastDotIndex) + ".*";
    }

    /**
     * URL 脱敏
     * 格式：https://www.example.com/abc***
     * 
     * @param url URL 地址
     * @return 脱敏后的字符串
     */
    public static String maskUrl(String url) {
        if (url == null || url.isEmpty()) {
            return MASK;
        }
        if (url.length() <= 20) {
            return MASK;
        }
        return url.substring(0, 20) + "...";
    }

    /**
     * 数据库连接 URL 脱敏（移除密码参数）
     * 
     * @param jdbcUrl JDBC 连接 URL
     * @return 脱敏后的字符串
     */
    public static String maskJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            return MASK;
        }
        // 移除 password 参数
        return jdbcUrl.replaceAll("(?i)(password=)[^&]*", "$1***");
    }

    /**
     * 通用脱敏方法（自定义脱敏长度）
     * 
     * @param value 原始值
     * @param keepPrefix 保留前缀长度
     * @param keepSuffix 保留后缀长度
     * @return 脱敏后的字符串
     */
    public static String mask(String value, int keepPrefix, int keepSuffix) {
        if (value == null || value.isEmpty()) {
            return MASK;
        }
        int length = value.length();
        if (length <= keepPrefix + keepSuffix) {
            return MASK;
        }
        return value.substring(0, keepPrefix) + MASK + value.substring(length - keepSuffix);
    }
}
