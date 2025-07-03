package com.moviid.subtitlesextractor.service;


import com.moviid.bean.MergeRequest;
import com.moviid.bean.MoviidGrappe;
import com.moviid.proxy.mongodb.repository.MoviidGrappeRepository;
import com.moviid.proxy.s3.S3VideoWriter;
import com.moviid.proxy.sqs.SQSMessageLoader;
import com.moviid.subtitlesextractor.service.opennlp.SentenceBuilderService;
import com.moviid.utils.ProcessingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.logging.Logger;

@Service
public class MergeSubtitlesToSegmentService {
    private static final Logger LOGGER = Logger.getLogger(MergeSubtitlesToSegmentService.class.getName());

    @Autowired
    private SentenceBuilderService sentenceBuilderService;

    @Autowired
    private MoviidGrappeRepository moviidGrappeRepository;

    @Autowired
    private SQSMessageLoader sqsMessageLoader;

    @Autowired
    private S3VideoWriter s3VideoWriter;

    // Object to synchronize on when accessing the MoviidGrappe document
    private final Object lock = new Object();

    public void mergeSubtitles(MergeRequest mergeRequest) {
        LOGGER.info("Merging subtitles for segment ID: " + mergeRequest.getSegmentId());

        // Log additional details from SqsVideoSegmentMessage
        LOGGER.info("Processing SqsVideoSegmentMessage: " + mergeRequest.getSqsMessage().getGlobalJobId());

        // Build sentences from the speech data
        MoviidGrappe.SpeechData updatedSpeechData = sentenceBuilderService.buildSentences(mergeRequest.getSpeechData()); // assuming 500ms as the max gap

        synchronized (lock) {
            // Retrieve the Moviid Grappe document by jobId
            MoviidGrappe moviidGrappe = moviidGrappeRepository.findByJobId(mergeRequest.getJob_id())
                    .orElseThrow(() -> new RuntimeException("Document not found with jobId: " + mergeRequest.getJob_id()));

            // Find the segment by segmentId
            MoviidGrappe.Segment segment = findSegmentById(moviidGrappe, mergeRequest.getSegmentId());

            if (segment == null) {
                LOGGER.warning("Segment not found for ID: " + mergeRequest.getSegmentId());
                return;
            }

            // Setting the segment status
            segment.setStatus(ProcessingStatus.WAITING_FOR_TRANSLATION.toString());

            // Merge existing speech data with updated speech data
            MoviidGrappe.SpeechData existingSpeechData = segment.getSpeechData();
            MoviidGrappe.SpeechData mergedSpeechData = mergeSpeechData(existingSpeechData, updatedSpeechData);

            if (mergedSpeechData == null) {
                LOGGER.warning("Merged speech data is null for segment ID: " + mergeRequest.getSegmentId());
                return;
            }
            // Update the segment with the merged speech data
            segment.setSpeechData(mergedSpeechData);
            moviidGrappe.setProcessingStatus(ProcessingStatus.WAITING_FOR_ENCODING.toString());
            // Save the updated document back to MongoDB
            moviidGrappeRepository.save(moviidGrappe);

            mergeRequest.getSqsMessage().setStatus(ProcessingStatus.WAITING_FOR_ENCODING.toString());
            mergeRequest.getSqsMessage().setStartTime(segment.getStartTime());
            mergeRequest.getSqsMessage().setEndTime(segment.getEndTime());
            mergeRequest.getSqsMessage().setDuration(segment.getDuration());

            sqsMessageLoader.sendMessageToQueue(mergeRequest.getSqsMessage());

            LOGGER.info("Updated Speech Data for segment ID: " + mergeRequest.getSegmentId());

            // Log additional processing from SqsVideoSegmentMessage if necessary
            // You can use the sqsMessage to trigger additional behavior or update status
        }
    }

    // Method to find a segment by its ID
    private MoviidGrappe.Segment findSegmentById(MoviidGrappe grappe, String segmentId) {
        return grappe.getSegments().stream()
                .filter(segment -> segment.getSegmentID().endsWith(segmentId))
                .findFirst()
                .orElse(null); // Return null if segment is not found
    }

    // Method to merge existing speech data with updated speech data
    private MoviidGrappe.SpeechData mergeSpeechData(MoviidGrappe.SpeechData existingSpeechData, MoviidGrappe.SpeechData updatedSpeechData) {
        if (updatedSpeechData == null) {
            LOGGER.warning("Updated speech data is null, nothing to merge.");
            return existingSpeechData;
        }

        if (existingSpeechData == null) {
            return updatedSpeechData;
        }

        // Assuming that SpeechData contains a list of words and sentences
        existingSpeechData.getWords().addAll(updatedSpeechData.getWords());
        existingSpeechData.getSentences().addAll(updatedSpeechData.getSentences());

        // Optionally handle duplicates or conflicts here
        return existingSpeechData;
    }
}
