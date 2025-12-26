# 快速开始指南

## 30 秒快速上手

### 1️⃣ 启动服务器
```bash
cd stress-test/server-stress-test
mvn clean package
java -jar target/server-stress-test-*-SNAPSHOT.jar --server.port=8081
```

### 2️⃣ 运行压测（新终端）
```bash
cd stress-test
python3 stress_test.py
```

**完成！** 压测将自动运行 1000 个管道，每个 1000 条消息。

---

## 常用命令

### 查看帮助
```bash
python3 stress_test.py --help
```

### 快速测试（小规模）
```bash
# 10 个管道，每个 100 条消息
python3 stress_test.py --pipes 10 --messages 100
```

### 完整压测（标准）
```bash
# 1000 个管道，每个 1000 条消息
python3 stress_test.py --pipes 1000 --messages 1000
```

### 高性能测试（大规模）
```bash
# 5000 个管道，并发度 50
python3 stress_test.py --pipes 5000 --workers 50
```

### 远程服务器压测
```bash
python3 stress_test.py --server http://192.168.1.100:8081
```

---

## 输出样例

### 实时进度（每 5 秒）
```
[2024-12-25 14:30:45] === 压测进度 (耗时: 20s) ===
[2024-12-25 14:30:45] 已完成管道: 250/1000, 已写入消息: 250000 条
[2024-12-25 14:30:45]   ✓ pipe-0 -> 消息数: 1000, 耗时: 245.32 ms
[2024-12-25 14:30:45]   ✓ pipe-1 -> 消息数: 1000, 耗时: 239.18 ms
```

### 最终结果
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

---

## 参数说明

| 参数 | 说明 | 默认值 | 示例 |
|------|------|--------|------|
| `--server` | 服务器地址 | `http://localhost:8081` | `--server http://192.168.1.1:8081` |
| `--pipes` | 管道数量 | 1000 | `--pipes 500` |
| `--messages` | 每个管道的消息数 | 1000 | `--messages 2000` |
| `--workers` | 并发任务数 | 20 | `--workers 50` |
| `--timeout` | 请求超时(秒) | 30 | `--timeout 60` |

---

## 故障排除

### ❌ 连接失败
```
error: Errno 111] Connection refused
```
**解决**：确保服务器已启动在 `http://localhost:8081`

### ❌ 超时
```
ReadTimeout: HTTPSConnectionPool(host='localhost')
```
**解决**：增加超时时间
```bash
python3 stress_test.py --timeout 60
```

### ❌ 权限不足
```
Permission denied: 'stress_test.py'
```
**解决**：添加执行权限
```bash
chmod +x stress_test.py
```

---

## 性能参考

根据硬件配置，典型的性能指标：

| 场景 | 管道数 | 消息数 | 吞吐量 | 耗时 |
|------|--------|---------|--------|-------|
| **小规模** | 100 | 100 | 5K msg/s | ~2s |
| **中规模** | 1000 | 1000 | 20K msg/s | ~50s |
| **大规模** | 5000 | 1000 | 25K msg/s | ~200s |

*注：实际性能取决于硬件配置和网络环境*

---

## 更多信息

- 📖 详细文档：见 `STRESS_TEST_GUIDE.md`
- 🔧 实现细节：见 `IMPLEMENTATION_SUMMARY.md`
- 🐍 Python 脚本：`stress_test.py`
- ☕ Java 工具：`client-stress-test/`

---

**提示**：首次运行时，建议从小规模测试开始，逐步增加压力。
