package com.zjgsu.wy.enrollment.exception;

/**
 * 服务不可用异常 - 用于 Feign Fallback
 */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
    
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
