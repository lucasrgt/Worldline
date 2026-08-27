import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Captures one exact private OpenCode session export without shell redirection. */
public final class OpenCodeSessionExport {
    private static final int MAX_BYTES = 64 * 1024 * 1024;

    private OpenCodeSessionExport() {
    }

    public static void main(String[] arguments) {
        try {
            Request request = Request.parse(arguments);
            Result result = capture(request.worktree, request.session, List.of(opencodeTool()));
            System.out.println("OpenCode session export PASS: " + result.path);
            System.out.println("  evidence sha256: " + result.sha);
        } catch (Exception error) {
            System.err.println("OpenCode session export failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static Result capture(Path worktree, String session, List<String> executable)
            throws Exception {
        Path exactWorktree = worktree.toAbsolutePath().normalize();
        require(session.matches("[A-Za-z0-9_-]+"), "invalid OpenCode session ID");
        require(Files.isDirectory(exactWorktree), "session worktree is missing");
        Path reports = exactWorktree.resolve(".worldline/reports/swarm");
        Path output = evidencePath(exactWorktree, session);
        require(!Files.exists(output), "session evidence already exists");
        Files.createDirectories(reports);
        Path temporary = Files.createTempFile(reports, ".opencode-session-" + session + "-", ".json.tmp");
        Path errors = Files.createTempFile(reports, ".opencode-session-" + session + "-", ".stderr.tmp");
        List<String> command = new ArrayList<>(executable);
        command.addAll(List.of("export", session, "--pure"));
        boolean passed = false;
        try {
            Process process = new ProcessBuilder(command).directory(exactWorktree.toFile())
                    .redirectOutput(temporary.toFile()).redirectError(errors.toFile()).start();
            process.getOutputStream().close();
            boolean ended = process.waitFor(120, TimeUnit.SECONDS);
            if (!ended) {
                destroy(process);
            }
            require(ended, "opencode export timed out");
            require(Files.size(errors) <= MAX_BYTES, "OpenCode export stderr is too large");
            require(process.exitValue() == 0,
                    "opencode export exited " + process.exitValue() + ": " + tail(errors));
            require(Files.size(temporary) <= MAX_BYTES, "OpenCode session export is too large");
            validateEvidence(Files.readString(temporary, StandardCharsets.UTF_8),
                    session, exactWorktree);
            String digest = sha(temporary);
            Files.move(temporary, output, StandardCopyOption.ATOMIC_MOVE);
            passed = true;
            return new Result(output.toString(), digest);
        } catch (Exception error) {
            String suffix = "-failed-" + ProcessHandle.current().pid() + "-" + System.nanoTime();
            Path failedOutput = reports.resolve("opencode-session-" + session + suffix + ".json");
            Path failedErrors = reports.resolve("opencode-session-" + session + suffix + ".stderr.log");
            Files.move(temporary, failedOutput, StandardCopyOption.ATOMIC_MOVE);
            Files.move(errors, failedErrors, StandardCopyOption.ATOMIC_MOVE);
            throw new IllegalStateException(error.getMessage() + "; preserved stdout=" + failedOutput
                    + " sha256=" + sha(failedOutput) + " stderr=" + failedErrors
                    + " sha256=" + sha(failedErrors), error);
        } finally {
            if (passed) {
                Files.deleteIfExists(temporary);
                Files.deleteIfExists(errors);
            }
        }
    }

    static void validateEvidence(String json, String session, Path worktree) throws Exception {
        Map<String, Object> root = MiniJson.object(json);
        Map<String, Object> info = MiniJson.asObject(root.get("info"), "info");
        require(MiniJson.string(info, "id").equals(session),
                "OpenCode session export ID drifted");
        Path claimed = Path.of(MiniJson.string(info, "directory"));
        require(claimed.isAbsolute(), "OpenCode session export worktree is relative");
        Path directory = claimed.normalize();
        Path exact = worktree.toAbsolutePath().normalize();
        require(directory.equals(exact) && directory.toRealPath().equals(exact.toRealPath()),
                "OpenCode session export worktree drifted");
    }

    static Path evidencePath(Path worktree, String session) {
        return worktree.toAbsolutePath().normalize().resolve(
                ".worldline/reports/swarm/opencode-session-" + session + ".json");
    }

    private static String sha(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                if (count > 0) {
                    digest.update(buffer, 0, count);
                }
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static String tail(Path path) throws Exception {
        String text = Files.readString(path, StandardCharsets.UTF_8).strip();
        return text.substring(Math.max(0, text.length() - 2048));
    }

    private static String opencodeTool() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return "opencode";
        }
        String appData = System.getenv("APPDATA");
        require(appData != null, "APPDATA is unavailable");
        Path tool = Path.of(appData, "npm", "node_modules", "opencode-ai", "bin", "opencode.exe");
        require(Files.isRegularFile(tool), "OpenCode executable is unavailable: " + tool);
        return tool.toString();
    }

    private static void destroy(Process process) {
        process.descendants().sorted(java.util.Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    record Result(String path, String sha) {
    }

    private record Request(String session, Path worktree) {
        static Request parse(String[] arguments) {
            String session = "";
            Path worktree = null;
            for (int index = 0; index < arguments.length; index += 2) {
                require(index + 1 < arguments.length, "missing value for " + arguments[index]);
                String value = arguments[index + 1];
                switch (arguments[index]) {
                    case "--session" -> session = value;
                    case "--worktree" -> worktree = Path.of(value);
                    default -> throw new IllegalArgumentException(
                            "unknown argument: " + arguments[index]);
                }
            }
            require(worktree != null, "missing --worktree");
            return new Request(session, worktree);
        }
    }
}
