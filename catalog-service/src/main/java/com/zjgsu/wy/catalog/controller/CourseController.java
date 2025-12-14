package com.zjgsu.wy.catalog.controller;

import com.zjgsu.wy.catalog.common.ApiResponse;
import com.zjgsu.wy.catalog.model.Course;
import com.zjgsu.wy.catalog.service.CourseService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程控制器
 */
@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
@Slf4j
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    @Value("${server.port}")
    private String serverPort;

    /**
     * 健康检查接口,返回服务端口号用于负载均衡测试
     * GET /api/courses/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "catalog-service");
        health.put("port", serverPort);
        health.put("timestamp", System.currentTimeMillis());
        log.info("[catalog-service:{}] Health check 请求", serverPort);
        return ResponseEntity.ok(health);
    }

    /**
     * 查询所有课程
     * GET /api/courses
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Course>>> getAllCourses() {
        log.info("[catalog-service:{}] 查询所有课程", serverPort);
        List<Course> courses = courseService.findAll();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    /**
     * 根据ID查询课程
     * GET /api/courses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> getCourseById(@PathVariable String id) {
        log.info("[catalog-service:{}] 查询课程 ID: {}", serverPort, id);
        Course course = courseService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    /**
     * 根据课程代码查询课程
     * GET /api/courses/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<Course>> getCourseByCode(@PathVariable String code) {
        log.info("[catalog-service:{}] 查询课程代码: {}", serverPort, code);
        Course course = courseService.findByCode(code);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    /**
     * 创建课程
     * POST /api/courses
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Course>> createCourse(@Valid @RequestBody Course course) {
        log.info("[catalog-service:{}] 创建课程: {}", serverPort, course.getName());
        Course createdCourse = courseService.create(course);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("课程创建成功", createdCourse));
    }

    /**
     * 更新课程
     * PUT /api/courses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> updateCourse(
            @PathVariable String id, 
            @RequestBody Map<String, Object> updateData) {
        log.info("[catalog-service:{}] 更新课程 ID: {}", serverPort, id);
        // 支持部分更新（用于更新enrolled字段）
        if (updateData.containsKey("enrolled") && updateData.size() == 1) {
            Integer enrolled = (Integer) updateData.get("enrolled");
            Course updatedCourse = courseService.partialUpdate(id, enrolled);
            return ResponseEntity.ok(ApiResponse.success("课程更新成功", updatedCourse));
        }
        
        // 完整更新
        Course course = new Course();
        if (updateData.containsKey("code")) {
            course.setCode((String) updateData.get("code"));
        }
        if (updateData.containsKey("title")) {
            course.setTitle((String) updateData.get("title"));
        }
        if (updateData.containsKey("capacity")) {
            course.setCapacity((Integer) updateData.get("capacity"));
        }
        if (updateData.containsKey("enrolled")) {
            course.setEnrolled((Integer) updateData.get("enrolled"));
        }
        
        Course updatedCourse = courseService.update(id, course);
        return ResponseEntity.ok(ApiResponse.success("课程更新成功", updatedCourse));
    }

    /**
     * 删除课程
     * DELETE /api/courses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteCourse(@PathVariable String id) {
        courseService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("课程删除成功", null));
    }
}
