# OutOfMemoryError: unable to create native thread 解决方案

## 问题描述
运行压测应用时出现：`java.lang.OutOfMemoryError: unable to create native thread: possibly out of memory or process/resource limits reached`

## 根本原因
系统无法创建新线程，通常由以下原因导致：
1. **堆内存不足** - 无足够内存分配给新线程
2. **线程栈内存不足** - 每个线程需要栈空间（默认1MB）
3. **系统资源限制** - 文件描述符或进程数达到上限

## 已采取的措施

### 1. JVM 堆内存配置（pom.xml）
```xml
<jvmArguments>-Xms512m -Xmx2g -Xss1m</jvmArguments>
```
- **-Xms512m**: 初始堆内存 512MB
- **-Xmx2g**: 最大堆内存 2GB
- **-Xss1m**: 线程栈大小 1MB（减小以支持更多线程）

### 2. 服务器配置优化（ServerConfiguration）
```java
.setExpiredPoolSize(10)                    // 控制在10个线程
.setCheckClientExpiredIntervalSeconds(5)   // 5秒检查一次
.setMaxMessagePipeCount(10000)              // 最大并发管道数
```

## 如果问题仍未解决，请按以下步骤调试

### 方案 A: 增加系统资源限制（macOS/Linux）

#### 1. 查看当前限制
```bash
# 查看文件描述符限制
ulimit -n

# 查看进程数限制
ulimit -u

# 查看所有限制
ulimit -a
```

#### 2. 临时增加限制（当前会话有效）
```bash
# 增加文件描述符到 10000
ulimit -n 10000

# 增加进程数到 4096
ulimit -u 4096
```

#### 3. 永久修改（macOS）
编辑 `~/.zshrc` 或 `~/.bash_profile`：
```bash
# 在文件末尾添加
ulimit -n 10000
ulimit -u 4096
```

#### 4. 永久修改（Linux）
编辑 `/etc/security/limits.conf`：
```
* soft nofile 10000
* hard nofile 10000
* soft nproc 4096
* hard nproc 4096
```
然后重新登录生效。

### 方案 B: 调整 JVM 参数

如果想支持更多线程，可以进一步调整 pom.xml：

```xml
<!-- 内存充足的机器可以使用 -->
<jvmArguments>-Xms1g -Xmx4g -Xss512k</jvmArguments>
```

参数说明：
- **-Xms1g**: 初始堆内存 1GB（更快的启动）
- **-Xmx4g**: 最大堆内存 4GB（支持更多数据）
- **-Xss512k**: 线程栈大小 512KB（更小的栈=更多线程）

> ⚠️ 注意：减小 `-Xss` 可能导致 `StackOverflowError`，需要根据应用实际情况测试

### 方案 C: 优化应用架构

1. **使用线程池而非无限创建线程**
   - 目前已配置 `ExpiredPoolSize=10`，这很合理

2. **异步处理消息**
   - 使用 CompletableFuture 或 Reactor 库处理大量请求

3. **减少并发连接数**
   - 在压测配置中限制并发客户端数量

## 监控线程使用情况

### 查看 Java 进程的线程数
```bash
# 获取进程 ID
jps -l

# 查看该进程的线程数
ps -eLf | grep <process-id> | wc -l

# 或使用 jstack 查看线程详情
jstack <process-id> | grep "tid"
```

### 使用 JVisualVM 或 JProfiler
1. 启动应用
2. 连接 JVisualVM: `jvisualvm`
3. 查看 "线程" 标签页，实时监控线程数

## 压测建议

为避免触发系统限制，建议压测参数：
- **单服务器并发客户端数**: 不超过 100
- **单客户端连接数**: 不超过 50
- **消息批量大小**: 100-1000 条

## 检查清单

- [ ] 修改 pom.xml JVM 参数（已完成）
- [ ] 运行压测前检查 `ulimit -n` 的值（>=1024）
- [ ] 使用 `jstack` 或 `jvisualvm` 监控线程数
- [ ] 逐步增加并发数，找到系统的稳定阈值
- [ ] 监控错误日志中是否有新的异常

## 相关资源

- [Java Thread 官方文档](https://docs.oracle.com/javase/tutorial/essential/concurrency/threads.html)
- [JVM 内存模型](https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-2.html)
- [macOS ulimit 配置](https://docs.oracle.com/en/java/javase/17/troubleshoot/troubleshoot-memory-issues.html)
