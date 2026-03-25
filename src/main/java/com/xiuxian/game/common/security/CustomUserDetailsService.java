package com.xiuxian.game.common.security;

import com.xiuxian.game.modules.player.entity.User;
import com.xiuxian.game.modules.player.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 自定义用户详情服�?
 * Spring Security 在认证时调用此服务加载用户信�?
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("加载用户信息: username={}", username);

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            log.warn("用户不存�? {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
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

