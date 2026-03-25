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
 * 全局异常处理�?
 *
 * <p>异常处理优先级说明（从最具体到最通用）：</p>
 * <ol>
 *   <li>BusinessException - 业务异常，携带结构化错误�?/li>
 *   <li>MethodArgumentNotValidException - 参数校验失败</li>
 *   <li>IllegalArgumentException - 非法参数</li>
 *   <li>BadCredentialsException - 认证失败</li>
 *   <li>AccessDeniedException - 权限不足</li>
 *   <li>Exception - 所有未捕获异常的兜底处�?/li>
 * </ol>
 *
 * <p>注意：不要单独添�?RuntimeException 处理器，否则会拦截掉 BusinessException 等子类，
 * 导致错误码丢失。所有非业务异常统一�?Exception 兜底处理�?/p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常（最高优先级�?
     * BusinessException 携带结构化错误码，返�?200 + 业务错误码，便于前端区分
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.ok()
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * 处理参数校验异常（@Valid 校验失败�?
     * 收集所有字段错误并�?Map 形式返回，方便前端精确定�?
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
        log.warn("非法参数: {}", ex.getMessage());
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
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("访问被拒�? {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("没有权限访问该资�?));
    }

    /**
     * 兜底处理所有未被上面捕获的异常
     *
     * <p>包含 RuntimeException、NullPointerException 等所有未处理异常�?
     * 不对外暴露内部错误细节（安全考虑），仅记录完整堆栈到日志�?/p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {
        log.error("未处理的异常: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系统内部错误，请稍后重试"));
    }
}

