package com.moviid.movidencoder.sqs;

import com.moviid.bean.EncodeRequest;
import com.moviid.bean.MoviidGrappe;
import com.moviid.bean.sqs.SqsVideoSegmentMessage;
import com.moviid.movidencoder.service.EncodingService;
import com.moviid.proxy.mongodb.repository.MoviidGrappeRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class SqsMessageListenerService {

    private static final Logger LOGGER = Logger.getLogger(SqsMessageListenerService.class.getName());

    private final EncodingService encodingService;
    private final MoviidGrappeRepository moviidGrappeRepository;

    @Autowired
    public SqsMessageListenerService(EncodingService encodingService,
                                     MoviidGrappeRepository moviidGrappeRepository) {
        this.encodingService = encodingService;
        this.moviidGrappeRepository = moviidGrappeRepository;
    }

    @SqsListener("WaitingForEncoding")
    public void receiveMessage(@Payload SqsVideoSegmentMessage message,
                               @Header(name = "ApproximateReceiveCount", required = false) Integer receiveCount) {

        int attempt = receiveCount != null ? receiveCount : 1;
        LOGGER.info(() -> "📨 Received encoding request (attempt " + attempt + ") – jobId=" + message.getJobId());

        try {
            MoviidGrappe grappe = moviidGrappeRepository.findByJobId(message.getJobId())
                    .orElseThrow(() -> new IllegalArgumentException("Grappe not found: " + message.getJobId()));

            MoviidGrappe.Segment segment = grappe.getSegments().stream()
                    .filter(s -> s.getSegmentID().equals(message.getInput().getSegmentId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Segment not found: " + message.getInput().getSegmentId()));

            EncodeRequest req = new EncodeRequest();
            req.setParentFolder(extractParentFolderFromS3(message.getInput().getS3Path()));
            req.setSegmentId(segment.getSegmentID());
            req.setJob_id(message.getJobId());
            req.setBucket("moviid");
            req.setSpeechData(segment.getSpeechData());

            req.setSqsMessage(message);

            encodingService.sendForEncoding(req);

            LOGGER.info(() -> "✅ Encoding finished – segmentId=" + segment.getSegmentID());
        } catch (Exception ex) {
            LOGGER.severe("❌ Encoding failed for job " + message.getJobId() + ": " + ex.getMessage());
            throw ex;
        }
    }


    private String extractParentFolderFromS3(String s3Path) {
        String[] parts = s3Path.split("/");
        if (parts.length >= 3) return parts[2];
        throw new IllegalArgumentException("Invalid S3 path: " + s3Path);
    }
}
