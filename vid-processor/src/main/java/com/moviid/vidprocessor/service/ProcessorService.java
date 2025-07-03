package com.moviid.vidprocessor.service;


import com.moviid.bean.FileSegment;
import com.moviid.config.AwsProperties;
import com.moviid.proxy.s3.S3VideoWriter;
import com.moviid.utils.ProcessingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

@Component
public class ProcessorService {

    @Autowired
    private S3VideoWriter s3VideoWriter;

    private final AwsProperties awsProperties;

    private static final Logger LOGGER = Logger.getLogger(ProcessorService.class.getName());

    public ProcessorService(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }

    public void processSegment(FileSegment fileSegment, String uniqueExecutionFolder) {
        LOGGER.info("Entered processSegment method for file: " + fileSegment.getFilePath());
        Path tempDir = null;
        try {
            // Create temporary directory
            tempDir = Files.createTempDirectory("segments_");
            String outputFilePattern = tempDir.resolve("segment_%d.mp4").toString();
            LOGGER.info("Temporary directory created at: " + tempDir);

            double segmentDuration = (fileSegment.getEndTimestamp() - fileSegment.getStartTimestamp()) / 1_000_000.0;
            LOGGER.info("Segment duration calculated: " + segmentDuration + " seconds");

            boolean isSuccess = splitVideo(fileSegment.getFilePath(), outputFilePattern, segmentDuration);

            LOGGER.info("FFmpeg process completed with isSuccess: " + isSuccess);

            if (isSuccess) {
                File segment = new File(String.format(outputFilePattern, fileSegment.getSegmentNumber()));
                if (segment.exists()) {
                    String fileKey = "Segmentation/" + uniqueExecutionFolder + "/" + segment.getName();
                    LOGGER.info("Uploading segment: " + segment.getName() + " to S3 with key: " + fileKey);
                    boolean uploadSuccess = s3VideoWriter.uploadToS3WithRetry(segment, fileKey, 5);
                    if (uploadSuccess) {
                        LOGGER.info("Video segment successfully uploaded to S3 with key: " + fileKey);
                        fileSegment.setStatus(ProcessingStatus.WAITING_FOR_RECOGNITION.toString());
                    } else {
                        LOGGER.severe("Failed to upload video segment to S3 with key: " + fileKey);
                    }
                } else {
                    LOGGER.severe("Segment file was not created: " + segment.getAbsolutePath());
                }
            } else {
                LOGGER.severe("Video splitting failed.");
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to process segment: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up temporary directory and files
            if (tempDir != null) {
                try {
                    cleanTempDirectory(tempDir);
                } catch (IOException e) {
                    LOGGER.warning("Failed to clean temporary directory: " + tempDir);
                    e.printStackTrace();
                }
            }
        }
    }

    private void cleanTempDirectory(Path tempDir) throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(file -> {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        LOGGER.warning("Failed to delete temporary file: " + file.getAbsolutePath());
                    }
                });
        LOGGER.info("Temporary directory cleaned: " + tempDir);
    }

    private boolean splitVideo(String inputFilePath, String outputFilePattern, double segmentDuration) {
        LOGGER.info("Attempting to extract keyframes from video: " + inputFilePath);

        ProcessBuilder keyframeProcessBuilder = new ProcessBuilder(
                "ffprobe",
                "-v", "quiet",
                "-select_streams", "v:0",
                "-show_entries", "packet=pts_time,flags",
                "-of", "csv=print_section=0",
                inputFilePath
        );

        List<Double> keyframeTimes = new ArrayList<>();
        try {
            Process keyframeProcess = keyframeProcessBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(keyframeProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LOGGER.info("Keyframe Line: " + line);  // Log every line for troubleshooting
                    String[] parts = line.split(",");
                    if (parts.length == 2 && parts[1].contains("K")) {
                        keyframeTimes.add(Double.parseDouble(parts[0]));
                    }
                }
            }
            int exitCode = keyframeProcess.waitFor();
            LOGGER.info("Keyframe extraction process exited with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            LOGGER.severe("Error extracting keyframes: " + e.getMessage());
            return false;
        }

        if (keyframeTimes.isEmpty()) {
            LOGGER.severe("No keyframes found in the video.");
            return false;
        }

        LOGGER.info("Keyframes found at times: " + keyframeTimes);

        StringBuilder segmentTimes = new StringBuilder();
        double currentTime = 0;
        for (Double keyframeTime : keyframeTimes) {
            if (keyframeTime - currentTime >= segmentDuration) {
                segmentTimes.append(keyframeTime).append(",");
                currentTime = keyframeTime;
            }
        }

        if (segmentTimes.length() == 0) {
            LOGGER.severe("No suitable segment times found based on keyframes.");
            return false;
        }
        segmentTimes.setLength(segmentTimes.length() - 1); // Remove last comma

        LOGGER.info("Segment times to be used: " + segmentTimes);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", inputFilePath,
                "-f", "segment",
                "-segment_times", segmentTimes.toString(),
                "-reset_timestamps", "1",
                "-c", "copy",
                "-map", "0",
                "-avoid_negative_ts", "1",
                "-y",
                outputFilePattern
        );

        LOGGER.info("Executing FFmpeg command: " + String.join(" ", processBuilder.command()));

        try {
            Process process = processBuilder.start();
            try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String s;
                while ((s = stdInput.readLine()) != null) {
                    LOGGER.info("FFmpeg Output: " + s);
                }
                while ((s = stdError.readLine()) != null) {
                    LOGGER.severe("FFmpeg Error: " + s);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LOGGER.severe("FFmpeg process exited with code: " + exitCode);
                return false;
            }
            LOGGER.info("FFmpeg process completed successfully.");
            return true;
        } catch (IOException | InterruptedException e) {
            LOGGER.severe("Error executing FFmpeg command: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
