package com.server.mcp.dto;

public record McpResponse(
        Object result,
        String error
) {
}
