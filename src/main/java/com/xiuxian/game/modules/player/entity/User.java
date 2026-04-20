package com.xiuxian.game.modules.player.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 */
@TableName("users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "username")
    private String username;

    @TableField(value = "password")
    @JsonIgnore
    private String password;

    @TableField(value = "email")
    private String email;

    @TableField(value = "role")
    @Builder.Default
    private String role = "USER";

    @TableField(value = "status")
    @Builder.Default
    private String status = "ACTIVE";

    @TableField(value = "must_change_password")
    @Builder.Default
    private Boolean mustChangePassword = false;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}
