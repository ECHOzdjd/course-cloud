package com.zjgsu.wy.enrollment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 选课记录实体类
 */
@Entity
@Table(name = "enrollments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_course_student", columnNames = {"course_id", "student_id"})
    },
    indexes = {
        @Index(name = "idx_course_id", columnList = "course_id"),
        @Index(name = "idx_student_id", columnList = "student_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_enrolled_at", columnList = "enrolled_at")
    }
)
public class Enrollment {
    @Id
    @JsonProperty("id")
    private String id;
    
    @Column(name = "course_id", nullable = false)
    @JsonProperty("courseId")
    private String courseId;
    
    @Column(name = "student_id", nullable = false)
    @JsonProperty("studentId")
    private String studentId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @JsonProperty("status")
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;
    
    @Column(name = "enrolled_at", nullable = false, updatable = false)
    @JsonProperty("enrolledAt")
    private LocalDateTime enrolledAt;

    // 默认构造函数
    public Enrollment() {
        this.id = UUID.randomUUID().toString();
        this.status = EnrollmentStatus.ACTIVE;
    }

    // 全参构造函数
    public Enrollment(String courseId, String studentId) {
        this.id = UUID.randomUUID().toString();
        this.courseId = courseId;
        this.studentId = studentId;
        this.status = EnrollmentStatus.ACTIVE;
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.enrolledAt == null) {
            this.enrolledAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = EnrollmentStatus.ACTIVE;
        }
    }

    // Getter和Setter方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public EnrollmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id='" + id + '\'' +
                ", courseId='" + courseId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", status=" + status +
                ", enrolledAt=" + enrolledAt +
                '}';
    }
}
