package com.server.s3.service;

import com.server.global.exception.ApplicationException;
import com.server.s3.domain.Partition;
import com.server.s3.exception.S3ErrorCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class S3UploaderTest {

    @MockitoBean
    private S3Client s3Client;

    @Autowired
    private S3Uploader s3Uploader;

    @Test
    @DisplayName("uploadFile Mock 테스트")
    void uploadFile_putObject() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("test-image.png");
        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream("dummy".getBytes(StandardCharsets.UTF_8)));

        Partition partition = Partition.USER;
        String relativeId = "12";
        String tag = "profile";

        String fileKey = s3Uploader.uploadFile(file, partition, relativeId, tag);

        ArgumentCaptor<PutObjectRequest> reqCaptor =
                ArgumentCaptor.forClass(PutObjectRequest.class);

        verify(s3Client, times(1))
                .putObject(reqCaptor.capture(), any(RequestBody.class));

        PutObjectRequest request = reqCaptor.getValue();

        assertThat(request.bucket()).isEqualTo("team2-jobda-s3");
        assertThat(request.key()).isEqualTo(fileKey);

        assertThat(fileKey)
                .startsWith("user/12/")
                .contains("_profile.")
                .endsWith(".png");
    }

    @Test
    @DisplayName("updateFile Mock 테스트 - 파일 업로드 실패")
    void uploadFile_IOException_FILE_UPLOAD_FAIL() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("test.png");
        when(file.getInputStream()).thenThrow(new IOException("boom"));

        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> s3Uploader.uploadFile(file, Partition.USER, "12", "profile")
        );

        assertThat(ex.getErrorCase()).isEqualTo(S3ErrorCase.FILE_UPLOAD_FAIL);
    }

    @Test
    @DisplayName("upload Mock 예외 테스트 - Null")
    void upload_null_FILENAME_NOT_FOUND() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);

        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> s3Uploader.upload(file, "user", "12", "any")
        );

        assertThat(ex.getErrorCase()).isEqualTo(S3ErrorCase.FILENAME_NOT_FOUND);
    }

    @Test
    @DisplayName("upload Mock 예외 테스트 - Blank")
    void upload_FILENAME_NOT_FOUND() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("   ");

        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> s3Uploader.upload(file, "user", "12", "any")
        );

        assertThat(ex.getErrorCase()).isEqualTo(S3ErrorCase.FILENAME_NOT_FOUND);
    }

    @Test
    @DisplayName("updateFile Mock 테스트")
    void updateFile_newFile_newFileKey() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("test-image.jpg");
        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream("dummy".getBytes(StandardCharsets.UTF_8)));

        String oldFileKey = "user/12/oldFileName_tag.png";

        String newFileKey = s3Uploader.updateFile(file, oldFileKey);

        assertThat(newFileKey)
                .startsWith("user/12/")
                .contains("_tag.")
                .endsWith(".jpg")
                .doesNotContain(oldFileKey);
    }

    @Test
    @DisplayName("validateFileKey Mock 예외 테스트")
    void validateFileKey_INVALID_FILE_KEY() throws Exception {
        String fileKey_name = "user123/12/oldFileName_tag.png";
        ApplicationException ex_name = assertThrows(
                ApplicationException.class,
                () -> s3Uploader.validateFileKey(fileKey_name)
        );

        String fileKey_id = "user123//oldFileName_tag.png";
        ApplicationException ex_id = assertThrows(
                ApplicationException.class,
                () -> s3Uploader.validateFileKey(fileKey_id)
        );

        String fileKey_tag = "user123/12/oldFileName_.png";
        ApplicationException ex_tag = assertThrows(
                ApplicationException.class,
                () -> s3Uploader.validateFileKey(fileKey_tag)
        );

        assertThat(ex_name.getErrorCase()).isEqualTo(S3ErrorCase.INVALID_FILE_KEY);
        assertThat(ex_id.getErrorCase()).isEqualTo(S3ErrorCase.INVALID_FILE_KEY);
        assertThat(ex_tag.getErrorCase()).isEqualTo(S3ErrorCase.INVALID_FILE_KEY);
    }

    @Test
    @DisplayName("deleteFile Mock 테스트")
    void  deleteFile() {
        String fileKey = "user123/12/test_tag.png";

        s3Uploader.deleteFile(fileKey);

        ArgumentCaptor<DeleteObjectRequest> reqCaptor =
                ArgumentCaptor.forClass(DeleteObjectRequest.class);

        verify(s3Client, times(1))
                .deleteObject(reqCaptor.capture());

        DeleteObjectRequest request = reqCaptor.getValue();

        assertThat(request.key()).isEqualTo(fileKey);
        assertThat(request.bucket()).isEqualTo("team2-jobda-s3");
    }
}