package com.xpathy.application.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class XPathyController {

    private final JavaSnippetRunner runner;

    public XPathyController(JavaSnippetRunner runner) {
        this.runner = runner;
    }

    @GetMapping("/execute")
    public String executeExpression(@RequestParam String expression) {
        try {
            return runner.compileAndRunExpression(expression, 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
