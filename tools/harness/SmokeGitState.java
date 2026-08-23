import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Captures the Git identity used by a smoke-suite attestation. */
record SmokeGitState(String head, String tree, boolean clean) {
    static SmokeGitState read(Path root) throws Exception {
        if ("1".equals(System.getenv("WORLDLINE_CONTAINER_ISOLATED"))) {
            String head = required("WORLDLINE_CONTAINER_HEAD");
            String tree = required("WORLDLINE_CONTAINER_TREE");
            require(head.matches("[0-9a-f]{40,64}") && tree.matches("[0-9a-f]{40,64}"),
                    "invalid container Git identity");
            return new SmokeGitState(head, tree, true);
        }
        String head = capture(root, List.of("git", "rev-parse", "HEAD")).trim();
        String tree = capture(root, List.of("git", "rev-parse", "HEAD^{tree}")).trim();
        String status = capture(root, List.of("git", "status", "--porcelain", "--untracked-files=all"));
        require(head.matches("[0-9a-f]{40,64}") && tree.matches("[0-9a-f]{40,64}"),
                "could not bind smoke suite to Git state");
        return new SmokeGitState(head, tree, status.isBlank());
    }

    private static String required(String name) {
        String value = System.getenv(name);
        require(value != null && !value.isBlank(), "missing " + name); return value;
    }

    private static String capture(Path root, List<String> command) throws Exception {
        Path output = Files.createTempFile("worldline-smoke-git-", ".log");
        Process process = new ProcessBuilder(new ArrayList<>(command)).directory(root.toFile())
                .redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                process.destroyForcibly(); throw new IllegalStateException("git command timed out");
            }
            String text = Files.readString(output, StandardCharsets.UTF_8);
            require(process.exitValue() == 0, String.join(" ", command) + " failed:\n" + text);
            return text;
        } finally { Files.deleteIfExists(output); }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
