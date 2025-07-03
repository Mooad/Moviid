package com.moviid.proxy.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviid.bean.sqs.SqsVideoSegmentMessage;
import com.moviid.config.AwsProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SqsPollingService {

    private static final Logger logger = LoggerFactory.getLogger(SqsPollingService.class);

    private final AwsProperties awsProperties;
    private final ObjectMapper objectMapper;
    private SqsClient sqsClient;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public SqsPollingService(AwsProperties awsProperties, ObjectMapper objectMapper) {
        this.awsProperties = awsProperties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        sqsClient = SqsClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsProperties.getAccessKey(), awsProperties.getSecretKey())
                ))
                .build();
    }

    @Scheduled(fixedDelay = 1000)
    public void pollQueue() {
        try {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(awsProperties.getSqsPollQueueUrl())
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(5)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(request).messages();

            for (Message message : messages) {
                executorService.submit(() -> processMessage(message));
            }
        } catch (Exception e) {
            logger.error("Error polling SQS queue", e);
        }
    }

    private void processMessage(Message message) {
        try {
            SqsVideoSegmentMessage segmentMessage = objectMapper.readValue(message.body(), SqsVideoSegmentMessage.class);
            logger.info("Received message: {}", segmentMessage);

            // Process message here...

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(awsProperties.getSqsPollQueueUrl())
                    .receiptHandle(message.receiptHandle())
                    .build());
        } catch (Exception e) {
            logger.error("Error processing message: {}", message.body(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (sqsClient != null) {
            sqsClient.close();
        }
        executorService.shutdown();
    }
}
