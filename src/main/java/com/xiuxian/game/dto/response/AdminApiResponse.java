package com.xiuxian.game.dto.response;

/**
 * 管理员API通用响应
 */
public class AdminApiResponse {
    private boolean success;
    private int code;
    private String message;
    private Object data;

    public AdminApiResponse() {}

    public AdminApiResponse(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public AdminApiResponse(boolean success, int code, String message, Object data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static AdminApiResponse success(String message, Object data) {
        return new AdminApiResponse(true, 0, message, data);
    }

    public static AdminApiResponse success(String message) {
        return new AdminApiResponse(true, 0, message);
    }

    public static AdminApiResponse error(String message) {
        return new AdminApiResponse(false, -1, message);
    }

    public static AdminApiResponse error(int code, String message) {
        return new AdminApiResponse(false, code, message);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}