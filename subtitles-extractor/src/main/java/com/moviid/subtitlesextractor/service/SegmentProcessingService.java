package com.moviid.subtitlesextractor.service;

import com.moviid.bean.sqs.SqsVideoSegmentMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

@Service
public class SegmentProcessingService {
    private static final Logger LOGGER = Logger.getLogger(SegmentProcessingService.class.getName());
    private final RestTemplate restTemplate;
    private final Executor taskExecutor;

    @Value("${vosk-transcribe-link}")
    private String voskApiUrl;

    public SegmentProcessingService(RestTemplate restTemplate, @Qualifier("taskExecutor") Executor taskExecutor) {
        this.restTemplate = restTemplate;
        this.taskExecutor = taskExecutor;
    }

    @Async("taskExecutor")
    public CompletableFuture<String> processSegmentAsync(SqsVideoSegmentMessage message) {
        try {
            // Send the SqsVideoSegmentMessage object as JSON to the Flask API
            String response = restTemplate.postForObject(voskApiUrl, message, String.class);

            // Log the response
            LOGGER.info("Received response: " + response);
            return CompletableFuture.completedFuture(response);

        } catch (Exception e) {
            LOGGER.severe("Unexpected error occurred: " + e.getMessage());
            return CompletableFuture.completedFuture("Error: " + e.getMessage());
        }
    }

    public void triggerSegmentProcessing(SqsVideoSegmentMessage message) {
        processSegmentAsync(message).thenAccept(result -> LOGGER.info("Processed segment result: " + result))
                .exceptionally(e -> {
                    LOGGER.severe("Error while processing segment: " + e.getMessage());
                    return null;
                });
    }
}
