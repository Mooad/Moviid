package com.moviid.translator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsProperties {

    @Value("${aws.sqs.queuepoll.url}")
    private String sqsPollQueueUrl;
    @Value("${aws.sqs.queuepush.url}")
    private String sqsPushQueueUrl;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    // Getters for the properties
    public String getSqsPollQueueUrl() {
        return sqsPollQueueUrl;
    }

    public String getRegion() {
        return region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getSqsPushQueueUrl() {
        return sqsPushQueueUrl;
    }

    public void setSqsPushQueueUrl(String sqsPushQueueUrl) {
        this.sqsPushQueueUrl = sqsPushQueueUrl;
    }
}
