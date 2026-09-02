package worldline.testkit;
import worldline.testapi.BlockLightPlan;
import worldline.testapi.BlockLightScenario;

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

/** Executes and signs caller-owned light rows through a selected runtime provider. */
public final class BlockLightFamilyCycle {
    private BlockLightFamilyCycle() { }

    public static String run(String[] arguments, String family, long expectedSeed,
            String serverProperty, TestRuntimeProvider provider,
            List<BlockLightScenario> rows) throws Exception {
        if (arguments.length != 4) throw new IllegalArgumentException(
                "usage: light family server.jar workspace port seed");
        Path server = Paths.get(arguments[0]).toAbsolutePath().normalize();
        Path workspace = Paths.get(arguments[1]).toAbsolutePath().normalize();
        Integer.parseInt(arguments[2]); long seed = Long.parseLong(arguments[3]);
        if (seed != expectedSeed) throw new IllegalStateException("light seed drift");
        if (!family.matches("[a-z0-9-]+") || rows.isEmpty() || provider == null) {
            throw new IllegalArgumentException("invalid light family");
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
            require(evidence.size() == rows.size(), "light evidence count drift");
            long worlds;
            try (java.util.stream.Stream<Path> paths = Files.list(workspace.resolve("worlds"))) {
                worlds = paths.filter(Files::isDirectory).count();
            }
            require(worlds == rows.size(), "light attempt isolation drift");
            StringBuilder canonical = new StringBuilder();
            for (String value : evidence.values()) canonical.append(value).append("---\n");
            String evidenceHash = sha(canonical.toString());
            String signal = "provider=" + provider.runtimeId() + ",family=" + family + ",rows="
                    + rows.size() + ",passed=" + rows.size() + ",probes=" + probes(rows)
                    + ",reload=FRESH_LOGINx" + rows.size() + ",evidence=" + evidenceHash
                    + ",isolation=" + rows.size() + "-fresh-worlds";
            String trace = "v1|server=official-b1.7.3|seed=" + seed + "|provider="
                    + provider.runtimeId() + "|family=" + family + "|rows=" + ids(rows)
                    + "|actions=sample-air+gameplay-place+fresh-login+sample-light-planes"
                    + "|oracle=canonical-public-light-evidence|evidence=" + evidenceHash;
            String signature = sha(trace);
            System.out.println("WORLDLINE_B173_LIGHT_SET=" + signal);
            System.out.println("WORLDLINE_B173_LIGHT_TRACE=" + trace);
            System.out.println("WORLDLINE_B173_LIGHT_SIGNATURE=" + signature);
            return signature;
        } finally {
            if (prior == null) System.clearProperty(serverProperty);
            else System.setProperty(serverProperty, prior);
        }
    }

    private static Map<String, String> evidence(TestRunResult run) throws Exception {
        Map<String, String> values = new TreeMap<String, String>();
        for (TestResult test : run.tests()) {
            require(test.passed() && test.artifacts().size() == 1,
                    "light result artifact drift: " + test.path());
            Path artifact = test.artifacts().get(0);
            require(artifact.getFileName().toString().equals(BlockLightPlan.EVIDENCE_ARTIFACT),
                    "unexpected light artifact");
            String value = new String(Files.readAllBytes(artifact), StandardCharsets.UTF_8);
            require(value.startsWith("schema=worldline.block-light-evidence.v1\n"),
                    "light evidence schema drift");
            String id = line(value, "scenario=");
            require(values.put(id, value) == null, "duplicate light evidence");
        }
        return values;
    }
    private static String line(String value, String prefix) {
        for (String row : value.split("\\n")) if (row.startsWith(prefix)) {
            return row.substring(prefix.length());
        }
        throw new IllegalStateException("light evidence lacks " + prefix);
    }
    private static int probes(List<BlockLightScenario> rows) {
        int result = 0; for (BlockLightScenario row : rows) result += row.probes().size();
        return result;
    }
    private static String ids(List<BlockLightScenario> rows) {
        StringBuilder value = new StringBuilder();
        for (BlockLightScenario row : rows) {
            if (value.length() > 0) value.append(','); value.append(row.id());
        }
        return value.toString();
    }
    private static String failure(TestRunResult run) {
        if (run.fatalError() != null) return run.fatalError();
        for (TestResult test : run.tests()) if (!test.passed()) {
            return test.path() + ": " + test.errorType() + ": " + test.errorMessage();
        }
        return "light TestKit run failed";
    }
    private static String sha(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255));
        return result.toString();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private static final class FamilySpec extends WorldlineSpec {
        private final String family, runtime; private final List<BlockLightScenario> rows;
        FamilySpec(String family, String runtime, List<BlockLightScenario> rows) {
            this.family = family; this.runtime = runtime; this.rows = rows;
        }
        @Override protected void define() {
            new BlockLightPlan(runtime, rows).register("official " + family + " light transport");
        }
    }
}
