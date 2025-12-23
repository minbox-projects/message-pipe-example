# Message Pipe 1.0.8-SNAPSHOT Stress Test

这个目录包含基于 message-pipe 1.0.8-SNAPSHOT 版本的压力测试项目。

## 项目结构

```
/Users/yuqiyu/OpenSource/message-pipe-example/stress-test/
├── README.md                           # 本文件
├── SETUP_GUIDE.md                      # 详细的设置和运行指南
├── server-stress-test-pom.xml          # Server 项目的 pom.xml
├── ServerStressTestApplication.java    # Server 应用主类
├── server-application.yml              # Server 应用配置文件
├── client-stress-test-pom.xml          # Client 项目的 pom.xml
├── ClientStressTestApplication.java    # Client 应用主类
├── client-application.yml              # Client 应用配置文件
└── setup-projects.sh                   # 项目初始化脚本
```

## Redis 连接信息

```
Host: 10.147.17.17
Port: 6379
Password: homeserver@2025
Database: 5
```

## 项目概述

### Server 压力测试项目
- 用于向 message-pipe 发送消息的服务端
- 提供 REST API 来发布单条或批量消息
- 支持多个管道（pipe）的并发操作
- 监控和统计发送的消息数量

**服务端口：8081**

REST API 端点：
- `GET /api/stress/publish?pipe=pipe-name&message=content` - 发送单条消息
- `POST /api/stress/publish-batch` - 批量发送消息
- `GET /api/stress/stats` - 查看统计信息
- `GET /api/stress/health` - 健康检查
- `POST /api/stress/reset` - 重置统计

### Client 压力测试项目
- 模拟多个消息消费端客户端
- 自动连接到 message-pipe server
- 接收来自 server 的消息并进行处理
- 支持多个消息处理器，各自处理不同的管道

**服务端口：8082**

REST API 端点：
- `GET /api/client/stats` - 查看处理统计信息（包括处理速率）
- `GET /api/client/health` - 健康检查
- `POST /api/client/reset` - 重置统计

## 消息处理器

### Server 端
1. **DefaultPipeProcessor** - 处理 "default-pipe" 管道的消息
2. **TestPipeProcessor** - 处理 "test-pipe" 管道的消息
3. **OrderEventsProcessor** - 处理 "order-events" 管道的消息

每个处理器会：
- 接收来自 server 的消息
- 解析消息内容
- 记录处理统计（每1000条消息输出一次速率信息）
- 返回处理结果（成功/失败）

## 运行步骤

### 前置要求
- JDK 11+
- Maven 3.6+
- Redis 实例已在 10.147.17.17:6379 运行并可访问

### 1. 初始化项目结构

```bash
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test
bash setup-projects.sh
```

此脚本会：
- 创建 server 和 client 项目的完整目录结构
- 复制 pom.xml 和应用文件到对应位置
- 创建所需的包目录

### 2. 编译 Server 项目

```bash
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/server-stress-test
mvn clean package -DskipTests
```

### 3. 编译 Client 项目

```bash
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/client-stress-test
mvn clean package -DskipTests
```

### 4. 启动 Server 测试应用

```bash
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/server-stress-test
mvn spring-boot:run
```

或者使用 jar 文件运行：
```bash
java -jar target/message-pipe-server-stress-test-1.0.0.jar --spring.config.location=file:../server-application.yml
```

### 5. 启动 Client 测试应用（新终端）

```bash
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/client-stress-test
mvn spring-boot:run
```

或者使用 jar 文件运行：
```bash
java -jar target/message-pipe-client-stress-test-1.0.0.jar --spring.config.location=file:../client-application.yml
```

## 压力测试场景

### 场景 1：单条消息发送

```bash
# 发送单条消息到 default-pipe
curl "http://localhost:8081/api/stress/publish?pipe=default-pipe&message=test-message-1"
```

### 场景 2：批量消息发送

```bash
# 发送 10000 条消息到 test-pipe
curl -X POST http://localhost:8081/api/stress/publish-batch \
  -H "Content-Type: application/json" \
  -d '{
    "pipeName": "test-pipe",
    "count": 10000,
    "messagePrefix": "batch-msg-"
  }'
```

### 场景 3：多管道并发发送

```bash
# 在后台发送消息到多个管道
for i in {1..5}; do
  curl -X POST http://localhost:8081/api/stress/publish-batch \
    -H "Content-Type: application/json" \
    -d "{
      \"pipeName\": \"pipe-$i\",
      \"count\": 5000,
      \"messagePrefix\": \"msg-$i-\"
    }" &
done
wait
```

### 场景 4：高速连续发送

```bash
# 发送 50000 条消息以测试最大吞吐量
curl -X POST http://localhost:8081/api/stress/publish-batch \
  -H "Content-Type: application/json" \
  -d '{
    "pipeName": "stress-test-pipe",
    "count": 50000,
    "messagePrefix": "stress-"
  }'
```

## 监控和统计

### 查看 Server 统计

```bash
curl http://localhost:8081/api/stress/stats
```

响应示例：
```json
{
  "success": true,
  "message": "...",
  "totalMessages": 50000,
  "totalErrors": 0,
  "timestamp": 1703332800000
}
```

### 查看 Client 统计

```bash
curl http://localhost:8082/api/client/stats
```

响应示例：
```json
{
  "processedMessages": 50000,
  "errorCount": 0,
  "elapsedMillis": 12345,
  "messagesPerSecond": 4050.5,
  "timestamp": 1703332800000
}
```

## 性能指标

运行测试后关注以下指标：

1. **消息吞吐量** - 每秒处理的消息数 (msg/sec)
2. **处理延迟** - 消息接收到处理完成的时间
3. **错误率** - 处理失败的消息数量
4. **内存使用** - JVM 堆内存占用情况
5. **Redis 连接** - Redis 中存储的管道数量和消息队列大小

## 故障排除

### 问题 1：连接被拒绝 (Connection refused)

检查：
- Redis 是否在 10.147.17.17:6379 正常运行
- Redis 密码是否正确
- 网络连接是否正常

### 问题 2：消息处理缓慢

检查：
- Server 和 Client 的日志是否有错误
- Redis 连接池配置是否合适
- 系统资源（CPU、内存）是否充足

### 问题 3：消息丢失

检查：
- Client 是否成功连接到 Server
- 消息处理器是否返回成功状态
- Redis 中是否还有待处理的消息

## 注意事项

1. **测试数据不会提交** - 这些项目创建在单独的分支和目录中，不会污染主代码库
2. **Redis 数据库隔离** - 使用数据库 5 以避免覆盖其他数据
3. **端口配置** - Server 使用 8081，Client 使用 8082，Message Pipe 通信端口为 5200/5201
4. **内存管理** - 大规模测试时注意 JVM 堆内存设置

## 清理

要清理测试项目：

```bash
# 删除项目文件（但保留测试成果的记录）
rm -rf /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/server-stress-test/target
rm -rf /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/client-stress-test/target

# 清理 Redis 中的测试数据（如需要）
# 连接到 Redis 并执行: SELECT 5; FLUSHDB;
```

## 更多信息

- Message Pipe 官方文档：https://github.com/minbox-projects/message-pipe
- Message Pipe 示例项目：https://github.com/minbox-projects/message-pipe-example
