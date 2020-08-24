package org.minbox.framework.exmaple.message.pipe.client;

import org.minbox.framework.message.pipe.client.config.ClientConfiguration;
import org.minbox.framework.message.pipe.spring.annotation.client.EnableMessagePipeClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 恒宇少年
 */
@Configuration
@EnableMessagePipeClient
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
                .setServerAddress("localhost")
                .setServerPort(5200);
    }
}
