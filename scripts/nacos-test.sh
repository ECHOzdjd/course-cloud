#!/bin/bash

echo "=========================================="
echo "Nacos 服务发现测试脚本"
echo "=========================================="
echo ""

echo "步骤 1: 启动所有服务..."
docker-compose up -d

echo ""
echo "步骤 2: 等待服务启动 (60秒)..."
for i in {60..1}; do
    echo -ne "倒计时: $i 秒\r"
    sleep 1
done
echo ""

echo ""
echo "步骤 3: 检查 Nacos 控制台..."
echo "Nacos URL: http://localhost:8848/nacos/"
curl -s http://localhost:8848/nacos/ > /dev/null && echo "✓ Nacos 控制台可访问" || echo "✗ Nacos 控制台不可访问"

echo ""
echo "步骤 4: 检查服务注册情况..."
echo ""
echo "4.1 检查 catalog-service 注册:"
curl -s -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service&groupName=COURSEHUB_GROUP&namespaceId=dev" | python3 -m json.tool 2>/dev/null || echo "请求失败或响应不是JSON格式"

echo ""
echo "4.2 检查 enrollment-service 注册:"
curl -s -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=enrollment-service&groupName=COURSEHUB_GROUP&namespaceId=dev" | python3 -m json.tool 2>/dev/null || echo "请求失败或响应不是JSON格式"

echo ""
echo "步骤 5: 测试服务间调用 (通过 enrollment-service 调用 catalog-service)..."
echo ""
for i in {1..10}; do
    echo "第 $i 次请求:"
    response=$(curl -s http://localhost:8082/api/enrollments/test)
    echo "$response" | python3 -m json.tool 2>/dev/null || echo "$response"
    echo ""
done

echo ""
echo "步骤 6: 查看容器状态..."
docker-compose ps

echo ""
echo "=========================================="
echo "测试完成!"
echo "=========================================="
echo ""
echo "提示:"
echo "1. 访问 Nacos 控制台: http://localhost:8848/nacos/"
echo "   用户名/密码: nacos/nacos"
echo "2. 查看服务列表,确认 catalog-service 和 enrollment-service 已注册"
echo "3. 在服务详情中可以看到实例的 IP、端口、健康状态等信息"
echo ""
