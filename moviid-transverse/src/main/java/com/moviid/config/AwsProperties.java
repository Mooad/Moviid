package com.moviid.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "aws")
@Component
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

    @Value("${aws.sqs-endpoint}")
    private String endpoint;

    @Value("${aws.bucket-name}")
    private String bucket_name;

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

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBuket_name() {
        return bucket_name;
    }

    public void setBuket_name(String bucket_name) {
        this.bucket_name = bucket_name;
    }
    public void setSqsPushQueueUrl(String sqsPushQueueUrl) {
        this.sqsPushQueueUrl = sqsPushQueueUrl;
    }
}
