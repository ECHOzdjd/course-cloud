package com.zjgsu.wy.user.controller;

import com.zjgsu.wy.user.dto.LoginRequest;
import com.zjgsu.wy.user.dto.LoginResponse;
import com.zjgsu.wy.user.model.User;
import com.zjgsu.wy.user.repository.UserRepository;
import com.zjgsu.wy.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理登录、注册等认证相关请求
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录
     * @param request 登录请求（用户名和密码）
     * @return Token 和用户信息
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        
        // 1. 验证用户名和密码
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
        
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            log.warn("Login failed for username: {}", request.getUsername());
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户名或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        // 2. 生成 JWT Token
        String token = jwtUtil.generateToken(
                user.getId().toString(),
                user.getUsername(),
                user.getRole() != null ? user.getRole() : "USER"
        );
        
        log.info("Login successful for user: {} (ID: {})", user.getUsername(), user.getId());
        
        // 3. 返回 Token 和用户信息
        return ResponseEntity.ok(new LoginResponse(token, user));
    }

    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        log.info("Register attempt for username: {}", user.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "用户名已存在");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        // 检查邮箱是否已存在
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "邮箱已被使用");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        // 设置默认角色
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        
        // 保存用户
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
        
        // 生成 Token
        String token = jwtUtil.generateToken(
                savedUser.getId().toString(),
                savedUser.getUsername(),
                savedUser.getRole()
        );
        
        return ResponseEntity.ok(new LoginResponse(token, savedUser));
    }

    /**
     * 初始化测试用户
     * 创建一些默认用户用于测试
     */
    @PostMapping("/init-test-users")
    public ResponseEntity<?> initTestUsers() {
        if (userRepository.count() > 0) {
            return ResponseEntity.ok("Test users already exist");
        }
        
        // 创建管理员用户
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setRole("ADMIN");
        admin.setEmail("admin@example.com");
        admin.setRealName("管理员");
        userRepository.save(admin);
        
        // 创建普通用户
        User user = new User();
        user.setUsername("user");
        user.setPassword("user123");
        user.setRole("USER");
        user.setEmail("user@example.com");
        user.setRealName("普通用户");
        userRepository.save(user);
        
        log.info("Test users created: admin, user");
        return ResponseEntity.ok("Test users created successfully");
    }
}
