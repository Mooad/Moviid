package com.moviid.translator.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.moviid.translator.bean.mongodb.MoviidGrappe;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TranslationRequestDTO {

    private String source_language;
    private String target_language;
    private List<MoviidGrappe.SpeechData.Sentence> sentences;

    public String getSource_language() {
        return source_language;
    }

    public void setSource_language(String source_language) {
        this.source_language = source_language;
    }

    public String getTarget_language() {
        return target_language;
    }

    public void setTarget_language(String target_language) {
        this.target_language = target_language;
    }

    public List<MoviidGrappe.SpeechData.Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(List<MoviidGrappe.SpeechData.Sentence> sentences) {
        this.sentences = sentences;
    }

    // Inner class for SentenceDTO
}
