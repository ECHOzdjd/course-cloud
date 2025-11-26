package com.zjgsu.wy.enrollment.model;

/**
 * 选课状态枚举
 */
public enum EnrollmentStatus {
    /**
     * 已选课（活跃状态）
     */
    ACTIVE("已选课"),
    
    /**
     * 已退课
     */
    DROPPED("已退课"),
    
    /**
     * 已完成
     */
    COMPLETED("已完成");
    
    private final String description;
    
    EnrollmentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
