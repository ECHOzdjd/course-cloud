package com.zjgsu.wy.enrollment.service;

import com.zjgsu.wy.enrollment.client.CatalogClient;
import com.zjgsu.wy.enrollment.common.ApiResponse;
import com.zjgsu.wy.enrollment.dto.CourseDto;
import com.zjgsu.wy.enrollment.exception.BusinessException;
import com.zjgsu.wy.enrollment.exception.ResourceNotFoundException;
import com.zjgsu.wy.enrollment.model.Enrollment;
import com.zjgsu.wy.enrollment.model.EnrollmentStatus;
import com.zjgsu.wy.enrollment.model.Student;
import com.zjgsu.wy.enrollment.repository.EnrollmentRepository;
import com.zjgsu.wy.enrollment.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 选课服务层
 * 使用 OpenFeign 调用课程目录服务验证课程
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class EnrollmentService {
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private CatalogClient catalogClient;

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
     * 使用 OpenFeign 调用课程目录服务验证课程存在性
     */
    @Transactional
    public Enrollment enroll(String courseId, String studentId) {
        log.info("开始选课流程 - courseId: {}, studentId: {}", courseId, studentId);
        
        // 1. 验证学生是否存在
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        log.debug("学生验证通过: {}", studentId);
        
        // 2. 使用 Feign 调用课程目录服务验证课程是否存在
        Long courseIdLong;
        try {
            courseIdLong = Long.parseLong(courseId);
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid course ID format");
        }
        
        ApiResponse<CourseDto> courseResponse;
        try {
            log.debug("调用 Catalog Service 获取课程信息: {}", courseId);
            courseResponse = catalogClient.getCourse(courseIdLong);
        } catch (Exception e) {
            log.error("调用课程目录服务失败: {}", e.getMessage(), e);
            throw new BusinessException("无法连接课程目录服务: " + e.getMessage());
        }
        
        if (courseResponse == null || courseResponse.getData() == null) {
            throw new ResourceNotFoundException("Course", courseId);
        }
        
        CourseDto course = courseResponse.getData();
        log.debug("课程信息获取成功: {} - {}", course.getCourseId(), course.getName());
        
        // 3. 检查课程容量
        if (course.getEnrolled() >= course.getCapacity()) {
            throw new BusinessException("Course is full");
        }
        
        // 4. 检查重复选课
        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new BusinessException("Already enrolled in this course");
        }
        
        // 5. 创建选课记录
        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(LocalDateTime.now());
        
        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("选课成功 - enrollmentId: {}, courseId: {}, studentId: {}", 
                saved.getId(), courseId, studentId);
        
        return saved;
    }

    /**
     * 学生退课
     */
    @Transactional
    public void unenroll(String id) {
        log.info("开始退课流程 - enrollmentId: {}", id);
        
        Enrollment enrollment = findById(id);
        
        // 检查选课记录状态
        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new BusinessException("该选课记录状态不是活跃状态，无法退课");
        }
        
        // 更新选课状态为已退课
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);
        
        log.info("退课成功 - enrollmentId: {}, courseId: {}, studentId: {}", 
                id, enrollment.getCourseId(), enrollment.getStudentId());
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
