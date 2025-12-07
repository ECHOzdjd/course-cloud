package com.zjgsu.wy.enrollment.service;

import com.zjgsu.wy.enrollment.exception.BusinessException;
import com.zjgsu.wy.enrollment.exception.ResourceNotFoundException;
import com.zjgsu.wy.enrollment.model.Enrollment;
import com.zjgsu.wy.enrollment.model.EnrollmentStatus;
import com.zjgsu.wy.enrollment.model.Student;
import com.zjgsu.wy.enrollment.repository.EnrollmentRepository;
import com.zjgsu.wy.enrollment.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 选课服务层
 * 实现学生选课管理,通过服务发现调用课程目录服务验证课程
 */
@Service
@Transactional(readOnly = true)
public class EnrollmentService {
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    // 使用服务名而非硬编码URL
    private static final String CATALOG_SERVICE_NAME = "http://catalog-service";

    /**
     * 查询所有选课记录
     */
    public List<Enrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    /**
     * 根据ID查询选课记录
     */
    public Enrollment findById(String id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));
    }

    /**
     * 学生选课
     * 通过服务发现调用课程目录服务验证课程存在性
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public Enrollment enroll(String courseId, String studentId) {
        // 1. 验证学生是否存在
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        
        // 2. 调用课程目录服务验证课程是否存在(使用服务名)
        String url = CATALOG_SERVICE_NAME + "/api/courses/" + courseId;
        Map<String, Object> courseResponse;
        try {
            courseResponse = restTemplate.getForObject(url, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Course", courseId);
        } catch (Exception e) {
            throw new BusinessException("无法连接课程目录服务: " + e.getMessage());
        }
        
        if (courseResponse == null) {
            throw new ResourceNotFoundException("Course", courseId);
        }
        
        // 3. 从响应中提取课程信息
        Map<String, Object> courseData = (Map<String, Object>) courseResponse.get("data");
        if (courseData == null) {
            throw new ResourceNotFoundException("Course", courseId);
        }
        
        Integer capacity = (Integer) courseData.get("capacity");
        Integer enrolled = (Integer) courseData.get("enrolled");
        
        // 4. 检查课程容量
        if (enrolled >= capacity) {
            throw new BusinessException("Course is full");
        }
        
        // 5. 检查重复选课
        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new BusinessException("Already enrolled in this course");
        }
        
        // 6. 创建选课记录
        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(LocalDateTime.now());
        
        Enrollment saved = enrollmentRepository.save(enrollment);
        
        // 7. 更新课程的已选人数（调用catalog-service）
        updateCourseEnrolledCount(courseId, enrolled + 1);
        
        return saved;
    }
    
    /**
     * 更新课程的已选人数
     */
    private void updateCourseEnrolledCount(String courseId, int newCount) {
        String url = CATALOG_SERVICE_NAME + "/api/courses/" + courseId;
        Map<String, Object> updateData = Map.of("enrolled", newCount);
        try {
            restTemplate.put(url, updateData);
        } catch (Exception e) {
            // 记录日志但不影响主流程
            System.err.println("Failed to update course enrolled count: " + e.getMessage());
        }
    }

    /**
     * 学生退课
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public void unenroll(String id) {
        Enrollment enrollment = findById(id);
        
        // 检查选课记录状态
        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new BusinessException("该选课记录状态不是活跃状态，无法退课");
        }
        
        String courseId = enrollment.getCourseId();
        
        // 更新选课状态为已退课
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);
        
        // 获取当前课程的已选人数并减1
        try {
            String url = CATALOG_SERVICE_NAME + "/api/courses/" + courseId;
            Map<String, Object> courseResponse = restTemplate.getForObject(url, Map.class);
            if (courseResponse != null) {
                Map<String, Object> courseData = (Map<String, Object>) courseResponse.get("data");
                if (courseData != null) {
                    Integer enrolled = (Integer) courseData.get("enrolled");
                    if (enrolled != null && enrolled > 0) {
                        updateCourseEnrolledCount(courseId, enrolled - 1);
                    }
                }
            }
        } catch (Exception e) {
            // 记录日志但不影响主流程
            System.err.println("Failed to update course enrolled count: " + e.getMessage());
        }
    }

    /**
     * 根据课程ID查询选课记录
     */
    public List<Enrollment> findByCourseId(String courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    /**
     * 根据学生ID查询选课记录
     */
    public List<Enrollment> findByStudentId(String studentId) {
        // 验证学生是否存在
        studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "studentId: " + studentId));
        
        return enrollmentRepository.findByStudentId(studentId);
    }
    
    /**
     * 根据课程ID和状态查询选课记录
     */
    public List<Enrollment> findByCourseIdAndStatus(String courseId, EnrollmentStatus status) {
        return enrollmentRepository.findByCourseIdAndStatus(courseId, status);
    }
    
    /**
     * 根据学生ID和状态查询选课记录
     */
    public List<Enrollment> findByStudentIdAndStatus(String studentId, EnrollmentStatus status) {
        return enrollmentRepository.findByStudentIdAndStatus(studentId, status);
    }
}
