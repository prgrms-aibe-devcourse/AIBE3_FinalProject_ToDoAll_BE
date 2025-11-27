package com.server.s3.service;

import com.server.global.exception.ApplicationException;
import com.server.s3.domain.Partition;
import com.server.s3.exception.S3ErrorCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class S3Uploader {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    private final Pattern FILE_KEY_PATTERN = Pattern.compile(
            "^(?<partition>user|job|resume)/(?<relativeId>[^/]+)/[^/_]+_(?<tag>[^\\.]+)\\.[^\\.]+$"
    );

    public S3Uploader(S3Client s3Client, S3Presigner presigner, @Value("${cloud.aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.s3Presigner = presigner;
        this.bucket = bucket;
    }

    public String uploadFile(MultipartFile multipartFile, Partition partition, String relativeId) {
        return upload(multipartFile, partition.getValue(), relativeId, "any");
    }

    public String uploadFile(MultipartFile multipartFile, Partition partition, String relativeId, String tag) {
        return upload(multipartFile, partition.getValue(), relativeId, tag);
    }

    String upload(MultipartFile multipartFile, String partition, String relativeId, String tag) {
        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ApplicationException(S3ErrorCase.FILENAME_NOT_FOUND);

        }

        String ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String fileKey = partition + "/" + relativeId + "/" + UUID.randomUUID() + "_" + tag + ext;

        log.info("FileKey : {}", fileKey);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();

        try {
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize())
            );
            return fileKey;
        } catch (IOException e) {
            throw new ApplicationException(S3ErrorCase.FILE_UPLOAD_FAIL);
        }
    }

    String createPresignedGetUrl(String fileKey, Duration expiresIn) {
        validateFileKey(fileKey);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(expiresIn)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest =
                s3Presigner.presignGetObject(presignRequest);

        return presignedGetObjectRequest.url().toString();
    }

    public String createPresignedGetUrl(String fileKey) {
        return createPresignedGetUrl(fileKey, Duration.ofMinutes(10));
    }

    Matcher validateFileKey(String fileKey) {
        Matcher matcher = FILE_KEY_PATTERN.matcher(fileKey);
        if (!matcher.matches()) {
            throw new ApplicationException(S3ErrorCase.INVALID_FILE_KEY);
        }

        return matcher;
    }

    public String updateFile(MultipartFile newFile, String oldFileKey) {
        Matcher matcher = validateFileKey(oldFileKey);

        String partition = matcher.group(1);
        String relativeId  = matcher.group(2);
        String tag         = matcher.group(3);
        String newFileKey = upload(newFile, partition, relativeId, tag);

        deleteFile(oldFileKey);
        return newFileKey;
    }

    void deleteFile(String fileKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();

        s3Client.deleteObject(request);
    }
}
