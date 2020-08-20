package org.minbox.framework.exmaple.message.pipe;

import com.alibaba.fastjson.JSON;
import com.github.houbb.junitperf.core.annotation.JunitPerfConfig;
import org.junit.jupiter.api.Test;
import org.minbox.framework.message.pipe.core.Message;
import org.minbox.framework.message.pipe.server.ClientManager;
import org.minbox.framework.message.pipe.server.MessagePipe;
import org.minbox.framework.message.pipe.server.manager.MessagePipeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest
class MessagePipeApplicationTests {

    @Autowired
    private MessagePipeManager manager;

    /**
     * 向 "test" 消息管道写入消息
     */
    @Test
    @JunitPerfConfig(threads = 20, duration = 10000)
    void putMessage() {
        Long currentTime = System.nanoTime();
        MessagePipe messagePipe = manager.getMessagePipe("test");
        Message message = new Message(currentTime.toString().getBytes());
        messagePipe.put(message);
    }
}
