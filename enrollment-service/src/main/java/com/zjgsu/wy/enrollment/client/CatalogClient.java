package com.zjgsu.wy.enrollment.client;

import com.zjgsu.wy.enrollment.common.ApiResponse;
import com.zjgsu.wy.enrollment.dto.CourseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Catalog Service Feign 客户端
 * 调用课程目录服务获取课程信息
 */
@FeignClient(
    name = "catalog-service",
    fallback = CatalogClientFallback.class
)
public interface CatalogClient {
    
    /**
     * 根据课程 ID 获取课程信息
     * @param id 课程 ID
     * @return 课程信息响应
     */
    @GetMapping("/api/courses/{id}")
    ApiResponse<CourseDto> getCourse(@PathVariable Long id);
}
