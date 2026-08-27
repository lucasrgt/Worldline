import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Small fail-closed Git ancestry boundary shared by source launchers. */
final class GitAncestry {
    private GitAncestry() {
    }

    static boolean contains(Path root, String ancestor, String descendant) throws Exception {
        List<String> command = new ArrayList<>(List.of("git", "merge-base", "--is-ancestor",
                ancestor, descendant));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        if (!process.waitFor(120, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException("git ancestry check timed out");
        }
        return process.exitValue() == 0;
    }
}
