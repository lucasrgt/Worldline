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

/** Executes and signs caller-owned bounded stability rows through one runtime provider. */
public final class BlockStabilityFamilyCycle {
    private BlockStabilityFamilyCycle() { }

    public static void run(String[] arguments, String family, long expectedSeed,
            String serverProperty, TestRuntimeProvider provider,
            List<BlockStabilityScenario> rows) throws Exception {
        if (arguments.length != 4) throw new IllegalArgumentException(
                "usage: stability family server.jar workspace port seed");
        Path server = Paths.get(arguments[0]).toAbsolutePath().normalize();
        Path workspace = Paths.get(arguments[1]).toAbsolutePath().normalize();
        Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        if (seed != expectedSeed) throw new IllegalStateException("stability seed drift");
        if (!family.matches("[a-z0-9-]+") || rows.isEmpty() || provider == null) {
            throw new IllegalArgumentException("invalid stability family");
        }
        String prior = System.getProperty(serverProperty);
        try {
            System.setProperty(serverProperty, server.toString());
            RunnerOptions options = new RunnerOptions().provider(provider).seed(seed)
                    .world(workspace.resolve("worlds")).artifacts(workspace.resolve("results"))
                    .snapshots(workspace.resolve("snapshots"))
                    .runtimeLock(workspace.resolve("runtime.lock")).timeout(300_000L);
            TestRunResult run = new TestRunner().run(new FamilySpec(
                    family, provider.runtimeId(), rows), options, null);
            require(run.passed() && run.tests().size() == rows.size(), failure(run));
            Map<String, String> evidence = evidence(run);
            verify(rows, evidence);
            long worlds;
            try (java.util.stream.Stream<Path> paths = Files.list(workspace.resolve("worlds"))) {
                worlds = paths.filter(Files::isDirectory).count();
            }
            require(worlds == rows.size(), "stability attempt isolation drift");
            StringBuilder canonical = new StringBuilder();
            for (String value : evidence.values()) canonical.append(value).append("---\n");
            String evidenceHash = sha(canonical.toString());
            String signal = "provider=" + provider.runtimeId() + ",family=" + family
                    + ",rows=" + rows.size() + ",passed=" + rows.size() + ",claims="
                    + (rows.size() * 2) + ",tick-window=" + tickWindow(rows)
                    + ",neighbor=stone-overhead-remove,reload=FRESH_LOGINx" + rows.size()
                    + ",evidence=" + evidenceHash + ",isolation=" + rows.size()
                    + "-fresh-worlds";
            String trace = "v1|server=official-b1.7.3|seed=" + seed + "|provider="
                    + provider.runtimeId() + "|family=" + family + "|rows=" + ids(rows)
                    + "|actions=place+tick-window" + tickWindow(rows)
                    + "+remove-stone-overhead+fresh-login"
                    + "|oracle=canonical-public-block-stability-evidence|evidence="
                    + evidenceHash;
            System.out.println("WORLDLINE_B173_STABILITY_SET=" + signal);
            System.out.println("WORLDLINE_B173_STABILITY_TRACE=" + trace);
            System.out.println("WORLDLINE_B173_STABILITY_SIGNATURE=" + sha(trace));
        } finally {
            if (prior == null) System.clearProperty(serverProperty);
            else System.setProperty(serverProperty, prior);
        }
    }

    private static Map<String, String> evidence(TestRunResult run) throws Exception {
        Map<String, String> values = new TreeMap<String, String>();
        for (TestResult test : run.tests()) {
            require(test.passed() && test.artifacts().size() == 1,
                    "stability result artifact drift: " + test.path());
            Path artifact = test.artifacts().get(0);
            require(artifact.getFileName().toString().equals(
                    BlockStabilityPlan.EVIDENCE_ARTIFACT), "unexpected stability artifact");
            String value = new String(Files.readAllBytes(artifact), StandardCharsets.UTF_8);
            require(value.startsWith("schema=worldline.block-stability-evidence.v1\n"),
                    "stability evidence schema drift");
            String id = line(value, "scenario=");
            require(values.put(id, value) == null, "duplicate stability evidence");
        }
        return values;
    }

    private static void verify(List<BlockStabilityScenario> rows, Map<String, String> evidence) {
        require(evidence.size() == rows.size(), "stability evidence count drift");
        for (BlockStabilityScenario row : rows) {
            String value = evidence.get(row.id());
            require(value != null && value.contains("subject=" + row.subject() + "\n")
                    && value.contains("claim.tick-policy=" + row.tickPolicy().claimId() + "|")
                    && value.contains("claim.neighbor-response="
                            + row.neighborResponse().claimId() + "|")
                    && value.contains("tick-window=" + row.tickWindow() + "\n")
                    && value.contains("neighbor=" ) && value.endsWith("reload=FRESH_LOGIN\n"),
                    "stability canonical evidence drift: " + row.id());
        }
    }

    private static int tickWindow(List<BlockStabilityScenario> rows) {
        int value = rows.get(0).tickWindow();
        for (BlockStabilityScenario row : rows) require(row.tickWindow() == value,
                "stability tick-window family drift");
        return value;
    }

    private static String ids(List<BlockStabilityScenario> rows) {
        StringBuilder value = new StringBuilder();
        for (BlockStabilityScenario row : rows) {
            if (value.length() > 0) value.append(','); value.append(row.id());
        }
        return value.toString();
    }

    private static String line(String text, String prefix) {
        for (String row : text.split("\\n")) if (row.startsWith(prefix)) {
            return row.substring(prefix.length());
        }
        throw new IllegalStateException("stability evidence lacks " + prefix);
    }

    private static String failure(TestRunResult run) {
        if (run.fatalError() != null) return run.fatalError();
        for (TestResult test : run.tests()) if (!test.passed()) {
            return test.path() + ": " + test.errorType() + ": " + test.errorMessage();
        }
        return "stability TestKit run failed";
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
        private final String family, runtime; private final List<BlockStabilityScenario> rows;
        FamilySpec(String family, String runtime, List<BlockStabilityScenario> rows) {
            this.family = family; this.runtime = runtime; this.rows = rows;
        }
        @Override protected void define() {
            new BlockStabilityPlan(runtime, rows).register("official " + family + " stability");
        }
    }
}
