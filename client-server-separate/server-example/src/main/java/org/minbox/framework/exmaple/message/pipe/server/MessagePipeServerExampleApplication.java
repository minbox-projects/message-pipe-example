package org.minbox.framework.exmaple.message.pipe.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 恒宇少年
 */
@SpringBootApplication
public class MessagePipeServerExampleApplication {
    /**
     * logger instance
     */
    static Logger logger = LoggerFactory.getLogger(MessagePipeServerExampleApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MessagePipeServerExampleApplication.class, args);
        logger.info("Message Pipe Server 服务启动成功.");
    }
}
