import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/** Selects physical-line-budgeted tools while smoke runners use statement budgets. */
final class VerificationRoots {
    private VerificationRoots() { }

    static List<Path> read(Path root) throws IOException {
        try (Stream<Path> paths = Files.list(root.resolve("tools"))) {
            return paths.filter(path -> !path.getFileName().toString().equals("smoke")).toList();
        }
    }
}
