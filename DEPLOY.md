# Course Cloud - Docker 部署指南

## 快速部署

### 前置条件

- Docker 20.10+
- Docker Compose 2.0+
- Maven 3.8+ (用于构建 JAR)
- JDK 17+ (用于构建 JAR)

### 一键部署

```bash
# 1. 构建 JAR 包
cd catalog-service && mvn clean package -DskipTests && cd ..
cd enrollment-service && mvn clean package -DskipTests && cd ..

# 2. 启动所有服务
docker-compose up --build -d

# 3. 查看服务状态
docker-compose ps
```

## 文件结构

```
course-cloud/
├── docker-compose.yml        # Docker 编排文件
├── catalog-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/                  # 源代码
└── enrollment-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/                  # 源代码
```

## 部署步骤

### 1. 构建 JAR 包

```bash
# 进入项目根目录
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

### 2. 启动所有服务

```bash
# 构建 Docker 镜像并启动
docker-compose up --build -d

# 查看服务状态
docker-compose ps

# 查看实时日志
docker-compose logs -f
```

### 3. 验证服务

等待约 60-90 秒让服务完全启动，然后测试：

```bash
# 检查服务健康状态
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health

# 测试 catalog-service (课程服务)
curl http://localhost:8081/api/courses

# 测试 enrollment-service (选课服务)
curl http://localhost:8082/api/students
curl http://localhost:8082/api/enrollments
```

### 4. 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| catalog-db | 3307 | 课程数据库 (MySQL) |
| enrollment-db | 3308 | 选课数据库 (MySQL) |
| catalog-service | 8081 | 课程目录服务 |
| enrollment-service | 8082 | 选课服务 |

## 常用命令

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷（清除数据）
docker-compose down -v

# 重启某个服务
docker-compose restart catalog-service
docker-compose restart enrollment-service

# 查看某个服务的日志
docker-compose logs -f catalog-service
docker-compose logs -f enrollment-service

# 重新构建并启动
docker-compose up --build -d
```

## 测试微服务通信

```bash
# 运行测试脚本
chmod +x test-services.sh
./test-services.sh
```

或手动测试：

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
    "major": "计算机科学",
    "grade": 2024,
    "email": "zhangsan@edu.cn"
  }'

# 3. 获取课程ID
COURSE_ID=$(curl -s http://localhost:8081/api/courses | jq -r '.data[0].id')
echo "Course ID: $COURSE_ID"

# 4. 学生选课
curl -X POST http://localhost:8082/api/enrollments \
  -H "Content-Type: application/json" \
  -d "{\"courseId\": \"$COURSE_ID\", \"studentId\": \"2024001\"}"
```

## 故障排除

### 1. 服务无法启动

```bash
# 查看详细日志
docker-compose logs

# 检查容器状态
docker ps -a
```

### 2. 数据库连接失败

```bash
# 确保数据库已启动
docker-compose ps catalog-db enrollment-db

# 等待数据库健康检查通过
docker-compose logs catalog-db
```

### 3. 端口被占用

```bash
# 检查端口占用
netstat -tlnp | grep -E '8081|8082|3307|3308'

# 修改 docker-compose.yml 中的端口映射
```

### 4. 重置环境

```bash
# 完全重置（删除所有数据）
docker-compose down -v
docker-compose up --build -d
```

## VMware 部署注意事项

1. 确保虚拟机网络配置正确（NAT 或桥接模式）
2. 如果从主机访问，使用虚拟机 IP 而非 localhost
3. 检查防火墙设置，确保端口开放
