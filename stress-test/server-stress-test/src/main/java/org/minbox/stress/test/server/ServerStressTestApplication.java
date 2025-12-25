package org.minbox.stress.test.server;

import lombok.extern.slf4j.Slf4j;
import org.minbox.framework.message.pipe.core.Message;
import org.minbox.framework.message.pipe.server.MessagePipe;
import org.minbox.framework.message.pipe.server.config.MessagePipeConfiguration;
import org.minbox.framework.message.pipe.server.config.ServerConfiguration;
import org.minbox.framework.message.pipe.server.manager.MessagePipeManager;
import org.minbox.framework.message.pipe.spring.annotation.ServerServiceType;
import org.minbox.framework.message.pipe.spring.annotation.server.EnableMessagePipeServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Message Pipe Server Stress Test Application
 * Provides REST endpoints to simulate message publishing for stress testing
 */
@SpringBootApplication
@Slf4j
public class ServerStressTestApplication {

    private static final AtomicLong messageCounter = new AtomicLong(0);
    private static final AtomicLong errorCounter = new AtomicLong(0);

    public static void main(String[] args) {
        SpringApplication.run(ServerStressTestApplication.class, args);
        log.info("=== Message Pipe Server Stress Test Started ===");
    }

    /**
     * Server configuration bean
     */
    @Configuration
    @EnableMessagePipeServer(serverType = ServerServiceType.GRPC)
    public static class ServerConfig {

        /**
         * Global MessagePipe configuration
         * This bean is required by DefaultMessagePipeManager
         */
        @Bean
        public MessagePipeConfiguration messagePipeConfiguration() {
            MessagePipeConfiguration configuration = MessagePipeConfiguration.defaultConfiguration();
            // 每次批量处理1000条消息
            configuration.setBatchSize(1000);
            // customer config
            return configuration;
        }

        @Bean
        public ServerConfiguration serverConfiguration() {
            return new ServerConfiguration()
                    .setServerPort(5200)                              // gRPC server port
                    .setExpiredPoolSize(10)                           // Thread pool size for client expiration
                    .setExpiredExcludeThresholdSeconds(30)            // Client timeout threshold (seconds)
                    .setCheckClientExpiredIntervalSeconds(5)          // Check interval (seconds)
                    .setMaxMessagePipeCount(1000)                     // Max concurrent pipes
                    .setCleanupExpiredMessagePipeIntervalSeconds(10)  // Cleanup interval (seconds)
                    .setCleanupExpiredMessagePipeThresholdSeconds(1800); // Pipe timeout (1800 seconds)
        }
    }

    /**
     * REST Controller for stress test operations
     */
    @Slf4j
    @RestController
    @RequestMapping("/api/stress")
    public static class StressTestController {

        private final MessagePipeManager messagePipeManager;

        public StressTestController(MessagePipeManager messagePipeManager) {
            this.messagePipeManager = messagePipeManager;
        }

        /**
         * Publish a single message to a pipe
         * GET /api/stress/publish?pipe=pipe-name&message=content
         */
        @GetMapping("/publish")
        public StressResponse publishMessage(
                @RequestParam(value = "pipe", defaultValue = "default-pipe") String pipeName,
                @RequestParam(value = "message", defaultValue = "stress-test-message") String messageContent) {

            try {
                MessagePipe pipe = messagePipeManager.createMessagePipe(pipeName);
                Message message = new Message(messageContent.getBytes("UTF-8"));
                pipe.putLast(message);

                long totalMessages = messageCounter.incrementAndGet();

                log.info("Published message to pipe: {} (Total: {})", pipeName, totalMessages);
                return new StressResponse(true, "Message published successfully", totalMessages, errorCounter.get());

            } catch (Exception e) {
                errorCounter.incrementAndGet();
                log.error("Failed to publish message to pipe: {}", pipeName, e);
                return new StressResponse(false, "Error: " + e.getMessage(), messageCounter.get(), errorCounter.get());
            }
        }

        /**
         * Publish messages in batch
         * POST /api/stress/publish-batch
         * Body: {"pipeName": "test-pipe", "count": 100, "messagePrefix": "msg-"}
         */
        @PostMapping("/publish-batch")
        public StressResponse publishBatch(@RequestBody BatchPublishRequest request) {
            long startTime = System.currentTimeMillis();
            try {
                MessagePipe pipe = messagePipeManager.createMessagePipe(request.getPipeName());
                List<Message> messages = new ArrayList<>(request.getCount());

                for (int i = 0; i < request.getCount(); i++) {
                    String content = request.getMessagePrefix() + request.getTimestamp() + "-" + i;
                    Message message = new Message(content.getBytes("UTF-8"));
                    messages.add(message);
                }

                pipe.putLastBatch(messages);
                long total = messageCounter.addAndGet(request.getCount());

                if (total % 1000 == 0) {
                    log.info("Published {} messages so far", total);
                }

                long duration = System.currentTimeMillis() - startTime;
                return new StressResponse(true,
                        "Batch published: " + request.getCount() + " messages",
                        messageCounter.get(),
                        errorCounter.get(),
                        duration);

            } catch (Exception e) {
                errorCounter.addAndGet(request.getCount());
                long duration = System.currentTimeMillis() - startTime;
                log.error("Failed to publish batch to pipe: {}", request.getPipeName(), e);
                return new StressResponse(false, "Error: " + e.getMessage(), messageCounter.get(), errorCounter.get(), duration);
            }
        }

        /**
         * Get stress test statistics
         * GET /api/stress/stats
         */
        @GetMapping("/stats")
        public StatsResponse getStats() {
            return new StatsResponse(
                    messageCounter.get(),
                    errorCounter.get(),
                    System.currentTimeMillis()
            );
        }

        /**
         * Reset statistics
         * POST /api/stress/reset
         */
        @PostMapping("/reset")
        public StressResponse reset() {
            messageCounter.set(0);
            errorCounter.set(0);
            log.info("Statistics reset");
            return new StressResponse(true, "Statistics reset", 0, 0);
        }

        /**
         * Health check
         * GET /api/stress/health
         */
        @GetMapping("/health")
        public HealthResponse health() {
            return new HealthResponse("UP", messageCounter.get(), errorCounter.get());
        }
    }

    // ==================== Response Models ====================

    public static class StressResponse {
        public boolean success;
        public String message;
        public long totalMessages;
        public long totalErrors;
        public long timestamp;
        public long duration;  // 执行耗时，单位：毫秒

        public StressResponse(boolean success, String message, long totalMessages, long totalErrors) {
            this.success = success;
            this.message = message;
            this.totalMessages = totalMessages;
            this.totalErrors = totalErrors;
            this.timestamp = System.currentTimeMillis();
            this.duration = 0;
        }

        public StressResponse(boolean success, String message, long totalMessages, long totalErrors, long duration) {
            this.success = success;
            this.message = message;
            this.totalMessages = totalMessages;
            this.totalErrors = totalErrors;
            this.timestamp = System.currentTimeMillis();
            this.duration = duration;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public long getTotalMessages() { return totalMessages; }
        public long getTotalErrors() { return totalErrors; }
        public long getTimestamp() { return timestamp; }
        public long getDuration() { return duration; }
    }

    public static class StatsResponse {
        public long totalMessages;
        public long totalErrors;
        public long timestamp;

        public StatsResponse(long totalMessages, long totalErrors, long timestamp) {
            this.totalMessages = totalMessages;
            this.totalErrors = totalErrors;
            this.timestamp = timestamp;
        }

        public long getTotalMessages() { return totalMessages; }
        public long getTotalErrors() { return totalErrors; }
        public long getTimestamp() { return timestamp; }
    }

    public static class HealthResponse {
        public String status;
        public long messageCount;
        public long errorCount;

        public HealthResponse(String status, long messageCount, long errorCount) {
            this.status = status;
            this.messageCount = messageCount;
            this.errorCount = errorCount;
        }

        public String getStatus() { return status; }
        public long getMessageCount() { return messageCount; }
        public long getErrorCount() { return errorCount; }
    }

    public static class BatchPublishRequest {
        public String pipeName;
        public int count;
        public String messagePrefix;
        public long timestamp;

        public BatchPublishRequest() {
            this.timestamp = System.currentTimeMillis();
        }

        public String getPipeName() { return pipeName != null ? pipeName : "default-pipe"; }
        public int getCount() { return count > 0 ? count : 1; }
        public String getMessagePrefix() { return messagePrefix != null ? messagePrefix : "msg-"; }
        public long getTimestamp() { return timestamp; }

        public void setPipeName(String pipeName) { this.pipeName = pipeName; }
        public void setCount(int count) { this.count = count; }
        public void setMessagePrefix(String messagePrefix) { this.messagePrefix = messagePrefix; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
