package com.zjgsu.wy.catalog.service;

import com.zjgsu.wy.catalog.exception.BusinessException;
import com.zjgsu.wy.catalog.exception.ResourceNotFoundException;
import com.zjgsu.wy.catalog.model.Course;
import com.zjgsu.wy.catalog.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 课程服务层
 */
@Service
@Transactional(readOnly = true)
public class CourseService {
    
    @Autowired
    private CourseRepository courseRepository;

    /**
     * 查询所有课程
     */
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    /**
     * 根据ID查询课程
     */
    public Course findById(String id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    /**
     * 根据课程代码查询课程
     */
    public Course findByCode(String code) {
        return courseRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "code: " + code));
    }

    /**
     * 创建课程
     */
    @Transactional
    public Course create(Course course) {
        // 检查课程代码是否已存在
        if (courseRepository.existsByCode(course.getCode())) {
            throw new BusinessException("课程代码已存在: " + course.getCode());
        }
        return courseRepository.save(course);
    }

    /**
     * 更新课程
     */
    @Transactional
    public Course update(String id, Course course) {
        Course existingCourse = findById(id);
        
        // 如果课程代码发生变化，检查新代码是否已存在
        if (!existingCourse.getCode().equals(course.getCode())) {
            if (courseRepository.existsByCode(course.getCode())) {
                throw new BusinessException("课程代码已存在: " + course.getCode());
            }
        }
        
        // 保留原有的ID和创建时间
        course.setId(id);
        course.setCreatedAt(existingCourse.getCreatedAt());
        
        // 如果enrolled没有传入，保留原有值
        if (course.getEnrolled() == null) {
            course.setEnrolled(existingCourse.getEnrolled());
        }
        
        return courseRepository.save(course);
    }

    /**
     * 部分更新课程（用于更新enrolled字段）
     */
    @Transactional
    public Course partialUpdate(String id, Integer enrolled) {
        Course existingCourse = findById(id);
        if (enrolled != null) {
            existingCourse.setEnrolled(enrolled);
        }
        return courseRepository.save(existingCourse);
    }

    /**
     * 删除课程
     */
    @Transactional
    public void deleteById(String id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course", id);
        }
        courseRepository.deleteById(id);
    }

    /**
     * 检查课程是否存在
     */
    public boolean existsById(String id) {
        return courseRepository.existsById(id);
    }
    
    /**
     * 根据标题关键字查询课程
     */
    public List<Course> searchByTitle(String keyword) {
        return courseRepository.findByTitleContainingIgnoreCase(keyword);
    }
    
    /**
     * 查询有剩余容量的课程
     */
    public List<Course> findAvailableCourses() {
        return courseRepository.findCoursesWithAvailableCapacity();
    }
}
