package org.minbox.framework.exmaple.message.pipe.server;

import org.minbox.framework.message.pipe.core.Message;
import org.minbox.framework.message.pipe.server.MessagePipe;
import org.minbox.framework.message.pipe.server.manager.MessagePipeManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 自动写入测试数据
 *
 * @author 恒宇少年
 */
@Configuration
public class PutMessage implements InitializingBean {
    @Autowired
    private MessagePipeManager manager;
    ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(10);

    @Override
    public void afterPropertiesSet() throws Exception {

        scheduledService.scheduleWithFixedDelay(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    String id = UUID.randomUUID().toString();
                    Message message = new Message(id.getBytes());
                    message.getMetadata().put("traceId", UUID.randomUUID().toString());
                    MessagePipe messagePipe = manager.getMessagePipe("test");
                    messagePipe.putLast(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 1000, 300, TimeUnit.MILLISECONDS);

    }
}
