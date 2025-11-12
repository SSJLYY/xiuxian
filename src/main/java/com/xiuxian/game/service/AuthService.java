package com.xiuxian.game.service;

import com.xiuxian.game.dto.request.LoginRequest;
import com.xiuxian.game.dto.request.RegisterRequest;
import com.xiuxian.game.dto.response.LoginResponse;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.User;
import com.xiuxian.game.repository.PlayerProfileRepository;
import com.xiuxian.game.repository.UserRepository;
import com.xiuxian.game.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PlayerService playerService;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        try {
            log.info("开始注册用户: {}", request.getUsername());

            // 检查用户名是否已存在
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }

            // 检查邮箱是否已存在
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("邮箱已被使用");
            }

            // 创建用户
            User user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email(request.getEmail())
                    .build();

            user = userRepository.save(user);
            log.info("用户创建成功: ID={}", user.getId());

            // 创建玩家档案
            PlayerProfile playerProfile = playerService.createNewPlayer(user, request.getNickname());
            log.info("玩家档案创建成功: ID={}", playerProfile.getId());

            // 生成JWT令牌
            String token = tokenProvider.generateToken(user.getUsername());
            log.info("JWT令牌生成成功");

            return buildLoginResponse(user, playerProfile, token);

        } catch (Exception e) {
            log.error("注册失败: {}", request.getUsername(), e);
            throw new RuntimeException("注册失败: " + e.getMessage());
        }
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            log.info("开始用户登录: {}", request.getUsername());

            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("用户认证成功: {}", request.getUsername());

            // 获取用户信息
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 获取玩家档案
            PlayerProfile playerProfile = playerProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("玩家档案不存在"));

            // 生成JWT令牌
            String token = tokenProvider.generateToken(user.getUsername());
            log.info("JWT令牌生成成功");

            // 更新最后登录时间
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            return buildLoginResponse(user, playerProfile, token);

        } catch (Exception e) {
            log.error("登录失败: {}", request.getUsername(), e);
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    public User getUserByUsername(String username) {
        try {
            log.info("根据用户名查询用户: {}", username);
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
        } catch (Exception e) {
            log.error("查询用户失败: {}", username, e);
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 构建登录响应
     */
    private LoginResponse buildLoginResponse(User user, PlayerProfile playerProfile, String token) {
        return LoginResponse.builder()
                .token(token)
                .user(LoginResponse.UserDto.builder()
                        .id(user.getId().longValue())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .player(LoginResponse.PlayerDto.builder()
                        .id(playerProfile.getId().longValue())
                        .nickname(playerProfile.getNickname())
                        .level(playerProfile.getLevel())
                        .realm(playerProfile.getRealm())
                        .exp(playerProfile.getExp())
                        .expToNext(playerProfile.getExpToNext())
                        .spiritStones(playerProfile.getSpiritStones())
                        .health(playerProfile.getHealth())
                        .mana(playerProfile.getMana())
                        .attack(playerProfile.getAttack())
                        .defense(playerProfile.getDefense())
                        .build())
                .build();
    }

    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new RuntimeException("用户未登录");
            }

            String username = authentication.getName();
            log.info("获取当前用户: {}", username);
            
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
        } catch (Exception e) {
            log.error("获取当前用户失败", e);
            throw new RuntimeException("获取当前用户失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    public void logout() {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null) {
                SecurityContextHolder.clearContext();
                log.info("用户登出成功: {}", authentication.getName());
            }
        } catch (Exception e) {
            log.error("登出失败", e);
            throw new RuntimeException("登出失败: " + e.getMessage());
        }
    }
}