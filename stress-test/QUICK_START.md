# Message Pipe 1.0.8-SNAPSHOT 压力测试 - 快速开始指南

## 环境配置完成！✓

你的两个 Spring Boot 压力测试项目已成功创建并编译完成。

### 项目位置

```
/Users/yuqiyu/OpenSource/message-pipe-example/stress-test/
├── server-stress-test/          # Server 压力测试项目
│   ├── pom.xml
│   ├── src/main/java/...
│   ├── src/main/resources/application.yml
│   └── target/message-pipe-server-stress-test-1.0.0.jar
│
├── client-stress-test/          # Client 压力测试项目
│   ├── pom.xml
│   ├── src/main/java/...
│   ├── src/main/resources/application.yml
│   └── target/message-pipe-client-stress-test-1.0.0.jar
│
├── README.md                    # 完整项目文档
├── QUICK_START.md               # 本文件
└── PROJECT_SUMMARY.txt          # 项目总结
```

### 编译状态

- ✓ Server 项目编译成功 (11 KB jar)
- ✓ Client 项目编译成功 (13 KB jar)

## Redis 连接信息

```yaml
Host: 10.147.17.17
Port: 6379
Password: homeserver@2025
Database: 5
```

## 快速启动（3 个步骤）

### 1. 启动 Server 压力测试应用

**终端 1:**
```bash
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/server-stress-test
mvn spring-boot:run
```

或使用 jar 文件：
```bash
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/server-stress-test
java -jar target/message-pipe-server-stress-test-1.0.0.jar
```

**预期输出：**
```
2025-12-23 13:10:00 [main] INFO  ... - === Message Pipe Server Stress Test Started ===
2025-12-23 13:10:01 [main] INFO  ... - Server started on port 8081
```

### 2. 启动 Client 压力测试应用

**终端 2:**
```bash
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/client-stress-test
mvn spring-boot:run
```

或使用 jar 文件：
```bash
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/client-stress-test
java -jar target/message-pipe-client-stress-test-1.0.0.jar
```

**预期输出：**
```
2025-12-23 13:10:02 [main] INFO  ... - === Message Pipe Client Stress Test Started ===
2025-12-23 13:10:03 [main] INFO  ... - Client started on port 8082
2025-12-23 13:10:03 [main] INFO  ... - Successfully registered with message pipe server
```

### 3. 执行压力测试

**终端 3 (执行测试命令):**

#### 发送单条消息
```bash
curl "http://localhost:8081/api/stress/publish?pipe=default-pipe&message=test-message"
```

#### 批量发送 10000 条消息
```bash
curl -X POST http://localhost:8081/api/stress/publish-batch \
  -H "Content-Type: application/json" \
  -d '{
    "pipeName": "default-pipe",
    "count": 10000,
    "messagePrefix": "batch-msg-"
  }'
```

#### 查看 Server 统计
```bash
curl http://localhost:8081/api/stress/stats | python -m json.tool
```

#### 查看 Client 统计（包括处理速率）
```bash
curl http://localhost:8082/api/client/stats | python -m json.tool
```

## 核心测试功能

### Server 端 (端口 8081)

**REST API 端点：**

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/stress/publish` | 发送单条消息 |
| POST | `/api/stress/publish-batch` | 批量发送消息 |
| GET | `/api/stress/stats` | 查看发送统计 |
| GET | `/api/stress/health` | 健康检查 |
| POST | `/api/stress/reset` | 重置统计 |

**请求参数 (单条消息):**
- `pipe` - 管道名称 (默认: default-pipe)
- `message` - 消息内容 (默认: stress-test-message)

**请求体 (批量):**
```json
{
  "pipeName": "order-events",      // 管道名称
  "count": 50000,                  // 发送消息数量
  "messagePrefix": "msg-"          // 消息前缀
}
```

### Client 端 (端口 8082)

**REST API 端点：**

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/client/stats` | 查看处理统计和速率 |
| GET | `/api/client/health` | 健康检查 |
| POST | `/api/client/reset` | 重置统计 |

**统计响应示例：**
```json
{
  "processedMessages": 50000,
  "errorCount": 0,
  "elapsedMillis": 12345,
  "messagesPerSecond": 4050.5,
  "timestamp": 1703332800000
}
```

## 支持的消息处理器

### Server 端接收的消息处理器

1. **DefaultPipeProcessor** - 处理 `default-pipe` 消息
2. **TestPipeProcessor** - 处理 `test-pipe` 消息
3. **OrderEventsProcessor** - 处理 `order-events` 消息

每个处理器会自动输出处理进度（每 1000 条消息输出一次速率信息）。

## 压力测试场景示例

### 场景 1：基础功能测试
```bash
# 发送 100 条消息到 default-pipe
curl -X POST http://localhost:8081/api/stress/publish-batch \
  -H "Content-Type: application/json" \
  -d '{"pipeName": "default-pipe", "count": 100, "messagePrefix": "basic-"}'

# 监控处理进度
watch -n 1 'curl -s http://localhost:8082/api/client/stats | python -m json.tool'
```

### 场景 2：高吞吐量测试
```bash
# 发送 100,000 条消息
curl -X POST http://localhost:8081/api/stress/publish-batch \
  -H "Content-Type: application/json" \
  -d '{"pipeName": "stress-pipe", "count": 100000, "messagePrefix": "stress-"}'

# 在另一个终端查看处理速率
curl http://localhost:8082/api/client/stats
```

### 场景 3：多管道并发测试
```bash
# 在后台向 5 个不同管道发送消息
for i in {1..5}; do
  curl -X POST http://localhost:8081/api/stress/publish-batch \
    -H "Content-Type: application/json" \
    -d "{
      \"pipeName\": \"pipe-$i\",
      \"count\": 20000,
      \"messagePrefix\": \"msg-pipe-$i-\"
    }" &
done
wait

# 查看总体处理统计
curl http://localhost:8082/api/client/stats
```

## 关键特性

### Server 应用特性
- ✓ 基于 gRPC 的消息分发
- ✓ 支持多个命名管道
- ✓ 自动消息计数和错误跟踪
- ✓ RESTful 管理接口
- ✓ 实时统计数据

### Client 应用特性
- ✓ 自动注册和心跳
- ✓ 支持多个消息处理器
- ✓ 实时处理速率计算（msg/sec）
- ✓ 错误恢复机制
- ✓ 监控和统计 API

## 配置说明

### Server 配置 (application.yml)
- Redis 连接：host=10.147.17.17, password=homeserver@2025, db=5
- gRPC 端口：5200
- HTTP 端口：8081
- 最大消息管道数：1000
- 客户端超时：30 秒

### Client 配置 (application.yml)
- Redis 连接：同上
- Server 地址：localhost:5200
- 客户端监听端口：5201
- HTTP 端口：8082
- 心跳间隔：10 秒

## 故障排除

### 问题：无法连接到 Redis
**解决方案：**
- 验证 Redis 是否在 10.147.17.17:6379 运行
- 验证网络连接和防火墙设置
- 确认 Redis 密码正确

### 问题：Client 无法连接到 Server
**解决方案：**
- 确保 Server 应用已启动
- 检查 gRPC 端口 5200 是否开放
- 查看应用日志获取详细错误信息

### 问题：消息处理缓慢
**解决方案：**
- 增加 Redis 连接池大小
- 检查系统资源（CPU、内存）
- 检查网络延迟
- 在 application.yml 中调整 timeout 配置

## 性能基准指标

运行测试后，关注这些指标：

- **消息吞吐量** - `messagesPerSecond` (应该在 1000+ msg/sec)
- **总处理时间** - `elapsedMillis`
- **错误率** - `errorCount` (应该为 0)
- **内存使用** - JVM 堆内存占用
- **Redis 连接** - 活动连接数

## 清理和重置

### 重置统计数据
```bash
# Server 端
curl -X POST http://localhost:8081/api/stress/reset

# Client 端
curl -X POST http://localhost:8082/api/client/reset
```

### 清理编译产物
```bash
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/server-stress-test && mvn clean
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/client-stress-test && mvn clean
```

### 查看原始 Redis 数据（可选）
```bash
# 连接到 Redis CLI
redis-cli -h 10.147.17.17 -a homeserver@2025

# 选择数据库 5
> SELECT 5

# 查看所有 key
> KEYS *

# 查看具体数据大小
> INFO memory
```

## 注意事项

1. **不会提交到 Git** - 测试项目在独立分支 `load-test/stress-test` 和目录中
2. **Redis 数据库隔离** - 使用数据库 5 以避免覆盖其他数据
3. **自动日志轮转** - 查看 `logs/` 目录（如有配置）
4. **生产环境** - 这些是测试项目，生产环境需要额外的性能优化和监控

## 更多资源

- Message Pipe 官方：https://github.com/minbox-projects/message-pipe
- Message Pipe 示例：https://github.com/minbox-projects/message-pipe-example
- 本工作区完整文档：`/Users/yuqiyu/OpenSource/message-pipe-example/stress-test/README.md`

---

**创建时间：** 2025-12-23
**Message Pipe 版本：** 1.0.8-SNAPSHOT
**Java 版本：** 17+
**Spring Boot 版本：** 2.7.14
