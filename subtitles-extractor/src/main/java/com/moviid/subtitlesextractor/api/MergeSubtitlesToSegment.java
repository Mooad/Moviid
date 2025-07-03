package com.moviid.subtitlesextractor.api;

import com.moviid.bean.MergeRequest;
import com.moviid.subtitlesextractor.service.MergeSubtitlesToSegmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subtitles")
public class MergeSubtitlesToSegment {

    private final MergeSubtitlesToSegmentService mergeSubtitlesToSegmentService;

    @Autowired
    public MergeSubtitlesToSegment(MergeSubtitlesToSegmentService mergeSubtitlesToSegmentService) {
        this.mergeSubtitlesToSegmentService = mergeSubtitlesToSegmentService;
    }

    @PostMapping("/merge")
    public ResponseEntity<String> mergeSubtitles(@RequestBody MergeRequest mergeRequest) {
        try {
            // Pass both the MergeRequest and SqsVideoSegmentMessage to the service
            mergeSubtitlesToSegmentService.mergeSubtitles(mergeRequest);
            return ResponseEntity.ok("Subtitles merged successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error merging subtitles: " + e.getMessage());
        }
    }
}
