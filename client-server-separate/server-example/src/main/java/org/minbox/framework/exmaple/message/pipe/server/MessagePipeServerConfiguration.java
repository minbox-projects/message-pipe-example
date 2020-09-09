package org.minbox.framework.exmaple.message.pipe.server;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import org.minbox.framework.message.pipe.server.config.MessagePipeConfiguration;
import org.minbox.framework.message.pipe.server.config.ServerConfiguration;
import org.minbox.framework.message.pipe.spring.annotation.ServerServiceType;
import org.minbox.framework.message.pipe.spring.annotation.server.EnableMessagePipeServer;
import org.springframework.beans.factory.annotation.Autowired;
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
     * 邮件通知异常处理
     */
    @Autowired
    private EmailNotificationExceptionHandler emailNotificationExceptionHandler;

    /**
     * 实例化消息管道全局配置
     *
     * @return
     */
    @Bean
    public MessagePipeConfiguration messagePipeConfiguration() {
        MessagePipeConfiguration configuration = MessagePipeConfiguration.defaultConfiguration();
        configuration
                .setRequestIdGenerator(new CustomRequestIdGenerator())
                //.setLoadBalanceStrategy(new SmoothClientLoadBalanceStrategy())
                .setExceptionHandler(emailNotificationExceptionHandler)
                .setLockTime(
                        new MessagePipeConfiguration.LockTime()
                                .setWaitTime(5)
                                .setLeaseTime(10)
                                .setTimeUnit(TimeUnit.SECONDS));
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

    /**
     * 配置{@link NamingService}服务实例
     *
     * @return The {@link NamingService} instance
     * @throws NacosException
     */
    @Bean
    public NamingService namingService() throws NacosException {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.USERNAME, "nacos");
        properties.put(PropertyKeyConst.PASSWORD, "nacos");
        // 查看 https://blog.yuqiyu.com/open-nacos-server.html
        properties.put(PropertyKeyConst.SERVER_ADDR, "open.nacos.yuqiyu.com:80");
        return NacosFactory.createNamingService(properties);
    }
}
