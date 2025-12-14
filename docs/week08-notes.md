# Week 08 - OpenFeign 服务调用与熔断降级

## 实验内容

本次实验实现了基于 OpenFeign 的微服务间通信，替代了原有的 RestTemplate 方式，并集成了 Resilience4j 实现熔断降级功能。

## 主要内容

1. OpenFeign 配置说明
2. 负载均衡测试结果（包含日志截图）
3. 熔断降级测试结果（包含日志截图）
4. OpenFeign vs RestTemplate 对比分析

---

## 1. OpenFeign 配置说明

1. 依赖配置（pom.xml）
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
2. 启用 OpenFeign（主应用类）
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients  // 启用 Feign 客户端
public class EnrollmentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnrollmentServiceApplication.class, args);
    }
}

3. Feign 客户端接口
@FeignClient(
    name = "catalog-service",           // 服务名（从 Nacos 获取）
    fallback = CatalogClientFallback.class  // 降级处理类
)
public interface CatalogClient {
    @GetMapping("/api/courses/{id}")
    ApiResponse<CourseDto> getCourse(@PathVariable Long id);
}

4. Fallback 降级类
@Component
@Slf4j
public class CatalogClientFallback implements CatalogClient {
    @Override
    public ApiResponse<CourseDto> getCourse(Long id) {
        log.warn("CatalogClient fallback triggered for course: {}", id);
        throw new ServiceUnavailableException("课程目录服务暂时不可用，请稍后再试");
    }
}

5. application.yml 配置
# Feign 配置
feign:
  circuitbreaker:
    enabled: true  # 启用熔断器
  client:
    config:
      default:
        connectTimeout: 3000  # 连接超时 3 秒
        readTimeout: 5000     # 读取超时 5 秒
        loggerLevel: BASIC    # 日志级别
      catalog-service:        # 针对特定服务的配置
        connectTimeout: 3000
        readTimeout: 5000
  compression:
    request:
      enabled: true
      mime-types: text/xml,application/xml,application/json
      min-request-size: 2048
    response:
      enabled: true

# Resilience4j 熔断器配置
resilience4j:
  circuitbreaker:
    instances:
      catalog-service:
        failureRateThreshold: 50        # 失败率阈值 50%
        slidingWindowSize: 10           # 滑动窗口大小
        waitDurationInOpenState: 10s    # 熔断器打开后等待时间
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true

6. 使用 Feign 客户端
@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final CatalogClient catalogClient;
    
    public Enrollment enroll(String courseId, String studentId) {
        // 直接调用 Feign 客户端方法
        ApiResponse<CourseDto> response = catalogClient.getCourse(courseId);
        CourseDto course = response.getData();
        // ... 业务逻辑
    }
}

---

## 2. 负载均衡测试结果

学习通提交

---

## 3. 熔断降级测试结果

学习通提交

---

## 4. OpenFeign vs RestTemplate 对比分析

特性	        RestTemplate	                  OpenFeign
编程方式	命令式编程，手动构建 URL	       声明式编程，接口定义
代码简洁性	冗长，需要手动拼接 URL 和参数	   简洁，使用注解自动映射
负载均衡	需要 @LoadBalanced 注解	          内置集成，自动负载均衡
熔断降级	需要手动集成 Resilience4j	      原生支持 Fallback 机制
超时配置	需要手动配置 RestTemplateBuilder	配置文件统一管理
请求拦截	需要自定义 ClientHttpRequestInterceptor	支持 RequestInterceptor
日志记录	需要手动实现	                     内置日志级别配置
请求压缩	需要手动配置	                     配置文件开启
类型安全	弱类型，使用 Map 接收响应	          强类型，使用 DTO 类
维护成本	高，URL 变化需要改代码	            低，接口定义集中管理

---


