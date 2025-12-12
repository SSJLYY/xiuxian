package com.xiuxian.game.dto.response;

/**
 * 管理员登录响应
 */
public class AdminLoginResponse {
    private boolean success;
    private int code;
    private String message;
    private AdminData data;

    public AdminLoginResponse() {}

    public AdminLoginResponse(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public AdminLoginResponse(boolean success, int code, String message, AdminData data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static AdminLoginResponse success(String message, AdminData data) {
        return new AdminLoginResponse(true, 0, message, data);
    }

    public static AdminLoginResponse success(String message) {
        return new AdminLoginResponse(true, 0, message);
    }

    public static AdminLoginResponse error(String message) {
        return new AdminLoginResponse(false, -1, message);
    }

    public static AdminLoginResponse error(int code, String message) {
        return new AdminLoginResponse(false, code, message);
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

    public AdminData getData() {
        return data;
    }

    public void setData(AdminData data) {
        this.data = data;
    }

    /**
     * 管理员数据
     */
    public static class AdminData {
        private String token;
        private AdminUser admin;

        public AdminData() {}

        public AdminData(String token, AdminUser admin) {
            this.token = token;
            this.admin = admin;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public AdminUser getAdmin() {
            return admin;
        }

        public void setAdmin(AdminUser admin) {
            this.admin = admin;
        }
    }

    /**
     * 管理员用户信息
     */
    public static class AdminUser {
        private Long id;
        private String username;
        private String email;
        private String role;

        public AdminUser() {}

        public AdminUser(Long id, String username, String email, String role) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.role = role;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}