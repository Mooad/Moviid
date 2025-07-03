package com.moviid.movidencoder.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.config.EndpointRegistrar;
import io.awspring.cloud.sqs.config.SqsListenerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

@Configuration
public class CustomQueueMessageHandlerConfig implements SqsListenerConfigurer {

    private final ObjectMapper objectMapper;

    public CustomQueueMessageHandlerConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public MappingJackson2MessageConverter sqsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }

    @Override
    public void configure(EndpointRegistrar registrar) {
        // Currently no custom endpoint registration needed.
        // You can register dynamic listeners here if needed.
    }
}
