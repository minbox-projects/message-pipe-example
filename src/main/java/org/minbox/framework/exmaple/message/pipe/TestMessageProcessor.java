package org.minbox.framework.exmaple.message.pipe;

import lombok.extern.slf4j.Slf4j;
import org.minbox.framework.message.pipe.client.process.MessageProcessor;
import org.springframework.stereotype.Service;

/**
 * 消息处理器
 *
 * @author 恒宇少年
 */
@Service
@Slf4j
public class TestMessageProcessor implements MessageProcessor {
    @Override
    public boolean processing(String requestId, String pipeName, byte[] messageBody) {
        log.info("RequestId：{}，PipeName：{}，MessageBody：{}", requestId, pipeName, new String(messageBody));
        return true;
    }
}