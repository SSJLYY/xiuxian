package com.xiuxian.game.common.security;

import com.xiuxian.game.modules.player.entity.User;
import com.xiuxian.game.modules.player.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 用户认证服务
 * Spring Security 认证提供者，将用户信息加载为安全上下文用户对象
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("根据用户名加载用户详情: username={}", username);

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            log.warn("用户名不存在: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        if ("LOCKED".equalsIgnoreCase(user.getStatus())) {
            throw new LockedException("Account is locked: " + username);
        }
        if ("BANNED".equalsIgnoreCase(user.getStatus())) {
            throw new DisabledException("Account is banned: " + username);
        }

        log.debug("用户加载成功: username={}, id={}", user.getUsername(), user.getId());

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + (user.getRole() == null ? "USER" : user.getRole()))
        );

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
