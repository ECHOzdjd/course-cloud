package com.zjgsu.wy.enrollment.client;

import com.zjgsu.wy.enrollment.dto.StudentDto;
import com.zjgsu.wy.enrollment.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * User Service Feign 客户端降级处理
 */
@Component
@Slf4j
public class UserClientFallback implements UserClient {
    
    @Override
    public StudentDto getStudent(Long id) {
        log.warn("UserClient fallback triggered for student: {}", id);
        throw new ServiceUnavailableException("用户服务暂时不可用，请稍后再试");
    }
}
