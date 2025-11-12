package com.xiuxian.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserDto user;
    private PlayerDto player;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String username;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerDto {
        private Long id;
        private String nickname;
        private Integer level;
        private String realm;
        private Long exp;
        private Long expToNext;
        private Long spiritStones;
        private Integer health;
        private Integer mana;
        private Integer attack;
        private Integer defense;
    }
}