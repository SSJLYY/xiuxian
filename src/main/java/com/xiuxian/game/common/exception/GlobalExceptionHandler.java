package com.xiuxian.game.common.exception;

import com.xiuxian.game.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * <p>统一处理所有控制器抛出的异常，包括：</p>
 * <ol>
 *   <li>BusinessException - 业务异常，直接返回错误码和错误信息</li>
 *   <li>MethodArgumentNotValidException - 参数校验失败</li>
 *   <li>IllegalArgumentException - 非法参数异常</li>
 *   <li>BadCredentialsException - 认证失败</li>
 *   <li>AccessDeniedException - 权限不足</li>
 *   <li>Exception - 其他未知异常，统一返回系统错误</li>
 * </ol>
 *
 * <p>注意：不要在 RuntimeException 子类（如 NullPointerException）上加 @ExceptionHandler，
 * 这类异常应该在代码中修复，而不是通过全局处理器掩盖。只有 Exception 兜底处理。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * BusinessException 直接返回 HTTP 200 + 业务错误码，前端根据 code 判断是否成功
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.ok()
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * 处理参数校验失败异常（@Valid 校验失败）
     * 收集所有字段错误，返回 Map 格式的错误信息，方便前端定位具体字段
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("参数校验失败: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("参数校验失败", errors));
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("非法参数异常: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 处理认证失败异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("认证失败: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("用户名或密码错误"));
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("权限不足: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("权限不足，无法执行该操作"));
    }

    /**
     * 兜底处理所有未捕获的异常
     *
     * <p>捕获 RuntimeException（如 NullPointerException）等未预期异常，
     * 避免将堆栈信息暴露给客户端，同时记录完整日志便于排查问题。</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {
        log.error("未知系统异常: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系统繁忙，请稍后重试"));
    }
}
