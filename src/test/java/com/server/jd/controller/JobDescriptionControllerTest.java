package com.server.jd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.jd.domain.JobStatus;
import com.server.jd.dto.*;
import com.server.jd.service.JobDescriptionService;
import com.server.jd.service.SkillQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobDescriptionController.class)
@MockitoBean(types = JpaMetamodelMappingContext.class)
class JobDescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobDescriptionService jobService;

    @MockitoBean
    private SkillQueryService skillQueryService;

    @Test
    @DisplayName("JD 목록 조회 성공 - 페이징 처리 확인")
    @WithMockUser
    void list_success() throws Exception {

        JobDescriptionListResponseDto dto = JobDescriptionListResponseDto.builder()
                .id(1L)
                .title("Backend Developer")
                .location("Seoul")
                .applicantCount(10L)
                .status(JobStatus.OPEN)
                .requiredSkills(List.of("Java", "Spring"))
                .startDate(LocalDate.now())
                .deadline(LocalDate.now().plusDays(7))
                .build();

        Page<JobDescriptionListResponseDto> pageResult = new PageImpl<>(List.of(dto));

        given(jobService.getMyList(any(Pageable.class), eq(5)))
                .willReturn(pageResult);


        mockMvc.perform(get("/api/v1/jd")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.data.content[0].title").value("Backend Developer"))
                .andExpect(jsonPath("$.data.content[0].id").value(1L));
    }

    @Test
    @DisplayName("JD 상세 조회 성공")
    @WithMockUser
    void get_detail_success() throws Exception {
        Long jdId = 1L;
        JobDescriptionDetailResponseDto responseDto = JobDescriptionDetailResponseDto.builder()
                .id(jdId)
                .title("Detailed Backend Job")
                .department("Dev Team")
                .status(JobStatus.OPEN)
                .build();

        given(jobService.getDetail(jdId)).willReturn(responseDto);

        mockMvc.perform(get("/api/v1/jd/{id}", jdId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.data.title").value("Detailed Backend Job"))
                .andExpect(jsonPath("$.data.id").value(jdId));
    }

    @Test
    @DisplayName("JD 임시 저장(Draft) 생성 성공")
    @WithMockUser
    void createDraft_success() throws Exception {
        JobDescriptionCreateRequestDto request = new JobDescriptionCreateRequestDto(
                "New Job", "IT", "Full-time", "Junior", "Bachelor",
                "50M", "Description", LocalDate.now().plusDays(30),
                "Good benefits", "Seoul", "url", 100L,
                List.of("Java"), List.of("Python")
        );

        Long createdId = 10L;
        given(jobService.createDraft(any(JobDescriptionCreateRequestDto.class))).willReturn(createdId);

        mockMvc.perform(post("/api/v1/jd")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/jd/" + createdId))
                .andExpect(jsonPath("$.data").value(createdId));
    }

    @Test
    @DisplayName("스킬 목록 조회 성공")
    @WithMockUser
    void getSkills_success() throws Exception {
        SkillResponseDto skill1 = new SkillResponseDto(1L, "Java");
        SkillResponseDto skill2 = new SkillResponseDto(2L, "Spring");

        given(skillQueryService.getSkills()).willReturn(List.of(skill1, skill2));

        mockMvc.perform(get("/api/v1/jd/skills")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("Java"));
    }

    @Test
    @DisplayName("JD 상태 변경 성공")
    @WithMockUser
    void updateStatus_success() throws Exception {
        Long jdId = 1L;
        JobDescriptionStatusRequestDto request = new JobDescriptionStatusRequestDto(JobStatus.CLOSED);
        JobDescriptionStatusResponseDto response = new JobDescriptionStatusResponseDto(jdId, JobStatus.CLOSED);

        given(jobService.updateStatus(eq(jdId), any(JobDescriptionStatusRequestDto.class)))
                .willReturn(response);

        mockMvc.perform(patch("/api/v1/jd/{id}/status", jdId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    @Test
    @DisplayName("JD 내용 수정 성공")
    @WithMockUser
    void update_success() throws Exception {
        Long jdId = 1L;
        JobDescriptionUpdateRequestDto request = new JobDescriptionUpdateRequestDto(
                "Updated Title", "Dept", "Type", "Exp", "Edu", "Sal", "Desc",
                LocalDate.now(), "Ben", "Loc", "Thumb", List.of(), List.of()
        );

        JobDescriptionDetailResponseDto response = JobDescriptionDetailResponseDto.builder()
                .id(jdId)
                .title("Updated Title")
                .build();

        given(jobService.update(eq(jdId), any(JobDescriptionUpdateRequestDto.class)))
                .willReturn(response);

        mockMvc.perform(patch("/api/v1/jd/{id}", jdId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    @DisplayName("내 인터뷰 JD 목록 조회 성공")
    @WithMockUser(username = "1", roles = "USER")
    void getMyInterviewJdList_success() throws Exception {
        List<JobDescriptionInterviewOptionDto> response = List.of(
                new JobDescriptionInterviewOptionDto(100L, "Interview JD 1")
        );

        given(jobService.getMyInterviewOptionJdList()).willReturn(response);

        mockMvc.perform(get("/api/v1/jd/interview/options")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].jdId").value(100L))
                .andExpect(jsonPath("$.data[0].title").value("Interview JD 1"));
    }
}