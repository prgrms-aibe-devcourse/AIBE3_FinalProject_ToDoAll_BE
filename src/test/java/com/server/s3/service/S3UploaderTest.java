package com.server.s3.service;

import com.server.s3.domain.Partition;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Tag("S3-Test")
@Slf4j
public class S3UploaderTest {
    @Autowired
    S3Uploader s3Uploader;

    @Autowired
    S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    String bucket;

    @Test
    @DisplayName("S3Uploader 실제 S3 업로드 E2E 테스트")
    public void s3UploaderE2E() {
        MockMultipartFile dummyFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy-image-content".getBytes()
        );

        String fileKey = s3Uploader.uploadFile(dummyFile, Partition.USER, "0");
        log.info("uploaded fileKey = {}", fileKey);

        HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();

        HeadObjectResponse headResponse = s3Client.headObject(headRequest);

        assertThat(headResponse).isNotNull();
        assertThat(headResponse.contentLength()).isEqualTo(dummyFile.getSize());
        assertThat(headResponse.contentType()).isEqualTo(dummyFile.getContentType());

        log.info("S3 File size={}, contentType={}",
                headResponse.contentLength(),
                headResponse.contentType());

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();
        s3Client.deleteObject(deleteRequest);

        assertThatThrownBy(() ->
                s3Client.headObject(headRequest)
        ).isInstanceOf(NoSuchKeyException.class);
    }
}