package org.minbox.stress.test.client;

import com.alibaba.nacos.client.naming.NacosNamingService;
import lombok.extern.slf4j.Slf4j;
import org.minbox.framework.message.pipe.client.process.MessageProcessorType;
import org.minbox.framework.message.pipe.core.Message;
import org.minbox.framework.message.pipe.client.config.ClientConfiguration;
import org.minbox.framework.message.pipe.client.process.MessageProcessor;
import org.minbox.framework.message.pipe.spring.annotation.ServerServiceType;
import org.minbox.framework.message.pipe.spring.annotation.client.EnableMessagePipeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Message Pipe Client Stress Test Application
 * Simulates multiple clients receiving and processing messages
 */
@SpringBootApplication
@EnableMessagePipeClient(serverType = ServerServiceType.NACOS)
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
     * Message Pipe Client Configuration Properties
     */
    @org.springframework.boot.context.properties.ConfigurationProperties(prefix = "message.pipe.client")
    @Component
    public static class MessagePipeClientProperties {
        private int localPort = 5201;
        private String serverAddress = "localhost";
        private int serverPort = 5200;
        private String networkInterface = "en11";
        private int retryRegisterTimes = 10;
        private int retryRegisterIntervalMilliSeconds = 1000;
        private int heartBeatIntervalSeconds = 10;

        public int getLocalPort() {
            return localPort;
        }

        public void setLocalPort(int localPort) {
            this.localPort = localPort;
        }

        public String getServerAddress() {
            return serverAddress;
        }

        public void setServerAddress(String serverAddress) {
            this.serverAddress = serverAddress;
        }

        public int getServerPort() {
            return serverPort;
        }

        public void setServerPort(int serverPort) {
            this.serverPort = serverPort;
        }

        public String getNetworkInterface() {
            return networkInterface;
        }

        public void setNetworkInterface(String networkInterface) {
            this.networkInterface = networkInterface;
        }

        public int getRetryRegisterTimes() {
            return retryRegisterTimes;
        }

        public void setRetryRegisterTimes(int retryRegisterTimes) {
            this.retryRegisterTimes = retryRegisterTimes;
        }

        public int getRetryRegisterIntervalMilliSeconds() {
            return retryRegisterIntervalMilliSeconds;
        }

        public void setRetryRegisterIntervalMilliSeconds(int retryRegisterIntervalMilliSeconds) {
            this.retryRegisterIntervalMilliSeconds = retryRegisterIntervalMilliSeconds;
        }

        public int getHeartBeatIntervalSeconds() {
            return heartBeatIntervalSeconds;
        }

        public void setHeartBeatIntervalSeconds(int heartBeatIntervalSeconds) {
            this.heartBeatIntervalSeconds = heartBeatIntervalSeconds;
        }
    }

    /**
     * Client configuration bean
     */
    @Configuration
    public static class ClientConfig {

        /**
         * Create NamingService bean for Nacos service discovery
         * Reads configuration from application.yml
         */
        @Bean
        public NamingService namingService(
                @org.springframework.beans.factory.annotation.Value("${spring.cloud.nacos.server-addr}") String serverAddr,
                @org.springframework.beans.factory.annotation.Value("${spring.cloud.nacos.username}") String username,
                @org.springframework.beans.factory.annotation.Value("${spring.cloud.nacos.password}") String password,
                @org.springframework.beans.factory.annotation.Value("${spring.cloud.nacos.discovery.namespace}") String namespace) throws NacosException {
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
            properties.put(PropertyKeyConst.USERNAME, username);
            properties.put(PropertyKeyConst.PASSWORD, password);
            properties.put(PropertyKeyConst.NAMESPACE, namespace);
            return NacosFactory.createNamingService(properties);
        }

        @Bean
        public ClientConfiguration clientConfiguration(MessagePipeClientProperties properties) {
            return new ClientConfiguration()
                    .setLocalPort(properties.getLocalPort())
                    .setNetworkInterface(properties.getNetworkInterface())
                    //.setLocalHost("localhost")
                    // Port for client server
                    .setServerAddress(properties.getServerAddress())        // Server address
                    .setServerPort(properties.getServerPort())              // Server port
                    .setRetryRegisterTimes(properties.getRetryRegisterTimes())                       // Retry count for registration
                    .setRetryRegisterIntervalMilliSeconds(properties.getRetryRegisterIntervalMilliSeconds())     // Retry interval ms
                    .setHeartBeatIntervalSeconds(properties.getHeartBeatIntervalSeconds());               // Heartbeat interval seconds
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
     * Message Processor for pipe* (regex pattern)
     * Handles all pipes matching pattern: pipe-0, pipe-1, pipe-2, ...
     */
    @Component
    @Slf4j
    public static class PipePatternProcessor implements MessageProcessor {

        @Override
        public String bindingPipeName() {
            // Use regex pattern to match pipe-*, pipe_*, etc.
            return "pipe-*";
        }

        @Override
        public boolean processing(String specificPipeName, String requestId, Message message) {
            try {
                String content = new String(message.getBody(), StandardCharsets.UTF_8);
                log.info("[" + specificPipeName + "] 消息：{}.", content);
                long processed = processedCounter.incrementAndGet();

                // Log every 10000 messages to reduce log volume
                if (processed % 10000 == 0) {
                    log.info("[{}] Processed {} messages so far, current: {}",
                            specificPipeName, processed, content);
                } else if (processed % 1000 == 0) {
                    long elapsed = System.currentTimeMillis() - startTime.get();
                    double rate = (processed * 1000.0) / elapsed;
                    log.debug("[{}] Processed {} messages (Rate: {} msg/sec)",
                            specificPipeName, processed, rate);
                }

                return true;
            } catch (Exception e) {
                errorCounter.incrementAndGet();
                log.error("[{}] Error processing message: {}", specificPipeName, e.getMessage());
                return false;
            }
        }

        @Override
        public MessageProcessorType processorType() {
            return MessageProcessorType.REGEX;
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
