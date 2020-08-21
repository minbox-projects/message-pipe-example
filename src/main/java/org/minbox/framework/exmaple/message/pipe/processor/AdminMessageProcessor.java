package org.minbox.framework.exmaple.message.pipe.processor;

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
public class AdminMessageProcessor implements MessageProcessor {
    @Override
    public String bindingPipeName() {
        return "admin";
    }

    @Override
    public boolean processing(String requestId, byte[] messageBody) {
        log.info("RequestId：{}，MessageBody：{}", requestId, new String(messageBody));
        return true;
    }
}
