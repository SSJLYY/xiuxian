package com.xiuxian.game.security;

import com.xiuxian.game.entity.User;
import com.xiuxian.game.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== DEBUG: CustomUserDetailsService ===");
        System.out.println("尝试查找用户名: " + username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("用户不存在: " + username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
        
        System.out.println("找到用户: " + user.getUsername() + ", ID: " + user.getId());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
        );
    }
}
