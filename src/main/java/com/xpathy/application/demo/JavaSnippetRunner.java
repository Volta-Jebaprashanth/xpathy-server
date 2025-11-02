package com.xpathy.application.demo;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class JavaSnippetRunner {

    static final String PKG = "sandbox";
    static final String CLASS_NAME = "UserProgram";
    static final String FQCN = PKG + "." + CLASS_NAME;

    private final ExecutorService pool = Executors.newCachedThreadPool();

    // Wraps the user's expression into a class we can compile.
    private String wrapSource(String expression) {
        // The expression must yield a com.xpathy.sourceCode.XPathy instance.
        return """
            package %s;
            import com.xpathy.sourceCode.And;
            import com.xpathy.sourceCode.Attribute;
            import static com.xpathy.sourceCode.Attribute.*;
            import com.xpathy.sourceCode.Case;
            import static com.xpathy.sourceCode.Case.*;
            import com.xpathy.sourceCode.Condition;
            import static com.xpathy.sourceCode.Condition.*;
            import com.xpathy.sourceCode.Only;
            import static com.xpathy.sourceCode.Only.*;
            import com.xpathy.sourceCode.Or;
            import com.xpathy.sourceCode.Style;
            import static com.xpathy.sourceCode.Style.*;
            import com.xpathy.sourceCode.Tag;
            import static com.xpathy.sourceCode.Tag.*;
            import com.xpathy.sourceCode.Text;
            import com.xpathy.sourceCode.XPathy;

            //overlapping
            import static com.xpathy.sourceCode.Attribute.title;
            import static com.xpathy.sourceCode.Attribute.style;
            import static com.xpathy.sourceCode.Attribute.width;
            import static com.xpathy.sourceCode.Attribute.height;

            public class %s {
                public static String run() {
                    XPathy pathy = %s;
                    return pathy.toString();
                }
            }
            """.formatted(PKG, CLASS_NAME, expression);
    }

    public String compileAndRunExpression(String expression, long timeout, TimeUnit unit)
            throws Exception {

        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Snippet/expression is empty.");
        }

        String source = wrapSource(expression);

        // Compile to memory
        InMemoryCompiler.CompilationOutput out = InMemoryCompiler.compile(FQCN, source);
        ClassLoader loader = out.getClassLoader();

        // Reflect and run with timeout
        Callable<String> task = () -> {
            Class<?> clazz = Class.forName(FQCN, true, loader);
            Method run = clazz.getDeclaredMethod("run");
            Object ret = run.invoke(null);
            return String.valueOf(ret);
        };

        Future<String> fut = pool.submit(task);
        try {
            return fut.get(timeout, unit);
        } catch (TimeoutException te) {
            fut.cancel(true);
            throw te;
        }
    }

    // Custom exception to surface compile diagnostics
    public static class CompilationException extends RuntimeException {
        private final List<Map<String, Object>> diagnostics;
        public CompilationException(String message, List<Map<String, Object>> diagnostics) {
            super(message);
            this.diagnostics = diagnostics;
        }
        public List<Map<String, Object>> getDiagnostics() { return diagnostics; }
    }

    /** In-memory compiler using javax.tools.JavaCompiler */
    /** In-memory compiler using Janino (no system compiler dependency) */
    /** In-memory compiler using Janino (safe for fat JARs, no system compiler dependency) */
    static class InMemoryCompiler {

        static class CompilationOutput {
            private final ClassLoader classLoader;
            CompilationOutput(ClassLoader classLoader) { this.classLoader = classLoader; }
            ClassLoader getClassLoader() { return classLoader; }
        }

        static CompilationOutput compile(String fqcn, String source) {
            try {
                // Janino compiler: compiles pure Java source in memory
                org.codehaus.janino.SimpleCompiler compiler = new org.codehaus.janino.SimpleCompiler();
                compiler.cook(source);

                // Janino automatically creates a class loader that can load the compiled classes
                return new CompilationOutput(compiler.getClassLoader());

            } catch (org.codehaus.commons.compiler.CompileException e) {
                Map<String, Object> diag = Map.of(
                        "kind", "ERROR",
                        "msg", e.getMessage(),
                        "line", e.getLocation() != null ? e.getLocation().getLineNumber() : -1,
                        "col", e.getLocation() != null ? e.getLocation().getColumnNumber() : -1
                );
                throw new CompilationException("Compilation failed", List.of(diag));

            } catch (Exception e) {
                throw new RuntimeException("Unexpected compile error: " + e.getMessage(), e);
            }
        }
    }




    /** Very basic restricted loader (use containers/VMs for real safety) */
    static class RestrictedClassLoader extends ClassLoader {
        private final Map<String, byte[]> defs;
        private static final Set<String> ALLOW_PREFIX = Set.of(
                "sandbox.",                 // the generated code
                "com.xpathy.",             // your library
                "java.lang.",              // minimal core
                "java.util."               // collections used by reflection
        );
        private static final Set<String> DENY_PREFIX = Set.of(
                "java.io.", "java.nio.", "java.net.", "java.lang.reflect.Proxy",
                "javax.script.", "java.lang.Process", "java.lang.Runtime",
                "java.lang.System", "java.security.", "sun.", "jdk."
        );

        RestrictedClassLoader(Map<String, byte[]> defs) {
            super(RestrictedClassLoader.class.getClassLoader());
            this.defs = defs;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] b = defs.get(name);
            if (b != null) return defineClass(name, b, 0, b.length);
            return super.findClass(name);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (isDenied(name)) throw new SecurityException("Access denied to " + name);
            if (isAllowed(name) && defs.containsKey(name)) {
                Class<?> c = findClass(name);
                if (resolve) resolveClass(c);
                return c;
            }
            return super.loadClass(name, resolve);
        }

        private boolean isAllowed(String name) {
            for (String p : ALLOW_PREFIX) if (name.startsWith(p)) return true;
            return false;
        }
        private boolean isDenied(String name) {
            for (String p : DENY_PREFIX) if (name.startsWith(p)) return true;
            return false;
        }
    }

    public void shutdown() {
        pool.shutdownNow(); // ✅ kill threads immediately
    }

}

