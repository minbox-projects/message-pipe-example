# MessagePipe 多管道压力测试工具

## 📌 项目简介

这是一个用于 MessagePipe 框架的**多管道并发压力测试工具**，能够：

- 🎯 **创建 1000+ 个独立消息管道**
- 📊 **每个管道写入 1000+ 条消息**
- ⚡ **支持 20+ 并发任务**
- 📈 **实时监控和性能统计**
- 🎬 **详细的最终报告**

总写入消息数可达 **100 万+**，完整的吞吐量分析。

---

## 🚀 快速开始（2 分钟）

### 前置要求

- Java 11+ 或 Python 3.6+
- Maven 3.6+（如果使用 Java）
- 网络连接

### 方式一：使用 Python 脚本（推荐 ⭐）

```bash
# 1. 安装依赖
pip3 install requests

# 2. 启动服务器
cd stress-test/server-stress-test
mvn clean package
java -jar target/server-stress-test-*-SNAPSHOT.jar --server.port=8081 &

# 3. 运行压测（新终端）
cd ../..
python3 stress_test.py
```

### 方式二：使用 Shell 脚本

```bash
# 一键启动（包括编译和运行）
chmod +x stress-test/run-stress-test.sh
./stress-test/run-stress-test.sh
```

---

## 📖 文档导航

| 文档 | 说明 | 用途 |
|------|------|------|
| **QUICK_START.md** | 30 秒快速开始 | 快速上手 |
| **STRESS_TEST_GUIDE.md** | 完整使用文档 | 详细了解 |
| **IMPLEMENTATION_SUMMARY.md** | 实现细节 | 理解技术 |
| **REQUIREMENTS.txt** | Python 依赖 | 环境配置 |

---

## 💻 常用命令

### 基础命令

```bash
# 查看帮助
python3 stress_test.py --help

# 运行默认压测（1000 管道 × 1000 消息）
python3 stress_test.py

# 快速测试（适合首次运行）
python3 stress_test.py --pipes 10 --messages 100
```

### 自定义参数

```bash
# 100 个管道，每个 500 条消息
python3 stress_test.py --pipes 100 --messages 500

# 增加并发度到 50
python3 stress_test.py --workers 50

# 连接远程服务器
python3 stress_test.py --server http://192.168.1.100:8081

# 完整示例
python3 stress_test.py \
  --server http://localhost:8081 \
  --pipes 500 \
  --messages 1000 \
  --workers 30 \
  --timeout 60
```

---

## 📊 输出示例

### 实时进度（每 5 秒）

```
[2024-12-25 14:30:45] === 压测进度 (耗时: 20s) ===
[2024-12-25 14:30:45] 已完成管道: 250/1000, 已写入消息: 250000 条
[2024-12-25 14:30:45]   ✓ pipe-0 -> 消息数: 1000, 耗时: 245.32 ms
[2024-12-25 14:30:45]   ✓ pipe-1 -> 消息数: 1000, 耗时: 239.18 ms
[2024-12-25 14:30:45]   ... 还有 240 个管道
```

### 最终统计

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

## 📁 项目结构

```
stress-test/
├── stress_test.py              # Python 压测工具（推荐）
├── run-stress-test.sh          # 一键启动脚本
├── QUICK_START.md              # 快速开始指南
├── STRESS_TEST_GUIDE.md        # 完整使用文档
├── STRESS_TEST_README.md       # 本文件
├── IMPLEMENTATION_SUMMARY.md   # 实现细节
├── REQUIREMENTS.txt            # Python 依赖
├── server-stress-test/         # 服务器模块
│   └── src/main/java/...
├── client-stress-test/         # 客户端模块
│   ├── src/main/java/...
│   └── MultiPipeStressTest.java # Java 版压测工具
└── README.md                   # 原始项目文档
```

---

## ⚙️ 配置参数

```
--server URL          服务器地址 (默认: http://localhost:8081)
--pipes NUM          管道数量 (默认: 1000)
--messages NUM       每个管道的消息数 (默认: 1000)
--workers NUM        并发任务数 (默认: 20)
--timeout SECONDS    请求超时时间 (默认: 30)
```

---

## 🔍 故障排除

### 连接失败

```
error: Connection refused
```

**解决**：确保服务器已启动
```bash
curl http://localhost:8081/api/stress/health
```

### 超时错误

```
ReadTimeout
```

**解决**：增加超时时间
```bash
python3 stress_test.py --timeout 60
```

### 权限问题

```
Permission denied
```

**解决**：添加执行权限
```bash
chmod +x stress_test.py run-stress-test.sh
```

更多问题，见 **STRESS_TEST_GUIDE.md** 的"故障排除"章节。

---

## 🎯 性能参考

| 场景 | 管道数 | 消息数 | 吞吐量 | 耗时 |
|------|--------|---------|--------|-------|
| 快速测试 | 10 | 100 | 5K msg/s | ~2s |
| 标准测试 | 1000 | 1000 | 20K msg/s | ~50s |
| 大规模测试 | 5000 | 1000 | 25K msg/s | ~200s |

*实际性能取决于硬件配置和网络环境*

---

## 💡 最佳实践

1. **首次运行**：从小规模测试开始
   ```bash
   python3 stress_test.py --pipes 10 --messages 100
   ```

2. **性能基准**：记录首次完整压测的结果
   ```bash
   python3 stress_test.py | tee baseline.log
   ```

3. **监控服务器**：在另一个终端监控服务器状态
   ```bash
   watch -n 1 'curl -s http://localhost:8081/api/stress/stats'
   ```

4. **优化参数**：根据结果调整并发度和超时时间

---

## 📞 获取帮助

### 查看完整文档

- **快速开始**：`QUICK_START.md`
- **详细文档**：`STRESS_TEST_GUIDE.md`
- **实现细节**：`IMPLEMENTATION_SUMMARY.md`

### 命令行帮助

```bash
python3 stress_test.py --help
```

### 常见问题

见 `STRESS_TEST_GUIDE.md` 的 **"常见问题"** 章节

---

## 🔗 相关资源

- [MessagePipe 官方文档](https://github.com/minbox-projects/message-pipe)
- [项目 README](README.md)
- [压测指南](STRESS_TEST_GUIDE.md)

---

## 📝 更新日志

### v1.0.0 (2024-12-25)
- ✅ 实现多管道并发压测工具
- ✅ Python 脚本版本（推荐）
- ✅ Java 工具类版本
- ✅ 完整的文档和示例
- ✅ 一键启动脚本

---

## 📄 许可证

MIT License

---

## 🙋 反馈和建议

欢迎提交 Issue 和 Pull Request！

---

**提示**：
- 📚 新用户请先阅读 `QUICK_START.md`
- 🔧 需要详细配置请查看 `STRESS_TEST_GUIDE.md`
- 💻 想了解实现细节请阅读 `IMPLEMENTATION_SUMMARY.md`

**祝你压测顺利！** 🚀
