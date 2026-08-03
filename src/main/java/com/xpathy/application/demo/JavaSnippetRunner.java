package com.xpathy.application.demo;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class JavaSnippetRunner {

    static final String PKG = "sandbox";
    static final String CLASS_NAME = "UserProgram";
    static final String FQCN = PKG + "." + CLASS_NAME;

    private final ExecutorService pool = Executors.newCachedThreadPool();

    // Wraps the user's expression into a class we can compile.
    private String wrapSource(String expression) {
        // The expression must yield a com.xpathy.XPathy instance.
        return """
            package %s;
            import com.xpathy.And;
            import com.xpathy.Attribute;
            import static com.xpathy.Attribute.*;
            import com.xpathy.Case;
            import static com.xpathy.Case.*;
            import com.xpathy.Condition;
            import static com.xpathy.Condition.*;
            import com.xpathy.Only;
            import static com.xpathy.Only.*;
            import com.xpathy.Or;
            import com.xpathy.Style;
            import static com.xpathy.Style.*;
            import com.xpathy.Tag;
            import static com.xpathy.Tag.*;
            import com.xpathy.Text;
            import com.xpathy.XPathy;

            //overlapping
            import static com.xpathy.Attribute.title;
            import static com.xpathy.Attribute.style;
            import static com.xpathy.Attribute.width;
            import static com.xpathy.Attribute.height;

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

    /** In-memory compiler backed by the JDK's own javac, via javax.tools.JavaCompiler. */
    static class InMemoryCompiler {

        static class CompilationOutput {
            private final ClassLoader classLoader;
            CompilationOutput(ClassLoader classLoader) { this.classLoader = classLoader; }
            ClassLoader getClassLoader() { return classLoader; }
        }

        // The classpath this JVM was actually loaded with. Resolved once and cached: in a packaged
        // Spring Boot fat jar, dependency jars live as "nested:" URLs inside the outer jar and have
        // to be extracted to a real file before javac's -classpath option can reference them.
        private static final String RUNTIME_CLASSPATH = buildRuntimeClasspath();

        static CompilationOutput compile(String fqcn, String source) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new IllegalStateException(
                        "No system Java compiler available - run this server on a JDK, not a JRE-only install.");
            }

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager standardFileManager =
                    compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8);
            InMemoryFileManager fileManager = new InMemoryFileManager(standardFileManager);

            JavaFileObject sourceFile = new JavaSourceFromString(fqcn, source);
            List<String> options = List.of("-classpath", RUNTIME_CLASSPATH);

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null, fileManager, diagnostics, options, null, List.of(sourceFile));

            boolean success = Boolean.TRUE.equals(task.call());

            if (!success) {
                List<Map<String, Object>> diagList = diagnostics.getDiagnostics().stream()
                        .map(d -> {
                            Map<String, Object> diag = new LinkedHashMap<String, Object>();
                            diag.put("kind", d.getKind().toString());
                            diag.put("msg", d.getMessage(null));
                            diag.put("line", d.getLineNumber());
                            diag.put("col", d.getColumnNumber());
                            return diag;
                        })
                        .collect(Collectors.toList());
                throw new CompilationException("Compilation failed", diagList);
            }

            ClassLoader loader = new InMemoryClassLoader(
                    fileManager.getCompiledClasses(), JavaSnippetRunner.class.getClassLoader());
            return new CompilationOutput(loader);
        }

        // Handles both a plain classpath launch (mvn spring-boot:run / java -cp ...), where
        // java.class.path is already complete, and a packaged fat jar (java -jar ...), where the
        // real dependency jars only exist as "nested:" URLs on the LaunchedClassLoader and must be
        // extracted to disk once before javac can open them.
        private static String buildRuntimeClasspath() {
            LinkedHashSet<String> entries = new LinkedHashSet<>();

            String javaClassPath = System.getProperty("java.class.path", "");
            if (!javaClassPath.isBlank()) {
                entries.addAll(Arrays.asList(javaClassPath.split(File.pathSeparator)));
            }

            ClassLoader cl = JavaSnippetRunner.class.getClassLoader();
            while (cl != null) {
                if (cl instanceof URLClassLoader urlClassLoader) {
                    for (URL url : urlClassLoader.getURLs()) {
                        String path = resolveToLocalFile(url);
                        if (path != null) entries.add(path);
                    }
                }
                cl = cl.getParent();
            }

            return String.join(File.pathSeparator, entries);
        }

        private static String resolveToLocalFile(URL url) {
            try {
                if ("file".equals(url.getProtocol())) {
                    return new File(url.toURI()).getAbsolutePath();
                }
                // e.g. Spring Boot's "nested:" scheme for BOOT-INF/lib/*.jar inside a fat jar --
                // extract once to a cached temp file so javac's -classpath can open it directly.
                File cached = new File(System.getProperty("java.io.tmpdir"),
                        "xpathy-cp-" + Integer.toHexString(url.toString().hashCode()) + ".jar");
                if (!cached.exists()) {
                    try (InputStream in = url.openStream()) {
                        Files.copy(in, cached.toPath());
                    }
                    cached.deleteOnExit();
                }
                return cached.getAbsolutePath();
            } catch (Exception e) {
                return null;
            }
        }
    }

    /** Wraps a source string as a compilation unit javac can consume without touching disk. */
    static class JavaSourceFromString extends SimpleJavaFileObject {
        private final String code;
        JavaSourceFromString(String fqcn, String code) {
            super(URI.create("string:///" + fqcn.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    /** Captures one compiled class's bytecode in memory instead of writing it to disk. */
    static class InMemoryClassFile extends SimpleJavaFileObject {
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        InMemoryClassFile(String fqcn) {
            super(URI.create("mem:///" + fqcn.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
        }
        @Override
        public OutputStream openOutputStream() {
            return bytes;
        }
        byte[] getBytes() {
            return bytes.toByteArray();
        }
    }

    /** Redirects javac's compiled .class output into memory instead of the filesystem. */
    static class InMemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<String, InMemoryClassFile> compiled = new HashMap<>();

        InMemoryFileManager(StandardJavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className,
                                                     JavaFileObject.Kind kind, FileObject sibling) {
            InMemoryClassFile file = new InMemoryClassFile(className);
            compiled.put(className, file);
            return file;
        }

        Map<String, byte[]> getCompiledClasses() {
            Map<String, byte[]> result = new HashMap<>();
            compiled.forEach((name, file) -> result.put(name, file.getBytes()));
            return result;
        }
    }

    /** Loads the classes InMemoryFileManager captured during compilation. */
    static class InMemoryClassLoader extends ClassLoader {
        private final Map<String, byte[]> classes;

        InMemoryClassLoader(Map<String, byte[]> classes, ClassLoader parent) {
            super(parent);
            this.classes = classes;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = classes.get(name);
            if (bytes == null) throw new ClassNotFoundException(name);
            return defineClass(name, bytes, 0, bytes.length);
        }
    }


    @PreDestroy
    public void shutdown() {
        pool.shutdownNow();
    }

}

