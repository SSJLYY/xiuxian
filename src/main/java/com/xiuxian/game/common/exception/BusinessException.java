package com.xiuxian.game.common.exception;

/**
 * 业务异常�?
 * 用于封装业务逻辑中的异常情况，包含错误码和错误消�?
 */
public class BusinessException extends RuntimeException {
    
    private final int code;
    private final String message;
    
    /**
     * 使用ErrorCode枚举构造异�?
     * @param errorCode 错误码枚�?
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
    
    /**
     * 使用ErrorCode枚举和自定义消息构造异�?
     * @param errorCode 错误码枚�?
     * @param customMessage 自定义错误消�?
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
        this.message = customMessage;
    }
    
    /**
     * 使用错误码和消息构造异�?
     * @param code 错误�?
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    /**
     * 使用消息构造异常，默认使用系统错误�?
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.SYSTEM_ERROR.getCode();
        this.message = message;
    }
    
    /**
     * 使用ErrorCode枚举和原始异常构造异�?
     * @param errorCode 错误码枚�?
     * @param cause 原始异常
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
    
    /**
     * 获取错误�?
     * @return 错误�?
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 获取错误消息
     * @return 错误消息
     */
    @Override
    public String getMessage() {
        return message;
    }
}

