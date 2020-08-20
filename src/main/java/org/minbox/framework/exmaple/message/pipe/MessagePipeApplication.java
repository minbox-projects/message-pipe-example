package org.minbox.framework.exmaple.message.pipe;

import org.minbox.framework.message.pipe.client.config.ClientConfiguration;
import org.minbox.framework.message.pipe.server.MessagePipe;
import org.minbox.framework.message.pipe.server.config.MessagePipeConfiguration;
import org.minbox.framework.message.pipe.server.config.ServerConfiguration;
import org.minbox.framework.message.pipe.server.manager.MessagePipeManager;
import org.minbox.framework.message.pipe.spring.annotation.client.EnableMessagePipeClient;
import org.minbox.framework.message.pipe.spring.annotation.server.EnableMessagePipeServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class MessagePipeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagePipeApplication.class, args);
    }
}
