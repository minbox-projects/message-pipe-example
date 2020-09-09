package org.minbox.framework.exmaple.message.pipe.server;

import lombok.extern.slf4j.Slf4j;
import org.minbox.framework.message.pipe.core.Message;
import org.minbox.framework.message.pipe.core.untis.JsonUtils;
import org.minbox.framework.message.pipe.server.exception.ExceptionHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * 自定义邮件通知{@link ExceptionHandler}
 *
 * @author 恒宇少年
 */
@Component
@Slf4j
public class EmailNotificationExceptionHandler implements ExceptionHandler {
    @Override
    public void handleException(Exception exception, Object target) {
        // 处理的消息对象，该对象可能为空
        if (!ObjectUtils.isEmpty(target)) {
            Message message = (Message) target;
            log.info("消息内容：{}", JsonUtils.objectToJson(message));
        }
        // 输出异常堆栈
        log.error(exception.getMessage(), exception);
        // 处理邮件通知逻辑
    }
}
