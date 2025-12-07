# Course Cloud - Nacos 快速开始指南

## 🚀 快速启动

### 1. 构建项目
```bash
cd /home/ywang/course-cloud

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
docker compose up -d
```

### 3. 等待服务启动
```bash
# 等待约 60 秒让所有服务完全启动
sleep 60

# 查看服务状态
docker compose ps
```

---

## 📋 Nacos 控制台配置

### 访问 Nacos 控制台
1. 打开浏览器访问: **http://localhost:8849**
2. 登录凭证:
   - 用户名: `nacos`
   - 密码: `nacos`

### 创建命名空间 (首次使用必须)
1. 登录后,点击左侧菜单 **"命名空间"**
2. 点击右上角 **"新建命名空间"** 按钮
3. 填写信息:
   - **命名空间ID**: `dev`
   - **命名空间名**: `开发环境`
   - **描述**: `Development Environment`
4. 点击 **"确定"** 保存

### 重启应用服务(创建命名空间后)
```bash
# 重启 catalog-service 和 enrollment-service
docker restart catalog-service enrollment-service

# 等待服务重新注册
sleep 30
```

---

## ✅ 验证服务注册

### 方法 1: Nacos 控制台查看
1. 登录 Nacos 控制台
2. 点击 **"服务管理" -> "服务列表"**
3. 在顶部选择命名空间: **dev**
4. 应该看到以下服务:
   - ✅ `catalog-service` (分组: COURSEHUB_GROUP)
   - ✅ `enrollment-service` (分组: COURSEHUB_GROUP)

### 方法 2: API 查询
```bash
# 查询 catalog-service
curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service&groupName=COURSEHUB_GROUP&namespaceId=dev"

# 查询 enrollment-service
curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=enrollment-service&groupName=COURSEHUB_GROUP&namespaceId=dev"
```

---

## 🧪 测试服务发现

### 测试 Catalog Service 健康检查
```bash
curl http://localhost:8081/api/courses/health
```

预期响应:
```json
{
  "status": "UP",
  "service": "catalog-service",
  "port": "8081",
  "timestamp": 1701936123456
}
```

### 测试 Enrollment Service 调用 Catalog Service
```bash
curl http://localhost:8082/api/enrollments/test
```

预期响应:
```json
{
  "enrollment-service-port": "8082",
  "catalog-service-response": {
    "status": "UP",
    "service": "catalog-service",
    "port": "8081",
    "timestamp": 1701936123456
  },
  "status": "SUCCESS"
}
```

---

## 📸 截图任务清单

### ✅ 必需截图

#### 截图 1: Nacos 服务列表
**步骤**:
1. 登录 Nacos 控制台 (http://localhost:8849)
2. 点击 "服务管理" -> "服务列表"
3. 选择命名空间 "dev"
4. 截取显示 catalog-service 和 enrollment-service 的页面

**内容应包含**:
- 服务名称
- 分组 (COURSEHUB_GROUP)
- 实例数量
- 健康实例数

#### 截图 2: 服务详情
**步骤**:
1. 在服务列表中点击 `catalog-service` 的 "详情"
2. 截取服务详情页面

**内容应包含**:
- IP 地址
- 端口号 (8081)
- 健康状态 (UP)
- 元数据 (version, region)

#### 截图 3: 服务调用测试
**步骤**:
1. 在终端执行:
```bash
curl -s http://localhost:8082/api/enrollments/test | python3 -m json.tool
```
2. 截取终端输出

**内容应包含**:
- enrollment-service-port: "8082"
- catalog-service-response 对象
- status: "SUCCESS"

---

## 🔄 负载均衡测试 (可选)

### 启动多个 Catalog Service 实例
```bash
# 启动第2个实例
docker run -d --name catalog-service-2 \
  --network course-network \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL="jdbc:mysql://catalog-db:3306/catalog_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true" \
  -e DB_USERNAME=catalog_user \
  -e DB_PASSWORD=catalog_pass \
  -e NACOS_SERVER_ADDR=nacos:8848 \
  -e SERVER_PORT=8083 \
  -p 8083:8083 \
  catalog-service:1.0.0

# 等待注册
sleep 15
```

### 测试负载均衡
```bash
# 多次调用,观察端口变化
for i in {1..6}; do
  echo "====== 请求 $i ======"
  curl -s http://localhost:8082/api/enrollments/test | grep -o '"port":"[0-9]*"'
  echo ""
done
```

### 截图 4: 负载均衡效果 (可选)
截取上述命令的输出,应该显示端口号在 8081 和 8083 之间交替。

---

## ⚠️ 故障转移测试 (可选)

### 停止一个实例
```bash
# 停止 catalog-service 主实例
docker stop catalog-service

# 等待 Nacos 检测
sleep 20

# 测试请求是否仍然成功
curl http://localhost:8082/api/enrollments/test
```

### 截图 5: 故障转移 (可选)
1. 截取停止实例的命令
2. 截取停止后仍然成功的请求响应
3. 截取 Nacos 控制台显示实例下线的状态

---

## 🛠️ 常见问题排查

### 问题 1: 服务未注册到 Nacos
**症状**: Nacos 服务列表为空

**解决方法**:
1. 确认已创建 `dev` 命名空间
2. 重启服务: `docker restart catalog-service enrollment-service`
3. 查看日志: `docker logs catalog-service`
4. 检查配置文件中的 namespace 是否为 "dev"

### 问题 2: 服务调用失败
**症状**: curl 返回错误或超时

**解决方法**:
1. 检查服务健康状态: `curl http://localhost:8081/actuator/health`
2. 检查 Nacos 服务列表是否显示服务
3. 查看 enrollment-service 日志: `docker logs enrollment-service`
4. 确认网络连接: `docker network inspect course-network`

### 问题 3: Nacos 控制台无法访问
**症状**: 浏览器无法打开 http://localhost:8849

**解决方法**:
1. 检查容器状态: `docker ps | grep nacos`
2. 查看 Nacos 日志: `docker logs nacos`
3. 确认端口未被占用: `netstat -tulpn | grep 8848`
4. 重启 Nacos: `docker restart nacos`

### 问题 4: 命名空间找不到
**症状**: 服务注册报错,提示命名空间不存在

**解决方法**:
1. 登录 Nacos 控制台
2. 点击"命名空间"菜单
3. 手动创建 ID 为 "dev" 的命名空间
4. 重启应用服务

---

## 📦 完整测试脚本

运行自动化测试脚本:
```bash
chmod +x scripts/nacos-test.sh
./scripts/nacos-test.sh
```

---

## 🎯 提交前检查清单

- [ ] 所有服务都在运行 (`docker compose ps`)
- [ ] Nacos 控制台可访问 (http://localhost:8849)
- [ ] 已创建 `dev` 命名空间
- [ ] catalog-service 和 enrollment-service 已注册
- [ ] 服务间调用成功 (`/api/enrollments/test`)
- [ ] 已完成所有必需的截图(至少3张)
- [ ] 文档已更新 (README.md)

---

## 📝 提交说明

### Git 提交
```bash
git add .
git commit -m "feat: 集成 Nacos 服务注册与发现

- 添加 Nacos 服务器到 docker-compose.yml
- 为 catalog-service 和 enrollment-service 添加 Nacos Discovery 依赖
- 配置服务自动注册到 Nacos (dev 命名空间, COURSEHUB_GROUP 分组)
- 使用 @LoadBalanced RestTemplate 实现服务发现调用
- 添加健康检查接口用于负载均衡测试
- 更新文档和测试脚本
"

git tag v1.1.0
git push origin main
git push origin v1.1.0
```

---

## 📚 相关文档

- [README.md](README.md) - 项目主文档
- [Nacos 测试指南](docs/NACOS_TESTING_GUIDE.md) - 详细测试说明
- [架构演进思考](docs/week07-reflection.md) - 技术总结

---

## 🎓 学习要点

1. **服务注册**: 服务启动时自动注册到 Nacos
2. **服务发现**: 通过服务名而非 IP 地址调用服务
3. **负载均衡**: @LoadBalanced 实现客户端负载均衡
4. **健康检查**: Nacos 定期检查服务健康状态
5. **故障转移**: 不健康实例自动摘除,请求路由到健康实例
6. **环境隔离**: 使用命名空间和分组实现环境和业务隔离

---

## ⏭️ 下一步

完成本周任务后,你应该:
1. ✅ 理解服务注册与发现的工作原理
2. ✅ 掌握 Nacos 的基本使用
3. ✅ 了解客户端负载均衡机制
4. ✅ 理解命名空间和分组的作用

**下周预告**: 将学习配置中心,实现动态配置管理!

