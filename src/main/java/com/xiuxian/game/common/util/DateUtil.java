package com.xiuxian.game.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期时间工具�?
 */
public class DateUtil {
    
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 格式化LocalDateTime
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }
    
    /**
     * 格式化日�?
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_FORMATTER);
    }
    
    /**
     * 解析字符串为LocalDateTime
     */
    public static LocalDateTime parse(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DEFAULT_FORMATTER);
    }
    
    /**
     * 获取当前时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
    
    /**
     * 计算两个时间之间的秒�?
     */
    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.SECONDS.between(start, end);
    }
    
    /**
     * 计算两个时间之间的分钟数
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }
    
    /**
     * 计算两个时间之间的小时数
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }
    
    /**
     * 计算两个时间之间的天�?
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.DAYS.between(start, end);
    }
    
    /**
     * 增加秒数
     */
    public static LocalDateTime plusSeconds(LocalDateTime dateTime, long seconds) {
        return dateTime.plusSeconds(seconds);
    }
    
    /**
     * 增加分钟�?
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.plusMinutes(minutes);
    }
    
    /**
     * 增加小时�?
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        return dateTime.plusHours(hours);
    }
    
    /**
     * 增加天数
     */
    public static LocalDateTime plusDays(LocalDateTime dateTime, long days) {
        return dateTime.plusDays(days);
    }
    
    /**
     * 判断是否过期
     */
    public static boolean isExpired(LocalDateTime expireTime) {
        if (expireTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expireTime);
    }
    
    /**
     * 判断是否在时间范围内
     */
    public static boolean isBetween(LocalDateTime time, LocalDateTime start, LocalDateTime end) {
        if (time == null) {
            return false;
        }
        return !time.isBefore(start) && !time.isAfter(end);
    }
    
    /**
     * LocalDateTime转Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Date转LocalDateTime
     */
    public static LocalDateTime fromDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}

