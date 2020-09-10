package org.minbox.framework.exmaple.message.pipe.server;

import lombok.extern.slf4j.Slf4j;
import org.minbox.framework.message.pipe.server.processing.push.PushMessageEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 消息写入管道事件{@link PushMessageEvent}监听示例
 *
 * @author 恒宇少年
 */
@Component
@Slf4j
public class ExamplePushMessageListener implements ApplicationListener<PushMessageEvent> {
    @Override
    public void onApplicationEvent(PushMessageEvent event) {
        String pipeName = event.getPipeName();
        log.info("消息管道：{}，有新消息写入.", pipeName);
    }
}
