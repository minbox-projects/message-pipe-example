package org.minbox.framework.exmaple.message.pipe.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 恒宇少年
 */
@SpringBootApplication
public class MessagePipeClientSecondExampleApplication {
    /**
     * logger instance
     */
    static Logger logger = LoggerFactory.getLogger(MessagePipeClientSecondExampleApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MessagePipeClientSecondExampleApplication.class, args);
        logger.info("Message Pipe Client服务启动成功.");
    }
}
