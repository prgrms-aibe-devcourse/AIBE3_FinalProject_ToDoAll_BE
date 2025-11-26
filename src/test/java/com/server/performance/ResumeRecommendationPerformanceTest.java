package com.server.performance;

import com.server.support.TestEnvLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:sql/fake_resume_1000.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ResumeRecommendationPerformanceTest extends TestEnvLoader {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testResumeRecommendationPerformance() throws Exception {
        int totalRequests = 100;
        long totalExecutionTime = 0;

        for (int i = 0; i < totalRequests; i++) {
            long start = System.currentTimeMillis();

            mockMvc.perform(get("/api/v1/matches/recommendations")
                            .param("jdId", "9999"))
                    .andExpect(status().isOk());

            long executionTime = System.currentTimeMillis() - start;
            totalExecutionTime += executionTime;
        }

        double averageTime = totalExecutionTime / (double) totalRequests;
        System.out.println("평균 추천 API 응답 시간: " + averageTime + "ms");
    }
}
