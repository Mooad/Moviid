package com.moviid.translator.service;

import com.moviid.translator.bean.TranslationRequestDTO;
import com.moviid.translator.bean.mongodb.MoviidGrappe;
import com.moviid.translator.bean.mongodb.MoviidGrappe.SpeechData;
import com.moviid.translator.bean.mongodb.MoviidGrappe.SpeechData.Sentence;
import com.moviid.translator.bean.sqs.SqsVideoSegmentMessage;
import com.moviid.translator.dao.MoviidGrappeRepository;
import com.moviid.translator.proxy.RestApiHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Service
public class TranslationService {

    private static final Logger LOGGER = Logger.getLogger(TranslationService.class.getName());
    private static final String TRANSLATION_API_URL = "http://localhost:5001/translate";

    @Autowired
    private MoviidGrappeRepository repository;

    @Autowired
    private RestApiHelper restApiHelper;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void translateSegment(String jobId, SqsVideoSegmentMessage segmentSqsMessage) {
        MoviidGrappe grappe = repository.findByJobId(jobId)
                .orElseThrow(() -> new IllegalArgumentException("JobId not found: " + jobId));

        MoviidGrappe.Segment segment = grappe.getSegments().stream()
                .filter(seg -> seg.getSegmentID().equals(segmentSqsMessage.getInput().getSegmentId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("SegmentId not found: " + segmentSqsMessage.getInput().getSegmentId()));

        if (grappe.getTargetLanguage() == null) {
            LOGGER.warning("No target language set for translation, skipping for jobId: " + jobId);
            return;
        }

        TranslationRequestDTO requestDTO = new TranslationRequestDTO();
        requestDTO.setSource_language(grappe.getOriginalLanguage());
        requestDTO.setTarget_language(grappe.getTargetLanguage());

        SpeechData speechData = segment.getSpeechData();
        if (speechData == null || speechData.getSentences() == null) {
            LOGGER.severe("SpeechData or sentences missing for jobId: " + jobId);
            return;
        }

        List<Sentence> sentences = speechData.getSentences();
        requestDTO.setSentences(sentences);

        CompletableFuture<String> translationFuture = restApiHelper.translateSentences(requestDTO, TRANSLATION_API_URL);

        translationFuture.thenAccept(response -> {
            List<Sentence> translatedSentences = restApiHelper.parseTranslatedSentences(response);

            for (int i = 0; i < sentences.size(); i++) {
                if (i < translatedSentences.size()) {
                    sentences.get(i).setTranslatedText(translatedSentences.get(i).getTranslatedText());
                }
            }

            // Perform atomic update on MongoDB to update only the specific segment
            Query query = new Query(Criteria.where("jobId").is(jobId).and("segments.segmentID").is(segment.getSegmentID()));
            Update update = new Update()
                    .set("segments.$.speechData", speechData)
                    .set("segments.$.status", "TRANSLATED");

            mongoTemplate.updateFirst(query, update, MoviidGrappe.class);
            LOGGER.info("Successfully updated translated text for segment with jobId: " + jobId + " and segmentId: " + segment.getSegmentID());
        }).exceptionally(ex -> {
            LOGGER.severe("Error during translation or saving: " + ex.getMessage());
            return null;
        });
    }
}
