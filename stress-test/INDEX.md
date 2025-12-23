# Message Pipe 1.0.8-SNAPSHOT 压力测试项目索引

## 📍 项目位置

```
/Users/yuqiyu/OpenSource/message-pipe-example/stress-test/
```

## 📚 文档导航

### 新手入门
👉 **从这里开始：** [QUICK_START.md](QUICK_START.md)
- 3 步快速启动
- API 端点说明
- 简单的测试命令

### 完整文档
📖 **详细参考：** [README.md](README.md)
- 完整的项目设置
- 多个测试场景
- 监控和统计方法
- 故障排除指南

### 项目总结
📋 **项目概览：** [PROJECT_SUMMARY.txt](PROJECT_SUMMARY.txt)
- 技术栈信息
- 配置参数
- 性能基准
- 常见问题

## 🚀 快速开始

### 3 个终端启动

**终端 1 - 启动 Server：**
```bash
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/server-stress-test
mvn spring-boot:run
```

**终端 2 - 启动 Client：**
```bash
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/client-stress-test
mvn spring-boot:run
```

**终端 3 - 执行测试：**
```bash
# 批量发送 10,000 条消息
curl -X POST http://localhost:8081/api/stress/publish-batch \
  -H "Content-Type: application/json" \
  -d '{
    "pipeName": "test-pipe",
    "count": 10000,
    "messagePrefix": "stress-test-"
  }'

# 查看处理统计和速率
curl http://localhost:8082/api/client/stats | python -m json.tool
```

## 📁 项目结构

```
stress-test/
├── INDEX.md                           # 本文件（项目导航）
├── QUICK_START.md                     # 快速开始指南
├── README.md                          # 完整项目文档
├── PROJECT_SUMMARY.txt                # 项目总结
├── setup-projects.sh                  # 项目初始化脚本
│
├── server-stress-test/                # Server 压力测试项目
│   ├── pom.xml                        # Maven 配置
│   ├── src/
│   │   └── main/
│   │       ├── java/.../ServerStressTestApplication.java
│   │       └── resources/application.yml
│   └── target/
│       └── message-pipe-server-stress-test-1.0.0.jar
│
└── client-stress-test/                # Client 压力测试项目
    ├── pom.xml                        # Maven 配置
    ├── src/
    │   └── main/
    │       ├── java/.../ClientStressTestApplication.java
    │       └── resources/application.yml
    └── target/
        └── message-pipe-client-stress-test-1.0.0.jar
```

## 🎯 应用端口

- **Server HTTP API**：8081
- **Server gRPC**：5200
- **Client HTTP API**：8082
- **Client gRPC 监听**：5201

## 🌐 Redis 配置

```
Host: 10.147.17.17
Port: 6379
Password: homeserver@2025
Database: 5
```

## 🔧 核心 API 端点

### Server 端 (8081)

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/stress/publish` | 发送单条消息 |
| POST | `/api/stress/publish-batch` | 批量发送消息 |
| GET | `/api/stress/stats` | 查看统计 |
| GET | `/api/stress/health` | 健康检查 |
| POST | `/api/stress/reset` | 重置统计 |

### Client 端 (8082)

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/client/stats` | 查看处理速率 |
| GET | `/api/client/health` | 健康检查 |
| POST | `/api/client/reset` | 重置统计 |

## 📊 支持的消息处理器

1. **DefaultPipeProcessor** - 处理 `default-pipe` 管道
2. **TestPipeProcessor** - 处理 `test-pipe` 管道
3. **OrderEventsProcessor** - 处理 `order-events` 管道

## ✨ 功能特性

- ✓ 基于 gRPC 的高性能消息分发
- ✓ 支持多个命名管道并发操作
- ✓ 实时消息吞吐量统计（msg/sec）
- ✓ RESTful 管理接口
- ✓ 自动错误恢复
- ✓ 心跳检测
- ✓ 分布式消息顺序保证

## 🧪 测试场景

### 基础功能
```bash
curl "http://localhost:8081/api/stress/publish?pipe=default-pipe&message=test"
```

### 高吞吐量（10,000 条消息）
```bash
curl -X POST http://localhost:8081/api/stress/publish-batch \
  -H "Content-Type: application/json" \
  -d '{"pipeName":"test-pipe","count":10000,"messagePrefix":"msg-"}'
```

### 超大规模（100,000 条消息）
```bash
curl -X POST http://localhost:8081/api/stress/publish-batch \
  -H "Content-Type: application/json" \
  -d '{"pipeName":"stress-pipe","count":100000,"messagePrefix":"msg-"}'
```

### 多管道并发
```bash
for i in {1..5}; do
  curl -X POST http://localhost:8081/api/stress/publish-batch \
    -H "Content-Type: application/json" \
    -d "{\"pipeName\":\"pipe-$i\",\"count\":10000,\"messagePrefix\":\"msg-$i-\"}" &
done
```

## 🔍 监控命令

```bash
# 查看 Server 统计
curl http://localhost:8081/api/stress/stats | python -m json.tool

# 查看 Client 处理速率
curl http://localhost:8082/api/client/stats | python -m json.tool

# Server 健康检查
curl http://localhost:8081/api/stress/health

# Client 健康检查
curl http://localhost:8082/api/client/health

# 实时监控处理速率（每秒刷新）
watch -n 1 'curl -s http://localhost:8082/api/client/stats | python -m json.tool'
```

## 🛠️ 重新编译

```bash
# Server 项目
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/server-stress-test
mvn clean package -DskipTests

# Client 项目
cd /Users/yuqiyu/OpenSource/message-pipe-example/stress-test/client-stress-test
mvn clean package -DskipTests
```

## 🔌 JAR 文件

| 名称 | 路径 | 大小 |
|------|------|------|
| Server | `server-stress-test/target/message-pipe-server-stress-test-1.0.0.jar` | 11 KB |
| Client | `client-stress-test/target/message-pipe-client-stress-test-1.0.0.jar` | 13 KB |

## 📖 技术栈

- **Java**：17+
- **Spring Boot**：2.7.14
- **Message Pipe**：1.0.8-SNAPSHOT
- **gRPC**：for server-client communication
- **Redisson**：3.20.1 (Redis client)
- **Lombok**：1.18.30
- **Maven**：3.6+

## ⚡ 性能指标

- **预期吞吐量**：1000-5000 msg/sec
- **消息延迟**：< 100ms（良好网络环境下）
- **内存占用**：Server ~200-500MB, Client ~150-400MB
- **连接池**：max=20, idle=10, min=5

## 📝 常见任务

### 查看日志
```bash
# Server 日志（运行 mvn spring-boot:run 时直接显示）
# 或从 IDE 控制台查看
```

### 修改 Redis 连接
编辑这些文件中的 `application.yml`：
```
server-stress-test/src/main/resources/application.yml
client-stress-test/src/main/resources/application.yml
```

### 重置测试数据
```bash
# 重置 Server 统计
curl -X POST http://localhost:8081/api/stress/reset

# 重置 Client 统计
curl -X POST http://localhost:8082/api/client/reset

# 清理 Redis 中的测试数据（可选）
redis-cli -h 10.147.17.17 -a homeserver@2025 -n 5 FLUSHDB
```

## ℹ️ 项目信息

- **创建日期**：2025-12-23
- **版本**：1.0.8-SNAPSHOT
- **分支**：load-test/stress-test
- **状态**：✓ 完全就绪
- **编译状态**：✓ 成功

## 🤝 支持

- **官方仓库**：https://github.com/minbox-projects/message-pipe
- **示例项目**：https://github.com/minbox-projects/message-pipe-example
- **本项目文档**：见上方"文档导航"

---

**快速导航：** [QUICK_START.md](QUICK_START.md) | [README.md](README.md) | [PROJECT_SUMMARY.txt](PROJECT_SUMMARY.txt)
