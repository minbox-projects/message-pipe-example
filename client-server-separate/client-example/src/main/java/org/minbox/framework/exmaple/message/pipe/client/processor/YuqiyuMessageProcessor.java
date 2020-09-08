package org.minbox.framework.exmaple.message.pipe.client.processor;

import lombok.extern.slf4j.Slf4j;
import org.minbox.framework.message.pipe.client.process.MessageProcessor;
import org.minbox.framework.message.pipe.core.Message;

/**
 * 消息处理器
 *
 * @author 恒宇少年issues
 */
//@Service
@Slf4j
public class YuqiyuMessageProcessor implements MessageProcessor {
    @Override
    public String bindingPipeName() {
        return "yuqiyu";
    }

    @Override
    public boolean processing(String specificPipeName, String requestId, Message message) {
        byte[] messageBody = message.getBody();
        log.info("PipeName：{}，RequestId：{}，MessageBody：{}", specificPipeName, requestId, new String(messageBody));
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
