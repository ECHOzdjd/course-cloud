#!/bin/bash

# 负载均衡测试脚本
# 用于测试 Catalog Service 多实例负载均衡效果

echo "=========================================="
echo "负载均衡测试 - Catalog Service"
echo "=========================================="
echo ""

# 测试次数
TEST_COUNT=15

# Enrollment Service 地址
ENROLLMENT_URL="http://localhost:8083"

echo "通过 Enrollment Service 调用 Catalog Service"
echo "测试次数: $TEST_COUNT"
echo ""

# 创建测试课程（如果不存在）
echo "步骤 1: 准备测试数据..."
curl -s -X POST http://localhost:8083/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{"courseId":"1","studentId":"S001"}' > /dev/null 2>&1

echo "数据准备完成"
echo ""

# 测试负载均衡
echo "步骤 2: 测试负载均衡（连续请求 $TEST_COUNT 次）"
echo "----------------------------------------"

for i in $(seq 1 $TEST_COUNT); do
  echo -n "请求 #$i: "
  
  # 调用 Enrollment Service，它会通过 Feign 调用 Catalog Service
  RESPONSE=$(curl -s "http://localhost:8083/api/enrollments/health")
  
  # 也可以直接观察日志
  echo "✓ 完成"
  
  # 短暂延迟
  sleep 0.5
done

echo ""
echo "=========================================="
echo "测试完成！"
echo "=========================================="
echo ""
echo "请查看日志以观察负载均衡效果："
echo "  docker-compose logs -f catalog-service-1"
echo "  docker-compose logs -f catalog-service-2"
echo "  docker-compose logs -f catalog-service-3"
echo ""
echo "或查看所有 catalog-service 日志："
echo "  docker-compose logs catalog-service-1 catalog-service-2 catalog-service-3 | grep 'catalog-service'"
echo ""
