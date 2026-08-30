import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Recognizes explicit and legacy candidates whose scenario is compiled by a frozen build. */
final class CandidateRuntimeBuild {
    private CandidateRuntimeBuild() {}

    static boolean owns(Path directory, Properties descriptor) {
        return Set.of("runtime-build", "external-runtime-build").contains(
                        descriptor.getProperty("candidate.compile"))
                || !Files.isDirectory(directory.resolve("src"))
                        && Files.isDirectory(directory.resolve("runtime-src"));
    }

    static void validate(Path root, Path directory, Properties descriptor) throws IOException {
        if ("external-runtime-build".equals(descriptor.getProperty("candidate.compile"))) {
            Path source = confined(root, descriptor.getProperty("candidate.runtime.source"));
            Path builder = confined(root, descriptor.getProperty("candidate.runtime.builder"));
            require(source.startsWith(root.resolve("adapters")) && Files.isDirectory(source)
                            && hasJava(source), "external runtime build has no adapter sources");
            require(builder.startsWith(root.resolve("tools/integration"))
                            && Files.isRegularFile(builder) && builder.toString().endsWith(".java"),
                    "external runtime build has no integration builder");
            return;
        }
        Path runtime = directory.resolve("runtime-src");
        require(Files.isDirectory(runtime) && hasJava(runtime),
                "runtime-build candidate has no runtime-src sources");
        String configured = descriptor.getProperty("runner", "");
        if (!configured.isBlank()) {
            require(configured.endsWith(".gradle")
                            && Files.isRegularFile(directory.resolve(configured)),
                    "runtime-build candidate requires a frozen Gradle runner");
            return;
        }
        try (Stream<Path> files = Files.list(directory)) {
            List<Path> runners = files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".gradle"))
                    .collect(Collectors.toList());
            require(runners.size() == 1,
                    "legacy runtime-build candidate requires exactly one frozen Gradle runner");
        }
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-external-runtime-build-");
        try {
            Path source = root.resolve("adapters/example/runtime-src");
            Path builder = root.resolve("tools/integration/Builder.java");
            Files.createDirectories(source); Files.createDirectories(builder.getParent());
            Files.writeString(source.resolve("Probe.java"), "final class Probe {}\n");
            Files.writeString(builder, "final class Builder {}\n");
            Properties values = new Properties();
            values.setProperty("candidate.compile", "external-runtime-build");
            values.setProperty("candidate.runtime.source", "adapters/example/runtime-src");
            values.setProperty("candidate.runtime.builder", "tools/integration/Builder.java");
            require(owns(root.resolve("smokes/example"), values), "external runtime build was not owned");
            validate(root, root.resolve("smokes/example"), values);
            values.setProperty("candidate.runtime.source", "../outside");
            boolean rejected = false;
            try { validate(root, root.resolve("smokes/example"), values); }
            catch (IllegalStateException expected) { rejected = true; }
            require(rejected, "escaping external runtime source was accepted");
            System.out.println("  external runtime build self-test: passed");
        } finally { SafeTreeDelete.delete(root); }
    }

    private static Path confined(Path root, String relative) {
        require(relative != null && !relative.isBlank(), "missing external runtime build path");
        Path path = root.resolve(relative).normalize();
        require(path.startsWith(root) && !path.equals(root), "external runtime build path escaped");
        return path;
    }

    private static boolean hasJava(Path runtime) throws IOException {
        return SafeTreeDelete.paths(runtime).stream()
                .anyMatch(path -> path.toString().endsWith(".java"));
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
