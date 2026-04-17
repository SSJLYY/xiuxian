package com.xiuxian.game.integration;

import com.xiuxian.game.XiuxianGameApplication;
import com.xiuxian.game.dto.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;

/**
 * 用户认证集成测试
 * 测试完整的登录注册流程
 */
@SpringBootTest(
    classes = XiuxianGameApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@DisplayName("用户认证集成测试")
class AuthenticationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("健康检查 - 服务已启动")
    void healthCheck() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            getUrl("/actuator/health"),
            String.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("用户注册 - 成功")
    void registerUser_Success() {
        // Given
        String registerUrl = getUrl("/api/auth/register");
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("testpass123");
        request.setEmail("test@example.com");
        request.setNickname("测试玩家");

        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            registerUrl,
            request,
            ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void loginUser_Success() {
        // Given
        String loginUrl = getUrl("/api/auth/login");
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("testpass123");

        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            loginUrl,
            request,
            ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void loginUser_WrongPassword() {
        // Given
        String loginUrl = getUrl("/api/auth/login");
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            loginUrl,
            request,
            ApiResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    private String getUrl(String path) {
        return "http://localhost:" + port + path;
    }

    // 请求 DTO 类
    static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String nickname;
        
        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
    }

    static class LoginRequest {
        private String username;
        private String password;
        
        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
