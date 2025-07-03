package com.moviid.subtitlesextractor.service;


import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class TextClarityChecker {

    private final SentenceDetectorME sentenceDetector;
    private final TokenizerME tokenizer;

    public TextClarityChecker() {
        try {

            // Load sentence detection model
            InputStream sentenceModelStream = getClass().getResourceAsStream("/models/opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin");
            SentenceModel sentenceModel = new SentenceModel(sentenceModelStream);
            sentenceDetector = new SentenceDetectorME(sentenceModel);

            // Load tokenizer model
            InputStream tokenModelStream = getClass().getResourceAsStream("/models/opennlp-en-ud-ewt-tokens-1.0-1.9.3.bin");
            TokenizerModel tokenizerModel = new TokenizerModel(tokenModelStream);
            tokenizer = new TokenizerME(tokenizerModel);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load OpenNLP models", e);
        }
    }

    public boolean isSentenceClear(String sentence) {
        // Basic check for sentence clarity
        if (sentence == null || sentence.trim().isEmpty()) {
            return false;
        }

        // Tokenize the sentence
        String[] tokens = tokenizer.tokenize(sentence);
        int tokenCount = tokens.length;

        // A simple clarity check: Check if sentence is long enough
        // In real applications, you might want to integrate more advanced NLP techniques
        return tokenCount > 3; // Assuming a sentence with more than 3 tokens is clearer
    }
}
