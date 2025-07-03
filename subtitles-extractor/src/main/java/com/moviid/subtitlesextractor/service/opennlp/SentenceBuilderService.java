package com.moviid.subtitlesextractor.service.opennlp;

import com.moviid.bean.MoviidGrappe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class SentenceBuilderService {

    private static final Logger LOGGER = Logger.getLogger(SentenceBuilderService.class.getName());
    private static final int MAX_CHARACTERS = 60;

    @Autowired
    private SentenceProcessor sentenceProcessor;

    public MoviidGrappe.SpeechData buildSentences(MoviidGrappe.SpeechData speechData) {
        List<MoviidGrappe.SpeechData.Sentence> sentences = new ArrayList<>();
        List<MoviidGrappe.SpeechData.Word> words = speechData.getWords();

        if (words.isEmpty()) {
            return speechData; // No words, return the original speech data
        }

        StringBuilder currentSentence = new StringBuilder();
        String sentenceStartTime = words.get(0).getStartTime();
        String currentSpeaker = words.get(0).getSpeaker();

        for (int i = 0; i < words.size(); i++) {
            MoviidGrappe.SpeechData.Word word = words.get(i);

            // Merge "Unknown" speaker into the next valid speaker sentence
            if (currentSpeaker.equals("Unknown") && !word.getSpeaker().equals("Unknown")) {
                currentSpeaker = word.getSpeaker();
                sentenceStartTime = word.getStartTime();
            }

            // If speaker changes and current sentence is valid, finalize it
            if (!word.getSpeaker().equals(currentSpeaker) && currentSentence.length() > 0) {
                processAndAddSentences(sentences, currentSentence.toString().trim(), sentenceStartTime, words.get(i - 1).getEndTime(), currentSpeaker);
                currentSentence.setLength(0);
                sentenceStartTime = word.getStartTime();
                currentSpeaker = word.getSpeaker();
            }

            // If sentence length exceeds limit, finalize and start a new sentence
            if (currentSentence.length() + word.getWord().length() + 1 > MAX_CHARACTERS) {
                processAndAddSentences(sentences, currentSentence.toString().trim(), sentenceStartTime, words.get(i - 1).getEndTime(), currentSpeaker);
                currentSentence.setLength(0);
                sentenceStartTime = word.getStartTime();
            }

            currentSentence.append(word.getWord()).append(" ");
        }

        // Process last sentence
        if (currentSentence.length() > 0) {
            processAndAddSentences(sentences, currentSentence.toString().trim(), sentenceStartTime, words.get(words.size() - 1).getEndTime(), currentSpeaker);
        }

        LOGGER.info("Built " + sentences.size() + " sentences from speech data.");
        speechData.setSentences(sentences);
        return speechData;
    }

    private void processAndAddSentences(List<MoviidGrappe.SpeechData.Sentence> sentences, String text, String startTime, String endTime, String speaker) {
        if (text.isEmpty() || text.length() <= 2) {
            return; // Skip empty or meaningless fragments
        }

        String[] detectedSentences = sentenceProcessor.detectSentences(text);
        for (String detectedSentence : detectedSentences) {
            String trimmedSentence = detectedSentence.trim();
            if (isSemanticallyCoherent(trimmedSentence)) {
                sentences.add(new MoviidGrappe.SpeechData.Sentence(startTime, endTime, trimmedSentence, speaker, true, ""));
            } else {
                sentences.add(new MoviidGrappe.SpeechData.Sentence(startTime, endTime, "N/A", speaker, false, ""));
            }
        }
    }

    private boolean isSemanticallyCoherent(String sentence) {
        return !sentence.isEmpty() && sentence.length() > 5;
    }
}
