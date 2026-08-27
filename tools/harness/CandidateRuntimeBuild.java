import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Recognizes explicit and legacy candidates whose scenario is compiled by a frozen build. */
final class CandidateRuntimeBuild {
    private CandidateRuntimeBuild() {}

    static boolean owns(Path directory, Properties descriptor) {
        return "runtime-build".equals(descriptor.getProperty("candidate.compile"))
                || !Files.isDirectory(directory.resolve("src"))
                        && Files.isDirectory(directory.resolve("runtime-src"));
    }

    static void validate(Path directory, Properties descriptor) throws IOException {
        Path runtime = directory.resolve("runtime-src");
        require(Files.isDirectory(runtime) && hasJava(runtime),
                "runtime-build candidate has no runtime-src sources");
        String configured = descriptor.getProperty("runner", "");
        if (!configured.isBlank()) {
            String id = directory.getFileName().toString();
            boolean local = configured.matches("[A-Za-z0-9._-]+[.]gradle");
            boolean repository = configured.matches("smokes/" + java.util.regex.Pattern.quote(id)
                    + "/[A-Za-z0-9._-]+[.]gradle");
            Path runner = repository
                    ? directory.getParent().getParent().resolve(configured) : directory.resolve(configured);
            require((local || repository) && Files.isRegularFile(runner.normalize()),
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

    private static boolean hasJava(Path runtime) throws IOException {
        return SafeTreeDelete.paths(runtime).stream()
                .anyMatch(path -> path.toString().endsWith(".java"));
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
