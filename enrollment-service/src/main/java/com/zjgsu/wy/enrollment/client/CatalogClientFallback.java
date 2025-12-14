package com.zjgsu.wy.enrollment.client;

import com.zjgsu.wy.enrollment.common.ApiResponse;
import com.zjgsu.wy.enrollment.dto.CourseDto;
import com.zjgsu.wy.enrollment.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Catalog Service Feign 客户端降级处理
 */
@Component
@Slf4j
public class CatalogClientFallback implements CatalogClient {
    
    @Override
    public ApiResponse<CourseDto> getCourse(Long id) {
        log.warn("CatalogClient fallback triggered for course: {}", id);
        throw new ServiceUnavailableException("课程目录服务暂时不可用，请稍后再试");
    }
}
