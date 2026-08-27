package worldline.b173server;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import worldline.test.WorldlineSpec;
import worldline.testkit.BlockLifecyclePlan;
import worldline.testkit.BlockLifecycleScenario;
import worldline.testkit.RunnerOptions;
import worldline.testkit.TestResult;
import worldline.testkit.TestRunResult;
import worldline.testkit.TestRunner;

/** Executes and signs one caller-owned lifecycle family against isolated official servers. */
public final class B173LifecycleFamilyCycle {
    private B173LifecycleFamilyCycle() { }

    public static void run(String[] arguments, String family,
            List<BlockLifecycleScenario> rows) throws Exception {
        if (arguments.length != 4) throw new IllegalArgumentException(
                "usage: lifecycle family server.jar workspace port seed");
        Path server = Paths.get(arguments[0]).toAbsolutePath().normalize();
        Path workspace = Paths.get(arguments[1]).toAbsolutePath().normalize();
        Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        if (seed != B173LifecycleArena.SEED) throw new IllegalStateException("lifecycle seed drift");
        if (!family.matches("[a-z0-9-]+") || rows.isEmpty())
            throw new IllegalArgumentException("invalid lifecycle family");
        String property = B173ServerLifecycleSettings.SERVER_PROPERTY;
        String prior = System.getProperty(property);
        try {
            System.setProperty(property, server.toString());
            RunnerOptions options = new RunnerOptions()
                    .provider(new B173ServerLifecycleTestRuntimeProvider()).seed(seed)
                    .world(workspace.resolve("worlds")).artifacts(workspace.resolve("results"))
                    .snapshots(workspace.resolve("snapshots"))
                    .runtimeLock(workspace.resolve("runtime.lock")).timeout(300_000L);
            TestRunResult run = new TestRunner().run(new FamilySpec(family, rows), options, null);
            require(run.passed() && run.tests().size() == rows.size(), failure(run));
            Map<String, String> evidence = evidence(run);
            B173LifecycleFamilyEvidence.verify(rows, evidence);
            long worlds;
            try (java.util.stream.Stream<Path> paths = Files.list(workspace.resolve("worlds"))) {
                worlds = paths.filter(Files::isDirectory).count();
            }
            require(worlds == rows.size(), "provider attempt isolation drift");
            StringBuilder canonical = new StringBuilder();
            for (String value : evidence.values()) canonical.append(value).append("---\n");
            String evidenceHash = sha(canonical.toString());
            String signal = "provider=b1.7.3-server-lifecycle,family=" + family + ",rows="
                    + rows.size() + ",passed=" + rows.size() + ",layers="
                    + B173LifecycleFamilyEvidence.layers(rows) + ",reload=FRESH_LOGINx"
                    + (rows.size() * 2) + ",evidence=" + evidenceHash + ",isolation="
                    + rows.size() + "-fresh-worlds";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|provider=b1.7.3-server-lifecycle|family=" + family + "|rows="
                    + B173LifecycleFamilyEvidence.rowIds(rows)
                    + "|actions=place+fresh-login+break+fresh-login"
                    + "|oracle=canonical-public-testkit-evidence|evidence=" + evidenceHash;
            System.out.println("WORLDLINE_B173_LIFECYCLE_FAMILY_SET=" + signal);
            System.out.println("WORLDLINE_B173_LIFECYCLE_FAMILY_TRACE=" + trace);
            System.out.println("WORLDLINE_B173_LIFECYCLE_FAMILY_SIGNATURE=" + sha(trace));
        } finally {
            if (prior == null) System.clearProperty(property); else System.setProperty(property, prior);
        }
    }

    private static Map<String, String> evidence(TestRunResult run) throws Exception {
        Map<String, String> values = new TreeMap<String, String>();
        for (TestResult test : run.tests()) {
            require(test.passed() && test.artifacts().size() == 1,
                    "lifecycle result artifact drift: " + test.path());
            Path artifact = test.artifacts().get(0);
            require(artifact.getFileName().toString().equals(BlockLifecyclePlan.EVIDENCE_ARTIFACT),
                    "unexpected lifecycle artifact");
            String text = new String(Files.readAllBytes(artifact), StandardCharsets.UTF_8);
            String id = line(text, "scenario=");
            require(values.put(id, text) == null, "duplicate lifecycle evidence");
        }
        return values;
    }

    private static String line(String text, String prefix) {
        for (String row : text.split("\\n")) if (row.startsWith(prefix))
            return row.substring(prefix.length());
        throw new IllegalStateException("lifecycle evidence lacks " + prefix);
    }

    private static String failure(TestRunResult run) {
        if (run.fatalError() != null) return run.fatalError();
        for (TestResult test : run.tests()) if (!test.passed())
            return test.path() + ": " + test.errorType() + ": " + test.errorMessage();
        return "lifecycle TestKit run failed";
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
        private final String family; private final List<BlockLifecycleScenario> rows;
        FamilySpec(String family, List<BlockLifecycleScenario> rows) {
            this.family = family; this.rows = rows;
        }
        @Override protected void define() {
            new BlockLifecyclePlan(B173ServerLifecycleTestRuntimeProvider.RUNTIME_ID, rows)
                    .register("official " + family + " lifecycle");
        }
    }
}
