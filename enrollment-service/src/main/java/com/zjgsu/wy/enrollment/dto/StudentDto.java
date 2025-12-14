package com.zjgsu.wy.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生信息 DTO - 用于 Feign 调用 User Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {
    private Long id;
    private String studentId;
    private String name;
    private String email;
    private String major;
    private Integer grade;
}
