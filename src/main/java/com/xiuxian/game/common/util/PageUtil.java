package com.xiuxian.game.common.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 分页工具类
 */
public class PageUtil {

    /**
     * 创建分页对象
     */
    public static <T> Page<T> createPage(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100; // 最大不超过100条
        }
        return new Page<>(pageNum, pageSize);
    }

    /**
     * 获取总页数
     */
    public static long getTotalPages(IPage<?> page) {
        return page.getPages();
    }

    /**
     * 是否有下一页
     */
    public static boolean hasNext(IPage<?> page) {
        return page.getCurrent() < page.getPages();
    }

    /**
     * 是否有上一页
     */
    public static boolean hasPrevious(IPage<?> page) {
        return page.getCurrent() > 1;
    }
}
