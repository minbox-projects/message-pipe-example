package org.minbox.stress.test.client;

import lombok.extern.slf4j.Slf4j;
import org.minbox.framework.message.pipe.core.Message;
import org.minbox.framework.message.pipe.client.config.ClientConfiguration;
import org.minbox.framework.message.pipe.client.process.MessageProcessor;
import org.minbox.framework.message.pipe.spring.annotation.ServerServiceType;
import org.minbox.framework.message.pipe.spring.annotation.client.EnableMessagePipeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Message Pipe Client Stress Test Application
 * Simulates multiple clients receiving and processing messages
 */
@SpringBootApplication
@EnableMessagePipeClient(serverType = ServerServiceType.GRPC)
@Slf4j
public class ClientStressTestApplication {

    private static final AtomicLong processedCounter = new AtomicLong(0);
    private static final AtomicLong errorCounter = new AtomicLong(0);
    private static final AtomicLong startTime = new AtomicLong(0);

    public static void main(String[] args) {
        startTime.set(System.currentTimeMillis());
        SpringApplication.run(ClientStressTestApplication.class, args);
        log.info("=== Message Pipe Client Stress Test Started ===");
    }

    /**
     * Client configuration bean
     */
    @Configuration
    public static class ClientConfig {

        @Bean
        public ClientConfiguration clientConfiguration() {
            return new ClientConfiguration()
                    .setLocalPort(5201)
                    .setNetworkInterface("en11")
                    //.setLocalHost("localhost")
                    // Port for client server
                    .setServerAddress("localhost")                  // Server address
                    .setServerPort(5200)                            // Server port
                    .setRetryRegisterTimes(3)                       // Retry count for registration
                    .setRetryRegisterIntervalMilliSeconds(1000)     // Retry interval ms
                    .setHeartBeatIntervalSeconds(10);               // Heartbeat interval seconds
        }
    }

    /**
     * Message Processor for default pipe
     */
    @Component
    @Slf4j
    public static class DefaultPipeProcessor implements MessageProcessor {

        @Override
        public String bindingPipeName() {
            return "default-pipe";
        }

        @Override
        public boolean processing(String specificPipeName, String requestId, Message message) {
            try {
                String content = new String(message.getBody(), "UTF-8");
                log.info("[default-pipe] 消息：{}.", content);
                long processed = processedCounter.incrementAndGet();

                if (processed % 1000 == 0) {
                    long elapsed = System.currentTimeMillis() - startTime.get();
                    double rate = (processed * 1000.0) / elapsed;
                    log.info("Processed {} messages (Rate: {} msg/sec)", processed, rate);
                }

                return true;
            } catch (Exception e) {
                errorCounter.incrementAndGet();
                log.error("Error processing message: {}", e.getMessage());
                return false;
            }
        }
    }

    /**
     * Message Processor for test-pipe
     */
    @Component
    @Slf4j
    public static class TestPipeProcessor implements MessageProcessor {

        @Override
        public String bindingPipeName() {
            return "test-pipe";
        }

        @Override
        public boolean processing(String specificPipeName, String requestId, Message message) {
            try {
                String content = new String(message.getBody(), "UTF-8");
                log.info("[test-pipe] 消息：{}.", content);
                long processed = processedCounter.incrementAndGet();

                if (processed % 1000 == 0) {
                    long elapsed = System.currentTimeMillis() - startTime.get();
                    double rate = (processed * 1000.0) / elapsed;
                    log.info("Processed {} messages (Rate: {} msg/sec)", processed, rate);
                }

                return true;
            } catch (Exception e) {
                errorCounter.incrementAndGet();
                log.error("Error processing message: {}", e.getMessage());
                return false;
            }
        }
    }

    /**
     * Message Processor for order-events pipe
     */
    @Component
    @Slf4j
    public static class OrderEventsProcessor implements MessageProcessor {

        @Override
        public String bindingPipeName() {
            return "order-events";
        }

        @Override
        public boolean processing(String specificPipeName, String requestId, Message message) {
            try {
                String content = new String(message.getBody(), "UTF-8");
                log.info("[order-events] 消息：{}.", content);
                // Simulate some processing time
                long processingTime = System.nanoTime();

                long processed = processedCounter.incrementAndGet();

                if (processed % 1000 == 0) {
                    long elapsed = System.currentTimeMillis() - startTime.get();
                    double rate = (processed * 1000.0) / elapsed;
                    log.info("Processed {} order events (Rate: {} msg/sec)", processed, rate);
                }

                return true;
            } catch (Exception e) {
                errorCounter.incrementAndGet();
                log.error("Error processing order event: {}", e.getMessage());
                return false;
            }
        }
    }

    /**
     * REST Controller for stress test monitoring
     */
    @RestController
    @RequestMapping("/api/client")
    @Slf4j
    public static class ClientStressTestController {

        /**
         * Get processing statistics
         * GET /api/client/stats
         */
        @GetMapping("/stats")
        public StatsResponse getStats() {
            long elapsed = System.currentTimeMillis() - startTime.get();
            double rate = elapsed > 0 ? (processedCounter.get() * 1000.0) / elapsed : 0;

            return new StatsResponse(
                    processedCounter.get(),
                    errorCounter.get(),
                    elapsed,
                    rate,
                    System.currentTimeMillis()
            );
        }

        /**
         * Health check
         * GET /api/client/health
         */
        @GetMapping("/health")
        public HealthResponse health() {
            return new HealthResponse("UP", processedCounter.get(), errorCounter.get());
        }

        /**
         * Reset statistics
         * POST /api/client/reset
         */
        @PostMapping("/reset")
        public StatsResponse reset() {
            startTime.set(System.currentTimeMillis());
            processedCounter.set(0);
            errorCounter.set(0);
            log.info("Client statistics reset");
            return getStats();
        }
    }

    // ==================== Response Models ====================

    public static class StatsResponse {
        public long processedMessages;
        public long errorCount;
        public long elapsedMillis;
        public double messagesPerSecond;
        public long timestamp;

        public StatsResponse(long processedMessages, long errorCount, long elapsedMillis,
                             double messagesPerSecond, long timestamp) {
            this.processedMessages = processedMessages;
            this.errorCount = errorCount;
            this.elapsedMillis = elapsedMillis;
            this.messagesPerSecond = messagesPerSecond;
            this.timestamp = timestamp;
        }

        public long getProcessedMessages() {
            return processedMessages;
        }

        public long getErrorCount() {
            return errorCount;
        }

        public long getElapsedMillis() {
            return elapsedMillis;
        }

        public double getMessagesPerSecond() {
            return messagesPerSecond;
        }

        public long getTimestamp() {
            return timestamp;
        }
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

        public String getStatus() {
            return status;
        }

        public long getMessageCount() {
            return messageCount;
        }

        public long getErrorCount() {
            return errorCount;
        }
    }
}
