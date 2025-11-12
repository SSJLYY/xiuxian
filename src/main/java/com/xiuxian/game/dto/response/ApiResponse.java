package com.xiuxian.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("操作成功")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }

    // 添加一些常用的成功响应方法
    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("创建成功")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> updated(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("更新成功")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> deleted() {
        return ApiResponse.<T>builder()
                .success(true)
                .message("删除成功")
                .build();
    }

    public static <T> ApiResponse<T> notFound(String resourceName) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(resourceName + "不存在")
                .build();
    }

    public static <T> ApiResponse<T> unauthorized() {
        return ApiResponse.<T>builder()
                .success(false)
                .message("未授权访问")
                .build();
    }

    public static <T> ApiResponse<T> forbidden() {
        return ApiResponse.<T>builder()
                .success(false)
                .message("权限不足")
                .build();
    }
}