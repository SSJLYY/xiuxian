package com.xiuxian.game.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 用户类型：player(普通用户) 或 admin(管理员)
     */
    private String userType = "player";
}
