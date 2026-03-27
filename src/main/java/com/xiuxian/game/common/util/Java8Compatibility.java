package com.xiuxian.game.common.util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Java 8 兼容性工具类
 * 用于在 Java 8 中使用 Java 9+ 的特性
 */
public class Java8Compatibility {

    /**
     * Java 8兼容Map.of()方法，快速创建Map (模拟Java 9的Map.of())
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mapOf(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("键值对必须成对出现");
        }

        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((K) keyValues[i], (V) keyValues[i + 1]);
        }
        return map;
    }

    /**
     * Java 8兼容List.of()方法，快速创建List (模拟Java 9的List.of())
     */
    @SafeVarargs
    public static <T> List<T> listOf(T... elements) {
        return Arrays.asList(elements);
    }

    /**
     * Java 8兼容Stream.toList()方法 (模拟Java 16的Stream.toList())
     */
    public static <T> List<T> toList(Stream<T> stream) {
        return stream.collect(Collectors.toList());
    }

    /**
     * Java 8兼容Optional.isEmpty()方法 (模拟Java 11的Optional.isEmpty())
     */
    public static <T> boolean isEmpty(Optional<T> optional) {
        return !optional.isPresent();
    }

    /**
     * Java 8兼容Optional.isPresent()方法 (模拟Java 8的Optional.isPresent())
     */
    public static <T> boolean isPresent(Optional<T> optional) {
        return optional.isPresent();
    }
}
