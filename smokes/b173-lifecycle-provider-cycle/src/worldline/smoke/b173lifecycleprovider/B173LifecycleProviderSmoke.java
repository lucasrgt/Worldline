package worldline.smoke.b173lifecycleprovider;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;
import worldline.b173server.B173ServerLifecycleFixtures;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.test.WorldlineSpec;
import worldline.testkit.BlockLifecyclePlan;
import worldline.testkit.RunnerOptions;
import worldline.testkit.TestResult;
import worldline.testkit.TestRunResult;
import worldline.testkit.TestRunner;

/** Executes all currently provisioned public lifecycle rows against isolated official servers. */
public final class B173LifecycleProviderSmoke {
    private B173LifecycleProviderSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 4) throw new IllegalArgumentException(
                "usage: B173LifecycleProviderSmoke server.jar workspace port seed");
        Path server = Paths.get(arguments[0]).toAbsolutePath().normalize();
        Path workspace = Paths.get(arguments[1]).toAbsolutePath().normalize();
        Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        require(seed == B173ServerLifecycleFixtures.SEED, "lifecycle seed drift");
        String property = "worldline.b173.lifecycle.serverJar";
        String prior = System.getProperty(property);
        try {
            System.setProperty(property, server.toString());
            RunnerOptions options = new RunnerOptions()
                    .provider(new B173ServerLifecycleTestRuntimeProvider())
                    .seed(seed).world(workspace.resolve("worlds"))
                    .artifacts(workspace.resolve("results"))
                    .snapshots(workspace.resolve("snapshots"))
                    .runtimeLock(workspace.resolve("runtime.lock")).timeout(300_000L);
            TestRunResult run = new TestRunner().run(new LifecycleSpec(), options, null);
            require(run.passed() && run.tests().size() == 3, failure(run));
            Map<String, String> evidence = evidence(run);
            require(evidence.get("cobblestone").equals(expected(
                    "cobblestone", "004", 4, "ARCHETYPE")), "cobblestone evidence drift");
            require(evidence.get("dirt").equals(expected(
                    "dirt", "003", 3, "ARCHETYPE")), "dirt evidence drift");
            require(evidence.get("empty-chest").equals(expected(
                    "empty-chest", "054", 54, "SINGULAR")), "empty chest evidence drift");
            long worlds;
            try (java.util.stream.Stream<Path> paths = Files.list(workspace.resolve("worlds"))) {
                worlds = paths.filter(Files::isDirectory).count();
            }
            require(worlds == 3L, "provider attempt isolation drift");
            StringBuilder canonical = new StringBuilder();
            for (String value : evidence.values()) canonical.append(value).append("---\n");
            String evidenceHash = sha(canonical.toString());
            String signal = "provider=b1.7.3-server-lifecycle,rows=3,passed=3,"
                    + "layers=U-U-U-A+U-U-U-A+U-U-U-S,reload=FRESH_LOGINx6,"
                    + "evidence=" + evidenceHash + ",isolation=3-fresh-worlds";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|provider=b1.7.3-server-lifecycle|rows=cobblestone+dirt+empty-chest"
                    + "|actions=place+fresh-login+break+fresh-login"
                    + "|oracle=canonical-public-testkit-evidence|evidence=" + evidenceHash;
            System.out.println("WORLDLINE_B173_LIFECYCLE_SET=" + signal);
            System.out.println("WORLDLINE_B173_LIFECYCLE_TRACE=" + trace);
            System.out.println("WORLDLINE_B173_LIFECYCLE_SIGNATURE=" + sha(trace));
        } finally {
            if (prior == null) System.clearProperty(property);
            else System.setProperty(property, prior);
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

    private static String expected(String scenario, String subject, int block, String dropLayer) {
        String claim = "b1.7.3:block/" + subject + "#";
        return "schema=worldline.block-lifecycle-evidence.v1\nscenario=" + scenario
                + "\nsubject=b1.7.3:block/" + subject
                + "\nclaim.gameplay-placement=" + claim + "gameplay-placement|UNIVERSAL"
                + "\nclaim.save-reload=" + claim + "save-reload|UNIVERSAL"
                + "\nclaim.break-transition=" + claim + "break-transition|UNIVERSAL"
                + "\nclaim.drop-matrix=" + claim + "drop-matrix|" + dropLayer
                + "\nsupport=4:71:4:1:0\ntarget=4:72:4\nplaced=" + block + ":0"
                + "\ndrops=" + block + ":1:0\nreload=FRESH_LOGIN\n";
    }

    private static String line(String text, String prefix) {
        for (String row : text.split("\\n")) if (row.startsWith(prefix)) {
            return row.substring(prefix.length());
        }
        throw new IllegalStateException("lifecycle evidence lacks " + prefix);
    }

    private static String failure(TestRunResult run) {
        if (run.fatalError() != null) return run.fatalError();
        for (TestResult test : run.tests()) if (!test.passed()) {
            return test.path() + ": " + test.errorType() + ": " + test.errorMessage();
        }
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

    private static final class LifecycleSpec extends WorldlineSpec {
        @Override protected void define() {
            new BlockLifecyclePlan(B173ServerLifecycleTestRuntimeProvider.RUNTIME_ID,
                    B173ServerLifecycleFixtures.scenarios()).register("official block lifecycle");
        }
    }
}
