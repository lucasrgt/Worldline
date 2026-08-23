import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Exercises selective invalidation, persisted reuse, and current-tree suite binding. */
public final class SmokeReceiptCacheTest {
    private SmokeReceiptCacheTest() {}

    public static void main(String[] arguments) {
        Path repository = null;
        try {
            repository = Files.createTempDirectory("worldline-smoke-cache-test-");
            execute(repository); SmokeFingerprintCheck.execute(Path.of("").toAbsolutePath().normalize());
            System.out.println("  smoke receipt cache self-test: passed");
        } catch (Exception error) {
            System.err.println("smoke receipt cache self-test failed: " + error.getMessage());
            System.exit(1);
        } finally { if (repository != null) try { delete(repository); } catch (Exception ignored) {} }
    }

    private static void execute(Path root) throws Exception {
        git(root, "init", "--quiet"); git(root, "config", "user.email", "worldline@example.invalid");
        git(root, "config", "user.name", "Worldline Test");
        write(root.resolve(".gitignore"), ".worldline/\n");
        write(root.resolve("harness.properties"), "java.release=8\nmodule.api.dependencies=\n");
        write(root.resolve("tools/harness/SmokeProcess.java"), "final class SmokeProcess {}\n");
        write(root.resolve("modules/api/src/main/java/example/Api.java"),
                "package example; public final class Api {}\n");
        smoke(root, "m1-one", "OneCycle"); smoke(root, "m2-two", "TwoCycle");
        git(root, "add", "."); git(root, "commit", "--quiet", "-m", "fixture");
        List<SmokeDiscovery.Entry> entries = SmokeDiscovery.discover(root);
        SmokeInputFingerprint first = new SmokeInputFingerprint(root);
        String one = first.compute(entries.get(0)), two = first.compute(entries.get(1));
        write(root.resolve("smokes/m1-one/input.txt"), "one\r\n");
        require(one.equals(new SmokeInputFingerprint(root).compute(entries.get(0))),
                "CRLF checkout invalidated a text fingerprint");
        write(root.resolve("smokes/m1-one/input.txt"), "changed\n");
        SmokeInputFingerprint changed = new SmokeInputFingerprint(root);
        require(!one.equals(changed.compute(entries.get(0))), "changed smoke retained its fingerprint");
        require(two.equals(changed.compute(entries.get(1))), "unrelated smoke was invalidated");
        write(root.resolve("smokes/m1-one/input.txt"), "one\n");
        Path cacheRoot = root.resolve(".worldline/test-cache");
        SmokeReceiptCache writer = new SmokeReceiptCache(root, cacheRoot, true);
        for (SmokeDiscovery.Entry entry : entries) {
            Path log = root.resolve(".worldline/smoke-logs").resolve(entry.id + ".log");
            write(log, entry.id + " PASS\n"); writer.passed(entry, writer.fingerprint(entry), 1L);
        }
        writer.finish(entries.size());
        SmokeReceiptCache reader = new SmokeReceiptCache(root, cacheRoot, true);
        for (SmokeDiscovery.Entry entry : entries)
            require(reader.restore(entry, reader.fingerprint(entry)), "PASS proof was not reused: " + entry.id);
        reader.finish(entries.size());
        String suite = Files.readString(root.resolve(".worldline/reports/smoke-suite.json"));
        require(suite.contains("\"executed\": 0") && suite.contains("\"reused\": 2"),
                "suite did not aggregate reused proofs");
        List<SmokePins.Entry> pins = new ArrayList<>();
        for (SmokeDiscovery.Entry entry : entries) pins.add(reader.availablePin(entry));
        new SmokePins(root).write(pins); delete(cacheRoot);
        SmokeReceiptCache clone = new SmokeReceiptCache(root, cacheRoot, true);
        for (SmokeDiscovery.Entry entry : entries)
            require(clone.restore(entry, clone.fingerprint(entry)), "tracked pin was not reused: " + entry.id);
        clone.finish(entries.size());
        suite = Files.readString(root.resolve(".worldline/reports/smoke-suite.json"));
        require(suite.contains("\"pinned\": 2"), "suite did not aggregate tracked pins");
    }

    private static void smoke(Path root, String id, String runner) throws Exception {
        write(root.resolve("smokes").resolve(id).resolve("smoke.properties"),
                "id=" + id + "\nrunner.source=tools/smoke/" + runner + ".java\n");
        write(root.resolve("smokes").resolve(id).resolve("input.txt"), id.startsWith("m1") ? "one\n" : "two\n");
        write(root.resolve("tools/smoke").resolve(runner + ".java"), "final class " + runner
                + " { Object input() { return product(\"api\"); } Object product(String name) { return name; } }\n");
    }

    private static void write(Path path, String value) throws Exception {
        Files.createDirectories(path.getParent()); Files.writeString(path, value, StandardCharsets.UTF_8);
    }

    private static void git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        require(process.waitFor(60, TimeUnit.SECONDS) && process.exitValue() == 0,
                "git failed: " + String.join(" ", arguments));
    }

    private static void delete(Path target) throws Exception {
        if (!Files.exists(target)) return;
        try (Stream<Path> paths = Files.walk(target)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                path.toFile().setWritable(true); Files.deleteIfExists(path);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
