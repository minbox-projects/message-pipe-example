package org.minbox.stress.test.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 多管道压力测试工具
 * 创建1000个消息管道，每个管道写入1000条数据
 * 每5秒输出每个管道的写入统计信息
 */
@Component
@Slf4j
public class MultiPipeStressTest implements ApplicationRunner {

    private static final int TOTAL_PIPES = 1000;
    private static final int MESSAGES_PER_PIPE = 1000;
    private static final int STATS_INTERVAL_SECONDS = 5;
    private static final String SERVER_URL = "http://localhost:8081/api/stress/publish-batch";

    private RestTemplate restTemplate;
    private final Map<String, PipeStatistics> pipeStatsMap = new ConcurrentHashMap<>();
    private final AtomicLong totalStartTime = new AtomicLong(0);
    private volatile boolean running = true;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 如果命令行参数中包含 stress-test，则运行压测
        if (args.containsOption("stress-test")) {
            initializeRestTemplate();
            startStressTest();
        }
    }

    private void initializeRestTemplate() {
        this.restTemplate = new RestTemplateBuilder().build();
    }

    private void startStressTest() throws InterruptedException {
        log.info("=== 开始多管道压测 ===");
        log.info("管道数量: {}, 每个管道消息数: {}", TOTAL_PIPES, MESSAGES_PER_PIPE);

        totalStartTime.set(System.currentTimeMillis());
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        // 启动统计输出线程
        Thread statsThread = new Thread(this::printStatistics);
        statsThread.setDaemon(true);
        statsThread.start();

        // 提交压测任务
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < TOTAL_PIPES; i++) {
            String pipeName = "pipe-" + i;
            futures.add(executorService.submit(() -> publishBatchToPipe(pipeName)));
        }

        // 等待所有任务完成
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                log.error("任务执行异常", e);
            }
        }

        running = false;
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // 最后输出一次统计
        printFinalStatistics();
    }

    private void publishBatchToPipe(String pipeName) {
        try {
            BatchPublishRequest request = new BatchPublishRequest();
            request.setPipeName(pipeName);
            request.setCount(MESSAGES_PER_PIPE);
            request.setMessagePrefix(pipeName + "-");

            long startTime = System.currentTimeMillis();
            BatchPublishResponse response = restTemplate.postForObject(SERVER_URL, request, BatchPublishResponse.class);
            long duration = System.currentTimeMillis() - startTime;

            if (response != null && response.isSuccess()) {
                PipeStatistics stats = pipeStatsMap.computeIfAbsent(pipeName, k -> new PipeStatistics(pipeName));
                stats.setTotalMessages(MESSAGES_PER_PIPE);
                stats.setDuration(duration);
                stats.setSuccess(true);
                log.debug("管道 {} 完成: 耗时 {} ms", pipeName, duration);
            } else {
                PipeStatistics stats = pipeStatsMap.computeIfAbsent(pipeName, k -> new PipeStatistics(pipeName));
                stats.setSuccess(false);
                log.warn("管道 {} 写入失败", pipeName);
            }
        } catch (Exception e) {
            PipeStatistics stats = pipeStatsMap.computeIfAbsent(pipeName, k -> new PipeStatistics(pipeName));
            stats.setSuccess(false);
            log.error("管道 {} 写入异常", pipeName, e);
        }
    }

    private void printStatistics() {
        while (running) {
            try {
                Thread.sleep(STATS_INTERVAL_SECONDS * 1000L);
                outputCurrentStats();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void outputCurrentStats() {
        long elapsedSeconds = (System.currentTimeMillis() - totalStartTime.get()) / 1000;
        long completedPipes = pipeStatsMap.values().stream()
                .filter(PipeStatistics::isSuccess)
                .count();
        long totalMessages = pipeStatsMap.values().stream()
                .filter(PipeStatistics::isSuccess)
                .mapToLong(PipeStatistics::getTotalMessages)
                .sum();

        log.info("=== 压测进度 (耗时: {}s) ===", elapsedSeconds);
        log.info("已完成管道: {}/{}, 已写入消息: {} 条", completedPipes, TOTAL_PIPES, totalMessages);

        // 输出前10个管道的详细信息
        pipeStatsMap.values().stream()
                .sorted(Comparator.comparing(PipeStatistics::getPipeName))
                .limit(10)
                .forEach(stats -> log.info("  {} -> 消息数: {}, 耗时: {} ms",
                        stats.getPipeName(), stats.getTotalMessages(), stats.getDuration()));

        if (completedPipes > 10) {
            log.info("  ... 还有 {} 个管道", completedPipes - 10);
        }
    }

    private void printFinalStatistics() {
        long totalElapsedMillis = System.currentTimeMillis() - totalStartTime.get();
        long totalElapsedSeconds = totalElapsedMillis / 1000;
        long completedPipes = pipeStatsMap.values().stream()
                .filter(PipeStatistics::isSuccess)
                .count();
        long totalMessages = pipeStatsMap.values().stream()
                .filter(PipeStatistics::isSuccess)
                .mapToLong(PipeStatistics::getTotalMessages)
                .sum();

        long avgDuration = completedPipes > 0 ?
                pipeStatsMap.values().stream()
                        .filter(PipeStatistics::isSuccess)
                        .mapToLong(PipeStatistics::getDuration)
                        .sum() / completedPipes : 0;

        log.info("\n");
        log.info("========== 压测最终统计 ==========");
        log.info("总耗时: {} 秒 ({} ms)", totalElapsedSeconds, totalElapsedMillis);
        log.info("成功完成管道: {}/{}", completedPipes, TOTAL_PIPES);
        log.info("失败管道: {}", TOTAL_PIPES - completedPipes);
        log.info("总写入消息数: {}", totalMessages);
        log.info("平均每个管道耗时: {} ms", avgDuration);
        log.info("写入吞吐量: {:.2f} 消息/秒", (totalMessages * 1000.0) / totalElapsedMillis);
        log.info("==================================\n");
    }

    /**
     * 管道统计信息
     */
    public static class PipeStatistics {
        private final String pipeName;
        private long totalMessages;
        private long duration;
        private boolean success;

        public PipeStatistics(String pipeName) {
            this.pipeName = pipeName;
            this.totalMessages = 0;
            this.duration = 0;
            this.success = false;
        }

        // Getters and Setters
        public String getPipeName() { return pipeName; }
        public long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }

    /**
     * 批量发布请求
     */
    public static class BatchPublishRequest {
        private String pipeName;
        private int count;
        private String messagePrefix;

        public String getPipeName() { return pipeName; }
        public void setPipeName(String pipeName) { this.pipeName = pipeName; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public String getMessagePrefix() { return messagePrefix; }
        public void setMessagePrefix(String messagePrefix) { this.messagePrefix = messagePrefix; }
    }

    /**
     * 批量发布响应
     */
    public static class BatchPublishResponse {
        private boolean success;
        private String message;
        private long totalMessages;
        private long totalErrors;
        private long duration;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }
        public long getTotalErrors() { return totalErrors; }
        public void setTotalErrors(long totalErrors) { this.totalErrors = totalErrors; }
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
    }
}
