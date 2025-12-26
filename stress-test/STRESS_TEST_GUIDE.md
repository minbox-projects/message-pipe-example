# MessagePipe 多管道压力测试指南

本指南介绍如何对 MessagePipe 进行多管道压力测试。

## 功能概述

- **多管道并发写入**：同时创建 1000 个独立消息管道
- **大量数据写入**：每个管道写入 1000 条消息（共 100 万条）
- **实时进度监控**：每 5 秒输出一次压测进度和统计信息
- **性能指标统计**：最终输出详细的性能统计报告

## 前置要求

### 方式一：Python 脚本（推荐）

```bash
# 检查 Python 版本
python3 --version  # 需要 3.6+

# 安装依赖
pip3 install requests
```

### 方式二：Java 应用

```bash
# 检查 Java 版本
java -version  # 需要 JDK 11+

# 检查 Maven
mvn -version  # 需要 3.6+
```

## 快速开始

### 步骤 1：启动 MessagePipe 服务器

```bash
# 编译服务器模块
cd stress-test/server-stress-test
mvn clean package

# 启动服务器（默认端口 8081）
java -jar target/message-pipe-server-stress-test-0.0.1-SNAPSHOT.jar --server.port=8081
```

或者简单地使用构建脚本：

```bash
./run-stress-test.sh
```

服务器启动成功的标志：
```
=== Message Pipe Server Stress Test Started ===
```

### 步骤 2：运行压力测试

#### 使用 Python 脚本（推荐）

```bash
# 使用默认参数（1000 个管道，每个 1000 条消息）
python3 stress_test.py

# 或指定自定义参数
python3 stress_test.py --pipes 100 --messages 500 --workers 20
```

#### 使用 Java 应用

```bash
cd stress-test/client-stress-test
mvn clean package

# 启动客户端压测
java -jar target/message-pipe-client-stress-test-0.0.1-SNAPSHOT.jar --server.port=8082 --stress-test
```

## Python 脚本使用说明

### 命令行选项

```
python3 stress_test.py [选项]

选项:
  --server URL          服务器地址 (默认: http://localhost:8081)
  --pipes NUM          管道数量 (默认: 1000)
  --messages NUM       每个管道的消息数 (默认: 1000)
  --workers NUM        并发任务数 (默认: 20)
  --timeout SECONDS    请求超时时间 (默认: 30)
  --help              显示帮助信息
```

### 使用示例

```bash
# 示例 1：快速测试（100 个管道，每个 100 条消息）
python3 stress_test.py --pipes 100 --messages 100

# 示例 2：增加并发度（测试高吞吐量）
python3 stress_test.py --workers 50

# 示例 3：完整的 1000 管道压测
python3 stress_test.py --pipes 1000 --messages 1000 --workers 30

# 示例 4：连接远程服务器
python3 stress_test.py --server http://192.168.1.100:8081 --pipes 500
```

## 输出说明

### 实时进度输出（每 5 秒）

```
=== 压测进度 (耗时: 15s) ===
已完成管道: 150/1000, 已写入消息: 150000 条
  ✓ pipe-0 -> 消息数: 1000, 耗时: 245.32 ms
  ✓ pipe-1 -> 消息数: 1000, 耗时: 239.18 ms
  ✓ pipe-2 -> 消息数: 1000, 耗时: 251.07 ms
  ...
  ... 还有 140 个管道
```

### 最终统计输出

```
========== 压测最终统计 ==========
总耗时: 45 秒 (45.23s)
成功完成管道: 1000/1000
失败管道: 0
未开始管道: 0
总写入消息数: 1000000
平均每个管道耗时: 245.50 ms
写入吞吐量: 22107.55 消息/秒
==================================
```

## 性能指标解释

| 指标 | 说明 | 单位 |
|------|------|------|
| **总耗时** | 整个压测从开始到完成的总时间 | 秒 |
| **成功完成管道** | 成功写入数据的管道数量 | 个 |
| **失败管道** | 写入失败的管道数量 | 个 |
| **总写入消息数** | 所有成功写入的消息总条数 | 条 |
| **平均管道耗时** | 每个管道平均的写入耗时 | ms |
| **写入吞吐量** | 单位时间内的消息写入速率 | msg/sec |

## 常见问题

### Q1：如何提高压测的吞吐量？

**A：** 可以采用以下措施：

1. **增加并发度**：使用 `--workers` 参数增加并发任务数
   ```bash
   python3 stress_test.py --workers 50  # 从默认的 20 增加到 50
   ```

2. **调整批量大小**：在服务器端修改 `MessagePipeConfiguration.setBatchSize()`

3. **优化网络**：确保网络连接稳定，减少延迟

### Q2：如何测试不同规模的场景？

**A：** 调整 `--pipes` 和 `--messages` 参数：

```bash
# 小规模（1 万消息）
python3 stress_test.py --pipes 10 --messages 1000

# 中规模（100 万消息）
python3 stress_test.py --pipes 1000 --messages 1000

# 大规模（1000 万消息）
python3 stress_test.py --pipes 10000 --messages 1000
```

### Q3：压测失败了怎么办？

**A：** 检查以下几点：

1. **服务器是否启动**：
   ```bash
   curl http://localhost:8081/api/stress/health
   ```

2. **端口是否被占用**：
   ```bash
   lsof -i :8081
   ```

3. **网络连接**：
   ```bash
   ping localhost
   ```

4. **查看服务器日志**：检查服务器的控制台输出是否有错误

### Q4：如何让压测支持更多的并发连接？

**A：** 需要调整系统参数：

```bash
# 增加文件描述符限制（Linux/macOS）
ulimit -n 65535

# 或永久修改 /etc/security/limits.conf（Linux）
* soft nofile 65535
* hard nofile 65535
```

## 性能优化建议

### 服务器端优化

```java
// 在 ServerStressTestApplication.java 中调整

// 1. 增加批量处理大小
configuration.setBatchSize(1000);  // 从 500 增加到 1000

// 2. 增加消息管道上限
.setMaxMessagePipeCount(5000)  // 从 1000 增加到 5000

// 3. 调整线程池大小
.setExpiredPoolSize(50)  // 从 10 增加到 50
```

### 客户端优化

```bash
# 增加客户端并发度
python3 stress_test.py --workers 100

# 增加请求超时时间（如果网络较慢）
python3 stress_test.py --timeout 60
```

## 监控和诊断

### 实时监控服务器状态

```bash
# 在另一个终端中运行
watch -n 1 'curl -s http://localhost:8081/api/stress/stats'
```

### 获取服务器统计信息

```bash
curl http://localhost:8081/api/stress/stats | jq .
```

### 重置统计数据

```bash
curl -X POST http://localhost:8081/api/stress/reset
```

## 高级用法

### 自定义压测脚本

可以修改 `stress_test.py` 的以下变量来自定义压测：

```python
# 修改这些变量来改变压测行为
TOTAL_PIPES = 1000              # 管道数
MESSAGES_PER_PIPE = 1000        # 每个管道的消息数
STATS_INTERVAL = 5              # 统计输出间隔（秒）
REQUEST_TIMEOUT = 30            # 请求超时（秒）
MAX_WORKERS = 20                # 默认并发数
```

### 压测结果对标

参考性能目标（根据具体硬件条件可能有差异）：

| 指标 | 目标 | 说明 |
|------|------|------|
| **写入吞吐量** | > 10,000 msg/sec | 单机服务器 |
| **平均延迟** | < 300 ms | 每个管道 |
| **成功率** | 99.9% | 需处理错误重试 |
| **管道并发** | > 1000 | 同时处理能力 |

## 故障排除

### 问题：连接超时

```
error: timed out connecting to 127.0.0.1:8081
```

**解决方案**：
1. 确认服务器已启动
2. 检查防火墙设置
3. 增加超时时间：`--timeout 60`

### 问题：请求失败

```
ERROR: 管道 pipe-0 请求异常
```

**解决方案**：
1. 查看服务器日志
2. 检查是否有资源不足（内存、连接数等）
3. 降低并发数：`--workers 10`

### 问题：性能不达预期

**解决方案**：
1. 检查网络连接质量
2. 调整批量大小和并发度
3. 检查服务器 CPU 和内存使用情况
4. 查看数据库连接池配置

## 许可证

MIT

## 贡献

欢迎提交 Issue 和 Pull Request！
