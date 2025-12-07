# Week 07 - 架构演进思考

## 项目信息

- **项目名称**: Course Cloud - 微服务选课系统
- **作者**: 王勇 (wy)
- **学号**: [您的学号]
- **完成日期**: 2025年12月7日
- **版本**: v1.1.0

## 问题与思考

### 1. 对比使用 Nacos 前后,服务间调用方式有什么变化?带来了哪些好处?

#### 使用 Nacos 前

**调用方式**:
- 使用硬编码的 URL 地址进行服务调用
- 在配置文件中配置服务地址: `catalog-service.url=http://catalog-service:8081`
- 代码中通过 `@Value` 注解注入配置的 URL

```java
@Value("${catalog-service.url}")
private String catalogServiceUrl;

String url = catalogServiceUrl + "/api/courses/" + courseId;
```

**存在的问题**:
1. **地址硬编码**: 服务地址写死在配置文件中,难以动态调整
2. **无法感知服务状态**: 不知道目标服务是否健康、是否可用
3. **无负载均衡**: 多实例情况下无法自动分发请求
4. **扩展性差**: 增加或减少服务实例需要手动修改配置
5. **环境切换麻烦**: 不同环境 (dev/test/prod) 需要维护不同的配置文件

#### 使用 Nacos 后

**调用方式**:
- 使用服务名进行调用,无需关心具体的 IP 和端口
- 启用 `@EnableDiscoveryClient` 和 `@LoadBalanced`
- 通过服务名调用: `http://catalog-service`

```java
private static final String CATALOG_SERVICE_NAME = "http://catalog-service";

String url = CATALOG_SERVICE_NAME + "/api/courses/" + courseId;
```

**带来的好处**:

1. **动态服务发现**
   - 服务启动时自动注册到 Nacos
   - 调用方自动从 Nacos 获取服务实例列表
   - 服务地址变更无需修改代码和配置

2. **健康检查与自动摘除**
   - Nacos 定期检查服务健康状态
   - 不健康的实例自动从服务列表中移除
   - 提高系统的可用性和稳定性

3. **自动负载均衡**
   - 多实例情况下,RestTemplate 结合 @LoadBalanced 自动实现客户端负载均衡
   - 支持多种负载均衡策略 (轮询、随机、权重等)
   - 请求均匀分发到各个健康实例

4. **环境隔离**
   - 通过命名空间 (Namespace) 实现环境隔离
   - 同一套配置可以在不同环境中使用
   - dev/test/prod 环境的服务互不干扰

5. **灵活扩缩容**
   - 动态增加或减少服务实例,无需修改配置
   - 新实例启动后自动注册,调用方立即可见
   - 实例下线后自动注销,调用方不再路由到该实例

6. **降低运维成本**
   - 统一的服务治理平台
   - 可视化的服务列表和健康状态监控
   - 减少人工配置和维护工作

---

### 2. Nacos 的临时实例和持久实例有什么区别?当前项目适合使用哪种?

#### 临时实例 (Ephemeral Instance)

**特点**:
- **注册方式**: 通过心跳保持注册状态
- **健康检查**: 客户端主动向 Nacos 发送心跳 (每 5 秒一次)
- **摘除机制**: 心跳超时 (默认 15 秒) 后,Nacos 自动将实例从服务列表中移除
- **数据存储**: 仅存储在内存中,Nacos 重启后实例信息丢失
- **适用场景**: 
  - 容器化部署的微服务 (Docker、Kubernetes)
  - 需要快速上下线的服务
  - 短生命周期的服务

**优点**:
- 快速故障检测和恢复
- 不占用持久化存储空间
- 适合云原生架构

**缺点**:
- 网络抖动可能导致误摘除
- Nacos 重启后需要重新注册

#### 持久实例 (Persistent Instance)

**特点**:
- **注册方式**: 持久化存储到数据库
- **健康检查**: Nacos 主动探测实例健康状态
- **摘除机制**: 即使实例下线,也会保留在服务列表中,但标记为不健康
- **数据存储**: 持久化到数据库,Nacos 重启后实例信息仍然存在
- **适用场景**:
  - 传统部署方式 (物理机、虚拟机)
  - 长期运行的核心服务
  - 需要手动控制上下线的服务

**优点**:
- 网络抖动不会导致实例被移除
- Nacos 重启后实例信息不丢失
- 便于服务上下线管理

**缺点**:
- 需要持久化存储
- 故障检测稍慢
- 需要手动删除下线的实例

#### 当前项目适合使用哪种?

**推荐使用临时实例 (ephemeral: true)**

**理由**:

1. **容器化部署**: 项目使用 Docker Compose 部署,服务以容器形式运行,容器的生命周期由编排工具管理

2. **快速故障转移**: 容器化环境下,服务实例可以快速启动和停止,临时实例能更快地检测故障并摘除不健康实例

3. **动态扩缩容**: 在云原生环境中,服务实例数量会动态变化,临时实例更适合这种场景

4. **资源占用**: 临时实例只在内存中存储,不需要持久化,减少了 Nacos 的存储压力

5. **重启恢复**: Docker Compose 管理的服务重启后会自动重新注册,不需要保留之前的注册信息

**当前配置**:
```yaml
spring:
  cloud:
    nacos:
      discovery:
        ephemeral: true  # 使用临时实例
        heart-beat-interval: 5000  # 心跳间隔 5 秒
        heart-beat-timeout: 15000  # 心跳超时 15 秒
```

**特殊情况下使用持久实例**:
- 如果项目部署在传统的物理机或虚拟机上
- 如果服务实例数量固定,不需要频繁扩缩容
- 如果需要在 Nacos 重启后保留服务注册信息

---

### 3. 如果 Nacos 服务器宕机,已经启动的服务还能正常通信吗?为什么?

#### 答案: 可以继续通信,但有一定限制

#### 工作原理分析

**Nacos 客户端缓存机制**:

1. **本地缓存**: 服务启动后,会从 Nacos 拉取服务列表并缓存到本地内存
2. **定期更新**: 客户端定期 (默认 30 秒) 从 Nacos 拉取最新的服务列表更新缓存
3. **缓存降级**: Nacos 不可用时,客户端会使用本地缓存的服务列表

**Nacos 宕机后的影响**:

✅ **可以继续通信的场景**:
- 已经启动并相互发现的服务之间可以继续通信
- 服务列表已缓存到客户端内存中
- 只要服务实例不变,调用可以正常进行

```
时间轴:
T1: Nacos 正常,服务 A 和服务 B 都已注册并相互发现
T2: Nacos 宕机
T3: 服务 A 调用服务 B - ✅ 成功 (使用本地缓存)
T4: 服务 A 继续调用服务 B - ✅ 成功 (使用本地缓存)
```

❌ **无法正常工作的场景**:

1. **新服务注册**: Nacos 宕机后,新启动的服务无法注册
2. **服务发现**: 新启动的服务无法发现其他服务
3. **动态扩缩容**: 新增或下线的服务实例无法被感知
4. **健康检查失效**: 无法及时发现和摘除不健康的实例

```
时间轴:
T1: Nacos 正常,服务 A 已注册
T2: Nacos 宕机
T3: 新启动服务 B - ❌ 无法注册到 Nacos
T4: 服务 A 调用服务 B - ❌ 失败 (本地缓存中没有服务 B)
T5: 服务 C 实例下线 - ⚠️ 服务 A 不知道,可能继续调用失败的实例
```

#### 实际影响

**短期影响** (Nacos 宕机 < 1 小时):
- 已运行的服务间调用基本不受影响
- 无法进行服务扩缩容
- 无法及时发现实例故障

**长期影响** (Nacos 宕机 > 1 小时):
- 服务实例可能因重启、故障等原因变化
- 本地缓存与实际情况不一致
- 可能出现调用失败、负载不均等问题

#### 高可用建议

为避免 Nacos 单点故障,生产环境应该:

1. **Nacos 集群部署**
   ```yaml
   nacos:
     image: nacos/nacos-server:v3.1.0
     environment:
       MODE: cluster  # 集群模式
       NACOS_SERVERS: nacos1:8848,nacos2:8848,nacos3:8848
   ```

2. **数据持久化**
   - 配置外部 MySQL 数据库存储 Nacos 数据
   - 防止 Nacos 重启后数据丢失

3. **监控告警**
   - 监控 Nacos 健康状态
   - 及时发现和处理 Nacos 故障

4. **客户端容错**
   - 配置合理的超时和重试策略
   - 使用断路器 (Hystrix/Sentinel) 保护服务调用

---

### 4. 命名空间 (Namespace) 和分组 (Group) 的作用是什么?如何利用它们实现环境隔离?

#### 命名空间 (Namespace)

**作用**:
- 实现**环境级别的隔离** (dev/test/prod)
- 不同命名空间的服务相互不可见
- 用于区分不同的部署环境

**特点**:
- 每个命名空间有唯一的 ID
- 默认命名空间为 `public`
- 不同命名空间的服务完全隔离

**使用场景**:
```yaml
# 开发环境
spring:
  cloud:
    nacos:
      discovery:
        namespace: dev  # 开发环境命名空间

# 测试环境
spring:
  cloud:
    nacos:
      discovery:
        namespace: test  # 测试环境命名空间

# 生产环境
spring:
  cloud:
    nacos:
      discovery:
        namespace: prod  # 生产环境命名空间
```

**好处**:
1. 同一套代码可以在不同环境运行
2. 环境间服务完全隔离,避免误调用
3. 便于环境管理和切换

#### 分组 (Group)

**作用**:
- 实现**业务级别的隔离**
- 同一命名空间下的服务分组管理
- 用于区分不同的业务线或团队

**特点**:
- 默认分组为 `DEFAULT_GROUP`
- 同一命名空间下可以有多个分组
- 不同分组的服务可以相互发现 (需要指定分组名)

**使用场景**:
```yaml
# 选课业务
spring:
  cloud:
    nacos:
      discovery:
        group: COURSEHUB_GROUP  # 选课系统分组

# 教务业务
spring:
  cloud:
    nacos:
      discovery:
        group: ADMIN_GROUP  # 教务管理系统分组
```

**好处**:
1. 在同一环境下隔离不同业务
2. 便于大型系统的服务管理
3. 支持多团队并行开发

#### Namespace 和 Group 的关系

```
┌─────────────────────────────────────────────────────┐
│                 Nacos Server                        │
│                                                     │
│  ┌───────────────────────────────────────────────┐ │
│  │       Namespace: dev (开发环境)                │ │
│  │                                               │ │
│  │  ┌─────────────────────────────────────────┐ │ │
│  │  │  Group: COURSEHUB_GROUP (选课业务)      │ │ │
│  │  │  - catalog-service                       │ │ │
│  │  │  - enrollment-service                    │ │ │
│  │  └─────────────────────────────────────────┘ │ │
│  │                                               │ │
│  │  ┌─────────────────────────────────────────┐ │ │
│  │  │  Group: ADMIN_GROUP (教务业务)          │ │ │
│  │  │  - admin-service                         │ │ │
│  │  │  - teacher-service                       │ │ │
│  │  └─────────────────────────────────────────┘ │ │
│  └───────────────────────────────────────────────┘ │
│                                                     │
│  ┌───────────────────────────────────────────────┐ │
│  │       Namespace: prod (生产环境)               │ │
│  │                                               │ │
│  │  ┌─────────────────────────────────────────┐ │ │
│  │  │  Group: COURSEHUB_GROUP                  │ │ │
│  │  │  - catalog-service                       │ │ │
│  │  │  - enrollment-service                    │ │ │
│  │  └─────────────────────────────────────────┘ │ │
│  └───────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

#### 实现环境隔离的最佳实践

**方案一: 使用 Namespace 隔离环境**

```yaml
# application-dev.yml (开发环境)
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nacos-dev:8848
        namespace: dev
        group: COURSEHUB_GROUP

# application-test.yml (测试环境)
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nacos-test:8848
        namespace: test
        group: COURSEHUB_GROUP

# application-prod.yml (生产环境)
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nacos-prod:8848
        namespace: prod
        group: COURSEHUB_GROUP
```

**方案二: 使用 Group 隔离业务**

在同一环境 (如 dev) 中运行多个业务系统:

```yaml
# 选课系统
spring:
  cloud:
    nacos:
      discovery:
        namespace: dev
        group: COURSEHUB_GROUP

# 教务系统
spring:
  cloud:
    nacos:
      discovery:
        namespace: dev
        group: ADMIN_GROUP

# 支付系统
spring:
  cloud:
    nacos:
      discovery:
        namespace: dev
        group: PAYMENT_GROUP
```

**方案三: Namespace + Group 组合**

大型系统推荐使用组合方案:

```
环境隔离 (Namespace): dev/test/prod
  └── 业务隔离 (Group): COURSEHUB_GROUP/ADMIN_GROUP/PAYMENT_GROUP
      └── 服务: catalog-service/enrollment-service/...
```

#### 当前项目配置

```yaml
spring:
  cloud:
    nacos:
      discovery:
        namespace: dev  # 使用 dev 命名空间隔离开发环境
        group: COURSEHUB_GROUP  # 使用 COURSEHUB_GROUP 分组标识选课业务
```

**选择理由**:
1. **Namespace = dev**: 将开发环境与测试、生产环境隔离
2. **Group = COURSEHUB_GROUP**: 将选课系统与其他业务系统隔离
3. 便于后续扩展到多环境部署

---

## 实验总结

### 完成情况

- ✅ 成功集成 Nacos 服务注册与发现
- ✅ 所有服务能够自动注册到 Nacos
- ✅ 实现基于服务名的动态服务调用
- ✅ 配置健康检查,Nacos 能准确监控服务状态
- ✅ 实现负载均衡测试接口
- ✅ 完成文档和测试脚本

### 技术收获

1. **服务治理能力提升**: 理解了服务注册、发现、健康检查的工作原理
2. **负载均衡实践**: 掌握了客户端负载均衡的实现方式
3. **环境隔离设计**: 学会了使用 Namespace 和 Group 进行环境和业务隔离
4. **高可用思考**: 理解了 Nacos 在微服务架构中的重要性和单点故障风险

### 遇到的问题

1. **Nacos 启动慢**: Nacos 容器启动需要较长时间,需要配置足够的 `start_period`
2. **服务注册延迟**: 服务启动后需要等待几秒才能在 Nacos 控制台看到
3. **命名空间创建**: 需要先在 Nacos 控制台创建 `dev` 命名空间,否则服务注册失败

### 改进方向

1. **Nacos 集群部署**: 当前为单机模式,生产环境应使用集群模式
2. **配置管理**: 可以进一步使用 Nacos 的配置中心功能,实现动态配置管理
3. **服务限流**: 集成 Sentinel 实现服务限流和熔断
4. **链路追踪**: 集成 Sleuth + Zipkin 实现分布式链路追踪

### 下一步计划

1. 配置 Nacos 集群实现高可用
2. 使用 Nacos Config 管理配置文件
3. 集成 Sentinel 实现服务保护
4. 添加服务监控和告警

---

## 附录

### Nacos 常用 API

```bash
# 查询服务列表
curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service&groupName=COURSEHUB_GROUP&namespaceId=dev"

# 注册实例
curl -X POST "http://localhost:8848/nacos/v1/ns/instance" \
  -d "serviceName=catalog-service&ip=192.168.1.100&port=8081&namespaceId=dev&groupName=COURSEHUB_GROUP"

# 注销实例
curl -X DELETE "http://localhost:8848/nacos/v1/ns/instance" \
  -d "serviceName=catalog-service&ip=192.168.1.100&port=8081&namespaceId=dev&groupName=COURSEHUB_GROUP"

# 查询服务详情
curl "http://localhost:8848/nacos/v1/ns/service?serviceName=catalog-service&groupName=COURSEHUB_GROUP&namespaceId=dev"
```

### 参考资料

- [Nacos 官方文档](https://nacos.io/zh-cn/docs/quick-start.html)
- [Spring Cloud Alibaba 文档](https://spring-cloud-alibaba-group.github.io/github-pages/2022/zh-cn/index.html)
- [Nacos 架构原理](https://nacos.io/zh-cn/docs/architecture.html)

