package worldline.testkit;
import worldline.testapi.BlockCollisionPlan;
import worldline.testapi.BlockCollisionScenario;

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

/** Executes and signs caller-owned collision rows through a selected runtime provider. */
public final class BlockCollisionFamilyCycle {
    private BlockCollisionFamilyCycle() { }

    public static void run(String[] arguments, String family, long expectedSeed,
            String serverProperty, TestRuntimeProvider provider,
            List<BlockCollisionScenario> rows) throws Exception {
        if (arguments.length != 4) throw new IllegalArgumentException(
                "usage: collision family server.jar workspace port seed");
        Path server = Paths.get(arguments[0]).toAbsolutePath().normalize();
        Path workspace = Paths.get(arguments[1]).toAbsolutePath().normalize();
        Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        if (seed != expectedSeed) throw new IllegalStateException("collision seed drift");
        if (!family.matches("[a-z0-9-]+") || rows.isEmpty() || provider == null) {
            throw new IllegalArgumentException("invalid collision family");
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
            require(evidence.size() == rows.size(), "collision evidence count drift");
            long worlds;
            try (java.util.stream.Stream<Path> paths = Files.list(workspace.resolve("worlds"))) {
                worlds = paths.filter(Files::isDirectory).count();
            }
            require(worlds == rows.size(), "collision attempt isolation drift");
            StringBuilder canonical = new StringBuilder();
            for (String value : evidence.values()) canonical.append(value).append("---\n");
            String evidenceHash = sha(canonical.toString());
            String signal = "provider=" + provider.runtimeId() + ",family=" + family + ",rows="
                    + rows.size() + ",passed=" + rows.size() + ",probes=" + probes(rows)
                    + ",reload=FRESH_LOGINx" + rows.size() + ",evidence=" + evidenceHash
                    + ",isolation=" + rows.size() + "-fresh-worlds";
            String trace = "v1|server=official-b1.7.3|seed=" + seed + "|provider="
                    + provider.runtimeId() + "|family=" + family + "|rows=" + ids(rows)
                    + "|actions=air-control+gameplay-place+trajectory-probes+fresh-login"
                    + "|oracle=canonical-public-collision-evidence|evidence=" + evidenceHash;
            System.out.println("WORLDLINE_B173_COLLISION_SET=" + signal);
            System.out.println("WORLDLINE_B173_COLLISION_TRACE=" + trace);
            System.out.println("WORLDLINE_B173_COLLISION_SIGNATURE=" + sha(trace));
        } finally {
            if (prior == null) System.clearProperty(serverProperty);
            else System.setProperty(serverProperty, prior);
        }
    }

    private static Map<String, String> evidence(TestRunResult run) throws Exception {
        Map<String, String> values = new TreeMap<String, String>();
        for (TestResult test : run.tests()) {
            require(test.passed() && test.artifacts().size() == 1,
                    "collision result artifact drift: " + test.path());
            Path artifact = test.artifacts().get(0);
            require(artifact.getFileName().toString().equals(BlockCollisionPlan.EVIDENCE_ARTIFACT),
                    "unexpected collision artifact");
            String value = new String(Files.readAllBytes(artifact), StandardCharsets.UTF_8);
            require(value.startsWith("schema=worldline.block-collision-evidence.v1\n"),
                    "collision evidence schema drift");
            String id = line(value, "scenario=");
            require(values.put(id, value) == null, "duplicate collision evidence");
        }
        return values;
    }

    private static String line(String text, String prefix) {
        for (String row : text.split("\\n")) if (row.startsWith(prefix)) {
            return row.substring(prefix.length());
        }
        throw new IllegalStateException("collision evidence lacks " + prefix);
    }

    private static int probes(List<BlockCollisionScenario> rows) {
        int result = 0; for (BlockCollisionScenario row : rows) result += row.probes().size();
        return result;
    }

    private static String ids(List<BlockCollisionScenario> rows) {
        StringBuilder value = new StringBuilder();
        for (BlockCollisionScenario row : rows) {
            if (value.length() > 0) value.append(','); value.append(row.id());
        }
        return value.toString();
    }

    private static String failure(TestRunResult run) {
        if (run.fatalError() != null) return run.fatalError();
        for (TestResult test : run.tests()) if (!test.passed()) {
            return test.path() + ": " + test.errorType() + ": " + test.errorMessage();
        }
        return "collision TestKit run failed";
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
        private final String family, runtime; private final List<BlockCollisionScenario> rows;
        FamilySpec(String family, String runtime, List<BlockCollisionScenario> rows) {
            this.family = family; this.runtime = runtime; this.rows = rows;
        }
        @Override protected void define() {
            new BlockCollisionPlan(runtime, rows).register("official " + family + " collision");
        }
    }
}
