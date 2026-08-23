import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Writes and validates portable handoff records for clean candidate commits. */
public final class SwarmHandoff {
    private static final java.util.regex.Pattern BRANCH = java.util.regex.Pattern.compile(
            "codex/(milestone|fix|experiment|train)-[a-z0-9]+(?:-[a-z0-9]+)*");
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("check"))) { new SwarmHandoff().check(); return; }
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            new SwarmHandoff().record(arguments);
        } catch (Exception error) {
            System.err.println("swarm handoff failed: " + error.getMessage()); System.exit(1);
        }
    }

    private void record(String[] arguments) throws Exception {
        require(arguments.length == 9 && arguments[0].equals("record")
                && arguments[1].equals("--ref") && arguments[3].equals("--base")
                && arguments[5].equals("--receipt") && arguments[7].equals("--disposition"), usage());
        String reference = arguments[2], branch = reference.replaceFirst("^refs/heads/", "");
        require(BRANCH.matcher(branch).matches(), "invalid handoff branch: " + branch);
        String head = git("rev-parse", "--verify", reference + "^{commit}").trim();
        String base = git("rev-parse", "--verify", arguments[4] + "^{commit}").trim();
        require(status("merge-base", "--is-ancestor", base, head) == 0, "handoff base is not an ancestor");
        Path worktree = worktree(head); require(worktree != null, "candidate has no registered worktree");
        require(gitAt(worktree, "status", "--porcelain").isBlank(), "candidate worktree is dirty");
        Path receipt = worktree.resolve(arguments[6]).normalize();
        require(receipt.startsWith(worktree) && Files.isRegularFile(receipt), "missing handoff receipt");
        String disposition = arguments[8];
        require(List.of("qualified", "integrated", "deferred", "archived").contains(disposition),
                "invalid handoff disposition");
        Path directory = root.resolve("coordination/handoffs"); Files.createDirectories(directory);
        Path output = directory.resolve(head.substring(0, 12) + "-"
                + branch.replace('/', '-') + ".properties");
        require(!Files.exists(output), "handoff record already exists: " + output.getFileName());
        Files.writeString(output, "schema=1\nbranch=" + branch + "\nworktree="
                + worktree.toString().replace('\\', '/')
                + "\nhead=" + head + "\nbase=" + base + "\nreceipt.path="
                + worktree.relativize(receipt).toString().replace('\\', '/') + "\nreceipt.sha256="
                + digest(receipt) + "\ndisposition=" + disposition + "\ncreated=" + Instant.now()
                + "\n", StandardCharsets.UTF_8);
        validate(output); System.out.println("handoff recorded: " + root.relativize(output));
    }

    private void check() throws Exception {
        Path directory = root.resolve("coordination/handoffs"); int count = 0;
        if (Files.isDirectory(directory)) try (var paths = Files.list(directory)) {
            for (Path path : paths.filter(item -> item.toString().endsWith(".properties")).sorted().toList()) {
                validate(path); count++;
            }
        }
        System.out.println("swarm handoffs: " + count + " valid records");
    }

    static void validate(Path path) throws Exception {
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); }
        require(values.stringPropertyNames().equals(java.util.Set.of("schema", "branch", "worktree",
                "head", "base", "receipt.path", "receipt.sha256", "disposition", "created")),
                "handoff fields drifted: " + path);
        require("1".equals(values.getProperty("schema")), "handoff schema drifted");
        require(BRANCH.matcher(values.getProperty("branch", "")).matches(), "invalid handoff branch");
        require(values.getProperty("head", "").matches("[0-9a-f]{40}")
                && values.getProperty("base", "").matches("[0-9a-f]{40}"), "invalid handoff SHA");
        require(values.getProperty("receipt.sha256", "").matches("[0-9a-f]{64}"),
                "invalid receipt digest");
        require(!values.getProperty("worktree", "").isBlank(), "missing handoff worktree");
        String receipt = values.getProperty("receipt.path", "");
        Path receiptPath = Path.of(receipt).normalize();
        require(!receipt.isBlank() && !receiptPath.isAbsolute() && !receiptPath.startsWith(".."),
                "invalid receipt path");
        require(List.of("qualified", "integrated", "deferred", "archived")
                .contains(values.getProperty("disposition")), "invalid handoff disposition");
        Instant.parse(values.getProperty("created", ""));
    }

    private Path worktree(String head) throws Exception {
        Path current = null;
        for (String line : git("worktree", "list", "--porcelain").lines().toList()) {
            if (line.startsWith("worktree ")) current = Path.of(line.substring(9)).toAbsolutePath().normalize();
            else if (line.equals("HEAD " + head)) return current;
        }
        return null;
    }

    private String git(String... arguments) throws Exception { return gitAt(root, arguments); }
    private static String gitAt(Path directory, String... arguments) throws Exception {
        List<String> command = new java.util.ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Path log = Files.createTempFile("worldline-handoff-git-", ".log");
        Process process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                destroy(process); throw new IllegalStateException("git timed out: " + String.join(" ", arguments));
            }
            String output = Files.readString(log, StandardCharsets.UTF_8);
            require(process.exitValue() == 0, "git failed: " + output); return output;
        } finally { Files.deleteIfExists(log); }
    }
    private int status(String... arguments) throws Exception {
        List<String> command = new java.util.ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            destroy(process); throw new IllegalStateException("git timed out: " + String.join(" ", arguments));
        }
        return process.exitValue();
    }
    private static String digest(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
    private static void selfTest() throws Exception {
        Path file = Files.createTempFile("worldline-handoff-", ".properties");
        try {
            Files.writeString(file, "schema=1\nbranch=codex/milestone-m1-test\nworktree=/tmp/w\n"
                    + "head=" + "a".repeat(40) + "\nbase=" + "b".repeat(40)
                    + "\nreceipt.path=.worldline/r.json\nreceipt.sha256=" + "c".repeat(64)
                    + "\ndisposition=qualified\ncreated=2026-08-23T00:00:00Z\n");
            validate(file);
            Files.writeString(file, Files.readString(file).replace("branch=codex/milestone-m1-test",
                    "branch=unscoped"));
            boolean rejected = false;
            try { validate(file); } catch (IllegalStateException expected) { rejected = true; }
            require(rejected, "invalid handoff branch was accepted");
            System.out.println("swarm handoff self-test passed");
        } finally { Files.deleteIfExists(file); }
    }
    private static void destroy(Process process) {
        process.descendants().sorted(java.util.Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(handle -> { if (handle.isAlive()) handle.destroyForcibly(); });
        if (process.isAlive()) process.destroyForcibly();
    }
    private static String usage() { return "usage: SwarmHandoff.java record --ref REF --base REF "
            + "--receipt RELATIVE_PATH --disposition qualified|integrated|deferred|archived | check"; }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
