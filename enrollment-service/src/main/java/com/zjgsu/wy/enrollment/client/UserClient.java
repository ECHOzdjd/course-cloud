package com.zjgsu.wy.enrollment.client;

import com.zjgsu.wy.enrollment.dto.StudentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * User Service Feign 客户端
 * 注意：由于当前项目中没有独立的 user-service，这里定义的是接口示例
 * 实际使用时需要根据 user-service 的实际 API 调整
 */
@FeignClient(
    name = "user-service",
    fallback = UserClientFallback.class
)
public interface UserClient {
    
    /**
     * 根据 ID 获取学生信息
     * @param id 学生 ID
     * @return 学生信息
     */
    @GetMapping("/api/users/students/{id}")
    StudentDto getStudent(@PathVariable Long id);
}
