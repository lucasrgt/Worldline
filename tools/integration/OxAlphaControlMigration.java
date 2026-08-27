import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** Rebinds one archived retryable checkpoint to a newer orchestrator control base. */
public final class OxAlphaControlMigration {
    private static final String SUPERVISION = "NYA-01M0VSCA8F3WSMVW32R9XME7DQ";
    private static final String CONTROL_BASE = "NYA-01M0ZHW1MB9W4BX3H0AY88CVY0";
    private static final List<String> OPTIONAL_CONTEXT_ERRORS = List.of(
            "Why This Way is not initialized; run wtw init",
            "rtw: Right This Way is not initialized; run rtw init",
            "Now We Can is not initialized; run nwc init");

    private OxAlphaControlMigration() {
    }

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) {
                selfTest();
                return;
            }
            migrate(Request.parse(arguments));
        } catch (Exception error) {
            System.err.println("Ox Alpha control migration failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void migrate(Request request) throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        require(request.id.matches("m[0-9]+-[a-z0-9-]+"), "invalid milestone id");
        require(request.archiveBase.matches("[0-9a-f]{40}")
                && request.preflightBase.matches("[0-9a-f]{40}")
                && request.receiptBase.matches("[0-9a-f]{40}")
                && request.newBase.matches("[0-9a-f]{40}"),
                "migration bases must be exact commit SHAs");
        require(git(root, "branch", "--show-current").trim()
                .equals("codex/milestone-" + request.id), "wrong milestone branch");
        String head = git(root, "rev-parse", "HEAD").trim();
        require(head.equals(request.newBase), "migration requires a clean worktree at the new base");
        require(git(root, "status", "--porcelain=v1", "--untracked-files=all").isBlank(),
                "migration worktree is dirty");
        require(ancestor(root, request.archiveBase, request.newBase),
                "new base does not contain the archived base");
        require(ancestor(root, request.preflightBase, request.newBase),
                "new base does not contain the preflight base");
        require(ancestor(root, request.receiptBase, request.newBase),
                "new base does not contain the prior receipt base");
        validateArchive(request);
        Path preflight = root.resolve(".worldline/reports/swarm/preflight-" + request.id + ".json");
        String prior = Files.readString(preflight, StandardCharsets.UTF_8);
        require(field(prior, "id", request.id) && field(prior, "base", request.preflightBase)
                && field(prior, "head", request.preflightBase) && field(prior, "status", "PASS"),
                "prior preflight is not bound to the archived base");
        Result context = capture(root, List.of("csm", "context", "--task", request.goal,
                "--path", "."), 300);
        require(contextAccepted(context), "CSM context failed on the new control base");
        String recallLimit = Integer.toString(recallLimit(root));
        String standard = output(root, List.of("csm", "nya", "recall", "--task", request.goal,
                "--path", "smokes/" + request.id, "--path", "tools/smoke",
                "--path", "modules/testkit", "--limit", recallLimit), 300);
        String control = output(root, List.of("csm", "nya", "recall", "--task",
                request.goal + " Required applicable scar " + CONTROL_BASE,
                "--path", "tools/integration", "--path", "coordination/swarm",
                "--limit", recallLimit), 300);
        require(standard.contains(SUPERVISION), "supervision scar absent after migration");
        require(control.contains(CONTROL_BASE), "control-base scar absent after migration");
        String archiveSha = sha(request.archive);
        Files.writeString(preflight, "{\n  \"schema\":3,\n  \"id\":\"" + request.id
                + "\",\n  \"created\":\"" + Instant.now() + "\",\n  \"base\":\""
                + request.newBase + "\",\n  \"head\":\"" + head
                + "\",\n  \"archive_base\":\"" + request.archiveBase
                + "\",\n  \"preflight_base\":\"" + request.preflightBase
                + "\",\n  \"receipt_base\":\"" + request.receiptBase
                + "\",\n  \"session\":\"" + request.session
                + "\",\n  \"prior_attempt\":" + request.priorAttempt
                + ",\n  \"migration_archive_sha256\":\"" + archiveSha
                + "\",\n  \"context_sha256\":\"" + sha(context.stdout)
                + "\",\n  \"recall_sha256\":\"" + sha(standard + control)
                + "\",\n  \"required_scar\":\"" + SUPERVISION
                + "\",\n  \"control_scar\":\"" + CONTROL_BASE
                + "\",\n  \"nested_delegation\":\"forbidden\",\n  \"status\":\"PASS\"\n}\n",
                StandardCharsets.UTF_8);
        System.out.println("Ox Alpha control-base migration PASS: " + request.id);
        System.out.println("  archive base: " + request.archiveBase);
        System.out.println("  preflight base: " + request.preflightBase);
        System.out.println("  receipt base: " + request.receiptBase);
        System.out.println("  new base: " + request.newBase);
        System.out.println("  archive sha256: " + archiveSha);
    }

    private static void validateArchive(Request request) throws Exception {
        require(Files.isRegularFile(request.archive), "migration archive is missing");
        require(sha(request.archive).equalsIgnoreCase(request.archiveSha),
                "migration archive SHA-256 drifted");
        try (ZipFile zip = new ZipFile(request.archive.toFile())) {
            Properties manifest = properties(zip, "manifest.properties");
            require(request.id.equals(manifest.getProperty("id"))
                    && request.archiveBase.equals(manifest.getProperty("base")),
                    "archive identity drifted");
            String state = manifest.getProperty("state", "");
            require(state.equals("DIRTY_SUSPENDED") || state.equals("FAILED_GATE"),
                    "archive is not a retryable legacy state");
            String suffix = "opencode-" + request.id + "-qualify-attempt"
                    + request.priorAttempt + ".json";
            ZipEntry receipt = zip.stream().filter(entry -> entry.getName().endsWith(suffix))
                    .findFirst().orElseThrow(() -> new IllegalStateException(
                            "archive lacks the prior qualify receipt"));
            String text;
            try (InputStream input = zip.getInputStream(receipt)) {
                text = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            require(field(text, "id", request.id) && field(text, "base", request.receiptBase)
                    && field(text, "session", request.session) && field(text, "phase", "qualify")
                    && text.contains("\"attempt\":" + request.priorAttempt)
                    && text.contains("\"completed\":true"), "prior qualify receipt drifted");
        }
    }

    private static Properties properties(ZipFile zip, String name) throws Exception {
        ZipEntry entry = zip.getEntry(name);
        require(entry != null, "archive lacks " + name);
        Properties result = new Properties();
        try (InputStream input = zip.getInputStream(entry)) {
            result.load(input);
        }
        return result;
    }

    private static void selfTest() throws Exception {
        Path archive = Files.createTempFile("worldline-control-migration-", ".zip");
        String id = "m1-contract";
        String base = "a".repeat(40);
        String session = "session-one";
        try {
            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archive))) {
                entry(zip, "manifest.properties", "id=" + id + "\nbase=" + base
                        + "\nstate=DIRTY_SUSPENDED\n");
                entry(zip, "evidence/opencode-" + id + "-qualify-attempt1.json",
                        "{\"id\":\"" + id + "\",\"base\":\"" + base
                                + "\",\"session\":\"" + session
                                + "\",\"phase\":\"qualify\",\"attempt\":1,"
                                + "\"completed\":true}");
            }
            Request valid = new Request(id, "goal", base, base, base, "b".repeat(40),
                    session, 1, archive, sha(archive));
            validateArchive(valid);
            boolean rejected = false;
            try {
                validateArchive(new Request(id, "goal", base, base, base, "b".repeat(40),
                        session, 1, archive, "0".repeat(64)));
            } catch (IllegalStateException expected) {
                rejected = true;
            }
            require(rejected, "archive hash mismatch was accepted");
            Request split = Request.parse(new String[] {"--id", id, "--goal", "goal",
                    "--archive-base", base, "--preflight-base", base, "--receipt-base",
                    "c".repeat(40), "--new-base", "b".repeat(40), "--session", session,
                    "--prior-attempt", "1",
                    "--archive", archive.toString(), "--archive-sha256", sha(archive)});
            require(split.archiveBase.equals(base) && split.preflightBase.equals(base)
                    && split.receiptBase.equals("c".repeat(40)),
                    "split historical bases were not parsed independently");
            String partial = "== nya ==\n" + SUPERVISION;
            require(contextAccepted(new Result(1, partial, OPTIONAL_CONTEXT_ERRORS.get(0))),
                    "verified partial CSM context was rejected");
            require(!contextAccepted(new Result(1, partial, "unknown failure")),
                    "unknown CSM context failure was accepted");
        } finally {
            Files.deleteIfExists(archive);
        }
    }

    private static void entry(ZipOutputStream zip, String name, String value) throws Exception {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(value.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static boolean field(String json, String name, String value) {
        return json.contains("\"" + name + "\":\"" + value + "\"");
    }

    private static boolean ancestor(Path root, String ancestor, String descendant) throws Exception {
        return status(root, List.of("git", "merge-base", "--is-ancestor", ancestor, descendant), 120) == 0;
    }

    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        return output(root, command, 120);
    }

    private static String output(Path root, List<String> command, int seconds) throws Exception {
        Result result = capture(root, command, seconds);
        require(result.exit == 0, "command failed: " + String.join(" ", command));
        return result.stdout;
    }

    private static Result capture(Path root, List<String> command, int seconds) throws Exception {
        Process process = new ProcessBuilder(command).directory(root.toFile()).start();
        CompletableFuture<byte[]> stdout = CompletableFuture.supplyAsync(() -> bytes(process, true));
        CompletableFuture<byte[]> stderr = CompletableFuture.supplyAsync(() -> bytes(process, false));
        require(process.waitFor(seconds, TimeUnit.SECONDS), "command timed out: " + command.get(0));
        return new Result(process.exitValue(), new String(stdout.get(), StandardCharsets.UTF_8),
                new String(stderr.get(), StandardCharsets.UTF_8));
    }

    private static byte[] bytes(Process process, boolean stdout) {
        try {
            return (stdout ? process.getInputStream() : process.getErrorStream()).readAllBytes();
        } catch (Exception error) {
            throw new IllegalStateException("command output could not be read", error);
        }
    }

    private static boolean contextAccepted(Result result) {
        if (result.exit == 0) {
            return true;
        }
        if (result.exit != 1 || !result.stdout.contains("== nya ==")
                || !result.stdout.contains(SUPERVISION)) {
            return false;
        }
        List<String> errors = result.stderr.lines().filter(line -> !line.isBlank()).toList();
        return !errors.isEmpty() && errors.stream().allMatch(OPTIONAL_CONTEXT_ERRORS::contains);
    }

    private static int recallLimit(Path root) throws Exception {
        Path scars = root.resolve(".csm/nya/scars");
        int count = 0;
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(scars, "*.toml")) {
            for (Path ignored : entries) {
                count++;
            }
        }
        require(count > 0, "NYA scar store is empty");
        return count;
    }

    private static int status(Path root, List<String> command, int seconds) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true);
        Process process = builder.start();
        require(process.waitFor(seconds, TimeUnit.SECONDS), "command timed out: " + command.get(0));
        return process.exitValue();
    }

    private static String sha(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }

    private static String sha(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private record Result(int exit, String stdout, String stderr) {
    }

    private record Request(String id, String goal, String archiveBase, String preflightBase,
            String receiptBase, String newBase, String session, int priorAttempt, Path archive,
            String archiveSha) {
        static Request parse(String[] arguments) {
            String id = "";
            String goal = "";
            String archiveBase = "";
            String preflightBase = "";
            String receiptBase = "";
            String newBase = "";
            String session = "";
            String sha = "";
            int attempt = 0;
            Path archive = null;
            for (int index = 0; index < arguments.length; index += 2) {
                require(index + 1 < arguments.length, "missing value for " + arguments[index]);
                switch (arguments[index]) {
                    case "--id" -> id = arguments[index + 1];
                    case "--goal" -> goal = arguments[index + 1];
                    case "--archive-base" -> archiveBase = arguments[index + 1];
                    case "--preflight-base" -> preflightBase = arguments[index + 1];
                    case "--receipt-base" -> receiptBase = arguments[index + 1];
                    case "--new-base" -> newBase = arguments[index + 1];
                    case "--session" -> session = arguments[index + 1];
                    case "--prior-attempt" -> attempt = Integer.parseInt(arguments[index + 1]);
                    case "--archive" -> archive = Path.of(arguments[index + 1]).toAbsolutePath().normalize();
                    case "--archive-sha256" -> sha = arguments[index + 1];
                    default -> throw new IllegalArgumentException("unknown argument: " + arguments[index]);
                }
            }
            require(!goal.isBlank() && !session.isBlank() && attempt > 0 && archive != null,
                    "missing control migration argument");
            require(!archiveBase.isBlank() && !preflightBase.isBlank() && !receiptBase.isBlank(),
                    "missing historical migration base");
            return new Request(id, goal, archiveBase, preflightBase, receiptBase, newBase,
                    session, attempt, archive, sha);
        }
    }
}
