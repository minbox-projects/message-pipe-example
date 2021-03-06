package org.minbox.framework.exmaple.message.pipe;

import org.minbox.framework.message.pipe.client.config.ClientConfiguration;
import org.minbox.framework.message.pipe.server.config.MessagePipeConfiguration;
import org.minbox.framework.message.pipe.server.config.ServerConfiguration;
import org.minbox.framework.message.pipe.spring.annotation.client.EnableMessagePipeClient;
import org.minbox.framework.message.pipe.spring.annotation.server.EnableMessagePipeServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 消息管道配置
 *
 * @author 恒宇少年
 */
@EnableMessagePipeServer
@EnableMessagePipeClient
@Configuration
public class MessagePipeAutoConfiguration {
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
                        .setTimeUnit(TimeUnit.SECONDS)
        );
        return configuration;
    }

    /**
     * 实例化服务端配置
     *
     * @return
     */
    @Bean
    public ServerConfiguration serverConfiguration() {
        return new ServerConfiguration();
    }

    /**
     * 实例化客户端配置
     *
     * @return
     */
    @Bean
    public ClientConfiguration clientConfiguration() {
        return new ClientConfiguration();
    }
}
