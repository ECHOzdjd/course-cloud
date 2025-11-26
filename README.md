# Course Cloud - 微服务选课系统

## 项目简介

**项目名称**: Course Cloud  
**版本**: 1.0.0  
**基于版本**: 基于 hw04b 单体选课系统拆分  

本项目将原有的单体选课系统拆分为两个独立的微服务：
- **catalog-service** (课程目录服务) - 负责课程管理
- **enrollment-service** (选课服务) - 负责学生管理和选课管理

## 架构图

```
                    ┌─────────────────────────────────────────────────────────────┐
                    │                        客户端                                │
                    └─────────────────────────────────────────────────────────────┘
                                                │
                    ┌───────────────────────────┴───────────────────────────┐
                    │                                                       │
                    ▼                                                       ▼
    ┌───────────────────────────────┐               ┌───────────────────────────────┐
    │   catalog-service (8081)      │               │  enrollment-service (8082)    │
    │                               │◄──────────────│                               │
    │   └── 课程管理                │   HTTP调用    │   ├── 学生管理                │
    │       ├── Course              │  (验证课程)   │   │   └── Student             │
    │       ├── Instructor          │               │   └── 选课管理                │
    │       └── ScheduleSlot        │               │       └── Enrollment          │
    └───────────────────────────────┘               └───────────────────────────────┘
                    │                                               │
                    ▼                                               ▼
    ┌───────────────────────────────┐               ┌───────────────────────────────┐
    │     catalog_db (3307)         │               │    enrollment_db (3308)       │
    │     MySQL 8.4                 │               │    MySQL 8.4                  │
    └───────────────────────────────┘               └───────────────────────────────┘
```

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 微服务框架 |
| Java | 17 | 编程语言 |
| MySQL | 8.4 | 数据库 |
| Docker | 20.10+ | 容器化 |
| Docker Compose | 2.0+ | 容器编排 |
| RestTemplate | - | 服务间通信 |
| Spring Data JPA | - | 数据访问层 |
| Maven | 3.8+ | 项目构建 |

## 环境要求

- **JDK**: 17 或更高版本
- **Maven**: 3.8 或更高版本
- **Docker**: 20.10 或更高版本
- **Docker Compose**: 2.0 或更高版本
- **内存**: 建议至少 4GB 可用内存

## 项目结构

```
course-cloud/
├── README.md                    # 项目文档
├── docker-compose.yml           # Docker编排文件
├── test-services.sh             # 测试脚本
├── VERSION                      # 版本号文件
│
├── catalog-service/             # 课程目录服务
│   ├── src/
│   │   └── main/
│   │       ├── java/com/zjgsu/wy/catalog/
│   │       │   ├── model/
│   │       │   │   ├── Course.java
│   │       │   │   ├── Instructor.java
│   │       │   │   └── ScheduleSlot.java
│   │       │   ├── repository/
│   │       │   │   └── CourseRepository.java
│   │       │   ├── service/
│   │       │   │   └── CourseService.java
│   │       │   ├── controller/
│   │       │   │   └── CourseController.java
│   │       │   ├── common/
│   │       │   │   └── ApiResponse.java
│   │       │   ├── exception/
│   │       │   │   ├── GlobalExceptionHandler.java
│   │       │   │   ├── ResourceNotFoundException.java
│   │       │   │   └── BusinessException.java
│   │       │   └── CatalogServiceApplication.java
│   │       └── resources/
│   │           ├── application.yml
│   │           └── application-prod.yml
│   ├── Dockerfile
│   └── pom.xml
│
└── enrollment-service/          # 选课服务
    ├── src/
    │   └── main/
    │       ├── java/com/zjgsu/wy/enrollment/
    │       │   ├── model/
    │       │   │   ├── Student.java
    │       │   │   ├── Enrollment.java
    │       │   │   └── EnrollmentStatus.java
    │       │   ├── repository/
    │       │   │   ├── StudentRepository.java
    │       │   │   └── EnrollmentRepository.java
    │       │   ├── service/
    │       │   │   ├── StudentService.java
    │       │   │   └── EnrollmentService.java
    │       │   ├── controller/
    │       │   │   ├── StudentController.java
    │       │   │   └── EnrollmentController.java
    │       │   ├── common/
    │       │   │   └── ApiResponse.java
    │       │   ├── exception/
    │       │   │   ├── GlobalExceptionHandler.java
    │       │   │   ├── ResourceNotFoundException.java
    │       │   │   └── BusinessException.java
    │       │   └── EnrollmentServiceApplication.java
    │       └── resources/
    │           ├── application.yml
    │           └── application-prod.yml
    ├── Dockerfile
    └── pom.xml
```

## 构建和运行

### 方式一：使用 Docker Compose (推荐)

#### 1. 构建 JAR 包

```bash
# 进入项目目录
cd course-cloud

# 构建 catalog-service
cd catalog-service
mvn clean package -DskipTests
cd ..

# 构建 enrollment-service
cd enrollment-service
mvn clean package -DskipTests
cd ..
```

#### 2. 启动所有服务

```bash
# 构建并启动所有服务
docker-compose up --build -d

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f
```

#### 3. 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

### 方式二：本地开发环境

#### 1. 启动数据库

```bash
# 启动 MySQL 数据库（可使用 Docker）
docker run -d --name catalog-db -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=root_password \
  -e MYSQL_DATABASE=catalog_db \
  -e MYSQL_USER=catalog_user \
  -e MYSQL_PASSWORD=catalog_pass \
  mysql:8.4

docker run -d --name enrollment-db -p 3308:3306 \
  -e MYSQL_ROOT_PASSWORD=root_password \
  -e MYSQL_DATABASE=enrollment_db \
  -e MYSQL_USER=enrollment_user \
  -e MYSQL_PASSWORD=enrollment_pass \
  mysql:8.4
```

#### 2. 启动微服务

```bash
# 终端1: 启动 catalog-service
cd catalog-service
mvn spring-boot:run

# 终端2: 启动 enrollment-service
cd enrollment-service
mvn spring-boot:run
```

## API 文档

### 课程目录服务 (catalog-service) - 端口 8081

#### 课程管理接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/courses` | 获取所有课程 |
| GET | `/api/courses/{id}` | 根据ID获取课程 |
| GET | `/api/courses/code/{code}` | 根据课程代码获取课程 |
| POST | `/api/courses` | 创建课程 |
| PUT | `/api/courses/{id}` | 更新课程 |
| DELETE | `/api/courses/{id}` | 删除课程 |

#### 创建课程请求示例

```json
POST /api/courses
{
    "code": "CS101",
    "title": "计算机科学导论",
    "instructor": {
        "id": "T001",
        "name": "张教授",
        "email": "zhang@example.edu.cn"
    },
    "schedule": {
        "dayOfWeek": "MONDAY",
        "startTime": "08:00",
        "endTime": "10:00",
        "expectedAttendance": 50
    },
    "capacity": 60,
    "enrolled": 0
}
```

### 选课服务 (enrollment-service) - 端口 8082

#### 学生管理接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/students` | 获取所有学生 |
| GET | `/api/students/{id}` | 根据ID获取学生 |
| POST | `/api/students` | 创建学生 |
| PUT | `/api/students/{id}` | 更新学生 |
| DELETE | `/api/students/{id}` | 删除学生 |

#### 创建学生请求示例

```json
POST /api/students
{
    "studentId": "2024001",
    "name": "张三",
    "major": "计算机科学与技术",
    "grade": 2024,
    "email": "zhangsan@example.edu.cn"
}
```

#### 选课管理接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/enrollments` | 获取所有选课记录 |
| GET | `/api/enrollments/{id}` | 根据ID获取选课记录 |
| GET | `/api/enrollments/course/{courseId}` | 根据课程ID查询选课记录 |
| GET | `/api/enrollments/student/{studentId}` | 根据学生ID查询选课记录 |
| POST | `/api/enrollments` | 学生选课 |
| DELETE | `/api/enrollments/{id}` | 学生退课 |

#### 选课请求示例

```json
POST /api/enrollments
{
    "courseId": "课程UUID",
    "studentId": "2024001"
}
```

### 健康检查接口

| 服务 | 路径 |
|------|------|
| catalog-service | `http://localhost:8081/actuator/health` |
| enrollment-service | `http://localhost:8082/actuator/health` |

## 测试说明

### 运行测试脚本

```bash
# 添加执行权限
chmod +x test-services.sh

# 运行测试
./test-services.sh
```

### 手动测试示例

```bash
# 1. 创建课程
curl -X POST http://localhost:8081/api/courses \
  -H "Content-Type: application/json" \
  -d '{
    "code": "CS101",
    "title": "计算机科学导论",
    "instructor": {"id": "T001", "name": "张教授", "email": "zhang@edu.cn"},
    "schedule": {"dayOfWeek": "MONDAY", "startTime": "08:00", "endTime": "10:00", "expectedAttendance": 50},
    "capacity": 60,
    "enrolled": 0
  }'

# 2. 创建学生
curl -X POST http://localhost:8082/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": "2024001",
    "name": "张三",
    "major": "计算机科学与技术",
    "grade": 2024,
    "email": "zhangsan@example.edu.cn"
  }'

# 3. 学生选课（需要替换 COURSE_ID）
curl -X POST http://localhost:8082/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": "COURSE_ID",
    "studentId": "2024001"
  }'

# 4. 查询选课记录
curl http://localhost:8082/api/enrollments
```

## 服务间通信

enrollment-service 通过 RestTemplate 调用 catalog-service 来验证课程是否存在：

```java
@Service
public class EnrollmentService {
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${catalog-service.url}")
    private String catalogServiceUrl;
    
    public Enrollment enroll(String courseId, String studentId) {
        // 调用 catalog-service 验证课程
        String url = catalogServiceUrl + "/api/courses/" + courseId;
        Map<String, Object> courseResponse = restTemplate.getForObject(url, Map.class);
        // ... 业务逻辑
    }
}
```

## 数据库设计

### catalog_db - 课程目录数据库

**courses 表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(36) | 主键 UUID |
| code | VARCHAR(50) | 课程代码 (唯一) |
| title | VARCHAR(200) | 课程标题 |
| instructor_id | VARCHAR(50) | 讲师ID |
| instructor_name | VARCHAR(100) | 讲师姓名 |
| instructor_email | VARCHAR(100) | 讲师邮箱 |
| schedule_day_of_week | VARCHAR(20) | 上课星期 |
| schedule_start_time | VARCHAR(10) | 开始时间 |
| schedule_end_time | VARCHAR(10) | 结束时间 |
| schedule_expected_attendance | INT | 预期出勤人数 |
| capacity | INT | 课程容量 |
| enrolled | INT | 已选人数 |
| created_at | DATETIME | 创建时间 |

### enrollment_db - 选课数据库

**students 表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(36) | 主键 UUID |
| student_id | VARCHAR(50) | 学号 (唯一) |
| name | VARCHAR(100) | 姓名 |
| major | VARCHAR(100) | 专业 |
| grade | INT | 年级 |
| email | VARCHAR(100) | 邮箱 (唯一) |
| created_at | DATETIME | 创建时间 |

**enrollments 表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(36) | 主键 UUID |
| course_id | VARCHAR(36) | 课程ID (不设外键) |
| student_id | VARCHAR(50) | 学号 |
| status | VARCHAR(20) | 状态 (ACTIVE/DROPPED/COMPLETED) |
| enrolled_at | DATETIME | 选课时间 |

## 遇到的问题和解决方案

### 1. 服务间通信失败

**问题**: enrollment-service 无法连接 catalog-service

**解决方案**:
- 确保 catalog-service 先启动并健康
- 检查 `catalog-service.url` 配置是否正确
- Docker 环境使用服务名而非 localhost

### 2. 数据库连接问题

**问题**: 服务启动时数据库连接失败

**解决方案**:
- 使用 `depends_on` 和 `healthcheck` 确保数据库先启动
- 增加 `start_period` 给数据库足够的初始化时间
- 添加 `allowPublicKeyRetrieval=true` 到数据库连接字符串

### 3. 端口冲突

**问题**: 本地已有服务占用端口

**解决方案**:
- 修改 `docker-compose.yml` 中的端口映射
- 或停止本地占用端口的服务

### 4. 中文乱码

**问题**: 数据库存储中文出现乱码

**解决方案**:
- MySQL 配置添加 `character-set-server=utf8mb4`
- 配置 `collation-server=utf8mb4_unicode_ci`
