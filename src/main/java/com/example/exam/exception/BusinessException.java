package com.example.exam.exception;

/**
 * 业务逻辑异常类
 * 用于表示业务逻辑错误，如数据验证失败、业务规则冲突等
 */
public class BusinessException extends RuntimeException {

    private Integer code;
    private String message;

    /**
     * 默认构造函数
     */
    public BusinessException() {
        super();
        this.code = 500;
    }

    /**
     * 指定错误消息的构造函数
     * 
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    /**
     * 指定错误码和错误消息的构造函数
     * 
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 指定错误消息和原因的构造函数
     * 
     * @param message 错误消息
     * @param cause 原因
     */
    public BusinessException(String message, Throwable cause) {
         super(message, cause);
         this.code = 500;
         this.message = message;
     }

    /**
     * 指定错误码、错误消息和原因的构造函数
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param cause 原因
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 获取错误码
     * 
     * @return 错误码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 设置错误码
     * 
     * @param code 错误码
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * 获取错误消息
     * 
     * @return 错误消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置错误消息
     * 
     * @param message 错误消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

}