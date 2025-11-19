package com.server.mcp.controller;

import com.server.mcp.dto.McpRequest;
import com.server.mcp.dto.McpResponse;
import com.server.mcp.tool.McpTool;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mcp")
public class McpController {
    private final List<McpTool> tools;
    public McpController(List<McpTool> tools) {
        this.tools = tools;
    }

    @PostMapping
    public McpResponse handle(@RequestBody McpRequest request) {
        try {
            if ("list_tools".equals(request.method())) {
                List<String> toolNames = tools.stream()
                        .map(McpTool::name)
                        .toList();
                return new McpResponse(toolNames, null);
            }

            if ("call_tool".equals(request.method())) {
                McpTool tool = tools.stream()
                        .filter(t -> t.name().equals(request.toolName()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown tool: " + request.toolName()));

                Object result = tool.call(request.params());
                return new McpResponse(result, null);
            }

            return new McpResponse(null, "Unknown method: " + request.method());
        } catch (Exception e) {
            return new McpResponse(null, e.getMessage());
        }
    }
}
