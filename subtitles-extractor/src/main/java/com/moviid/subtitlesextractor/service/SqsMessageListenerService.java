package com.moviid.subtitlesextractor.service;

import com.moviid.bean.sqs.SqsVideoSegmentMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class SqsMessageListenerService {

    private static final Logger LOGGER = Logger.getLogger(SqsMessageListenerService.class.getName());
    private final SegmentProcessingService segmentProcessingService;

    @Autowired
    public SqsMessageListenerService(SegmentProcessingService segmentProcessingService) {
        this.segmentProcessingService = segmentProcessingService;
    }

    @SqsListener("WaitingForSpeechRecognition")
    public void receiveMessage(@Payload SqsVideoSegmentMessage message) {
        LOGGER.info("Received SQS message: " + message.toJsonString());
        segmentProcessingService.triggerSegmentProcessing(message);
    }
}