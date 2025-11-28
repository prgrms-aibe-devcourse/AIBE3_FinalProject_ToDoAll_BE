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
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * S3 연동 코드 구현
 *
 * <p>파일키 규칙은 다음과 같습니다.
 * <blockquote>
 *     <p>{@code {partition}/{relativeId}/filename_{tag}.ext}
 *     <p>ex) user/12/9fba8dc6-d030-4690-bdc1-542c58a7fb59_profile.pdf
 * </blockquote>
 * <p>filename은 uuid로 구성됩니다. (원본 파일 이름은 사용하지 않습니다.)
 * <p>tag의 default 값은 {@code any}' 입니다.
 *
 * <p>현재 코드에서 사용 중인 presigned url 방식은 만료 시간이 정해져있어서,
 * s3에 파일을 요청할 때마다 링크가 변경됩니다.
 * 따라서 DB에는 URL이 아닌 파일키를 저장하시고,
 * 클라이언트 단에는 {@code createPresignedGetUrl} 메소드로 presigned url을 발급받아 넘겨주셔야 합니다.
 * @author  dbjoung
 */
@Slf4j
@Service
public class S3Uploader {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    private final Pattern FILE_KEY_PATTERN = Pattern.compile(
            "^(?<partition>[^/]+)/(?<relativeId>[^/]+)/[^/_]+_(?<tag>[^\\.]+)\\.[^\\.]+$"
    );

    public S3Uploader(S3Client s3Client, S3Presigner presigner, @Value("${cloud.aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.s3Presigner = presigner;
        this.bucket = bucket;
    }

    public String uploadFile(MultipartFile multipartFile, Partition partition, String relativeId) {
        return upload(multipartFile, partition.getValue(), relativeId, "any");
    }

    /**
     * S3에 파일을 업로드하는 메소드입니다.
     * @param multipartFile 업로드할 파일 정보를 가진 MultipartFile 객체
     * @param partition 파일을 업로드 할 폴더(파티션). Partition Enum에서 폴더 종류를 관리합니다. 폴더 추가 필요 시 해당 enum을 수정해 사용해주세요.
     * @param relativeId 업로드하려는 이미지와 관련된 id값 (ex. 프로필 이미지일 경우 해당 사용자의 id)
     * @param tag 파일명 끝부분에 덧붙여지는 추가 구분자입니다. (default = 'any')
     * @return {@code FileKey} 문자열을 반환합니다.
     */
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
                .contentType(multipartFile.getContentType())
                .build();

        try {
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize())
            );
            return fileKey;
        } catch (Exception e) {
            log.error("S3 파일 업로드 실패. FileKey={}", fileKey, e);
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

    Matcher validateFileKey(String fileKey) {
        Matcher matcher = FILE_KEY_PATTERN.matcher(fileKey);
        if (!matcher.matches()) {
            throw new ApplicationException(S3ErrorCase.INVALID_FILE_KEY);
        }

        boolean exists = Arrays.stream(Partition.values())
                .anyMatch(p -> p.getValue().equals(matcher.group("partition")));
        if (!exists) {
            throw new ApplicationException(S3ErrorCase.INVALID_FILE_KEY);
        }

        return matcher;
    }

    /**
     * 기존에 저장돼있던 파일을 새 파일로 교체하는 메소드입니다. 새 파일 업로드 후, 기존 파일을 삭제하는 순서로 작동합니다.
     * 기존 파일의 partition, relativeId, tag를 재사용합니다.
     * @param newFile 새 파일
     * @param oldFileKey 교체할 기존 파일의 파일키
     * @return 새 파일의 파일 키를 반환합니다.
     */
    public String updateFile(MultipartFile newFile, String oldFileKey) {
        Matcher matcher = validateFileKey(oldFileKey);

        String partition = matcher.group("partition");
        String relativeId = matcher.group("relativeId");
        String tag = matcher.group("tag");
        String newFileKey = upload(newFile, partition, relativeId, tag);

        deleteFile(oldFileKey);
        return newFileKey;
    }

    /**
     * S3에서 특정 파일키의 파일을 삭제합니다.
     * <p>현재는 package-private이지만 필요 시 public으로 변경 후 사용해주세요.
     * @param fileKey 삭제할 파일의 파일키
     */
    void deleteFile(String fileKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(request);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패. FileKey={}", fileKey, e);
        }
    }
}
