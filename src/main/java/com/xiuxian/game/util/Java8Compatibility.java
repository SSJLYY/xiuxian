package com.xiuxian.game.util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Java 8 兼容性工具类
 * 提供Java 9+方法的Java 8兼容实现
 */
public class Java8Compatibility {
    
    /**
     * Java 8兼容的Map创建方法 (替代Map.of())
     */
    public static <K, V> Map<K, V> mapOf(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("键值对数量必须为偶数");
        }
        
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((K) keyValues[i], (V) keyValues[i + 1]);
        }
        return map;
    }
    
    /**
     * Java 8兼容的List创建方法 (替代List.of())
     */
    @SafeVarargs
    public static <T> List<T> listOf(T... elements) {
        return Arrays.asList(elements);
    }
    
    /**
     * Java 8兼容的Stream.toList()方法
     */
    public static <T> List<T> toList(Stream<T> stream) {
        return stream.collect(Collectors.toList());
    }
    
    /**
     * Java 8兼容的Optional.isEmpty()方法
     */
    public static <T> boolean isEmpty(Optional<T> optional) {
        return !optional.isPresent();
    }
    
    /**
     * Java 8兼容的Optional.isPresent()方法
     */
    public static <T> boolean isPresent(Optional<T> optional) {
        return optional.isPresent();
    }
}