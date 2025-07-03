package com.moviid.translator.service;

import com.moviid.translator.bean.sqs.SqsVideoSegmentMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class SqsMessageListenerService {

    private static final Logger LOGGER = Logger.getLogger(SqsMessageListenerService.class.getName());

    @Autowired
    private TranslationService translationService;

    @SqsListener("WaitingForTranslation")
    public void receiveMessage(@Payload SqsVideoSegmentMessage message) {LOGGER.info("Received SQS message: " + message.toJsonString());
        translationService.translateSegment(message.getJobId(), message);
    }
}
