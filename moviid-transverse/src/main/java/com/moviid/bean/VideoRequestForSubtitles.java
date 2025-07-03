package com.moviid.bean;

// Inner class representing the request body for the Flask API
public  class VideoRequestForSubtitles {

    private String video_url;

    public String getJob_id() {
        return job_id;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    private String job_id;

    public VideoRequestForSubtitles(String video_url) {
        this.video_url = video_url;
    }
    public VideoRequestForSubtitles(String video_url, String job_id) {
        this.video_url = video_url;
                this.job_id =  job_id;
    }
    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }
}