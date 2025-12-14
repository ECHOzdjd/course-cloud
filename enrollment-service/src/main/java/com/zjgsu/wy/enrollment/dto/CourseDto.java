package com.zjgsu.wy.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 课程信息 DTO - 用于 Feign 调用 Catalog Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String courseId;
    private String name;
    private String description;
    private Integer credits;
    private String department;
    private Integer capacity;
    private Integer enrolled;
    private List<InstructorDto> instructors;
    private List<ScheduleSlotDto> schedules;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorDto {
        private Long id;
        private String name;
        private String email;
        private String department;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleSlotDto {
        private Long id;
        private String dayOfWeek;
        private String startTime;
        private String endTime;
        private String location;
    }
}
