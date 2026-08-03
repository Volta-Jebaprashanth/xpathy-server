package uk.xpathy.server.application.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XPathyMcpToolsTest {

    @Mock
    private JavaSnippetRunner runner;

    @Test
    void returnsRunnerResultOnSuccess() throws Exception {
        when(runner.compileAndRunExpression("id.contains(\"id-1\")", 1, TimeUnit.SECONDS))
                .thenReturn("//*[contains(@id, 'id-1')]");

        XPathyMcpTools tools = new XPathyMcpTools(runner);

        assertThat(tools.executeXPathy("id.contains(\"id-1\")"))
                .isEqualTo("//*[contains(@id, 'id-1')]");
    }

    @Test
    void wrapsRunnerFailureAsErrorMessage() throws Exception {
        when(runner.compileAndRunExpression("bad", 1, TimeUnit.SECONDS))
                .thenThrow(new JavaSnippetRunner.CompilationException("Compilation failed", java.util.List.of()));

        XPathyMcpTools tools = new XPathyMcpTools(runner);

        assertThat(tools.executeXPathy("bad")).isEqualTo("Error: Compilation failed");
    }
}
