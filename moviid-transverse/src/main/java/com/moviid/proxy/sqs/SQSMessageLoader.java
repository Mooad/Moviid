package com.moviid.proxy.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviid.bean.sqs.SqsVideoSegmentMessage;
import com.moviid.config.AwsProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;


@Component
public class SQSMessageLoader {

    private final AwsProperties awsProperties;
    private final ObjectMapper objectMapper;
    private SqsClient sqsClient;

    @Autowired
    public SQSMessageLoader(AwsProperties awsProperties, ObjectMapper objectMapper) {
        this.awsProperties = awsProperties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        this.sqsClient = SqsClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .endpointOverride(java.net.URI.create(awsProperties.getEndpoint()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public void sendMessageToQueue(SqsVideoSegmentMessage message) {
        String queueUrl = awsProperties.getSqsPushQueueUrl();
        String messageBody = convertToJson(message);

        SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .build();

        sqsClient.sendMessage(sendMsgRequest);
        System.out.println("Message sent successfully to SQS queue.");
    }

    private String convertToJson(SqsVideoSegmentMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert message to JSON", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (sqsClient != null) {
            sqsClient.close();
        }
    }
}
