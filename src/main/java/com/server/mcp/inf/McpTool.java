package com.server.mcp.inf;

import java.util.Map;

public interface McpTool {
    String name();
    Object call(Map<String, Object> params);
}
