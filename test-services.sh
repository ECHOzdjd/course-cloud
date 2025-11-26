#!/bin/bash

# 微服务测试脚本
# 测试课程目录服务和选课服务的功能

set -e

echo "=== 微服务功能测试 ==="
echo "测试时间: $(date)"
echo ""

# 定义服务地址
CATALOG_URL="http://localhost:8081"
ENROLLMENT_URL="http://localhost:8082"

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试函数
test_endpoint() {
    local description=$1
    local method=$2
    local url=$3
    local data=$4
    
    echo -e "${YELLOW}>>> $description${NC}"
    
    if [ "$method" == "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$url")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✓ 成功 (HTTP $http_code)${NC}"
    else
        echo -e "${RED}✗ 失败 (HTTP $http_code)${NC}"
    fi
    
    echo "$body" | jq . 2>/dev/null || echo "$body"
    echo ""
}

# 检查服务健康状态
echo "=========================================="
echo "1. 检查服务健康状态"
echo "=========================================="

echo -e "\n检查 catalog-service 健康状态..."
curl -s "$CATALOG_URL/actuator/health" | jq . 2>/dev/null || echo "服务未响应"

echo -e "\n检查 enrollment-service 健康状态..."
curl -s "$ENROLLMENT_URL/actuator/health" | jq . 2>/dev/null || echo "服务未响应"

# 测试课程目录服务
echo ""
echo "=========================================="
echo "2. 测试课程目录服务 (catalog-service)"
echo "=========================================="

# 创建课程
test_endpoint "创建课程 - CS101 计算机科学导论" "POST" "$CATALOG_URL/api/courses" '{
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
}'

# 创建第二门课程
test_endpoint "创建课程 - CS102 数据结构" "POST" "$CATALOG_URL/api/courses" '{
    "code": "CS102",
    "title": "数据结构",
    "instructor": {
        "id": "T002",
        "name": "李教授",
        "email": "li@example.edu.cn"
    },
    "schedule": {
        "dayOfWeek": "WEDNESDAY",
        "startTime": "14:00",
        "endTime": "16:00",
        "expectedAttendance": 40
    },
    "capacity": 50,
    "enrolled": 0
}'

# 获取所有课程
test_endpoint "获取所有课程" "GET" "$CATALOG_URL/api/courses"

# 保存课程ID用于后续测试
echo "获取课程ID..."
COURSE_RESPONSE=$(curl -s "$CATALOG_URL/api/courses")
COURSE_ID=$(echo "$COURSE_RESPONSE" | jq -r '.data[0].id')
echo "课程ID: $COURSE_ID"
echo ""

# 根据ID获取课程
if [ "$COURSE_ID" != "null" ] && [ -n "$COURSE_ID" ]; then
    test_endpoint "根据ID获取课程" "GET" "$CATALOG_URL/api/courses/$COURSE_ID"
fi

# 根据课程代码获取课程
test_endpoint "根据课程代码获取课程 (CS101)" "GET" "$CATALOG_URL/api/courses/code/CS101"

# 测试选课服务
echo ""
echo "=========================================="
echo "3. 测试选课服务 (enrollment-service)"
echo "=========================================="

# 创建学生
test_endpoint "创建学生 - 张三" "POST" "$ENROLLMENT_URL/api/students" '{
    "studentId": "2024001",
    "name": "张三",
    "major": "计算机科学与技术",
    "grade": 2024,
    "email": "zhangsan@example.edu.cn"
}'

# 创建第二个学生
test_endpoint "创建学生 - 李四" "POST" "$ENROLLMENT_URL/api/students" '{
    "studentId": "2024002",
    "name": "李四",
    "major": "软件工程",
    "grade": 2024,
    "email": "lisi@example.edu.cn"
}'

# 获取所有学生
test_endpoint "获取所有学生" "GET" "$ENROLLMENT_URL/api/students"

# 测试服务间通信 - 学生选课
echo ""
echo "=========================================="
echo "4. 测试服务间通信 - 学生选课"
echo "=========================================="

if [ "$COURSE_ID" != "null" ] && [ -n "$COURSE_ID" ]; then
    # 学生选课
    test_endpoint "张三选课 (CS101)" "POST" "$ENROLLMENT_URL/api/enrollments" "{
        \"courseId\": \"$COURSE_ID\",
        \"studentId\": \"2024001\"
    }"
    
    # 李四选同一门课
    test_endpoint "李四选课 (CS101)" "POST" "$ENROLLMENT_URL/api/enrollments" "{
        \"courseId\": \"$COURSE_ID\",
        \"studentId\": \"2024002\"
    }"
fi

# 获取所有选课记录
test_endpoint "获取所有选课记录" "GET" "$ENROLLMENT_URL/api/enrollments"

# 根据课程ID查询选课记录
if [ "$COURSE_ID" != "null" ] && [ -n "$COURSE_ID" ]; then
    test_endpoint "根据课程ID查询选课记录" "GET" "$ENROLLMENT_URL/api/enrollments/course/$COURSE_ID"
fi

# 根据学生ID查询选课记录
test_endpoint "根据学生ID查询选课记录" "GET" "$ENROLLMENT_URL/api/enrollments/student/2024001"

# 验证课程已选人数更新
echo ""
echo "=========================================="
echo "5. 验证课程已选人数是否更新"
echo "=========================================="

if [ "$COURSE_ID" != "null" ] && [ -n "$COURSE_ID" ]; then
    test_endpoint "查看课程已选人数" "GET" "$CATALOG_URL/api/courses/$COURSE_ID"
fi

# 测试异常情况
echo ""
echo "=========================================="
echo "6. 测试异常情况"
echo "=========================================="

# 测试重复选课
if [ "$COURSE_ID" != "null" ] && [ -n "$COURSE_ID" ]; then
    test_endpoint "测试重复选课 (应该失败)" "POST" "$ENROLLMENT_URL/api/enrollments" "{
        \"courseId\": \"$COURSE_ID\",
        \"studentId\": \"2024001\"
    }"
fi

# 测试选择不存在的课程
test_endpoint "测试选择不存在的课程 (应该失败)" "POST" "$ENROLLMENT_URL/api/enrollments" '{
    "courseId": "non-existent-course-id",
    "studentId": "2024001"
}'

# 测试不存在的学生选课
if [ "$COURSE_ID" != "null" ] && [ -n "$COURSE_ID" ]; then
    test_endpoint "测试不存在的学生选课 (应该失败)" "POST" "$ENROLLMENT_URL/api/enrollments" "{
        \"courseId\": \"$COURSE_ID\",
        \"studentId\": \"9999999\"
    }"
fi

# 测试退课
echo ""
echo "=========================================="
echo "7. 测试退课功能"
echo "=========================================="

# 获取选课记录ID
ENROLLMENT_RESPONSE=$(curl -s "$ENROLLMENT_URL/api/enrollments")
ENROLLMENT_ID=$(echo "$ENROLLMENT_RESPONSE" | jq -r '.data[0].id')

if [ "$ENROLLMENT_ID" != "null" ] && [ -n "$ENROLLMENT_ID" ]; then
    test_endpoint "学生退课" "DELETE" "$ENROLLMENT_URL/api/enrollments/$ENROLLMENT_ID"
    
    # 验证课程已选人数减少
    if [ "$COURSE_ID" != "null" ] && [ -n "$COURSE_ID" ]; then
        test_endpoint "验证退课后课程已选人数" "GET" "$CATALOG_URL/api/courses/$COURSE_ID"
    fi
fi

echo ""
echo "=========================================="
echo "=== 测试完成 ==="
echo "=========================================="
echo ""
echo "测试总结:"
echo "- 课程目录服务 (catalog-service) 运行在端口 8081"
echo "- 选课服务 (enrollment-service) 运行在端口 8082"
echo "- 服务间通过 RestTemplate 进行 HTTP 通信"
echo ""
