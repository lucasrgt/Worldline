import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Skips smoke fingerprint work when the last suite executed 0 smokes on this clean tree. */
final class SmokeFingerprintWork {
    private SmokeFingerprintWork() { }

    static boolean skipWhenUnchanged(Path root, int catalogSize) throws Exception {
        if ("off".equalsIgnoreCase(System.getenv().getOrDefault("WORLDLINE_SMOKE_CACHE", "on")))
            return false;
        Path suite = root.resolve(".worldline/reports/smoke-suite.json");
        if (!Files.isRegularFile(suite) || catalogSize < 1) return false;
        Map<String, Object> document = MiniJson.object(
                Files.readString(suite, StandardCharsets.UTF_8));
        SmokeGitState state = SmokeGitState.read(root);
        return state.clean()
                && "passed".equals(MiniJson.string(document, "status"))
                && state.tree().equals(MiniJson.string(document, "tree"))
                && catalogSize == MiniJson.integer(document, "count")
                && MiniJson.integer(document, "executed") == 0;
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-smoke-skip-");
        PrintStream original = System.out;
        try {
            fixture(root);
            List<SmokeDiscovery.Entry> smokes = SmokeDiscovery.discover(root);
            require(smokes.size() == 1, "skip fixture catalog drifted");
            require(skipWhenUnchanged(root, smokes.size()),
                    "clean 0-execution receipt did not skip");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            long skipStarted = System.nanoTime();
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            try { SmokeSuite.run(false, root); }
            finally { System.setOut(original); }
            long skipNanos = System.nanoTime() - skipStarted;
            String output = buffer.toString(StandardCharsets.UTF_8);
            require(output.contains("smoke fingerprints skipped: 0 executions"),
                    "SmokeSuite did not take the 0-execution skip path: " + output);
            require(Files.readString(root.resolve(".worldline/reports/smoke-suite.json"),
                    StandardCharsets.UTF_8).contains("\"executed\": 0"),
                    "skip rewrote the suite receipt");
            require(!Files.isDirectory(root.resolve(".worldline/reports/smokes")),
                    "skip still wrote smoke attestations");
            Path live = Path.of("").toAbsolutePath().normalize();
            List<SmokeDiscovery.Entry> liveSmokes = SmokeDiscovery.discover(live);
            long forceStarted = System.nanoTime();
            String fingerprint = new SmokeInputFingerprint(live).compute(liveSmokes.get(0));
            long forceNanos = System.nanoTime() - forceStarted;
            require(fingerprint != null && !fingerprint.isBlank(),
                    "forced fingerprint recompute skipped work");
            require(skipNanos < forceNanos || skipNanos < 250_000_000L,
                    "zero-execution fingerprint skip was not cheaper than a recompute");
            write(root.resolve(".worldline/reports/smoke-suite.json"),
                    receipt(root, 1, 1));
            require(!skipWhenUnchanged(root, smokes.size()),
                    "prior executions still skipped fingerprint work");
            write(root.resolve(".worldline/reports/smoke-suite.json"),
                    receipt(root, 1, 0));
            write(root.resolve("dirty.txt"), "uncommitted\n");
            require(!skipWhenUnchanged(root, smokes.size()),
                    "dirty worktree skipped fingerprint work");
            System.out.println("  smoke fingerprint skip self-test: passed");
        } finally {
            System.setOut(original);
            try { SafeTreeDelete.delete(root); } catch (Exception ignored) { }
        }
    }

    private static void fixture(Path root) throws Exception {
        git(root, "init", "--quiet");
        git(root, "config", "user.email", "worldline@example.invalid");
        git(root, "config", "user.name", "Worldline Test");
        git(root, "config", "commit.gpgsign", "false");
        write(root.resolve(".gitignore"), ".worldline/\n");
        write(root.resolve("harness.properties"), "java.release=8\nmodule.api.dependencies=\n");
        write(root.resolve("tools/harness/SmokeProcess.java"), "final class SmokeProcess {}\n");
        write(root.resolve("modules/api/src/main/java/example/Api.java"),
                "package example; public final class Api {}\n");
        write(root.resolve("smokes/m1-one/smoke.properties"),
                "id=m1-one\nrunner.source=tools/smoke/OneCycle.java\n");
        write(root.resolve("smokes/m1-one/input.txt"), "one\n");
        write(root.resolve("tools/smoke/OneCycle.java"),
                "final class OneCycle { Object product(String name) { return name; }\n"
                        + "Object input() { return product(\"api\"); } }\n");
        git(root, "add", ".");
        git(root, "commit", "--quiet", "-m", "fixture");
        write(root.resolve(".worldline/reports/smoke-suite.json"), receipt(root, 1, 0));
    }

    private static String receipt(Path root, int count, int executed) throws Exception {
        SmokeGitState state = SmokeGitState.read(root);
        return "{\n  \"schema\": 1,\n  \"status\": \"passed\",\n  \"tree\": \""
                + state.tree() + "\",\n  \"count\": " + count
                + ",\n  \"executed\": " + executed + "\n}\n";
    }

    private static void write(Path path, String value) throws Exception {
        Files.createDirectories(path.getParent());
        Files.writeString(path, value, StandardCharsets.UTF_8);
    }

    private static void git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        require(process.waitFor(60, TimeUnit.SECONDS) && process.exitValue() == 0,
                "git failed: " + String.join(" ", arguments));
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
