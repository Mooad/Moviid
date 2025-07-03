package com.moviid.subtitlesextractor.service.opennlp;

import opennlp.tools.sentdetect.SentenceDetectorME;
import org.springframework.stereotype.Service;

@Service
public class SentenceProcessor {

    private final SentenceDetectorME sentenceDetector;

    public SentenceProcessor(SentenceDetectorME sentenceDetector) {
        this.sentenceDetector = sentenceDetector;
    }

    public String[] detectSentences(String text) {
        return sentenceDetector.sentDetect(text);
    }
}
