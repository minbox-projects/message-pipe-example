package org.minbox.framework.exmaple.message.pipe;

import org.junit.jupiter.api.Test;
import org.minbox.framework.message.pipe.core.Message;
import org.minbox.framework.message.pipe.server.MessagePipe;
import org.minbox.framework.message.pipe.server.manager.MessagePipeManager;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class MessagePipeApplicationTests {

    @Autowired
    private MessagePipeManager manager;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 向 "test"、"admin" 消息管道写入消息
     */
    @Test
    //@JunitPerfConfig(threads = 50, duration = 10000)
    void putMessage() {
        for (int i = 0; i < 1000; i++) {
            try {
                //MessagePipe messagePipe = manager.getMessagePipe("test");
                MessagePipe messagePipe2 = manager.getMessagePipe("admin");
                Message message = new Message(String.valueOf(i).getBytes());
                //messagePipe.put(message);
                messagePipe2.put(message);
            } catch (Exception e) {

            }
        }
    }
}
