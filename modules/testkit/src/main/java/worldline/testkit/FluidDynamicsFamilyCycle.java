package worldline.testkit;
import worldline.testapi.FluidDynamicsPlan;
import worldline.testapi.FluidDynamicsScenario;

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

/** Executes and signs caller-owned source-fluid dynamics rows. */
public final class FluidDynamicsFamilyCycle {
    private FluidDynamicsFamilyCycle() { }

    public static void run(String[] arguments, String family, long expectedSeed,
            String serverProperty, TestRuntimeProvider provider,
            List<FluidDynamicsScenario> rows) throws Exception {
        if (arguments.length != 4) throw new IllegalArgumentException(
                "usage: fluid dynamics family server.jar workspace port seed");
        Path server = Paths.get(arguments[0]).toAbsolutePath().normalize();
        Path workspace = Paths.get(arguments[1]).toAbsolutePath().normalize();
        Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        if (seed != expectedSeed) throw new IllegalStateException("fluid dynamics seed drift");
        if (!family.matches("[a-z0-9-]+") || rows.isEmpty() || provider == null) {
            throw new IllegalArgumentException("invalid fluid dynamics family");
        }
        String prior = System.getProperty(serverProperty);
        try {
            System.setProperty(serverProperty, server.toString());
            RunnerOptions options = new RunnerOptions().provider(provider).seed(seed)
                    .world(workspace.resolve("worlds")).artifacts(workspace.resolve("results"))
                    .snapshots(workspace.resolve("snapshots"))
                    .runtimeLock(workspace.resolve("runtime.lock")).timeout(420_000L);
            TestRunResult run = new TestRunner().run(new FamilySpec(
                    family, provider.runtimeId(), rows), options, null);
            require(run.passed() && run.tests().size() == rows.size(), failure(run));
            Map<String, String> evidence = evidence(run);
            verify(rows, evidence);
            long worlds;
            try (java.util.stream.Stream<Path> paths = Files.list(workspace.resolve("worlds"))) {
                worlds = paths.filter(Files::isDirectory).count();
            }
            require(worlds == rows.size(), "fluid dynamics attempt isolation drift");
            StringBuilder canonical = new StringBuilder();
            for (String value : evidence.values()) canonical.append(value).append("---\n");
            String evidenceHash = sha(canonical.toString());
            String signal = "provider=" + provider.runtimeId() + ",family=" + family
                    + ",rows=" + rows.size() + ",passed=" + rows.size() + ",claims="
                    + (rows.size() * 4) + ",gate=stone-to-air-to-flow,reload=FRESH_LOGINx"
                    + rows.size() + ",evidence=" + evidenceHash + ",isolation="
                    + rows.size() + "-fresh-worlds";
            String trace = "v1|server=official-b1.7.3|seed=" + seed + "|provider="
                    + provider.runtimeId() + "|family=" + family + "|rows=" + ids(rows)
                    + "|actions=place-source+settle+break-gate+flow+fresh-login"
                    + "|oracle=canonical-public-fluid-dynamics-evidence|evidence="
                    + evidenceHash;
            System.out.println("WORLDLINE_B173_FLUID_DYNAMICS_SET=" + signal);
            System.out.println("WORLDLINE_B173_FLUID_DYNAMICS_TRACE=" + trace);
            System.out.println("WORLDLINE_B173_FLUID_DYNAMICS_SIGNATURE=" + sha(trace));
        } finally {
            if (prior == null) System.clearProperty(serverProperty);
            else System.setProperty(serverProperty, prior);
        }
    }

    private static Map<String, String> evidence(TestRunResult run) throws Exception {
        Map<String, String> values = new TreeMap<String, String>();
        for (TestResult test : run.tests()) {
            require(test.passed() && test.artifacts().size() == 1,
                    "fluid result artifact drift: " + test.path());
            Path artifact = test.artifacts().get(0);
            require(artifact.getFileName().toString().equals(
                    FluidDynamicsPlan.EVIDENCE_ARTIFACT), "unexpected fluid artifact");
            String value = new String(Files.readAllBytes(artifact), StandardCharsets.UTF_8);
            require(value.startsWith("schema=worldline.fluid-dynamics-evidence.v1\n"),
                    "fluid evidence schema drift");
            String id = line(value, "scenario=");
            require(values.put(id, value) == null, "duplicate fluid evidence");
        }
        return values;
    }

    private static void verify(List<FluidDynamicsScenario> rows,
            Map<String, String> evidence) {
        require(evidence.size() == rows.size(), "fluid evidence count drift");
        for (FluidDynamicsScenario row : rows) {
            String value = evidence.get(row.id());
            require(value != null && value.contains("subject=" + row.subject() + "\n")
                    && value.contains("claim.gameplay-placement=" + row.placement().claimId())
                    && value.contains("claim.save-reload=" + row.persistence().claimId())
                    && value.contains("claim.tick-policy=" + row.tickPolicy().claimId())
                    && value.contains("claim.neighbor-response="
                            + row.neighborResponse().claimId())
                    && value.contains("gate=") && value.endsWith("reload=FRESH_LOGIN\n"),
                    "fluid canonical evidence drift: " + row.id());
        }
    }

    private static String ids(List<FluidDynamicsScenario> rows) {
        StringBuilder value = new StringBuilder();
        for (FluidDynamicsScenario row : rows) {
            if (value.length() > 0) value.append(','); value.append(row.id());
        }
        return value.toString();
    }

    private static String line(String text, String prefix) {
        for (String row : text.split("\\n")) if (row.startsWith(prefix)) {
            return row.substring(prefix.length());
        }
        throw new IllegalStateException("fluid evidence lacks " + prefix);
    }

    private static String failure(TestRunResult run) {
        if (run.fatalError() != null) return run.fatalError();
        for (TestResult test : run.tests()) if (!test.passed()) {
            return test.path() + ": " + test.errorType() + ": " + test.errorMessage();
        }
        return "fluid dynamics TestKit run failed";
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
        private final String family, runtime; private final List<FluidDynamicsScenario> rows;
        FamilySpec(String family, String runtime, List<FluidDynamicsScenario> rows) {
            this.family = family; this.runtime = runtime; this.rows = rows;
        }
        @Override protected void define() {
            new FluidDynamicsPlan(runtime, rows).register("official " + family + " dynamics");
        }
    }
}
