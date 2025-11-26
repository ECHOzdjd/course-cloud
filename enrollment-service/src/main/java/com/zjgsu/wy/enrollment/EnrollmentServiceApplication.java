package com.zjgsu.wy.enrollment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * 选课服务启动类
 */
@SpringBootApplication
public class EnrollmentServiceApplication {

    /**
     * 配置 RestTemplate Bean 用于服务间通信
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(EnrollmentServiceApplication.class, args);
    }
}
