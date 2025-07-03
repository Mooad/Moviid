package com.moviid.utils;

public class Utils {

    public static boolean isVideoFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".mp4") || lowerCaseName.endsWith(".avi") || lowerCaseName.endsWith(".mov"); // Add other extensions if needed
    }

}
