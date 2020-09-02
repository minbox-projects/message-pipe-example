package org.minbox.framework.exmaple.message.pipe.server;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import org.minbox.framework.message.pipe.server.config.MessagePipeConfiguration;
import org.minbox.framework.message.pipe.server.config.ServerConfiguration;
import org.minbox.framework.message.pipe.spring.annotation.ServerServiceType;
import org.minbox.framework.message.pipe.spring.annotation.server.EnableMessagePipeServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Message Server相关配置
 *
 * @author 恒宇少年
 */
@Configuration
@EnableMessagePipeServer(serverType = ServerServiceType.NACOS)
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

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        return container;
    }


    @Bean
    public NamingService namingService() throws NacosException {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.USERNAME, "nacos");
        properties.put(PropertyKeyConst.PASSWORD, "nacos");
        properties.put(PropertyKeyConst.SERVER_ADDR, "open.nacos.yuqiyu.com:80");
        return NacosFactory.createNamingService(properties);
    }
}
