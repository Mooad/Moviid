package com.moviid.translator.config;

import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory;
import org.springframework.cloud.aws.messaging.listener.QueueMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

import java.util.Collections;

@Configuration
public class CustomQueueMessageHandlerConfig {

    private final MappingJackson2MessageConverter messageConverter;

    public CustomQueueMessageHandlerConfig(MappingJackson2MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    @Bean
    public QueueMessageHandler queueMessageHandler() {
        QueueMessageHandlerFactory factory = new QueueMessageHandlerFactory();
        factory.setArgumentResolvers(Collections.emptyList());
        factory.setMessageConverters(Collections.singletonList(messageConverter));
        return factory.createQueueMessageHandler();
    }
}
