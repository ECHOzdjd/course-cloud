package com.zjgsu.wy.enrollment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学生实体类
 */
@Entity
@Table(name = "students", 
    indexes = {
        @Index(name = "idx_student_id", columnList = "student_id", unique = true),
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_major", columnList = "major"),
        @Index(name = "idx_grade", columnList = "grade")
    }
)
public class Student {
    @Id
    @JsonProperty("id")
    private String id;
    
    @Column(name = "student_id", nullable = false, unique = true, length = 50)
    @JsonProperty("studentId")
    @NotBlank(message = "学号不能为空")
    private String studentId;
    
    @Column(nullable = false, length = 100)
    @JsonProperty("name")
    @NotBlank(message = "姓名不能为空")
    private String name;
    
    @Column(nullable = false, length = 100)
    @JsonProperty("major")
    @NotBlank(message = "专业不能为空")
    private String major;
    
    @Column(nullable = false)
    @JsonProperty("grade")
    @NotNull(message = "年级不能为空")
    private Integer grade;
    
    @Column(nullable = false, unique = true, length = 100)
    @JsonProperty("email")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    // 默认构造函数
    public Student() {
        this.id = UUID.randomUUID().toString();
    }

    // 全参构造函数
    public Student(String studentId, String name, String major, Integer grade, String email) {
        this.id = UUID.randomUUID().toString();
        this.studentId = studentId;
        this.name = name;
        this.major = major;
        this.grade = grade;
        this.email = email;
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getter和Setter方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", major='" + major + '\'' +
                ", grade=" + grade +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
