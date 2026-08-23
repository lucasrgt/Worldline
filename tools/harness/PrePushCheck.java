import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

/** Platform-neutral pre-push policy used by every hook launcher. */
public final class PrePushCheck {
    private static final String ZERO = "0000000000000000000000000000000000000000";

    private PrePushCheck() { }

    public static void main(String[] arguments) {
        Path root = Path.of("").toAbsolutePath().normalize();
        try {
            if (!currentHarness(root)) { refreshAndRestart(root); return; }
            List<String> updates;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                updates = reader.lines().toList();
            }
            Decision decision = inspect(root, updates);
            if (decision.guarded) guarded(root, decision);
            else ordinary(root);
        } catch (Exception error) {
            System.err.println("pre-push blocked: " + error.getMessage()); System.exit(1);
        }
    }

    static Decision inspect(Path root, List<String> updates) throws Exception {
        String guardedSha = null;
        String guardedRef = null;
        for (String update : updates) {
            String[] fields = update.trim().split("\\s+");
            require(fields.length == 4, "invalid pre-push update line");
            String localRef = fields[0], localSha = fields[1];
            String remoteRef = fields[2], remoteSha = fields[3];
            if (ZERO.equals(localSha)) {
                require(!"refs/heads/main".equals(remoteRef), "deleting main is forbidden");
                continue;
            }
            boolean guarded = localRef.startsWith("refs/heads/codex/")
                    || "refs/heads/main".equals(remoteRef);
            String base = remoteSha;
            if (ZERO.equals(base)) base = newBranchBase(root, localSha);
            require(base != null && !base.isBlank(), "could not determine the pushed revision base");
            String changed = ProcessCapture.require(root,
                    List.of("git", "diff", "--name-only", base + "..." + localSha), 60);
            if (changed.lines().anyMatch(PrePushCheck::milestonePath)) guarded = true;
            if (!guarded) continue;
            require(guardedSha == null || guardedSha.equals(localSha),
                    "protected refs must share one orchestrator-qualified SHA");
            guardedSha = localSha;
            if (guardedRef == null || "refs/heads/main".equals(remoteRef)) guardedRef = remoteRef;
        }
        return new Decision(guardedSha != null, guardedSha, guardedRef);
    }

    static boolean milestonePath(String path) {
        String normalized = path.replace('\\', '/');
        return normalized.startsWith("smokes/") && normalized.substring(7).contains("/");
    }

    private static String newBranchBase(Path root, String localSha) throws Exception {
        String value = captureOrEmpty(root,
                List.of("git", "merge-base", localSha, "refs/remotes/origin/main"));
        if (!value.isBlank()) return value;
        return captureOrEmpty(root, List.of("git", "rev-parse", localSha + "^"));
    }

    private static String captureOrEmpty(Path root, List<String> command) throws Exception {
        try { return ProcessCapture.require(root, command, 60).trim(); }
        catch (IllegalStateException error) { return ""; }
    }

    private static void guarded(Path root, Decision decision) throws Exception {
        require("1".equals(System.getenv("WORLDLINE_ORCHESTRATOR_PUSH")),
                "milestone and main pushes belong to the orchestrator");
        PushCheck.verify(root, decision.sha, decision.remoteRef);
        System.out.println("pre-push: orchestrator authorization matches " + shortSha(decision.sha));
        if (smokeRequested()) runGate(root, "--smoke");
    }

    private static void ordinary(Path root) throws Exception {
        if (smokeRequested()) { runGate(root, "--smoke"); return; }
        if ("1".equals(System.getenv("WORLDLINE_PREPUSH_BOOTSTRAPPED"))) {
            System.out.println("pre-push: repository gate passed during Java bootstrap"); return;
        }
        System.out.println("pre-push: running repository gate...");
        runGate(root);
    }

    private static boolean currentHarness(Path root) throws Exception {
        Path marker = root.resolve(".worldline/gate/sources.sha256");
        return Files.isRegularFile(marker) && Files.readString(marker, StandardCharsets.UTF_8)
                .trim().equals(harnessDigest(root));
    }

    private static String harnessDigest(Path root) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(System.getProperty("java.version").getBytes(StandardCharsets.UTF_8));
        try (Stream<Path> paths = Files.list(root.resolve("tools/harness"))) {
            for (Path source : paths.filter(path -> path.toString().endsWith(".java")).sorted().toList()) {
                digest.update(root.relativize(source).toString().replace('\\', '/')
                        .getBytes(StandardCharsets.UTF_8));
                digest.update(Files.readAllBytes(source));
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void refreshAndRestart(Path root) throws Exception {
        runGate(root);
        List<String> command = List.of(javaTool("java"), "-cp",
                root.resolve(".worldline/gate/classes").toString(), "PrePushCheck");
        ProcessBuilder builder = new ProcessBuilder(command).directory(root.toFile()).inheritIO();
        builder.environment().put("WORLDLINE_PREPUSH_BOOTSTRAPPED", "1");
        int exit = waitFor(builder.start());
        if (exit != 0) System.exit(exit);
    }

    private static void runGate(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of(javaTool("java"),
                root.resolve("tools/harness/Gate.java").toString()));
        command.addAll(List.of(arguments));
        int exit = waitFor(new ProcessBuilder(command).directory(root.toFile()).inheritIO().start());
        require(exit == 0, "canonical Gate exited " + exit);
    }

    private static int waitFor(Process process) throws Exception {
        try { return process.waitFor(); }
        catch (InterruptedException error) {
            ProcessCapture.destroy(process); Thread.currentThread().interrupt(); throw error;
        }
    }

    private static boolean smokeRequested() {
        return "1".equals(System.getenv("WORLDLINE_PREPUSH_SMOKE"));
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin",
                name + (windows ? ".exe" : "")).toString();
    }

    private static String shortSha(String value) { return value.substring(0, Math.min(12, value.length())); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    record Decision(boolean guarded, String sha, String remoteRef) { }
}
