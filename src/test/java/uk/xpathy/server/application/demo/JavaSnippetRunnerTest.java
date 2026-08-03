package uk.xpathy.server.application.demo;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JavaSnippetRunnerTest {

    private final JavaSnippetRunner runner = new JavaSnippetRunner();

    @Test
    void evaluatesContainsExpression() throws Exception {
        String result = runner.compileAndRunExpression("id.contains(\"login-button\")", 5, TimeUnit.SECONDS);

        assertThat(result).isEqualTo("//*[contains(@id, 'login-button')]");
    }

    @Test
    void evaluatesAttributeEqualsExpression() throws Exception {
        String result = runner.compileAndRunExpression(
                "div.byAttribute(id).equals(\"main-container\")", 5, TimeUnit.SECONDS);

        assertThat(result).isEqualTo("//div[@id='main-container']");
    }

    @Test
    void evaluatesTextStartsWithExpression() throws Exception {
        String result = runner.compileAndRunExpression("Text.startsWith(\"Success\")", 5, TimeUnit.SECONDS);

        assertThat(result).isEqualTo("//*[starts-with(text(), 'Success')]");
    }

    @Test
    void rejectsNullExpression() {
        assertThatThrownBy(() -> runner.compileAndRunExpression(null, 5, TimeUnit.SECONDS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankExpression() {
        assertThatThrownBy(() -> runner.compileAndRunExpression("   ", 5, TimeUnit.SECONDS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void surfacesCompilationErrorsForInvalidSyntax() {
        assertThatThrownBy(() -> runner.compileAndRunExpression("this is not valid java", 5, TimeUnit.SECONDS))
                .isInstanceOf(JavaSnippetRunner.CompilationException.class);
    }

    @Test
    void surfacesCompilationErrorsForUnknownSymbol() {
        assertThatThrownBy(() ->
                runner.compileAndRunExpression("totallyUnknownThing.contains(\"x\")", 5, TimeUnit.SECONDS))
                .isInstanceOf(JavaSnippetRunner.CompilationException.class);
    }

    @Test
    void compilationExceptionCarriesDiagnostics() {
        assertThatThrownBy(() -> runner.compileAndRunExpression("!!!not java!!!", 5, TimeUnit.SECONDS))
                .isInstanceOf(JavaSnippetRunner.CompilationException.class)
                .satisfies(ex -> assertThat(((JavaSnippetRunner.CompilationException) ex).getDiagnostics())
                        .isNotEmpty());
    }

    @Test
    void timesOutOnExpressionThatNeverReturns() {
        assertThatThrownBy(() -> runner.compileAndRunExpression(
                "new java.util.function.Supplier<com.xpathy.XPathy>() { public com.xpathy.XPathy get() { " +
                        "try { Thread.sleep(10_000); } catch (InterruptedException e) {} return id.contains(\"x\"); } }.get()",
                200, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);
    }
}
