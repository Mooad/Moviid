package com.moviid.vidprocessor.api;

import com.moviid.bean.VideoRequestForSegmentation;
import com.moviid.vidprocessor.service.RunnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class SegmentationController {

    @Autowired
    private RunnerService runnerService;

    @PostMapping("/segment-video")
    public ResponseEntity<String> segmentVideo(@RequestBody VideoRequestForSegmentation videoRequest) {
        try {

            runnerService.runSegmentation(videoRequest);
            // Immediate response to indicate the process has started
            return ResponseEntity.accepted().body("Video segmentation process started.");
        } catch (Exception e) {
            // Handling the case where the asynchronous process couldn't be started
            return ResponseEntity.internalServerError().body("Error initiating video segmentation: " + e.getMessage());
        }
    }

    @GetMapping("/health-check")
    public ResponseEntity<String> isAlive() {
        return ResponseEntity.accepted().body("I'm Aliiiiiiiiiiive");
    }
}


