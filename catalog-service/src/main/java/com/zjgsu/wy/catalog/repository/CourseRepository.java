package com.zjgsu.wy.catalog.repository;

import com.zjgsu.wy.catalog.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 课程数据访问层
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    
    /**
     * 根据课程代码查询课程
     */
    Optional<Course> findByCode(String code);
    
    /**
     * 根据讲师ID查询课程列表
     */
    @Query("SELECT c FROM Course c WHERE c.instructor.id = :instructorId")
    List<Course> findByInstructorId(@Param("instructorId") String instructorId);
    
    /**
     * 查询有剩余容量的课程
     */
    @Query("SELECT c FROM Course c WHERE c.enrolled < c.capacity")
    List<Course> findCoursesWithAvailableCapacity();
    
    /**
     * 根据标题关键字模糊查询
     */
    List<Course> findByTitleContainingIgnoreCase(String keyword);
    
    /**
     * 检查课程代码是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 统计讲师的课程数量
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.instructor.id = :instructorId")
    long countByInstructorId(@Param("instructorId") String instructorId);
}
