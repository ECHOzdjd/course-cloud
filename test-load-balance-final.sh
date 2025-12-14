#!/bin/bash

echo "========================================"
echo "负载均衡测试 - Catalog Service 多实例"
echo "========================================"
echo ""

echo "步骤 1: 验证 3 个 catalog-service 实例都在运行"
echo "----------------------------------------"
docker compose ps | grep catalog-service
echo ""

echo "步骤 2: 连续发送 20 次请求，观察负载分布"
echo "----------------------------------------"
echo ""

# 创建临时文件记录结果
TEMP_FILE=$(mktemp)

for i in {1..20}; do
  # 通过 enrollment-service 调用，它会使用 Feign 调用 catalog-service
  RESPONSE=$(curl -s http://localhost:8083/api/enrollments/health 2>/dev/null)
  PORT=$(echo "$RESPONSE" | jq -r '.port' 2>/dev/null || echo "error")
  echo "$PORT" >> "$TEMP_FILE"
  echo "请求 #$i: enrollment-service 响应端口 $PORT"
  sleep 0.3
done

echo ""
echo "步骤 3: 统计结果（观察负载是否均衡分布）"
echo "----------------------------------------"
echo "端口 8082 (enrollment-service) 被调用次数:"
grep "8082" "$TEMP_FILE" | wc -l
echo ""
echo "注意：由于我们调用的是 enrollment-service，"
echo "需要查看 catalog-service 的日志来验证负载均衡"
echo ""

rm -f "$TEMP_FILE"

echo "步骤 4: 查看各 catalog-service 实例的请求日志"
echo "----------------------------------------"
echo ""
echo "=== catalog-service-1 日志 ==="
docker compose logs --tail=10 catalog-service-1 2>&1 | grep -E "CourseController|查询课程|health" | tail -5 || echo "暂无相关日志"
echo ""
echo "=== catalog-service-2 日志 ==="
docker compose logs --tail=10 catalog-service-2 2>&1 | grep -E "CourseController|查询课程|health" | tail -5 || echo "暂无相关日志"
echo ""
echo "=== catalog-service-3 日志 ==="
docker compose logs --tail=10 catalog-service-3 2>&1 | grep -E "CourseController|查询课程|health" | tail -5 || echo "暂无相关日志"

echo ""
echo "========================================"
echo "负载均衡测试完成！"
echo "========================================"
