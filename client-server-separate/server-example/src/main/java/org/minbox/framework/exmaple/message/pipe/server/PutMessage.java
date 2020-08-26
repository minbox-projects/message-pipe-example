package org.minbox.framework.exmaple.message.pipe.server;

import org.minbox.framework.message.pipe.core.Message;
import org.minbox.framework.message.pipe.server.MessagePipe;
import org.minbox.framework.message.pipe.server.manager.MessagePipeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 自动写入测试数据
 *
 * @author 恒宇少年
 */
@Configuration
public class PutMessage {
    @Autowired
    private MessagePipeManager manager;
    ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(10);

    public PutMessage() {
        scheduledService.scheduleWithFixedDelay(() -> {
            for (int i = 0; i < 1000; i++) {
                /*MessagePipe messagePipe = manager.getMessagePipe("test");
                MessagePipe messagePipe2 = manager.getMessagePipe("admin");
                MessagePipe messagePipe4 = manager.getMessagePipe("hengboy");
                MessagePipe messagePipe5 = manager.getMessagePipe("yuqiyu");
                Message message = new Message(String.valueOf(i).getBytes());
                messagePipe2.put(message);
                messagePipe.put(message);
                messagePipe4.put(message);
                messagePipe5.put(message);*/
                MessagePipe testMessagePipe = manager.getMessagePipe("test");
                MessagePipe messagePipe = manager.getMessagePipe("car.11111");
                MessagePipe messagePipe2 = manager.getMessagePipe("car.22222");
                Message message = new Message(String.valueOf(i).getBytes());
                testMessagePipe.put(message);
                messagePipe.put(message);
                messagePipe2.put(message);
            }
        }, 2000, 500, TimeUnit.MILLISECONDS);
    }
}
