package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库 users 表，存储系统用户的基本信息
 * 
 * @author xiuxian
 * @version 1.0
 */
@TableName("users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 用户ID，主键，自增长
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名，唯一标识，用于登录
     */
    @TableField(value = "username")
    private String username;

    /**
     * 密码，经过加密存储
     */
    @TableField(value = "password")
    private String password;

    /**
     * 邮箱地址，用于找回密码和通知
     */
    @TableField(value = "email")
    private String email;

    /**
     * 用户角色，默认值为 USER
     * 可选值：USER, ADMIN
     */
    @TableField(value = "role")
    @Builder.Default
    private String role = "USER";

    /**
     * 是否需要修改密码标志
     * 首次登录或密码过期时需要修改
     */
    @TableField(value = "must_change_password")
    @Builder.Default
    private Boolean mustChangePassword = false;

    /**
     * 创建时间，记录用户注册时间
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间，记录最后信息修改时间
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}