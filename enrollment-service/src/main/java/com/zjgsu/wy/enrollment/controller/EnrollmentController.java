package com.zjgsu.wy.enrollment.controller;

import com.zjgsu.wy.enrollment.common.ApiResponse;
import com.zjgsu.wy.enrollment.model.Enrollment;
import com.zjgsu.wy.enrollment.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选课控制器
 */
@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "*")
public class EnrollmentController {
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${server.port}")
    private String serverPort;

    /**
     * 测试接口,通过服务发现调用 catalog-service
     * GET /api/enrollments/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testServiceDiscovery() {
        Map<String, Object> result = new HashMap<>();
        result.put("enrollment-service-port", serverPort);
        
        try {
            // 通过服务名调用 catalog-service
            String url = "http://catalog-service/api/courses/health";
            Map<String, Object> catalogHealth = restTemplate.getForObject(url, Map.class);
            result.put("catalog-service-response", catalogHealth);
            result.put("status", "SUCCESS");
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 查询所有选课记录
     * GET /api/enrollments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Enrollment>>> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentService.findAll();
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    /**
     * 根据ID查询选课记录
     * GET /api/enrollments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Enrollment>> getEnrollmentById(@PathVariable String id) {
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
        
        if (courseId == null || studentId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("courseId和studentId不能为空"));
        }
        
        Enrollment enrollment = enrollmentService.enroll(courseId, studentId);
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
