package com.zjgsu.wy.catalog.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 课程实体类
 */
@Entity
@Table(name = "courses", indexes = {
    @Index(name = "idx_course_code", columnList = "code", unique = true)
})
public class Course {
    @Id
    @JsonProperty("id")
    private String id;
    
    @Column(nullable = false, unique = true, length = 50)
    @JsonProperty("code")
    private String code;
    
    @Column(nullable = false, length = 200)
    @JsonProperty("title")
    private String title;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "instructor_id")),
        @AttributeOverride(name = "name", column = @Column(name = "instructor_name")),
        @AttributeOverride(name = "email", column = @Column(name = "instructor_email"))
    })
    @JsonProperty("instructor")
    private Instructor instructor;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "dayOfWeek", column = @Column(name = "schedule_day_of_week")),
        @AttributeOverride(name = "startTime", column = @Column(name = "schedule_start_time")),
        @AttributeOverride(name = "endTime", column = @Column(name = "schedule_end_time")),
        @AttributeOverride(name = "expectedAttendance", column = @Column(name = "schedule_expected_attendance"))
    })
    @JsonProperty("schedule")
    private ScheduleSlot schedule;
    
    @Column(nullable = false)
    @JsonProperty("capacity")
    private Integer capacity;
    
    @Column(nullable = false)
    @JsonProperty("enrolled")
    private Integer enrolled = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    // 默认构造函数
    public Course() {
        this.id = UUID.randomUUID().toString();
        this.enrolled = 0;
    }

    // 全参构造函数
    public Course(String code, String title, Instructor instructor, ScheduleSlot schedule, Integer capacity) {
        this.id = UUID.randomUUID().toString();
        this.code = code;
        this.title = title;
        this.instructor = instructor;
        this.schedule = schedule;
        this.capacity = capacity;
        this.enrolled = 0;
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.enrolled == null) {
            this.enrolled = 0;
        }
    }

    // Getter和Setter方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public ScheduleSlot getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleSlot schedule) {
        this.schedule = schedule;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(Integer enrolled) {
        this.enrolled = enrolled;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", instructor=" + instructor +
                ", schedule=" + schedule +
                ", capacity=" + capacity +
                ", enrolled=" + enrolled +
                ", createdAt=" + createdAt +
                '}';
    }
}
