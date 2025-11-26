package com.zjgsu.wy.enrollment.repository;

import com.zjgsu.wy.enrollment.model.Enrollment;
import com.zjgsu.wy.enrollment.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 选课记录数据访问层
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {
    
    /**
     * 根据课程ID查询选课记录
     */
    List<Enrollment> findByCourseId(String courseId);
    
    /**
     * 根据学生ID查询选课记录
     */
    List<Enrollment> findByStudentId(String studentId);
    
    /**
     * 根据课程ID和状态查询选课记录
     */
    List<Enrollment> findByCourseIdAndStatus(String courseId, EnrollmentStatus status);
    
    /**
     * 根据学生ID和状态查询选课记录
     */
    List<Enrollment> findByStudentIdAndStatus(String studentId, EnrollmentStatus status);
    
    /**
     * 根据课程ID、学生ID和状态查询选课记录
     */
    Optional<Enrollment> findByCourseIdAndStudentIdAndStatus(String courseId, String studentId, EnrollmentStatus status);
    
    /**
     * 检查学生是否已选某门课程（仅活跃状态）
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e WHERE e.courseId = :courseId AND e.studentId = :studentId AND e.status = 'ACTIVE'")
    boolean existsByCourseIdAndStudentId(@Param("courseId") String courseId, @Param("studentId") String studentId);
    
    /**
     * 统计某门课程的活跃选课人数
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.courseId = :courseId AND e.status = 'ACTIVE'")
    long countByCourseId(@Param("courseId") String courseId);
    
    /**
     * 统计某学生的活跃选课数量
     */
    long countByStudentIdAndStatus(String studentId, EnrollmentStatus status);
}
