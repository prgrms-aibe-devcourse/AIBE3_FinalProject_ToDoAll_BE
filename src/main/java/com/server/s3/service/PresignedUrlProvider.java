package com.server.s3.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
public class PresignedUrlProvider {
    private final S3Presigner s3Presigner;
    private final String bucket;

    public PresignedUrlProvider(S3Presigner presigner,
                                @Value("${cloud.aws.s3.bucket}") String bucket) {
        this.s3Presigner = presigner;
        this.bucket = bucket;
    }

    String createPresignedGetUrl(String fileKey, Duration expiresIn) {
        S3FileKey.from(fileKey);

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

    /**
     * S3에 저장돼있는 파일의 파일키를 넘겨주면 presigned url을 반환합니다.
     * 해당 url로 s3 비인가 사용자도 해당 파일에 접근할 수 있지만, 만료 시간이 있는 url입니다.
     * <p>현재 만료 시간 : 24시간
     * @param fileKey presigned url을 발급 받을 파일의 파일키
     * @return presigned url 을 반환합니다.
     */
    public String createPresignedGetUrl(String fileKey) {
        return createPresignedGetUrl(fileKey, Duration.ofHours(24));
    }
}
