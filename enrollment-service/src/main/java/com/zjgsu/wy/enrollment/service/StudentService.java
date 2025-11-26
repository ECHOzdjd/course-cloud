package com.zjgsu.wy.enrollment.service;

import com.zjgsu.wy.enrollment.exception.BusinessException;
import com.zjgsu.wy.enrollment.exception.ResourceNotFoundException;
import com.zjgsu.wy.enrollment.model.EnrollmentStatus;
import com.zjgsu.wy.enrollment.model.Student;
import com.zjgsu.wy.enrollment.repository.EnrollmentRepository;
import com.zjgsu.wy.enrollment.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学生服务层
 */
@Service
@Transactional(readOnly = true)
public class StudentService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    /**
     * 查询所有学生
     */
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    /**
     * 根据ID查询学生
     */
    public Student findById(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
    }

    /**
     * 创建学生
     */
    @Transactional
    public Student create(Student student) {
        // 检查学号是否已存在
        if (studentRepository.existsByStudentId(student.getStudentId())) {
            throw new BusinessException("学号已存在: " + student.getStudentId());
        }
        
        // 检查邮箱是否已存在
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new BusinessException("邮箱已存在: " + student.getEmail());
        }
        
        return studentRepository.save(student);
    }

    /**
     * 更新学生信息
     */
    @Transactional
    public Student update(String id, Student student) {
        Student existingStudent = findById(id);
        
        // 如果学号发生变化，检查新学号是否已存在
        if (!existingStudent.getStudentId().equals(student.getStudentId())) {
            if (studentRepository.existsByStudentId(student.getStudentId())) {
                throw new BusinessException("学号已存在: " + student.getStudentId());
            }
        }
        
        // 如果邮箱发生变化，检查新邮箱是否已存在
        if (!existingStudent.getEmail().equals(student.getEmail())) {
            if (studentRepository.existsByEmail(student.getEmail())) {
                throw new BusinessException("邮箱已存在: " + student.getEmail());
            }
        }
        
        // 保留原有的ID和创建时间
        student.setId(id);
        student.setCreatedAt(existingStudent.getCreatedAt());
        return studentRepository.save(student);
    }

    /**
     * 删除学生
     */
    @Transactional
    public void deleteById(String id) {
        Student student = findById(id);
        
        // 检查是否有活跃的选课记录
        long activeEnrollmentCount = enrollmentRepository.countByStudentIdAndStatus(
                student.getStudentId(), EnrollmentStatus.ACTIVE);
        
        if (activeEnrollmentCount > 0) {
            throw new BusinessException("无法删除：该学生存在活跃的选课记录");
        }
        
        studentRepository.deleteById(id);
    }

    /**
     * 根据学号查询学生
     */
    public Student findByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "studentId: " + studentId));
    }

    /**
     * 检查学生是否存在
     */
    public boolean existsById(String id) {
        return studentRepository.existsById(id);
    }
    
    /**
     * 根据专业查询学生列表
     */
    public List<Student> findByMajor(String major) {
        return studentRepository.findByMajor(major);
    }
    
    /**
     * 根据年级查询学生列表
     */
    public List<Student> findByGrade(Integer grade) {
        return studentRepository.findByGrade(grade);
    }
}
