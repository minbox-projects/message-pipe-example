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
                // 默认为5201
                .setLocalPort(5202)
                .setServerAddress("localhost")
                .setServerPort(5200);
    }
}
