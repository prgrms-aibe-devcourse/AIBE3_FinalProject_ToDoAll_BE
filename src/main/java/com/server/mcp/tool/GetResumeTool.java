package com.server.mcp.tool;

import com.server.resume.service.ResumeService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GetResumeTool implements McpTool {
    private final ResumeService resumeService;
    public GetResumeTool(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @Override
    public String name() {
        return "get_resume";
    }

    @Override
    public Object call(Map<String, Object> params) {
        Long candidateId = Long.valueOf(params.get("candidate_id").toString());
        String resumeText = resumeService.getResumeById(candidateId).toString();
        return Map.of("candidateId", candidateId, "resumeText", resumeText);
    }
}
