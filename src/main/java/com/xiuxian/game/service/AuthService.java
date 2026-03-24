package com.xiuxian.game.service;

import com.xiuxian.game.dto.request.LoginRequest;
import com.xiuxian.game.dto.request.RegisterRequest;
import com.xiuxian.game.dto.response.LoginResponse;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.User;
import com.xiuxian.game.exception.BusinessException;
import com.xiuxian.game.exception.ErrorCode;
import com.xiuxian.game.mapper.PlayerProfileMapper;
import com.xiuxian.game.mapper.UserMapper;
import com.xiuxian.game.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
 * @version 1.1
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

    /**
     * 管理员用户名（来自配置文件）
     * 注意：此处仅读取用户名用于查找账号，密码对比通过 BCrypt 哈希完成
     */
    @Value("${spring.security.user.name:admin}")
    private String adminUsername;

    /**
     * 管理员密码（来自配置文件，明文）
     * 仅用于初次创建管理员账号时加密存储，登录时通过 BCrypt 匹配
     */
    @Value("${spring.security.user.password:password}")
    private String adminPasswordPlain;

    // =====================================================================
    // 公开 API
    // =====================================================================

    /**
     * 用户注册
     *
     * @param request 注册请求，包含用户名、密码、邮箱、昵称
     * @return 登录响应，包含 JWT 令牌和用户信息
     * @throws BusinessException 当用户名或邮箱已存在时
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info("开始注册流程: username={}, email={}", request.getUsername(), request.getEmail());

        // 1. 检查用户名唯一性
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            log.warn("注册失败: 用户名已存在 - {}", request.getUsername());
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 2. 检查邮箱唯一性
        if (userMapper.selectByEmail(request.getEmail()) != null) {
            log.warn("注册失败: 邮箱已被使用 - {}", request.getEmail());
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 3. 创建用户账号（密码 BCrypt 加密）
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
        log.debug("用户账号创建成功: id={}", user.getId());

        // 4. 创建玩家档案
        PlayerProfile playerProfile = playerService.createNewPlayer(user, request.getNickname());
        log.debug("玩家档案创建成功: id={}, nickname={}", playerProfile.getId(), playerProfile.getNickname());

        // 5. 生成 JWT 令牌
        String token = tokenProvider.generateToken(user.getUsername());

        log.info("注册成功: username={}", user.getUsername());
        return buildLoginResponse(user, playerProfile, token);
    }

    /**
     * 用户登录
     *
     * @param request 登录请求，包含用户名、密码和用户类型
     * @return 登录响应，包含 JWT 令牌和用户信息
     * @throws BusinessException 当认证失败时
     */
    public LoginResponse login(LoginRequest request) {
        log.info("登录请求: username={}, userType={}", request.getUsername(), request.getUserType());
        if ("admin".equals(request.getUserType())) {
            return handleAdminLogin(request);
        } else {
            return handlePlayerLogin(request);
        }
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     * @throws BusinessException 用户不存在时
     */
    public User getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前用户实体
     * @throws BusinessException 未登录或用户不存在时
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        String username = authentication.getName();
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * 用户登出
     * 清除 SecurityContext 中的认证信息
     */
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.info("用户登出: {}", authentication.getName());
            SecurityContextHolder.clearContext();
        }
    }

    // =====================================================================
    // 私有方法
    // =====================================================================

    /**
     * 管理员登录处理
     *
     * <p>安全说明：</p>
     * <ul>
     *   <li>管理员账号首次登录时自动创建，密码以 BCrypt 哈希形式存储</li>
     *   <li>后续登录使用 BCrypt 匹配，不做明文比较</li>
     *   <li>配置文件中的明文密码仅在初始化时用于加密，不参与实际对比</li>
     * </ul>
     */
    @Transactional
    private LoginResponse handleAdminLogin(LoginRequest request) {
        log.info("管理员登录: {}", request.getUsername());

        // 用户名校验（快速失败）
        if (!adminUsername.equals(request.getUsername())) {
            log.warn("管理员登录失败: 用户名不匹配");
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 查找或初始化管理员账号
        User adminUser = userMapper.selectByUsername(request.getUsername());
        if (adminUser == null) {
            // 首次登录：创建管理员账号，密码加密存储
            log.info("首次登录，初始化管理员账号");
            adminUser = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(adminPasswordPlain))
                    .email("admin@xiuxian.game")
                    .role("ADMIN")
                    .mustChangePassword(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userMapper.insert(adminUser);
            log.debug("管理员账号创建完成: id={}", adminUser.getId());
        }

        // 使用 BCrypt 安全比对密码（不做明文比较）
        if (!passwordEncoder.matches(request.getPassword(), adminUser.getPassword())) {
            log.warn("管理员登录失败: 密码错误, username={}", request.getUsername());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 更新最后登录时间
        adminUser.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(adminUser);

        String token = tokenProvider.generateToken(adminUser.getUsername());
        log.info("管理员登录成功: {}", adminUser.getUsername());
        return buildLoginResponse(adminUser, null, token);
    }

    /**
     * 普通玩家登录处理
     * 委托给 Spring Security AuthenticationManager 完成认证
     */
    @Transactional
    private LoginResponse handlePlayerLogin(LoginRequest request) {
        log.info("玩家登录: {}", request.getUsername());

        // Spring Security 统一认证（会抛出 BadCredentialsException）
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("玩家登录失败: 用户名或密码错误, username={}", request.getUsername());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 获取完整用户信息
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            // 理论上不应发生（认证通过则用户必存在），防御性处理
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 获取玩家档案
        PlayerProfile playerProfile = playerProfileMapper.selectByUserId(user.getId());
        if (playerProfile == null) {
            log.warn("用户 {} 没有玩家档案", user.getUsername());
        }

        // 更新最后登录时间
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成 JWT 令牌
        String token = tokenProvider.generateToken(user.getUsername());

        log.info("玩家登录成功: username={}", user.getUsername());
        return buildLoginResponse(user, playerProfile, token);
    }

    /**
     * 构建统一登录响应
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
}
