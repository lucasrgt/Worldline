import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Positive and negative process fixtures for exact OpenCode session capture. */
final class OpenCodeSessionExportSelfTest {
    private OpenCodeSessionExportSelfTest() {
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length > 0) {
            child(arguments);
            return;
        }
        Path root = Files.createTempDirectory("worldline-opencode-session-export-");
        try {
            String session = "ses_exact_export";
            List<String> valid = childPrefix("valid", root);
            OpenCodeSessionExport.Result result = OpenCodeSessionExport.capture(root, session, valid);
            Path evidence = Path.of(result.path());
            require(Files.isRegularFile(evidence) && result.sha().matches("[0-9a-f]{64}"),
                    "valid session export was not sealed");
            require(java.util.Arrays.equals(Files.readAllBytes(evidence), fixture(session, root)),
                    "captured session export was not byte-for-byte exact");
            require(!Files.readString(evidence).contains("synthetic diagnostic"),
                    "OpenCode stderr contaminated the session JSON");
            expectFailure(() -> OpenCodeSessionExport.capture(root, session, valid),
                    "existing canonical session evidence was overwritten");

            List<String> invalid = childPrefix("wrong-session", root);
            expectFailure(() -> OpenCodeSessionExport.capture(root, "ses_expected_export", invalid),
                    "mismatched session export was accepted");
            require(!Files.exists(root.resolve(".worldline/reports/swarm/"
                    + "opencode-session-ses_expected_export.json")),
                    "invalid session export was persisted");

            List<String> duplicate = childPrefix("duplicate", root);
            expectFailure(() -> OpenCodeSessionExport.capture(root, "ses_duplicate_export", duplicate),
                    "duplicate-key session JSON was accepted");
            List<String> wrongDirectory = childPrefix("wrong-directory", root);
            expectFailure(() -> OpenCodeSessionExport.capture(
                    root, "ses_wrong_directory", wrongDirectory),
                    "wrong session worktree was accepted");
            List<String> relative = childPrefix("relative-directory", root);
            expectFailure(() -> OpenCodeSessionExport.capture(
                    root, "ses_relative_directory", relative),
                    "relative session worktree was accepted");
            List<String> failed = childPrefix("fail", root);
            expectFailure(() -> OpenCodeSessionExport.capture(root, "ses_failed_export", failed),
                    "failed OpenCode export process was accepted");
            try (var paths = Files.list(root.resolve(".worldline/reports/swarm"))) {
                require(paths.noneMatch(path -> path.getFileName().toString().endsWith(".tmp")),
                        "session exporter left ambiguous temporary evidence");
            }
        } finally {
            SafeTreeDelete.delete(root);
        }
    }

    static List<String> childPrefix(String mode, Path worktree) {
        String classes = Path.of(System.getProperty("java.class.path")).toAbsolutePath().toString();
        return List.of(javaTool(), "-cp", classes,
                "OpenCodeSessionExportSelfTest", "--fake-cli", mode, worktree.toString());
    }

    private static void child(String[] arguments) {
        require(arguments.length == 6 && "--fake-cli".equals(arguments[0])
                && "export".equals(arguments[3]) && "--pure".equals(arguments[5]),
                "exporter did not append the canonical OpenCode arguments");
        String mode = arguments[1];
        Path worktree = Path.of(arguments[2]);
        String session = arguments[4];
        if ("fail".equals(mode)) {
            System.err.println("synthetic export failure");
            System.exit(7);
        }
        System.err.println("synthetic diagnostic");
        if ("duplicate".equals(mode)) {
            System.out.print("{\"info\":{\"id\":\"" + session + "\",\"id\":\""
                    + session + "\",\"directory\":\"" + escape(worktree) + "\"}}\n");
            return;
        }
        require("valid".equals(mode) || "wrong-session".equals(mode)
                        || "wrong-directory".equals(mode) || "relative-directory".equals(mode),
                "invalid fake OpenCode mode");
        String exported = "wrong-session".equals(mode) ? "ses_wrong_export" : session;
        if ("relative-directory".equals(mode)) {
            System.out.writeBytes(fixture(exported, Path.of("relative-worktree")));
        } else {
            Path directory = "wrong-directory".equals(mode) ? worktree.getParent() : worktree;
            System.out.writeBytes(fixture(exported, directory));
        }
    }

    private static byte[] fixture(String session, Path worktree) {
        String json = "{\n  \"info\":{\"id\":\"" + session + "\",\"directory\":\""
                + escape(worktree) + "\",\"title\":\"Worldline Ox Alpha\"},"
                + "\n  \"messages\":[]\n}\n";
        return json.getBytes(StandardCharsets.UTF_8);
    }

    private static String escape(Path value) {
        return value.normalize().toString().replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static String javaTool() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin",
                "java" + (windows ? ".exe" : "")).toString();
    }

    private static void expectFailure(Throwing action, String message) throws Exception {
        boolean rejected = false;
        try {
            action.run();
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        require(rejected, message);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    @FunctionalInterface
    private interface Throwing {
        void run() throws Exception;
    }
}
