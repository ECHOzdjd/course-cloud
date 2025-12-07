#!/bin/bash

# 负载均衡和故障转移测试脚本

echo "========================================"
echo "第一部分: 负载均衡测试"
echo "========================================"
echo ""
echo "当前运行的 catalog-service 实例:"
docker ps | grep catalog-service
echo ""
echo "开始测试负载均衡 (观察端口在 8081 和 8083 之间交替):"
echo ""

for i in {1..10}; do
  echo "====== 请求 $i ======"
  curl -s http://localhost:8082/api/enrollments/test | python3 -m json.tool | grep '"port"'
  sleep 0.5
done

echo ""
echo "========================================"
echo "第二部分: 故障转移测试"
echo "========================================"
echo ""
echo "停止 catalog-service-2 实例 (端口 8083)..."
docker stop catalog-service-2

echo ""
echo "等待 Nacos 检测到实例下线 (15秒)..."
sleep 15

echo ""
echo "测试请求是否仍然成功 (应该只路由到端口 8081):"
echo ""

for i in {1..6}; do
  echo "====== 请求 $i ======"
  result=$(curl -s http://localhost:8082/api/enrollments/test)
  echo "$result" | python3 -m json.tool | grep -E '"port"|"status"'
  echo ""
  sleep 0.5
done

echo "========================================"
echo "测试完成!"
echo "========================================"

