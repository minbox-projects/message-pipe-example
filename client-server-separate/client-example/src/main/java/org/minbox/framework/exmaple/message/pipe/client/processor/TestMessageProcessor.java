package org.minbox.framework.exmaple.message.pipe.client.processor;

import lombok.extern.slf4j.Slf4j;
import org.minbox.framework.message.pipe.client.process.MessageProcessor;
import org.minbox.framework.message.pipe.core.Message;
import org.minbox.framework.message.pipe.core.untis.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 消息处理器
 *
 * @author 恒宇少年
 */
@Service
@Slf4j
public class TestMessageProcessor implements MessageProcessor {
    @Override
    public String bindingPipeName() {
        return "test";
    }

    @Override
    public boolean processing(String specificPipeName, String requestId, Message message) {
        Map<String, Object> metadata = message.getMetadata();
        byte[] messageBody = message.getBody();
        log.info("PipeName：{}，RequestId：{}，MessageBody：{}，metadata：{}.",
                specificPipeName, requestId, new String(messageBody), JsonUtils.objectToJson(message.getMetadata()));
        return true;
    }
}
