#!/usr/bin/env python3
"""
MessagePipe 多管道压力测试工具 (Python 版本)

功能:
- 并发创建 1000 个消息管道
- 每个管道写入 1000 条数据
- 每 5 秒输出压测进度和统计信息
"""

import requests
import threading
import time
import json
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime
from collections import defaultdict
import argparse

# 配置
DEFAULT_SERVER_URL = "http://localhost:8081"
BATCH_PUBLISH_ENDPOINT = "/api/stress/publish-batch"
TOTAL_PIPES = 1000
MESSAGES_PER_PIPE = 1000
STATS_INTERVAL = 5  # 秒
REQUEST_TIMEOUT = 30  # 秒
MAX_WORKERS = 20  # 并发数

# 全局统计
pipe_stats = defaultdict(dict)
total_start_time = None
lock = threading.Lock()
running = True


class Colors:
    """ANSI 颜色代码"""
    HEADER = '\033[95m'
    BLUE = '\033[94m'
    CYAN = '\033[96m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'


def log_info(msg):
    """输出信息日志"""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"{Colors.GREEN}[{timestamp}]{Colors.ENDC} {msg}")


def log_warn(msg):
    """输出警告日志"""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"{Colors.YELLOW}[{timestamp}]{Colors.ENDC} {msg}")


def log_error(msg):
    """输出错误日志"""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"{Colors.RED}[{timestamp}]{Colors.ENDC} {msg}")


def log_debug(msg):
    """输出调试日志"""
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"{Colors.CYAN}[{timestamp}]{Colors.ENDC} {msg}")


def publish_batch_to_pipe(server_url, pipe_name, count):
    """发布批量消息到指定管道"""
    try:
        url = server_url + BATCH_PUBLISH_ENDPOINT
        payload = {
            "pipeName": pipe_name,
            "count": count,
            "messagePrefix": pipe_name + "-"
        }

        start_time = time.time()
        response = requests.post(url, json=payload, timeout=REQUEST_TIMEOUT)
        duration = (time.time() - start_time) * 1000  # 毫秒

        if response.status_code == 200:
            data = response.json()
            if data.get("success"):
                with lock:
                    pipe_stats[pipe_name] = {
                        "count": count,
                        "duration": duration,
                        "success": True,
                        "timestamp": time.time()
                    }
                log_debug(f"管道 {pipe_name} 完成: 耗时 {duration:.2f} ms")
                return True
            else:
                with lock:
                    pipe_stats[pipe_name] = {
                        "count": count,
                        "duration": duration,
                        "success": False,
                        "error": data.get("message", "Unknown error")
                    }
                log_warn(f"管道 {pipe_name} 写入失败: {data.get('message')}")
                return False
        else:
            with lock:
                pipe_stats[pipe_name] = {
                    "count": count,
                    "duration": duration,
                    "success": False,
                    "error": f"HTTP {response.status_code}"
                }
            log_error(f"管道 {pipe_name} 返回错误: HTTP {response.status_code}")
            return False

    except requests.exceptions.RequestException as e:
        with lock:
            pipe_stats[pipe_name] = {
                "count": count,
                "success": False,
                "error": str(e)
            }
        log_error(f"管道 {pipe_name} 请求异常: {e}")
        return False


def print_statistics():
    """定期输出压测统计"""
    global running

    while running:
        time.sleep(STATS_INTERVAL)

        with lock:
            if not pipe_stats:
                continue

            elapsed_seconds = time.time() - total_start_time
            completed_pipes = sum(1 for s in pipe_stats.values() if s.get("success"))
            total_messages = sum(s.get("count", 0) for s in pipe_stats.values() if s.get("success"))

            print()
            print(f"{Colors.BOLD}{Colors.BLUE}=== 压测进度 (耗时: {int(elapsed_seconds)}s) ==={Colors.ENDC}")
            print(f"{Colors.CYAN}已完成管道: {completed_pipes}/{TOTAL_PIPES}, "
                  f"已写入消息: {total_messages} 条{Colors.ENDC}")

            # 显示前 10 个管道的详细信息
            sorted_pipes = sorted(pipe_stats.items(), key=lambda x: x[0])
            for i, (pipe_name, stats) in enumerate(sorted_pipes[:10]):
                if stats.get("success"):
                    duration = stats.get("duration", 0)
                    print(f"  {Colors.GREEN}✓{Colors.ENDC} {pipe_name} -> 消息数: {stats.get('count')}, "
                          f"耗时: {duration:.2f} ms")
                else:
                    error = stats.get("error", "Unknown")
                    print(f"  {Colors.RED}✗{Colors.ENDC} {pipe_name} -> 失败: {error}")

            if completed_pipes > 10:
                print(f"  ... 还有 {completed_pipes - 10} 个管道")


def print_final_statistics():
    """输出最终统计信息"""
    total_elapsed = time.time() - total_start_time
    completed_pipes = sum(1 for s in pipe_stats.values() if s.get("success"))
    failed_pipes = sum(1 for s in pipe_stats.values() if not s.get("success"))
    skipped_pipes = TOTAL_PIPES - len(pipe_stats)
    total_messages = sum(s.get("count", 0) for s in pipe_stats.values() if s.get("success"))

    avg_duration = 0
    if completed_pipes > 0:
        total_duration = sum(s.get("duration", 0) for s in pipe_stats.values() if s.get("success"))
        avg_duration = total_duration / completed_pipes

    throughput = 0
    if total_elapsed > 0:
        throughput = total_messages / total_elapsed

    print()
    print(f"{Colors.BOLD}{Colors.BLUE}========== 压测最终统计 =========={Colors.ENDC}")
    print(f"总耗时: {int(total_elapsed)} 秒 ({total_elapsed:.2f}s)")
    print(f"成功完成管道: {completed_pipes}/{TOTAL_PIPES}")
    print(f"失败管道: {failed_pipes}")
    if skipped_pipes > 0:
        print(f"跳过管道: {skipped_pipes}")
    print(f"总写入消息数: {total_messages}")
    print(f"平均每个管道耗时: {avg_duration:.2f} ms")
    print(f"{Colors.GREEN}写入吞吐量: {throughput:.2f} 消息/秒{Colors.ENDC}")
    print(f"{Colors.BOLD}{Colors.BLUE}=================================={Colors.ENDC}")
    print()


def run_stress_test(server_url, num_pipes, messages_per_pipe, max_workers):
    """运行压力测试"""
    global total_start_time, running, TOTAL_PIPES, MESSAGES_PER_PIPE

    # 更新全局配置参数
    TOTAL_PIPES = num_pipes
    MESSAGES_PER_PIPE = messages_per_pipe

    log_info(f"{Colors.BOLD}{Colors.BLUE}开始多管道压测{Colors.ENDC}")
    log_info(f"服务器地址: {server_url}")
    log_info(f"管道数量: {num_pipes}")
    log_info(f"每个管道消息数: {messages_per_pipe}")
    log_info(f"并发度: {max_workers}")
    print()

    total_start_time = time.time()

    # 启动统计输出线程
    stats_thread = threading.Thread(target=print_statistics, daemon=True)
    stats_thread.start()

    # 使用线程池执行压测任务
    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        futures = {
            executor.submit(publish_batch_to_pipe, server_url, f"pipe-{i}", messages_per_pipe): i
            for i in range(num_pipes)
        }

        completed = 0
        for future in as_completed(futures):
            completed += 1
            try:
                future.result()
            except Exception as e:
                log_error(f"任务执行异常: {e}")

            # 每 100 个任务完成时输出进度
            if completed % 100 == 0:
                elapsed = time.time() - total_start_time
                rate = completed / elapsed if elapsed > 0 else 0
                log_info(f"进度: {completed}/{num_pipes} ({rate:.1f} pipes/sec)")

    running = False
    time.sleep(1)  # 等待统计线程输出最后的信息

    print_final_statistics()


def main():
    """主函数"""
    parser = argparse.ArgumentParser(
        description='MessagePipe 多管道压力测试工具',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
示例:
  # 使用默认参数运行压测
  python3 stress_test.py

  # 指定服务器地址
  python3 stress_test.py --server http://127.0.0.1:8081

  # 自定义管道数量和消息数
  python3 stress_test.py --pipes 100 --messages 500 --workers 10
        """)

    parser.add_argument('--server', type=str, default=DEFAULT_SERVER_URL,
                        help='MessagePipe 服务器地址 (默认: %(default)s)')
    parser.add_argument('--pipes', type=int, default=TOTAL_PIPES,
                        help='创建的消息管道数量 (默认: %(default)s)')
    parser.add_argument('--messages', type=int, default=MESSAGES_PER_PIPE,
                        help='每个管道写入的消息数 (默认: %(default)s)')
    parser.add_argument('--workers', type=int, default=MAX_WORKERS,
                        help='并发任务数 (默认: %(default)s)')
    parser.add_argument('--timeout', type=int, default=REQUEST_TIMEOUT,
                        help='请求超时时间(秒) (默认: %(default)s)')

    args = parser.parse_args()

    # 验证输入
    if args.pipes <= 0:
        log_error("管道数量必须大于 0")
        return
    if args.messages <= 0:
        log_error("消息数必须大于 0")
        return
    if args.workers <= 0:
        log_error("并发数必须大于 0")
        return

    # 检查服务器连接
    try:
        log_info(f"检查服务器连接: {args.server}")
        response = requests.get(f"{args.server}/api/stress/health", timeout=5)
        if response.status_code == 200:
            log_info(f"{Colors.GREEN}服务器连接成功{Colors.ENDC}")
        else:
            log_error(f"服务器返回错误: HTTP {response.status_code}")
            return
    except Exception as e:
        log_error(f"无法连接到服务器: {e}")
        log_error(f"请确保 MessagePipe 服务器运行在 {args.server}")
        return

    # 运行压测
    print()
    run_stress_test(args.server, args.pipes, args.messages, args.workers)


if __name__ == '__main__':
    main()
