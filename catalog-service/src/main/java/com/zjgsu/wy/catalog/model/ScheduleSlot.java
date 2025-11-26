package com.zjgsu.wy.catalog.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;

/**
 * 课程时间安排嵌入式对象
 */
@Embeddable
public class ScheduleSlot {
    @JsonProperty("dayOfWeek")
    private String dayOfWeek;
    
    @JsonProperty("startTime")
    private String startTime;
    
    @JsonProperty("endTime")
    private String endTime;
    
    @JsonProperty("expectedAttendance")
    private Integer expectedAttendance;

    // 默认构造函数
    public ScheduleSlot() {}

    // 全参构造函数
    public ScheduleSlot(String dayOfWeek, String startTime, String endTime, Integer expectedAttendance) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.expectedAttendance = expectedAttendance;
    }

    // Getter和Setter方法
    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getExpectedAttendance() {
        return expectedAttendance;
    }

    public void setExpectedAttendance(Integer expectedAttendance) {
        this.expectedAttendance = expectedAttendance;
    }

    @Override
    public String toString() {
        return "ScheduleSlot{" +
                "dayOfWeek='" + dayOfWeek + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", expectedAttendance=" + expectedAttendance +
                '}';
    }
}
