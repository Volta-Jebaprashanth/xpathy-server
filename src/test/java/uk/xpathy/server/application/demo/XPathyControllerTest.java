package uk.xpathy.server.application.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XPathyControllerTest {

    @Mock
    private JavaSnippetRunner runner;

    @Test
    void returnsRunnerResultOnSuccess() throws Exception {
        when(runner.compileAndRunExpression("id.contains(\"x\")", 1, TimeUnit.SECONDS))
                .thenReturn("//*[contains(@id, 'x')]");

        XPathyController controller = new XPathyController(runner);

        assertThat(controller.executeExpression("id.contains(\"x\")"))
                .isEqualTo("//*[contains(@id, 'x')]");
    }

    @Test
    void wrapsRunnerFailureAsErrorMessage() throws Exception {
        when(runner.compileAndRunExpression("bad", 1, TimeUnit.SECONDS))
                .thenThrow(new IllegalArgumentException("boom"));

        XPathyController controller = new XPathyController(runner);

        assertThat(controller.executeExpression("bad")).isEqualTo("Error: boom");
    }
}
