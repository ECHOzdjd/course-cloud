package com.zjgsu.wy.enrollment.repository;

import com.zjgsu.wy.enrollment.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学生数据访问层
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    
    /**
     * 根据学号查询学生
     */
    Optional<Student> findByStudentId(String studentId);
    
    /**
     * 根据邮箱查询学生
     */
    Optional<Student> findByEmail(String email);
    
    /**
     * 检查学号是否已存在
     */
    boolean existsByStudentId(String studentId);
    
    /**
     * 检查邮箱是否已存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据专业查询学生列表
     */
    List<Student> findByMajor(String major);
    
    /**
     * 根据年级查询学生列表
     */
    List<Student> findByGrade(Integer grade);
    
    /**
     * 根据专业和年级查询学生列表
     */
    List<Student> findByMajorAndGrade(String major, Integer grade);
}
