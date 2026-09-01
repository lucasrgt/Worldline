package worldline.testkit;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import worldline.test.TestRuntimeProvider;
import worldline.test.WorldlineSpec;

/** Executes and signs caller-owned bounded random-tick spread rows. */
public final class BlockRandomTickSpreadFamilyCycle {
    private BlockRandomTickSpreadFamilyCycle() { }

    public static void run(String[] arguments, String family, long expectedSeed,
            String serverProperty, TestRuntimeProvider provider,
            List<BlockRandomTickSpreadScenario> rows) throws Exception {
        if (arguments.length != 4) throw new IllegalArgumentException(
                "usage: random-tick spread family server.jar workspace port seed");
        Path server = Paths.get(arguments[0]).toAbsolutePath().normalize();
        Path workspace = Paths.get(arguments[1]).toAbsolutePath().normalize();
        Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        if (seed != expectedSeed || !family.matches("[a-z0-9-]+")
                || provider == null || rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("invalid random-tick spread family");
        }
        String prior = System.getProperty(serverProperty);
        try {
            System.setProperty(serverProperty, server.toString());
            RunnerOptions options = new RunnerOptions().provider(provider).seed(seed)
                    .world(workspace.resolve("worlds")).artifacts(workspace.resolve("results"))
                    .snapshots(workspace.resolve("snapshots"))
                    .runtimeLock(workspace.resolve("runtime.lock")).timeout(3_600_000L);
            TestRunResult run = new TestRunner().run(new FamilySpec(
                    family, provider.runtimeId(), rows), options, null);
            require(run.passed() && run.tests().size() == rows.size(), failure(run));
            Map<String, String> evidence = evidence(run); verify(rows, evidence);
            long worlds;
            try (java.util.stream.Stream<Path> paths = Files.list(workspace.resolve("worlds"))) {
                worlds = paths.filter(Files::isDirectory).count();
            }
            require(worlds == rows.size(), "random-tick spread isolation drift");
            StringBuilder canonical = new StringBuilder();
            for (String value : evidence.values()) canonical.append(value).append("---\n");
            String hash = sha(canonical.toString());
            String signal = "provider=" + provider.runtimeId() + ",family=" + family
                    + ",rows=" + rows.size() + ",passed=" + rows.size() + ",claims="
                    + (rows.size() * 5) + ",bounded-windows=" + bounds(rows)
                    + ",winning-window=excluded,control=invalid-support-air,reload=FRESH_LOGINx"
                    + rows.size()
                    + ",evidence=" + hash + ",isolation=" + rows.size() + "-fresh-worlds";
            String trace = "v1|server=official-b1.7.3|seed=" + seed + "|provider="
                    + provider.runtimeId() + "|family=" + family + "|rows=" + ids(rows)
                    + "|actions=place+physical-probes+bounded-random-ticks+remove-support+reload"
                    + "|oracle=canonical-public-block-random-tick-spread-evidence|evidence=" + hash;
            System.out.println("WORLDLINE_B173_RANDOM_TICK_SPREAD_SET=" + signal);
            System.out.println("WORLDLINE_B173_RANDOM_TICK_SPREAD_TRACE=" + trace);
            System.out.println("WORLDLINE_B173_RANDOM_TICK_SPREAD_SIGNATURE=" + sha(trace));
        } finally {
            if (prior == null) System.clearProperty(serverProperty);
            else System.setProperty(serverProperty, prior);
        }
    }

    private static Map<String, String> evidence(TestRunResult run) throws Exception {
        Map<String, String> values = new TreeMap<String, String>();
        for (TestResult test : run.tests()) {
            require(test.passed() && test.artifacts().size() == 1,
                    "random-tick spread artifact drift: " + test.path());
            Path artifact = test.artifacts().get(0);
            require(artifact.getFileName().toString().equals(
                    BlockRandomTickSpreadPlan.EVIDENCE_ARTIFACT), "unexpected spread artifact");
            String value = Files.readString(artifact, StandardCharsets.UTF_8);
            require(value.startsWith("schema=worldline.block-random-tick-spread-evidence.v1\n"),
                    "random-tick spread evidence schema drift");
            String id = line(value, "scenario=");
            require(values.put(id, value) == null, "duplicate random-tick spread evidence");
        }
        return values;
    }
    private static void verify(List<BlockRandomTickSpreadScenario> rows,
            Map<String, String> evidence) {
        require(rows.size() == evidence.size(), "random-tick spread evidence count drift");
        for (BlockRandomTickSpreadScenario row : rows) {
            String value = evidence.get(row.id());
            require(value != null && value.contains("subject=" + row.subject() + "\n")
                    && value.contains("winning-window=excluded\n")
                    && value.contains("control=invalid-support-air\n")
                    && value.endsWith("reload=FRESH_LOGIN\n"),
                    "random-tick spread canonical evidence drift: " + row.id());
        }
    }
    private static String bounds(List<BlockRandomTickSpreadScenario> rows) {
        String value = rows.get(0).maxWindows() + "x" + rows.get(0).windowTicks();
        for (BlockRandomTickSpreadScenario row : rows) require(value.equals(
                row.maxWindows() + "x" + row.windowTicks()), "spread bounds differ");
        return value;
    }
    private static String ids(List<BlockRandomTickSpreadScenario> rows) {
        StringBuilder value = new StringBuilder();
        for (BlockRandomTickSpreadScenario row : rows) {
            if (value.length() > 0) value.append(',');
            value.append(row.id());
        }
        return value.toString();
    }
    private static String line(String text, String prefix) {
        for (String row : text.split("\\n")) if (row.startsWith(prefix)) {
            return row.substring(prefix.length());
        }
        throw new IllegalStateException("evidence lacks " + prefix);
    }
    private static String failure(TestRunResult run) {
        if (run.fatalError() != null) return run.fatalError();
        for (TestResult test : run.tests()) if (!test.passed()) {
            return test.path() + ": " + test.errorType() + ": " + test.errorMessage();
        }
        return "random-tick spread TestKit run failed";
    }
    private static String sha(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255));
        return result.toString();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private static final class FamilySpec extends WorldlineSpec {
        private final String family, runtime; private final List<BlockRandomTickSpreadScenario> rows;
        FamilySpec(String family, String runtime, List<BlockRandomTickSpreadScenario> rows) {
            this.family = family; this.runtime = runtime; this.rows = rows;
        }
        @Override protected void define() {
            new BlockRandomTickSpreadPlan(runtime, rows).register(
                    "official " + family + " random-tick spread");
        }
    }
}
