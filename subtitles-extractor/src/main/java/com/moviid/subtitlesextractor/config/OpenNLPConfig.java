package com.moviid.subtitlesextractor.config;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class OpenNLPConfig {

    @Bean
    public SentenceDetectorME sentenceDetectorME() throws IOException {
        // Ensure that the model path is correctly referenced
        try (InputStream modelStream = getClass().getResourceAsStream("/models/opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin")) {
            if (modelStream == null) {
                throw new IOException("Model file not found");
            }
            SentenceModel model = new SentenceModel(modelStream);
            return new SentenceDetectorME(model);
        }
    }
}
