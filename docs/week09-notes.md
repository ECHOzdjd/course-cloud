# Week 09 - API Gateway 与 JWT 认证

## 实验内容

本次实验在 hw08 的 OpenFeign 和负载均衡基础上，搭建 API 网关作为系统统一入口，并实现基于 JWT 的统一认证机制。

## 版本信息

- **项目名称**: course-cloud
- **版本号**: v2.0.0（引入 API Gateway，重大架构变更）
- **基于版本**: v1.2.0

## 系统架构

```
客户端 → Gateway (8090) → User/Catalog/Enrollment Services
```

## 主要内容

1. Gateway 路由配置说明
2. JWT 认证流程说明
3. 测试结果截图

---

## 1. Gateway 路由配置说明
核心配置 (application.yml)
spring:
  cloud:
    gateway:
      routes:
        # User Service 路由
        - id: user-service
          uri: lb://user-service              # 使用 LoadBalancer 从 Nacos 发现服务
          predicates:
            - Path=/api/users/**              # 匹配路径
          filters:
            - StripPrefix=0                   # 不去掉前缀，直接转发 /api/users/**
        
        # Catalog Service 路由
        - id: catalog-service
          uri: lb://catalog-service
          predicates:
            - Path=/api/courses/**
          filters:
            - StripPrefix=0
        
        # Enrollment Service 路由
        - id: enrollment-service
          uri: lb://enrollment-service
          predicates:
            - Path=/api/enrollments/**, /api/students/**
          filters:
            - StripPrefix=0
        
        # 认证路由（登录/注册）
        - id: auth-service
          uri: lb://user-service
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=0

路由工作流程
路径匹配: Gateway 根据 predicates 中的 Path 规则匹配请求路径
服务发现: 通过 uri: lb://服务名 从 Nacos 获取服务实例列表
负载均衡: Spring Cloud LoadBalancer 自动选择一个健康的实例
前缀处理: StripPrefix=0 保留完整路径，StripPrefix=1 会去掉第一段路径（如 /api）
请求转发: 将请求转发到选中的服务实例

CORS 跨域配置
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: "*"
            allowedMethods: [GET, POST, PUT, DELETE, OPTIONS]
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600

---

## 2. JWT 认证流程说明

整体流程图
┌──────────┐                 ┌──────────┐                 ┌──────────────┐
│  客户端   │                 │ Gateway  │                 │ User Service │
└─────┬────┘                 └────┬─────┘                 └──────┬───────┘
      │                           │                               │
      │ 1. POST /api/auth/login   │                               │
      │  {username, password}     │                               │
      ├──────────────────────────>│                               │
      │                           │ 2. 转发登录请求                 │
      │                           ├──────────────────────────────>│
      │                           │                               │
      │                           │ 3. 验证用户 + 生成 JWT Token   │
      │                           │<──────────────────────────────┤
      │ 4. 返回 Token             │                               │
      │<──────────────────────────┤                               │
      │   {token, user}           │                               │
      │                           │                               │
      │ 5. GET /api/students      │                               │
      │  Header: Authorization:   │                               │
      │    Bearer <token>         │                               │
      ├──────────────────────────>│                               │
      │                           │ 6. JWT 认证过滤器              │
      │                           │  - 验证 Token                 │
      │                           │  - 解析用户信息                │
      │                           │  - 添加请求头:                │
      │                           │    X-User-Id: 1               │
      │                           │    X-Username: admin          │
      │                           │    X-User-Role: ADMIN         │
      │                           │                               │
      │                           │ 7. 转发请求 + 用户信息          │
      │                           ├──────────────────────────────>│
      │                           │                      Enrollment│
      │                           │                        Service │
      │ 8. 返回业务数据             │                               │
      │<──────────────────────────┤<──────────────────────────────┤
      │                           │                               │

详细流程说明
1. 登录流程 (Login)
步骤 1: 用户登录
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
步骤 2: 验证用户
// User Service - AuthController.java
User user = userRepository.findByUsername(request.getUsername());
if (user == null || !user.getPassword().equals(request.getPassword())) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body("用户名或密码错误");
}
步骤 3: 生成 JWT Token
// JwtUtil.java
String token = Jwts.builder()
    .setSubject(userId)                    // 用户 ID
    .claim("username", username)           // 用户名
    .claim("role", role)                   // 角色
    .setIssuedAt(new Date())              // 签发时间
    .setExpiration(new Date(now + 86400000)) // 过期时间（24小时）
    .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS512)
    .compact();
步骤 4: 返回 Token
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIi...",
  "user": {
    "id": 1,
    "username": "admin",
    "role": "ADMIN",
    "email": "admin@example.com"
  }
}

2. 认证流程 (Authentication)
步骤 1: 客户端携带 Token 访问
GET /api/students
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
步骤 2: Gateway JWT 认证过滤器
// JwtAuthenticationFilter.java

// 2.1 白名单检查
if (WHITE_LIST.stream().anyMatch(path::startsWith)) {
    return chain.filter(exchange);  // 白名单路径直接放行
}

// 2.2 获取 Authorization 请求头
String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    return response.setComplete();  // 返回 401
}

// 2.3 提取并验证 Token
String token = authHeader.substring(7);
if (!jwtUtil.validateToken(token)) {
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    return response.setComplete();  // Token 无效，返回 401
}

// 2.4 解析 Token 获取用户信息
String userId = jwtUtil.getUserIdFromToken(token);
String username = jwtUtil.getUsernameFromToken(token);
String role = jwtUtil.getRoleFromToken(token);

// 2.5 将用户信息添加到请求头
ServerHttpRequest modifiedRequest = request.mutate()
    .header("X-User-Id", userId)
    .header("X-Username", username)
    .header("X-User-Role", role)
    .build();

// 2.6 转发请求到下游服务
return chain.filter(modifiedExchange);
步骤 3: 后端服务获取用户信息
// Enrollment Service - Controller
@GetMapping
public ResponseEntity<List<Student>> getAllStudents(
    @RequestHeader("X-User-Id") String userId,
    @RequestHeader("X-Username") String username,
    @RequestHeader("X-User-Role") String role
) {
    log.info("用户 {} (ID: {}, 角色: {}) 查询学生列表", username, userId, role);
    // 业务逻辑...
}
JWT Token 结构
Header（头部）
{
  "alg": "HS512",
  "typ": "JWT"
}

Payload（载荷）
{
  "sub": "1",                              // 用户 ID
  "username": "admin",                     // 用户名
  "role": "ADMIN",                         // 角色
  "iat": 1765713413,                       // 签发时间
  "exp": 1765799813                        // 过期时间
}

Signature（签名）
HMACSHA512(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)

白名单路径（不需要认证）
private static final List<String> WHITE_LIST = Arrays.asList(
    "/api/auth/login",       // 登录接口
    "/api/auth/register",    // 注册接口
    "/actuator/health"       // 健康检查
);

---

## 3. 测试结果

学习通提交

---


