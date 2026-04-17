package com.xiuxian.game.modules.player.service;

import com.xiuxian.game.dto.request.LoginRequest;
import com.xiuxian.game.dto.request.RegisterRequest;
import com.xiuxian.game.dto.response.LoginResponse;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.entity.User;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.mapper.UserMapper;
import com.xiuxian.game.common.security.JwtTokenProvider;
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
    private String adminPasswordPlain;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info("开始注册流程：username={}, email={}", request.getUsername(), request.getEmail());

        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (userMapper.selectByEmail(request.getEmail()) != null) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (playerProfileMapper.selectByNickname(request.getNickname()) != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "昵称已被使用，请更换其他昵称");
        }

        if (!isValidPassword(request.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, 
                "密码强度不足：必须至少 8 位，包含字母和数字");
        }

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

        PlayerProfile playerProfile = playerService.createNewPlayer(user, request.getNickname());

        String token = tokenProvider.generateToken(user.getUsername());
        return buildLoginResponse(user, playerProfile, token);
    }
        if (userMapper.selectByEmail(request.getEmail()) != null) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

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

        PlayerProfile playerProfile = playerService.createNewPlayer(user, request.getNickname());

        String token = tokenProvider.generateToken(user.getUsername());
        return buildLoginResponse(user, playerProfile, token);
    }

    public LoginResponse login(LoginRequest request) {
        if ("admin".equals(request.getUserType())) {
            return handleAdminLogin(request);
        } else {
            return handlePlayerLogin(request);
        }
    }

    public User getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

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

    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.info("用户登出: {}", authentication.getName());
            SecurityContextHolder.clearContext();
        }
    }

    @Transactional
    private LoginResponse handleAdminLogin(LoginRequest request) {
        if (!adminUsername.equals(request.getUsername())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        User adminUser = userMapper.selectByUsername(request.getUsername());
        if (adminUser == null) {
            log.warn("管理员用户不存在，将使用配置文件中的 BCrypt 哈希值创建管理员用户");
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
        }

        if (!passwordEncoder.matches(request.getPassword(), adminUser.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        adminUser.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(adminUser);

        String token = tokenProvider.generateToken(adminUser.getUsername());
        return buildLoginResponse(adminUser, null, token);
    }

    @Transactional
    private LoginResponse handlePlayerLogin(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        PlayerProfile playerProfile = playerProfileMapper.selectByUserId(user.getId());

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        String token = tokenProvider.generateToken(user.getUsername());
        return buildLoginResponse(user, playerProfile, token);
    }

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
     * 验证密码强度
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        // 至少包含一个字母和一个数字
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        return hasLetter && hasDigit;
    }
}

