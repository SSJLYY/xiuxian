package com.xiuxian.game.common.exception;

/**
 * 业务异常类
 * 用于抛出业务逻辑异常，携带错误码和错误信息，便于统一处理和返回给客户端
 */
public class BusinessException extends RuntimeException {

    private final int code;
    private final String message;

    /**
     * 使用ErrorCode构造业务异常
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    /**
     * 使用ErrorCode构造业务异常，并自定义消息
     * @param errorCode 错误码枚举
     * @param customMessage 自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
        this.message = customMessage;
    }

    /**
     * 使用错误码和消息构造业务异常
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 使用消息构造业务异常，使用默认系统错误码
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.SYSTEM_ERROR.getCode();
        this.message = message;
    }

    /**
     * 使用ErrorCode构造业务异常，并携带原始异常
     * @param errorCode 错误码枚举
     * @param cause 原始异常
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    /**
     * 获取错误码
     * @return 错误码
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
