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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final QuestService questService;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被使用");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .build();

        user = userRepository.save(user);

        PlayerProfile profile = PlayerProfile.builder()
                .user(user)
                .nickname(request.getNickname())
                .build();

        profile = playerProfileRepository.save(profile);

        // 为新玩家初始化任务
        questService.initializePlayerQuests(profile);

        System.out.println("=== DEBUG: 注册时生成Token ===");
        System.out.println("用户ID: " + user.getId());
        System.out.println("用户名: " + user.getUsername());
        System.out.println("传递给generateToken的参数: " + user.getUsername());

        String token = tokenProvider.generateToken(user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .user(LoginResponse.UserDto.builder()
                        .id(user.getId().longValue())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        System.out.println("=== DEBUG: 登录时生成Token ===");
        System.out.println("用户ID: " + user.getId());
        System.out.println("用户名: " + user.getUsername());
        System.out.println("传递给generateToken的参数: " + user.getUsername());

        String token = tokenProvider.generateToken(user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .user(LoginResponse.UserDto.builder()
                        .id(user.getId().longValue())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .build();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}