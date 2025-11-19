package com.server.mcp.dto;

import java.util.Map;

public record McpRequest(
        String method,
        String toolName,
        Map<String, Object> params
) {
}
