package com.moviid.vidprocessor.service;

public class ParamService {

    /**
     * Calculate the number of segments for a video based on its duration.
     *
     * @param videoDurationInSeconds the duration of the video in seconds
     * @return the number of segments the video should be divided into
     */
    public static int calculateNumberOfSegments(long videoDurationInSeconds) {
        // Convert duration from seconds to minutes for easier comparison
        double videoDurationInMinutes = videoDurationInSeconds / 60.0;

        // Determine the number of segments based on the duration
        if (videoDurationInMinutes <= 1) {
            return 6; // If video duration is less than or equal to 1 minute
        } else if (videoDurationInMinutes > 1 && videoDurationInMinutes <= 3) {
            return 6; // If video duration is more than 1 minute and up to 3 minutes
        } else if (videoDurationInMinutes > 3 && videoDurationInMinutes <= 5) {
            return 20; // If video duration is more than 3 minutes and up to 5 minutes
        } else if (videoDurationInMinutes > 5 && videoDurationInMinutes <= 7) {
            return 25; // If video duration is more than 5 minutes and up to 7 minutes
        } else if (videoDurationInMinutes > 7 && videoDurationInMinutes <= 10) {
            return 30; // If video duration is more than 7 minutes and up to 10 minutes
        } else {
            // For videos longer than 10 minutes, you might want to define another segment strategy
            // Here, just returning a default value or you could implement a more complex logic
            return 30; // Default to 30 segments or adjust as needed
        }
    }
}
