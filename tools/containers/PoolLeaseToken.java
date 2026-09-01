import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Creates a commit-bound capability consumed by canonical pooled Gate workers. */
public final class PoolLeaseToken {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();
    private PoolLeaseToken() {}

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            require((arguments.length == 5 || arguments.length == 8)
                            && arguments[0].equals("create"),
                    "usage: PoolLeaseToken create FILE SECRET PARENT_PID LOCK"
                            + " [executor-differential LEASE_PID [LEASE_STARTED]]");
            String purpose = arguments.length == 8 ? arguments[5] : "smoke-pool";
            String lease = arguments.length == 8 ? arguments[6] : null;
            String leaseStarted = arguments.length == 8 ? arguments[7] : null;
            create(Path.of(arguments[1]), arguments[2], arguments[3], Path.of(arguments[4]),
                    true, purpose, lease, leaseStarted);
        } catch (Exception error) {
            System.err.println("pool lease token failed: " + error.getMessage()); System.exit(1);
        }
    }

    private static void create(Path requested, String secret, String parent, Path lock,
            boolean requireClean, String purpose, String leasePid, String leaseStarted) throws Exception {
        Path directory = ROOT.resolve(".worldline/runtime-fabric").normalize();
        Path file = requested.toAbsolutePath().normalize();
        require(file.startsWith(directory) && !file.equals(directory), "token escaped runtime fabric");
        require(secret.matches("[0-9a-f-]{36}") && parent.matches("[0-9]+"), "invalid token identity");
        ProcessHandle owner = ProcessHandle.of(Long.parseLong(parent)).orElseThrow();
        require(owner.isAlive(), "pool owner is not alive");
        require(List.of("smoke-pool", "executor-differential").contains(purpose),
                "invalid pool purpose");
        require((purpose.equals("executor-differential") && leasePid != null
                        && leasePid.matches("[0-9]+"))
                        || (purpose.equals("smoke-pool") && leasePid == null),
                "invalid differential lease identity");
        if (purpose.equals("executor-differential")) {
            require(leaseStarted != null, "differential lease start identity is absent");
            require(leaseStarted.matches("[0-9]+"), "invalid differential lease start identity");
            ProcessHandle lease = ProcessHandle.of(Long.parseLong(leasePid)).orElseThrow();
            require(lease.isAlive() && lease.info().startInstant().orElseThrow().toEpochMilli()
                    == Long.parseLong(leaseStarted), "differential lease PID/start identity mismatch");
        }
        require(!requireClean || capture("status", "--porcelain", "--untracked-files=all").isBlank(),
                "pool token requires a clean worktree");
        Properties values = new Properties(); values.setProperty("schema", "1");
        values.setProperty("secret", secret); values.setProperty("parent.pid", parent);
        values.setProperty("root", ROOT.toString()); values.setProperty("head", capture("rev-parse", "HEAD"));
        values.setProperty("tree", capture("rev-parse", "HEAD^{tree}"));
        values.setProperty("lock.path", lock.toAbsolutePath().normalize().toString());
        values.setProperty("purpose", purpose);
        if (leasePid != null) values.setProperty("lease.pid", leasePid);
        if (leaseStarted != null) values.setProperty("lease.started", leaseStarted);
        values.setProperty("created", Instant.now().toString()); Files.createDirectories(directory);
        try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            values.store(writer, "Worldline official-runtime pool capability");
        }
    }

    private static String capture(String... arguments) throws Exception {
        java.util.ArrayList<String> command = new java.util.ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments)); Path log = Files.createTempFile("worldline-pool-git-", ".log");
        Process process = new ProcessBuilder(command).directory(ROOT.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            require(process.waitFor(60, TimeUnit.SECONDS), "git timed out");
            String output = Files.readString(log, StandardCharsets.UTF_8).trim();
            require(process.exitValue() == 0, "git failed: " + output); return output;
        } finally { Files.deleteIfExists(log); }
    }

    private static void selfTest() throws Exception {
        Path file = ROOT.resolve(".worldline/runtime-fabric/token-self-test.properties");
        create(file, "00000000-0000-0000-0000-000000000000",
                Long.toString(ProcessHandle.current().pid()), file.resolveSibling("lock"),
                false, "smoke-pool", null, null);
        Properties values = new Properties(); try (var reader = Files.newBufferedReader(file)) {
            values.load(reader);
        }
        require("1".equals(values.getProperty("schema"))
                && "smoke-pool".equals(values.getProperty("purpose"))
                && values.getProperty("head").matches("[0-9a-f]{40,64}"), "token self-test drift");
        Files.delete(file); System.out.println("pool lease token self-test passed");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
