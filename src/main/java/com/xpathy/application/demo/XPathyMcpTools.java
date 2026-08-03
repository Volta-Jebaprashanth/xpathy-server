package com.xpathy.application.demo;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class XPathyMcpTools {

    private final JavaSnippetRunner runner;

    public XPathyMcpTools(JavaSnippetRunner runner) {
        this.runner = runner;
    }

    @McpTool(name = "execute_xpathy", description = "Convert an XPathy Java snippet into its equivalent XPath expression. " +
            "Example snippet: id.contains(\"id-1\")")
    public String executeXPathy(
            @McpToolParam(description = "The XPathy Java snippet to evaluate", required = true)
            String expression) {
        try {
            return runner.compileAndRunExpression(expression, 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
