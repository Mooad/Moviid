package com.moviid.bean;

public class VideoRequestForSegmentation {
    private String path;
    private String sourceLanguage;
    private String destinationLanguage;
    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getDestinationLanguage() {
        return destinationLanguage;
    }

    public void setDestinationLanguage(String destinationLanguage) {
        this.destinationLanguage = destinationLanguage;
    }




    private String bucket;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}