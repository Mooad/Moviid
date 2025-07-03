package com.moviid.bean;

import lombok.Data;

import java.io.File;
import java.util.List;

@Data
public class GlobalVideoData {
    private String title;
    private long duration;
    private String originalLanguage;
    private String targetLanguage;
    private String videoSourceUrl;
    private List<FileSegment> segments;
    private File videoFile;
    private String s3SegmentationResultFolderName;
    private String jobId; // Added to track the overall job ID
    private String status; // Added to track the processing status of the entire video

    // New methods to track and update processing stages
    public void updateStatus(String status) {
        this.status = status;
    }

    public void addSegment(FileSegment segment) {
        this.segments.add(segment);
    }
}
