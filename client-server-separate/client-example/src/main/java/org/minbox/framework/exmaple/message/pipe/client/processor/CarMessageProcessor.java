package org.minbox.framework.exmaple.message.pipe.client.processor;

import lombok.extern.slf4j.Slf4j;
import org.minbox.framework.message.pipe.client.process.MessageProcessor;
import org.minbox.framework.message.pipe.client.process.MessageProcessorType;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * @author 恒宇少年
 */
@Service
@Slf4j
public class CarMessageProcessor implements MessageProcessor {
    @Override
    public String bindingPipeName() {
        return "car.+";
    }

    @Override
    public MessageProcessorType processorType() {
        return MessageProcessorType.REGEX;
    }

    @Override
    public boolean processing(String specificPipeName, String requestId, byte[] messageBody) {
        log.info("PipeName：{}，RequestId：{}，MessageBody：{}", specificPipeName, requestId, new String(messageBody));
        try {
            Thread.sleep(new Random().nextInt(2000));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
