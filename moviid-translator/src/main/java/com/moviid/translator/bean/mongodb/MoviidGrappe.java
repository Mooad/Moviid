package com.moviid.translator.bean.mongodb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
public class MoviidGrappe {
    @Id
    private String id;
    private String title;
    private String uploadTimestamp;
    private long duration;
    private String originalLanguage;
    private String targetLanguage;
    private String videoSourceUrl;
    private String processingStatus;
    private List<Segment> segments;
    private String completionTimestamp;
    private List<ErrorLog> errorLogs;
    private String jobId;
    private boolean wordsExtracted;
    private boolean sentencesExtracted;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Segment {
        private String segmentID;
        private long startTime;
        private long endTime;
        private long duration;
        private String segmentUrl;
        private String s3Path;
        private SpeechData speechData;
        private List<Translation> translations;
        private List<Subtitle> subtitles;
        private String status;
        private String parentJobId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SpeechData {
        private List<Word> words;
        private List<Sentence> sentences;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Word {
            private String startTime;
            private String endTime;
            private String word;
            private String speaker;  // New field to identify the speaker (Voice 1, Voice 2, etc.)
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Sentence {
            private String startTime;
            private String endTime;
            private String sentence;
            private String speaker; // New field
            private boolean isClear;
            private String translatedText;         }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Translation {
        private String translatedText; // The translated text
        private String translationTimestamp; // Timestamp when the translation was performed
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Subtitle {
        private String subtitleID; // Unique ID of the subtitle
        private long startTime; // Start time of the subtitle in microseconds
        private long endTime; // End time of the subtitle in microseconds
        private String text; // The original subtitle text
        private String translation; // The translated subtitle text
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorLog {
        private String errorTimestamp; // Timestamp when the error occurred
        private String errorMessage; // The error message
    }
}
