# message-pipe-example
提供 "message-pipe" 使用示例



## 1. 环境准备

由于`message-pipe`是基于`Redis`实现的，所以我们本地需要安装`Redis`。

### Docker方式

```sh
# 拉取Redis镜像
docker pull redis
# 创建一个名为"redis"的后台运行容器，端口号映射宿主机6379
docker run --name redis -d -p 6379:6379 redis
```

### 查看Redis数据

```sh
# 运行容器内命令
docker exec -it redis /bin/sh
# 运行Redis客户端
redis-cli
# 选择索引为1的数据库
select 1
# 查看全部的数据
keys *
```



## 2. 启动示例项目

```sh
# 下载源码
git clone https://github.com/minbox-projects/message-pipe-example.git
# 进入项目目录
cd message-pipe-example/client-server-merge
# 运行项目
mvn spring-boot:run
```

