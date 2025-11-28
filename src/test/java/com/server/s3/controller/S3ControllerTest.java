package com.server.s3.controller;

import com.server.s3.service.S3Uploader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@WebMvcTest(S3Controller.class)
class S3ControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    S3Uploader s3Uploader;

    @Test
    @WithMockUser(username = "testUser", roles = "USER") // ✅ 이 줄 추가
    void getDownloadUrl() throws Exception {
        String testKey = "user/0/9fba8dc6-d030-4690-bdc1-542c58a7fb59_test.pdf";
        String presignedUrl =
                "https://team2-jobda-s3.s3.ap-northeast-2.amazonaws.com/" + testKey;

        given(s3Uploader.createPresignedGetUrl(testKey))
                .willReturn(presignedUrl);

        mockMvc.perform(
                        get("/api/v1/files/download")
                                .param("fileKey", testKey)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data")
                        .value(startsWith("https://team2-jobda-s3.s3.ap-northeast-2.amazonaws.com/user/0/")));
    }
}