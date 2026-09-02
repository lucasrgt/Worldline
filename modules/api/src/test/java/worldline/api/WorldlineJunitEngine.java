package worldline.api;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Discovers and runs @org.junit.jupiter.api.Test methods on host test classes. */
public final class WorldlineJunitEngine {
    private WorldlineJunitEngine() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length < 1 || arguments.length > 2)
            throw new IllegalArgumentException("usage: WorldlineJunitEngine CLASS [--expect-failure]");
        boolean expectFailure = arguments.length == 2 && "--expect-failure".equals(arguments[1]);
        if (arguments.length == 2 && !expectFailure)
            throw new IllegalArgumentException("usage: WorldlineJunitEngine CLASS [--expect-failure]");
        Result result = run(arguments[0]);
        System.out.print(result.report);
        if (expectFailure) {
            if (result.failures == 0)
                throw new AssertionError("JUnit engine reported success for a failing test");
            return;
        }
        if (result.failures != 0) System.exit(1);
    }

    static Result run(String className) throws Exception {
        Class<?> type = Class.forName(className);
        Object instance = type.getDeclaredConstructor().newInstance();
        List<Method> tests = new ArrayList<Method>();
        for (Method method : type.getMethods())
            if (method.getAnnotation(Test.class) != null && method.getParameterCount() == 0)
                tests.add(method);
        if (tests.isEmpty()) throw new IllegalStateException("no @Test methods: " + className);
        int failures = 0;
        StringBuilder report = new StringBuilder();
        for (Method method : tests) {
            try {
                method.invoke(instance);
                report.append(className).append('.').append(method.getName())
                        .append(" PASSED\n");
            } catch (Exception error) {
                failures++;
                Throwable cause = error.getCause() == null ? error : error.getCause();
                report.append(className).append('.').append(method.getName())
                        .append(" FAILED: ").append(cause).append('\n');
            }
        }
        report.append("JUnit engine: tests=").append(tests.size())
                .append(" failures=").append(failures).append('\n');
        return new Result(failures, report.toString());
    }

    static String sha256Text(String value) throws Exception {
        Process process = new ProcessBuilder("java",
                Path.of("tools/harness/HexDigest.java").toString(),
                "--sha256-text", value).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException("HexDigest failed: " + output);
        return output.trim();
    }

    static final class Result {
        final int failures;
        final String report;
        Result(int failures, String report) {
            this.failures = failures;
            this.report = report;
        }
    }
}
