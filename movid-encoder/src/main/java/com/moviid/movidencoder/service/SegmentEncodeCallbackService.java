package com.moviid.movidencoder.service;

import com.moviid.bean.MoviidGrappe;
import com.moviid.bean.EncodeRequest;
import com.moviid.proxy.mongodb.repository.MoviidGrappeRepository;
import com.moviid.proxy.s3.S3VideoWriter;
import com.moviid.proxy.s3.S3VideoReader;
import com.moviid.utils.ProcessingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class SegmentEncodeCallbackService {

    @Autowired
    private MoviidGrappeRepository moviidGrappeRepository;
    @Autowired
    private S3VideoWriter s3VideoWriter;
    @Autowired
    private S3VideoReader s3VideoReader;

    private final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    // Main entry: called when callback received
    public void handleEncodedSegment(EncodeRequest encodeRequest) {
        // 1. Update segment status in MongoDB
        MoviidGrappe grappe = moviidGrappeRepository.findByJobId(encodeRequest.getJob_id())
                .orElseThrow(() -> new RuntimeException("No Grappe found for job: " + encodeRequest.getJob_id()));
        MoviidGrappe.Segment segment = grappe.getSegments().stream()
                .filter(s -> s.getSegmentID().equals(encodeRequest.getSegmentId()))
                .findFirst().orElseThrow(() -> new RuntimeException("No segment found: " + encodeRequest.getSegmentId()));

        segment.setStatus(ProcessingStatus.COMPLETED.toString());
        moviidGrappeRepository.save(grappe);

        // 2. Check if all segments are ENCODED
        boolean allEncoded = grappe.getSegments().stream()
                .allMatch(s -> ProcessingStatus.COMPLETED.toString().equals(s.getStatus()));

        if (allEncoded) {
            // 3. Merge segments!
            try {
                mergeAndUpload(grappe);
            } catch (Exception e) {
                throw new RuntimeException("Failed to merge and upload: " + e.getMessage(), e);
            }
        }
    }

    // Merges all encoded segments and uploads merged video to S3
    private void mergeAndUpload(MoviidGrappe grappe) throws IOException, InterruptedException {
        String jobId = grappe.getJobId();
        String bucket = "moviid";
        String encodedPrefix = "Encoding/" + jobId + "/";  // all segments here

        // Create a temp folder
        Path tmpDir = Files.createTempDirectory("merge-" + jobId + "-");
        List<String> segmentFiles = new ArrayList<>();

        // Download all *_encoded.mp4 files from S3 to temp dir, build file list for ffmpeg
        for (MoviidGrappe.Segment s : grappe.getSegments()) {
            String key = encodedPrefix + s.getSegmentID() + "_encoded.mp4";
            byte[] videoBytes = s3VideoReader.readVideoFromS3(bucket, key);
            if (videoBytes == null) throw new IOException("Failed to download segment: " + key);
            Path videoPath = tmpDir.resolve( s.getSegmentID() + "_encoded.mp4");
            Files.write(videoPath, videoBytes);
            segmentFiles.add(videoPath.toString());
        }

        // Write ffmpeg concat list file
        Path listFile = tmpDir.resolve("list.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(listFile)) {
            for (String f : segmentFiles) {
                writer.write("file '" + f.replace("'", "'\\''") + "'\n");
            }
        }

        // Output merged video
        Path mergedFile = tmpDir.resolve(jobId + ".mp4");
        List<String> ffmpegCmd = Arrays.asList(
                "ffmpeg", "-y", "-f", "concat", "-safe", "0",
                "-i", listFile.toString(),
                "-c", "copy", mergedFile.toString()
        );

        ProcessBuilder pb = new ProcessBuilder(ffmpegCmd);
        pb.redirectErrorStream(true);
        Process proc = pb.start();

        // Log ffmpeg output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[FFMPEG] " + line);
            }
        }
        int exit = proc.waitFor();
        if (exit != 0) {
            throw new RuntimeException("FFmpeg merge failed with exit code " + exit);
        }

        // Upload merged file to S3: moviid/Encoding/jobId.mp4
        String outKey = "Encoding/" + jobId + ".mp4";
        s3VideoWriter.writeVideoToS3(bucket, outKey, mergedFile.toString());

        // Optionally: update Grappe status, clean up, etc.
        grappe.setProcessingStatus(ProcessingStatus.MERGED.toString());
        moviidGrappeRepository.save(grappe);

        // Clean up temp files
        for (String f : segmentFiles) Files.deleteIfExists(Paths.get(f));
        Files.deleteIfExists(listFile);
        Files.deleteIfExists(mergedFile);
        Files.deleteIfExists(tmpDir);
    }


}