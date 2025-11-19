package com.server.mcp.tool;

import java.util.Map;

public interface McpTool {
    String name();
    Object call(Map<String, Object> params);
}
