package com.moviid.movidencoder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviid.bean.EncodeRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EncodingService {

    private static final Logger LOGGER = Logger.getLogger(EncodingService.class.getName());

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final URI encoderEndpoint;

    public EncodingService(RestTemplateBuilder builder,
                           @Value("${encoder.service.url}") String encoderServiceUrl,
                           ObjectMapper objectMapper) {

        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(180))
                .build();

        this.encoderEndpoint = URI.create(
                encoderServiceUrl);

        this.objectMapper = objectMapper;
    }

    /** POST EncodeRequest → Flask, return patched EncodeRequest. */
    public EncodeRequest sendForEncoding(EncodeRequest req) {
        try {
            LOGGER.info(() -> "➡️  Send segment " + req.getSegmentId() + " to " + encoderEndpoint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(
                    objectMapper.writeValueAsString(req), headers);

            ResponseEntity<EncodeRequest> resp = restTemplate.exchange(
                    encoderEndpoint, HttpMethod.POST, request, EncodeRequest.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                LOGGER.info(() -> "✅  Encoded: " + req.getSegmentId());
                return resp.getBody();
            }
            throw new EncoderCallException("HTTP " + resp.getStatusCodeValue() + " from encoder");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌  Encode failed for " + req.getSegmentId(), e);
            throw new EncoderCallException("Encoder call failed", e);
        }
    }

    public static class EncoderCallException extends RuntimeException {
        public EncoderCallException(String msg)               { super(msg); }
        public EncoderCallException(String msg, Throwable ex) { super(msg, ex); }
    }
}
