package com.moviid.movidencoder.controller;


import com.moviid.bean.EncodeRequest;
import com.moviid.movidencoder.service.SegmentEncodeCallbackService;
import com.moviid.movidencoder.sqs.SqsMessageListenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.logging.Logger;

@RestController
public class MoviidEncodingController {

    @Autowired
    SegmentEncodeCallbackService segmentEncodeCallbackService;
    private static final Logger LOGGER = Logger.getLogger(SqsMessageListenerService.class.getName());

    @PostMapping("/callback")
    public ResponseEntity<Void> handleCallback(@RequestBody EncodeRequest result) throws IOException, InterruptedException {
        LOGGER.info("✅ received encoded segment: "+ result.getSegmentId());
        segmentEncodeCallbackService.handleEncodedSegment(result);
        return ResponseEntity.ok().build();
    }

}
