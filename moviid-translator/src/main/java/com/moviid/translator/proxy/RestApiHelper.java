package com.moviid.translator.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviid.translator.bean.TranslationRequestDTO;
import com.moviid.translator.bean.mongodb.MoviidGrappe.SpeechData.Sentence;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class RestApiHelper {

    private static final Logger LOGGER = Logger.getLogger(RestApiHelper.class.getName());
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RestApiHelper() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<String> translateSentences(TranslationRequestDTO requestDTO, String apiUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpHeaders headers = createHeaders();
                String requestBody = objectMapper.writeValueAsString(requestDTO);
                HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

                // Execute the request
                String response = restTemplate.postForObject(apiUrl, requestDTO, String.class);
                LOGGER.info("Successfully called translation API: " + apiUrl);
                return response;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during translation", e);
                throw new RuntimeException("Translation request failed", e);
            }
        });
    }

    public List<Sentence> parseTranslatedSentences(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            List<Sentence> sentences = new ArrayList<>();

            JsonNode sentencesNode = rootNode.path("sentences");
            for (JsonNode sentenceNode : sentencesNode) {
                Sentence sentence = new Sentence();
                sentence.setStartTime(sentenceNode.path("startTime").asText());
                sentence.setEndTime(sentenceNode.path("endTime").asText());
                sentence.setSentence(sentenceNode.path("sentence").asText());
                sentence.setTranslatedText(sentenceNode.path("translatedText").asText());
                sentence.setClear(sentenceNode.path("isClear").asBoolean());
                sentences.add(sentence);
            }
            return sentences;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error parsing translation response", e);
            throw new RuntimeException("Failed to parse translation response", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
