package org.minbox.framework.exmaple.message.pipe.server;

import org.minbox.framework.message.pipe.core.Message;
import org.minbox.framework.message.pipe.server.MessagePipe;
import org.minbox.framework.message.pipe.server.manager.MessagePipeManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 自动写入测试数据
 *
 * @author 恒宇少年
 */
//@Configuration
public class PutMessage {
    @Autowired
    private MessagePipeManager manager;
    ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(10);

    public PutMessage() {
        scheduledService.scheduleWithFixedDelay(() -> {
            for (int i = 0; i < 100; i++) {
                MessagePipe messagePipe = manager.getMessagePipe("test");
                String id = UUID.randomUUID().toString();
                Message message = new Message(id.getBytes());
                messagePipe.put(message);
            }
        }, 2000, 10, TimeUnit.MILLISECONDS);
    }
}
