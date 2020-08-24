package org.minbox.framework.exmaple.message.pipe.server;

import org.minbox.framework.message.pipe.server.config.MessagePipeConfiguration;
import org.minbox.framework.message.pipe.server.config.ServerConfiguration;
import org.minbox.framework.message.pipe.spring.annotation.server.EnableMessagePipeServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Message Server相关配置
 *
 * @author 恒宇少年
 */
@Configuration
@EnableMessagePipeServer
public class MessagePipeServerConfiguration {
    /**
     * 实例化消息管道全局配置
     *
     * @return
     */
    @Bean
    public MessagePipeConfiguration messagePipeConfiguration() {
        MessagePipeConfiguration configuration = MessagePipeConfiguration.defaultConfiguration();
        configuration.setLockTime(
                new MessagePipeConfiguration.LockTime()
                        .setLeaseTime(10)
                        .setTimeUnit(TimeUnit.SECONDS))
                .setDistributionMessagePoolSize(10);
        return configuration;
    }

    /**
     * 实例化服务端配置
     *
     * @return The {@link ServerConfiguration} instance
     */
    @Bean
    public ServerConfiguration serverConfiguration() {
        return new ServerConfiguration()
                .setExpiredExcludeThresholdSeconds(10)
                .setCheckClientExpiredIntervalSeconds(5);
    }
}
