package org.minbox.framework.exmaple.message.pipe.client;

import org.minbox.framework.message.pipe.client.config.ClientConfiguration;
import org.minbox.framework.message.pipe.spring.annotation.ServerServiceType;
import org.minbox.framework.message.pipe.spring.annotation.client.EnableMessagePipeClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @author 恒宇少年
 */
@Configuration
@EnableMessagePipeClient(serverType = ServerServiceType.NACOS)
public class MessagePipeClientConfiguration {
    /**
     * 实例化客户端配置
     *
     * @return
     */
    @Bean
    public ClientConfiguration clientConfiguration() {
        return new ClientConfiguration()
                .setLocalPort(5201)
                .setServerAddress("open.nacos.yuqiyu.com")
                .setServerPort(80);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        return container;
    }
}
