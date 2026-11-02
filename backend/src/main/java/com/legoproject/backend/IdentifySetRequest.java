package com.legoproject.backend;

import java.util.List;

public class IdentifySetRequest {

    private List<String> candidates;
    private String ocrText;

    public IdentifySetRequest() {
    }

    public List<String> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<String> candidates) {
        this.candidates = candidates;
    }

    public String getOcrText() {
        return ocrText;
    }

    public void setOcrText(String ocrText) {
        this.ocrText = ocrText;
    }
}
