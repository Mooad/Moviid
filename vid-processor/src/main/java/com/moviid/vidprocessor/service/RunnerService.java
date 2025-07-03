package com.moviid.vidprocessor.service;


import com.moviid.bean.GlobalVideoData;
import com.moviid.bean.MoviidGrappe;
import com.moviid.bean.VideoRequestForSegmentation;
import com.moviid.proxy.mongodb.repository.VideoSectionsRepository;
import com.moviid.proxy.s3.S3VideoReader;
import com.moviid.vidprocessor.service.metadata.SectionsMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class RunnerService {

    @Autowired
    private S3VideoReader s3VideoReader;

    @Autowired
    private SegmentorService segmentorService;

    @Autowired
    private SectionsMetadataService sectionsMetadataService;

    @Autowired
    private VideoSectionsRepository videoSectionsRepository;

    @Async
    public void runSegmentation(VideoRequestForSegmentation videoRequest) {
        long startTime = System.currentTimeMillis();

        GlobalVideoData globalVideoData = new GlobalVideoData();
        ExecutorService executor = Executors.newFixedThreadPool(15);

        String jobGlobalId = String.valueOf(new Date().getTime()); // Create a job ID based on the current time

        try {
            // Download the video file from S3
            byte[] videoData = s3VideoReader.readVideoFromS3(videoRequest.getBucket(), videoRequest.getPath());

            if (videoData == null) {
                System.err.println("Failed to download video from S3.");
                return;
            }

            // Save the video file to a temporary location
            File videoFile = saveVideoFile(videoData, videoRequest.getPath());

            // Save metadata of the video
            globalVideoData.setVideoSourceUrl(videoRequest.getPath());
            globalVideoData.setTitle(videoFile.getName());
            globalVideoData.setVideoFile(videoFile);
            globalVideoData.setOriginalLanguage(videoRequest.getSourceLanguage());
            globalVideoData.setTargetLanguage(videoRequest.getDestinationLanguage());
            globalVideoData.setJobId(jobGlobalId);
            // Splitting the video and saving metadata
            globalVideoData = segmentorService.splitVideo(globalVideoData, executor);


            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);

            // Transform to MongoDB video metadata document
            MoviidGrappe moviidGrappe = sectionsMetadataService.updateVideoSectionData(globalVideoData);
            videoSectionsRepository.save(moviidGrappe);

            // Optionally, clean up the temporary file
            videoFile.delete();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
            long endTime = System.currentTimeMillis();
            System.out.println("Total Execution Time: " + (endTime - startTime) + " milliseconds");
        }
    }

    /**
     * Saves video data to a temporary file with a sanitized prefix and appropriate file extension.
     *
     * @param videoData Byte array containing the video data to be saved.
     * @param fileName  Original file name which may contain the desired file extension.
     * @return          A File object pointing to the saved temporary video file.
     * @throws IOException If an I/O error occurs during file creation or writing.
     */
    private File saveVideoFile(byte[] videoData, String fileName) throws IOException {
        String fileExtension = ".tmp"; // Default extension if none is found

        // Extract the file extension if present
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            // If a dot is found and it's not the first or last character, extract the extension
            fileExtension = fileName.substring(lastDotIndex); // Includes the dot
        }

        // Ensure the prefix is valid by removing any invalid characters
        String validPrefix = fileName.replaceAll("[^a-zA-Z0-9_\\-]", "");
        if (validPrefix.length() > 3) {
            // Limit prefix length to 3 for safety
            validPrefix = validPrefix.substring(0, 3);
        } else if (validPrefix.isEmpty()) {
            // Fallback prefix if sanitization leaves it empty
            validPrefix = "vid"; // Default prefix if sanitization leaves it empty
        }

        // Create a temporary file with the sanitized prefix and the determined file extension
        Path tempFile = Files.createTempFile(validPrefix, fileExtension);

        // Write the video data to the temporary file
        Files.write(tempFile, videoData);

        // Return the temporary file as a File object
        return tempFile.toFile();
    }
}
