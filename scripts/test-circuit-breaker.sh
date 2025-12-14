#!/bin/bash

# 熔断降级测试脚本
# 测试 Feign 的 Fallback 降级机制

echo "=========================================="
echo "熔断降级测试"
echo "=========================================="
echo ""

ENROLLMENT_URL="http://localhost:8083"

echo "步骤 1: 验证服务正常运行"
echo "----------------------------------------"
curl -s "$ENROLLMENT_URL/api/enrollments/health" | jq '.'
echo ""

echo "步骤 2: 停止所有 Catalog Service 实例"
echo "----------------------------------------"
docker-compose stop catalog-service-1 catalog-service-2 catalog-service-3
echo "Catalog Service 已停止"
echo ""

echo "等待 5 秒..."
sleep 5
echo ""

echo "步骤 3: 尝试选课（应触发降级）"
echo "----------------------------------------"
echo "发送选课请求..."
RESPONSE=$(curl -s -X POST "$ENROLLMENT_URL/api/enrollments" \
  -H "Content-Type: application/json" \
  -d '{"courseId":"1","studentId":"S001"}')

echo "响应结果:"
echo "$RESPONSE" | jq '.'
echo ""

echo "步骤 4: 查看 Enrollment Service 日志（查找 fallback 记录）"
echo "----------------------------------------"
docker-compose logs --tail=20 enrollment-service | grep -i "fallback\|unavailable"
echo ""

echo "步骤 5: 重启 Catalog Service 实例"
echo "----------------------------------------"
docker-compose start catalog-service-1 catalog-service-2 catalog-service-3
echo "Catalog Service 正在启动..."
echo ""

echo "等待服务恢复（30秒）..."
sleep 30
echo ""

echo "步骤 6: 验证服务恢复"
echo "----------------------------------------"
curl -s "$ENROLLMENT_URL/api/enrollments/health" | jq '.'
echo ""

echo "=========================================="
echo "熔断降级测试完成！"
echo "=========================================="
echo ""
echo "请查看以下日志确认降级行为："
echo "  docker-compose logs enrollment-service | grep -i fallback"
echo ""
