package com.zjgsu.wy.enrollment.exception;

/**
 * 资源未找到异常
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceType, String identifier) {
        super(resourceType + " not found with identifier: " + identifier);
    }
}
