package com.moviid.proxy.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviid.config.AwsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class S3VideoWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3VideoWriter.class);

    private final AwsProperties awsProperties;
    private final S3Client s3Client;

    @Autowired
    public S3VideoWriter(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
        this.s3Client = createS3Client();
    }

    private S3Client createS3Client() {
        return S3Client.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                awsProperties.getAccessKey(),
                                awsProperties.getSecretKey()
                        )
                ))
                .build();
    }

    public void writeVideoToS3(String bucketName, String fileKey, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            LOGGER.error("File does not exist: {}", filePath);
            throw new IOException("File does not exist: " + filePath);
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));
            LOGGER.info("File uploaded successfully to S3. Bucket: {}, Key: {}", bucketName, fileKey);
        } catch (S3Exception e) {
            LOGGER.error("S3 exception occurred while uploading file. Bucket: {}, Key: {}", bucketName, fileKey, e);
            throw new IOException("Failed to upload file to S3", e);
        }
    }

    public boolean uploadToS3WithRetry(File file, String fileKey, int maxRetries) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                LOGGER.info("Attempting to upload segment to S3 (attempt " + (attempt + 1) + ")");
                this.writeVideoToS3("moviid", fileKey, file.getAbsolutePath());
                LOGGER.info("Segment uploaded successfully on attempt " + (attempt + 1));
                return true;
            } catch (Exception e) {
                attempt++;
                LOGGER.warn("Upload attempt " + attempt + " failed: " + e.getMessage());
                if (attempt >= maxRetries) {
                    LOGGER.error("Error uploading to S3 after " + attempt + " attempts: " + e.getMessage());
                    return false;
                }
                try {
                    Thread.sleep((long) Math.pow(2, attempt) * 1000); // Exponential backoff
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Serialises any POJO to a temp file and uploads it to S3.
     *
     * @return the S3 key used (in case you want to store it in Mongo)
     */
    public String uploadJsonWithRetry(Object value,
                                      String s3Key,
                                      int maxRetries) {

        File tmp;
        try {
            tmp = File.createTempFile("speechData_", ".json");
            tmp.deleteOnExit();

            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp, value);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON temp file", e);
        }

        boolean ok = this.uploadToS3WithRetry(tmp, s3Key, maxRetries);
        if (!ok) {
            throw new RuntimeException("Failed to upload JSON to S3: " + s3Key);
        }
        return s3Key;
    }
}