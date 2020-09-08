package org.minbox.framework.exmaple.message.pipe.client.processor;

import lombok.extern.slf4j.Slf4j;
import org.minbox.framework.message.pipe.client.process.MessageProcessor;
import org.minbox.framework.message.pipe.core.Message;

/**
 * 消息处理器
 *
 * @author 恒宇少年
 */
//@Service
@Slf4j
public class HengboyMessageProcessor implements MessageProcessor {
    @Override
    public String bindingPipeName() {
        return "hengboy";
    }

    @Override
    public boolean processing(String specificPipeName, String requestId, Message message) {
        byte[] messageBody = message.getBody();
        log.info("PipeName：{}，RequestId：{}，MessageBody：{}", specificPipeName, requestId, new String(messageBody));
        return true;
    }
}
