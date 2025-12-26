#!/bin/bash

# 多管道压测脚本
# 用法: ./run-stress-test.sh [选项]

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="$PROJECT_DIR/server-stress-test"
CLIENT_DIR="$PROJECT_DIR/client-stress-test"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║    MessagePipe 多管道压力测试工具      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo ""

# 检查 JDK
if ! command -v java &> /dev/null; then
    echo -e "${RED}✗ 未找到 Java，请确保 JDK 已安装${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep "version" | head -n1)
echo -e "${GREEN}✓ Java 环境${NC}: $JAVA_VERSION"

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}✗ 未找到 Maven，请确保 Maven 已安装${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Maven 环境${NC}正常"
echo ""

# 构建服务器
echo -e "${YELLOW}[1/3] 编译服务器模块...${NC}"
cd "$SERVER_DIR"
mvn clean package -DskipTests -q
echo -e "${GREEN}✓ 服务器模块编译完成${NC}"

# 构建客户端
echo -e "${YELLOW}[2/3] 编译客户端模块...${NC}"
cd "$CLIENT_DIR"
mvn clean package -DskipTests -q
echo -e "${GREEN}✓ 客户端模块编译完成${NC}"
echo ""

# 启动服务器
echo -e "${YELLOW}[3/3] 启动服务器...${NC}"
cd "$SERVER_DIR"
SERVER_JAR=$(find target -name "*SNAPSHOT.jar" | head -n1)
if [ -z "$SERVER_JAR" ]; then
    echo -e "${RED}✗ 未找到服务器 JAR 文件${NC}"
    exit 1
fi

java -jar "$SERVER_JAR" --server.port=8081 &
SERVER_PID=$!
echo -e "${GREEN}✓ 服务器启动完成 (PID: $SERVER_PID)${NC}"

# 等待服务器启动
echo -e "${YELLOW}等待服务器启动...${NC}"
sleep 3

# 检查服务器是否启动成功
if ! curl -s http://localhost:8081/api/stress/health > /dev/null 2>&1; then
    echo -e "${RED}✗ 服务器启动失败${NC}"
    kill $SERVER_PID 2>/dev/null || true
    exit 1
fi
echo -e "${GREEN}✓ 服务器已就绪${NC}"
echo ""

# 启动客户端压测
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo -e "${BLUE}         开始执行多管道压力测试          ${NC}"
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo ""

cd "$CLIENT_DIR"
CLIENT_JAR=$(find target -name "*SNAPSHOT.jar" | head -n1)
if [ -z "$CLIENT_JAR" ]; then
    echo -e "${RED}✗ 未找到客户端 JAR 文件${NC}"
    kill $SERVER_PID 2>/dev/null || true
    exit 1
fi

java -jar "$CLIENT_JAR" --server.port=8082 --stress-test &
CLIENT_PID=$!

# 等待压测完成
wait $CLIENT_PID
CLIENT_EXIT_CODE=$?

# 清理资源
echo ""
echo -e "${YELLOW}清理资源...${NC}"
kill $SERVER_PID 2>/dev/null || true

if [ $CLIENT_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✓ 压力测试完成${NC}"
else
    echo -e "${RED}✗ 压力测试异常终止${NC}"
    exit $CLIENT_EXIT_CODE
fi

echo ""
echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║         压力测试已完成                 ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
