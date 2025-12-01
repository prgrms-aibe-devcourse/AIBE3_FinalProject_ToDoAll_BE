package com.server.s3.service;

import com.server.global.exception.ApplicationException;
import com.server.s3.domain.Partition;
import com.server.s3.exception.S3ErrorCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class S3UploaderMockTest {

    @Mock
    S3Client s3Client;

    S3Uploader s3Uploader;

    @BeforeEach
    void setUp() {
        s3Uploader = new S3Uploader(s3Client, "team2-jobda-s3");
    }

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