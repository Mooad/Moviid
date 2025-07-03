package com.moviid.bean.sqs;

import com.google.gson.Gson;

import java.util.Map;

public class SqsVideoSegmentMessage {
    private String jobId;
    private String globalJobId;
    private String originalLanguage;
    private String targetLanguage;
    private String module;
    private String groupe;
    private String action;
    private InputData input;
    private OutputData output;
    private String status; // To track the status of this specific message
    private long startTime;            // Start time of the segment in microseconds
    private long endTime;              // End time of the segment in microseconds
    private long duration;             // Duration of the segment in microseconds


    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }


    // Getters and setters
    public String getGroupe() { return groupe; }
    public void setGroupe(String groupe) { this.groupe = groupe; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public InputData getInput() { return input; }
    public void setInput(InputData input) { this.input = input; }

    public OutputData getOutput() { return output; }
    public void setOutput(OutputData output) { this.output = output; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Convert object to JSON string
    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    // Inner classes representing input and output data
    public static class InputData {
        private String videoUrl;
        private String segmentId;

        private String jobGlobalId;

        private String s3Path; // Added to include the S3 path of the segment

        // Getters and setters
        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

        public String getSegmentId() { return segmentId; }
        public void setSegmentId(String segmentId) { this.segmentId = segmentId; }

        public String getS3Path() { return s3Path; }
        public void setS3Path(String s3Path) { this.s3Path = s3Path; }
        public String getJobGlobalId() {return jobGlobalId;}
        public void setJobGlobalId(String jobGlobalId) {this.jobGlobalId = jobGlobalId;}

    }
    public String getGlobalJobId() {
        return globalJobId;
    }

    public void setGlobalJobId(String globalJobId) {
        this.globalJobId = globalJobId;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
    public static class OutputData {
        private String videoUrl;
        private Map<String, String> metadata;

        // Getters and setters
        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }


    }
}
