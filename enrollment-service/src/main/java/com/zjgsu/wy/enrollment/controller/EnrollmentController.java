package com.zjgsu.wy.enrollment.controller;

import com.zjgsu.wy.enrollment.common.ApiResponse;
import com.zjgsu.wy.enrollment.model.Enrollment;
import com.zjgsu.wy.enrollment.service.EnrollmentService;
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
 * 选课控制器
 */
@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "*")
@Slf4j
public class EnrollmentController {
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Value("${server.port}")
    private String serverPort;

    /**
     * 健康检查接口
     * GET /api/enrollments/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "enrollment-service");
        health.put("port", serverPort);
        health.put("timestamp", System.currentTimeMillis());
        log.info("[enrollment-service:{}] Health check 请求", serverPort);
        return ResponseEntity.ok(health);
    }

    /**
     * 查询所有选课记录
     * GET /api/enrollments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Enrollment>>> getAllEnrollments() {
        log.info("[enrollment-service:{}] 查询所有选课记录", serverPort);
        List<Enrollment> enrollments = enrollmentService.findAll();
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    /**
     * 根据ID查询选课记录
     * GET /api/enrollments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Enrollment>> getEnrollmentById(@PathVariable String id) {
        log.info("[enrollment-service:{}] 查询选课记录 ID: {}", serverPort, id);
        Enrollment enrollment = enrollmentService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(enrollment));
    }

    /**
     * 学生选课
     * POST /api/enrollments
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Enrollment>> enroll(@RequestBody Map<String, String> request) {
        String courseId = request.get("courseId");
        String studentId = request.get("studentId");
        
        log.info("[enrollment-service:{}] 选课请求 - courseId: {}, studentId: {}", 
                serverPort, courseId, studentId);
        
        if (courseId == null || studentId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("courseId和studentId不能为空"));
        }
        
        Enrollment enrollment = enrollmentService.enroll(courseId, studentId);
        log.info("[enrollment-service:{}] 选课成功 - enrollmentId: {}", serverPort, enrollment.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("选课成功", enrollment));
    }

    /**
     * 学生退课
     * DELETE /api/enrollments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> unenroll(@PathVariable String id) {
        enrollmentService.unenroll(id);
        return ResponseEntity.ok(ApiResponse.success("退课成功", null));
    }

    /**
     * 根据课程ID查询选课记录
     * GET /api/enrollments/course/{courseId}
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<Enrollment>>> getEnrollmentsByCourse(@PathVariable String courseId) {
        List<Enrollment> enrollments = enrollmentService.findByCourseId(courseId);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    /**
     * 根据学生ID查询选课记录
     * GET /api/enrollments/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<Enrollment>>> getEnrollmentsByStudent(@PathVariable String studentId) {
        List<Enrollment> enrollments = enrollmentService.findByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }
}
