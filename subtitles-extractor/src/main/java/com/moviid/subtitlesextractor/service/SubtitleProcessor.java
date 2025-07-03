package com.moviid.subtitlesextractor.service;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SubtitleProcessor {

    public List<String> processSubtitles(Map<String, Object> transcriptionResponse) {
        List<Map<String, String>> words = (List<Map<String, String>>) transcriptionResponse.get("words");
        List<String> phrases = new ArrayList<>();
        StringBuilder currentPhrase = new StringBuilder();

        for (int i = 0; i < words.size(); i++) {
            Map<String, String> wordInfo = words.get(i);
            String word = wordInfo.get("word");
            currentPhrase.append(word).append(" ");

            if (i == words.size() - 1 || !areCloseInTime(words.get(i), words.get(i + 1))) {
                phrases.add(currentPhrase.toString().trim());
                currentPhrase = new StringBuilder();
            }
        }
        return phrases;
    }

    private boolean areCloseInTime(Map<String, String> currentWord, Map<String, String> nextWord) {
        // Implement logic to determine if the next word is close enough in time to be part of the same phrase
        // For example, you could parse the "endTime" of the current word and the "startTime" of the next word and compare them
        String endTime = currentWord.get("endTime");
        String startTime = nextWord.get("startTime");
        // Logic to compare times
        return true; // Or false based on your comparison logic
    }
}
