package com.xpathy.application.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


    @GetMapping("/execute")
    public String executeExpression(@RequestParam String expression) {
        return execute(expression);
    }


    public static String execute(String expression) {

        JavaSnippetRunner runner = new JavaSnippetRunner();
        try {
            return runner.compileAndRunExpression(expression, 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        } finally {
            runner.shutdown();
        }
    }
}

