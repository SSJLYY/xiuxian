package com.xiuxian.game.service;

import com.xiuxian.game.dto.request.LoginRequest;
import com.xiuxian.game.dto.request.RegisterRequest;
import com.xiuxian.game.dto.response.LoginResponse;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.User;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.mapper.UserMapper;
import com.xiuxian.game.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务类
 * 负责用户注册、登录、登出等认证相关功能
 * 
 * @author xiuxian
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PlayerService playerService;
    
    @Value("${spring.security.user.name:admin}")
    private String adminUsername;
    
    @Value("${spring.security.user.password:password}")
    private String adminPassword;

    /**
     * 用户注册
     * 
     * @param request 注册请求，包含用户名、密码、邮箱、昵称
     * @return 登录响应，包含JWT令牌和用户信息
     * @throws RuntimeException 当用户名或邮箱已存在时抛出异常
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        try {
            log.info("========== 开始用户注册流程 ==========");
            log.info("注册用户名: {}, 邮箱: {}, 昵称: {}", 
                    request.getUsername(), request.getEmail(), request.getNickname());

            // 1. 检查用户名是否已存在
            User existingUserByUsername = userMapper.selectByUsername(request.getUsername());
            if (existingUserByUsername != null) {
                log.warn("注册失败: 用户名已存在 - {}", request.getUsername());
                throw new RuntimeException("用户名已存在");
            }

            // 2. 检查邮箱是否已存在
            User existingUserByEmail = userMapper.selectByEmail(request.getEmail());
            if (existingUserByEmail != null) {
                log.warn("注册失败: 邮箱已被使用 - {}", request.getEmail());
                throw new RuntimeException("邮箱已被使用");
            }

            // 3. 创建用户账号
            log.info("创建用户账号...");
            User user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email(request.getEmail())
                    .role("USER")
                    .mustChangePassword(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            userMapper.insert(user);
            log.info("用户账号创建成功: ID={}, 用户名={}", user.getId(), user.getUsername());

            // 4. 创建玩家档案
            log.info("创建玩家档案...");
            PlayerProfile playerProfile = playerService.createNewPlayer(user, request.getNickname());
            log.info("玩家档案创建成功: ID={}, 昵称={}, 等级={}", 
                    playerProfile.getId(), playerProfile.getNickname(), playerProfile.getLevel());

            // 5. 生成JWT令牌
            log.info("生成JWT令牌...");
            String token = tokenProvider.generateToken(user.getUsername());
            log.info("JWT令牌生成成功");

            // 6. 构建响应
            LoginResponse response = buildLoginResponse(user, playerProfile, token);
            log.info("========== 用户注册成功 ==========");
            
            return response;

        } catch (RuntimeException e) {
            log.error("注册失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("注册过程发生异常: 用户名={}", request.getUsername(), e);
            throw new RuntimeException("注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     * 
     * @param request 登录请求，包含用户名、密码和用户类型
     * @return 登录响应，包含JWT令牌和用户信息
     * @throws RuntimeException 当用户名或密码错误时抛出异常
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            log.info("========== 开始用户登录流程 ==========");
            log.info("登录用户名: {}, 用户类型: {}", request.getUsername(), request.getUserType());

            // 检查是否为管理员登录
            if ("admin".equals(request.getUserType())) {
                return handleAdminLogin(request);
            } else {
                return handlePlayerLogin(request);
            }

        } catch (RuntimeException e) {
            log.error("登录失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("登录过程发生异常: 用户名={}", request.getUsername(), e);
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理管理员登录
     */
    private LoginResponse handleAdminLogin(LoginRequest request) {
        log.info("处理管理员登录: {}", request.getUsername());
        
        // 验证管理员凭证（使用配置文件中的凭证）
        if (!adminUsername.equals(request.getUsername()) || !adminPassword.equals(request.getPassword())) {
            log.error("管理员登录失败: 用户名或密码错误");
            throw new RuntimeException("管理员用户名或密码错误");
        }
        
        // 检查或创建管理员用户记录
        User adminUser = userMapper.selectByUsername(request.getUsername());
        if (adminUser == null) {
            log.info("创建管理员用户记录");
            adminUser = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email("admin@xiuxian.game")
                    .role("ADMIN")
                    .mustChangePassword(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userMapper.insert(adminUser);
            log.info("管理员用户记录创建成功: ID={}", adminUser.getId());
        }
        
        // 生成JWT令牌
        String token = tokenProvider.generateToken(adminUser.getUsername());
        
        // 更新最后登录时间
        adminUser.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(adminUser);
        
        // 构建响应（管理员不需要玩家档案）
        LoginResponse response = buildLoginResponse(adminUser, null, token);
        log.info("========== 管理员登录成功 ==========");
        
        return response;
    }
    
    /**
     * 处理普通用户登录
     */
    private LoginResponse handlePlayerLogin(LoginRequest request) {
        log.info("处理普通用户登录: {}", request.getUsername());
        
        // 使用Spring Security进行身份认证
        log.info("开始身份认证...");
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 将认证信息存入安全上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("用户认证成功: {}", request.getUsername());

        // 获取用户完整信息
        log.info("获取用户信息...");
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            log.error("认证成功但用户不存在: {}", request.getUsername());
            throw new RuntimeException("用户不存在");
        }
        log.info("用户信息获取成功: ID={}, 角色={}", user.getId(), user.getRole());

        // 获取玩家档案
        log.info("获取玩家档案...");
        PlayerProfile playerProfile = playerProfileMapper.selectByUserId(user.getId());
        if (playerProfile != null) {
            log.info("玩家档案获取成功: ID={}, 昵称={}, 等级={}, 境界={}", 
                    playerProfile.getId(), playerProfile.getNickname(), 
                    playerProfile.getLevel(), playerProfile.getRealm());
        } else {
            log.warn("用户 {} 没有玩家档案", user.getUsername());
        }

        // 生成JWT令牌
        log.info("生成JWT令牌...");
        String token = tokenProvider.generateToken(user.getUsername());
        log.info("JWT令牌生成成功");

        // 更新最后登录时间
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("更新最后登录时间");

        // 构建响应
        LoginResponse response = buildLoginResponse(user, playerProfile, token);
        log.info("========== 普通用户登录成功 ==========");
        
        return response;
    }

    public User getUserByUsername(String username) {
        try {
            log.info("根据用户名查询用户: {}", username);
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            return user;
        } catch (Exception e) {
            log.error("查询用户失败: {}", username, e);
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 构建登录响应
     */
    private LoginResponse buildLoginResponse(User user, PlayerProfile playerProfile, String token) {
        LoginResponse.LoginResponseBuilder builder = LoginResponse.builder()
                .token(token)
                .user(LoginResponse.UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build());

        if (playerProfile != null) {
            builder.player(LoginResponse.PlayerDto.builder()
                    .id(playerProfile.getId())
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
                    .build());
        }

        return builder.build();
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
            
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            return user;
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