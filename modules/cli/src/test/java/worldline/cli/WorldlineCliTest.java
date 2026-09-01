package worldline.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import worldline.api.RuntimeSnapshot;
import worldline.modtest.ModTestResult;
import worldline.modtest.ModTestRunner;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioRunner;
import worldline.mods.ModArtifact;
import worldline.mods.ModLoader;
import worldline.reproduction.ReplayProvider;
import worldline.reproduction.ReplayReport;
import worldline.reproduction.ReproductionBundle;
import worldline.trace.CanonicalStateDocument;

public final class WorldlineCliTest {
    private WorldlineCliTest() {}

    public static void main(String[] arguments) throws Exception {
        ProfilerCommandTest.main(arguments);
        String testClasses = System.getProperty("worldline.test.classes", ".worldline/build/test-classes");
        Path bundle = Files.createTempFile("worldline-cli-test", ".wlrb");
        Path left = Files.createTempFile("worldline-cli-left", ".wltrace");
        Path right = Files.createTempFile("worldline-cli-right", ".wltrace");
        Path mod = Files.createTempFile("worldline-cli-mod", ".jar");
        Path secondMod = Files.createTempFile("worldline-cli-mod-two", ".jar");
        Path leftResult = Files.createTempFile("worldline-cli-left", ".wlmtest");
        Path rightResult = Files.createTempFile("worldline-cli-right", ".wlmtest");
        Path scenario = Files.createTempFile("worldline-cli", ".wlscenario");
        Path dslScenario = Files.createTempFile("worldline-cli-dsl", ".wlscenario");
        Path runResult = Files.createTempFile("worldline-cli-run", ".wlmtest");
        Path runTrace = Files.createTempFile("worldline-cli-run", ".wltrace");
        String previous = System.getProperty("worldline.replay.provider");
        try {
            ReproductionBundle value = ReproductionBundle.create("test-runtime", "1.2.3",
                    repeat('a', 64), repeat('b', 40), RuntimeSnapshot.of(new byte[] {1}));
            Files.write(bundle, value.bytes());
            writeMod(mod, "1.0.0", "b1.7.3");
            writeMod(secondMod, "1.1.0", "b1.7.3");
            Files.delete(leftResult); Files.delete(rightResult); Files.delete(scenario);
            Files.delete(dslScenario); Files.delete(runResult); Files.delete(runTrace);
            System.setProperty("worldline.replay.provider", FakeProvider.class.getName());
            ByteArrayOutputStream output = new ByteArrayOutputStream(), error = new ByteArrayOutputStream();
            int status = WorldlineCli.run(new String[] {"replay", bundle.toString()},
                    new PrintStream(output, true, "UTF-8"), new PrintStream(error, true, "UTF-8"));
            String text = output.toString(StandardCharsets.UTF_8.name());
            require(status == 0 && error.size() == 0 && text.contains("WORLDLINE_REPLAY=PASS")
                    && text.contains("state=tick0=ok"), "CLI replay failed");
            Files.write(left, "v2|seed=7|schema=x,y|tick0=1,2".getBytes(StandardCharsets.UTF_8));
            Files.write(right, "v2|seed=7|schema=x,y|tick0=1,9".getBytes(StandardCharsets.UTF_8));
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"trace", "show", left.toString()},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_TRACE_SHOW=PASS")
                    && output.toString().contains("index\tlabel\tx\ty"), "CLI trace show failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"trace", "diff", left.toString(), left.toString()},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_TRACE_DIFF=EQUAL"),
                    "CLI equal trace diff failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"trace", "diff", left.toString(), right.toString()},
                    new PrintStream(output), new PrintStream(error));
            require(status == 3 && output.toString().contains("WORLDLINE_TRACE_DIFF=DIVERGED")
                    && output.toString().contains("field=y") && output.toString().contains("right=9")
                    && output.toString().contains("role=ENTITY_POS_Y")
                    && !output.toString().contains("invariant="),
                    "CLI divergent trace diff failed");
            Files.write(left, "v2|seed=7|schema=block65|tick0=0".getBytes(StandardCharsets.UTF_8));
            Files.write(right, "v2|seed=7|schema=block65|tick0=20".getBytes(StandardCharsets.UTF_8));
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"trace", "diff", left.toString(), right.toString()},
                    new PrintStream(output), new PrintStream(error));
            require(status == 3 && output.toString().contains("field=block65")
                    && output.toString().contains("invariant=block-conservation"),
                    "CLI invariant alias failed");
            Files.write(left, "v2|seed=7|schema=x,y|tick0=1,2".getBytes(StandardCharsets.UTF_8));
            Files.write(right, "v2|seed=7|schema=x,y|tick0=1,9".getBytes(StandardCharsets.UTF_8));
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"mod", "inspect", mod.toString()},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_MOD_INSPECT=PASS")
                    && output.toString().contains("id=worldline.probe")
                    && output.toString().contains("compatibility=COMPATIBLE"),
                    "CLI mod inspection failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"mod", "test", "record", mod.toString(),
                    left.toString(), leftResult.toString()}, new PrintStream(output), new PrintStream(error));
            require(status == 0 && Files.isRegularFile(leftResult)
                    && output.toString().contains("WORLDLINE_MOD_TEST_RECORD=PASS"),
                    "CLI mod test record failed");
            byte[] recorded = Files.readAllBytes(leftResult); output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"mod", "test", "record", mod.toString(),
                    right.toString(), leftResult.toString()}, new PrintStream(output), new PrintStream(error));
            require(status == 1 && java.util.Arrays.equals(recorded, Files.readAllBytes(leftResult)),
                    "CLI overwrote an existing mod test result");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"mod", "test", "record", secondMod.toString(),
                    right.toString(), rightResult.toString()}, new PrintStream(output), new PrintStream(error));
            require(status == 0 && Files.isRegularFile(rightResult), "CLI second mod test record failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"mod", "test", "diff", leftResult.toString(),
                    rightResult.toString()}, new PrintStream(output), new PrintStream(error));
            require(status == 3 && output.toString().contains("WORLDLINE_MOD_TEST_DIFF=DIVERGED")
                    && output.toString().contains("same.mod=true")
                    && output.toString().contains("same.version=false")
                    && output.toString().contains("field=y"), "CLI mod version diff failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"mod", "test", "diff", leftResult.toString(),
                    leftResult.toString()}, new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_MOD_TEST_DIFF=EQUAL"),
                    "CLI equal mod test diff failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"scenario", "create", scenario.toString(),
                    "noise:a", "tick", "observe:target"}, new PrintStream(output), new PrintStream(error));
            require(status == 0 && Files.isRegularFile(scenario)
                    && output.toString().contains("WORLDLINE_SCENARIO_CREATE=PASS")
                    && output.toString().contains("steps=3"), "CLI scenario creation failed");
            byte[] scenarioBytes = Files.readAllBytes(scenario); output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"scenario", "create", scenario.toString(), "tick"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 1 && java.util.Arrays.equals(scenarioBytes, Files.readAllBytes(scenario)),
                    "CLI overwrote an existing scenario");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"scenario", "inspect", scenario.toString()},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_SCENARIO_INSPECT=PASS")
                    && output.toString().contains("1=tick")
                    && output.toString().contains("2=observe:target"), "CLI scenario inspection failed");
            Files.write(dslScenario, Scenario.of(java.util.Arrays.asList("observe:before",
                    "block:8,65,8:20", "tick:2", "reseed:101", "observe:after")).bytes());
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"scenario", "validate", dslScenario.toString()},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_SCENARIO_VALIDATE=PASS")
                    && output.toString().contains("dsl=worldline-scenario-dsl/1")
                    && output.toString().contains("2=TICK:tick:2"), "CLI scenario validate failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"scenario", "validate", scenario.toString()},
                    new PrintStream(output), new PrintStream(error));
            require(status == 1 && error.toString().contains("unknown scenario step"),
                    "CLI accepted a step outside the DSL");
            System.setProperty("worldline.modtest.provider", FakeModTestRunner.class.getName());
            System.setProperty("worldline.scenario.provider", FakeScenarioRunner.class.getName());
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"mod", "test", "run", mod.toString(),
                    "17320110707", "16", runResult.toString()}, new PrintStream(output), new PrintStream(error));
            require(status == 0 && Files.isRegularFile(runResult)
                    && output.toString().contains("WORLDLINE_MOD_TEST_RUN=PASS")
                    && output.toString().contains("execution=controlled-runtime")
                    && output.toString().contains("seed=17320110707") && output.toString().contains("ticks=16"),
                    "CLI mod test run failed");
            byte[] executed = Files.readAllBytes(runResult);
            require(ModTestResult.parse(executed).executed(), "run result lost attestation");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"mod", "test", "run", mod.toString(),
                    "17320110707", "16", runResult.toString()}, new PrintStream(output), new PrintStream(error));
            require(status == 1 && java.util.Arrays.equals(executed, Files.readAllBytes(runResult)),
                    "CLI overwrote an executed mod test result");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"scenario", "run", dslScenario.toString(),
                    "4242", runTrace.toString()}, new PrintStream(output), new PrintStream(error));
            require(status == 0 && Files.isRegularFile(runTrace)
                    && output.toString().contains("WORLDLINE_SCENARIO_RUN=PASS")
                    && output.toString().contains("trace.sha256="), "CLI scenario run failed");
            require(CanonicalStateDocument.parse(new String(Files.readAllBytes(runTrace),
                    StandardCharsets.UTF_8)).seed() == 4242L, "scenario run trace seed mismatch");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"scenario", "bogus", scenario.toString()},
                    new PrintStream(output), new PrintStream(error));
            require(status == 2 && error.toString().contains("usage: worldline"),
                    "CLI accepted an unknown scenario subcommand");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"mod", "bogus", mod.toString()},
                    new PrintStream(output), new PrintStream(error));
            require(status == 2, "CLI accepted an unknown mod subcommand");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"semantics", "show"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_SEMANTICS=PASS")
                    && output.toString().contains("complete=true")
                    && output.toString().contains("CLIENT_TICK_ROOT"), "CLI semantics show failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"semantics", "role", "CLIENT_TICK_ROOT"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("runTick"), "CLI semantics role failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"semantics", "graph"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_SEMANTICS_GRAPH=PASS")
                    && output.toString().contains("complete=true"), "CLI semantics graph failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"semantics", "category", "energy"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 1 && error.toString().contains("unknown category"),
                    "CLI unknown semantics category failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"semantics", "adapter"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("b173-client=driver")
                    && output.toString().contains("b173-server=driver")
                    && output.toString().contains("stationapi=driver")
                    && output.toString().contains("aero-model-lib=extension"),
                    "CLI semantics adapter failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"semantics", "adapter", "check", "."},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("aero-model-lib=extension"),
                    "CLI semantics adapter check failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"atlas", "status"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_ATLAS=PASS")
                    && output.toString().contains("role=")
                    && output.toString().contains("coverage_unit=182")
                    && output.toString().contains("mapping_set=2"),
                    "CLI atlas status failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"atlas", "show",
                    "atlas.role.CLIENT_TICK_ROOT"}, new PrintStream(output),
                    new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_ATLAS_SHOW=PASS")
                    && output.toString().contains("CLIENT_TICK_ROOT"),
                    "CLI atlas show failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"atlas", "search", "item-conservation"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("atlas.invariant.item-conservation"),
                    "CLI atlas search failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"atlas", "coverage"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_ATLAS_COVERAGE=PASS")
                    && output.toString().contains("worldgen"),
                    "CLI atlas coverage failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"atlas", "gaps"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_ATLAS_GAPS=PASS"),
                    "CLI atlas gaps failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"atlas", "evidence",
                    "atlas.experiment.m80-natural-membership-rebuild"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("expected.signature="),
                    "CLI atlas evidence failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"atlas", "graph",
                    "atlas.role.CLIENT_TICK_ROOT"}, new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_ATLAS_GRAPH=PASS")
                    && output.toString().contains("READS atlas.boundary."),
                    "CLI atlas graph failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"atlas", "export"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_ATLAS_EXPORT=PASS")
                    && output.toString().contains("WORLDLINE-ATLAS-STORE/1"),
                    "CLI atlas export failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"atlas", "changed", "--since", "M70"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_ATLAS_CHANGED=PASS")
                    && output.toString().contains("m80-natural-membership-rebuild"),
                    "CLI atlas changed failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"behaviors", "list"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && error.size() == 0
                    && output.toString().contains("WORLDLINE_BEHAVIORS=PASS")
                    && output.toString().contains("void-death\tenvironment\tatlas.scenario.void-death"),
                    "CLI behavior catalog failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"atlas", "show",
                    "atlas.hypothesis.aero-historical-spike"}, new PrintStream(output),
                    new PrintStream(error));
            require(status == 0 && output.toString().contains("NON_CLAIM"),
                    "CLI atlas hypothesis failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"atlas", "show", "atlas.role.NOT_A_ROLE"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 1 && error.toString().contains("unknown atlas id"),
                    "CLI unknown atlas id failed");
            require(WorldlineCli.run(new String[0], System.out, new PrintStream(error)) == 2,
                    "CLI usage did not fail");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"test", "--help"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("worldline test")
                    && output.toString().contains("--reporter="), "test CLI help failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"test", "run", "--help"},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && error.size() == 0 && output.toString().contains("worldline test")
                    && output.toString().contains("--name="), "test subcommand help failed");
            Path testArtifacts = Files.createTempDirectory("worldline-cli-testkit-");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"test", "run",
                    testClasses, CliSpec.class.getName(), "--no-runtime",
                    "--reporter=agent", "--artifacts=" + testArtifacts},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_TEST=PASS"),
                    "test CLI run failed: " + error);
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"test", "list",
                    testClasses}, new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("worldline.cli.DiscoverySpec")
                    && output.toString().contains("test=discovered"), "test discovery failed: " + error);
        } finally {
            if (previous == null) System.clearProperty("worldline.replay.provider");
            else System.setProperty("worldline.replay.provider", previous);
            System.clearProperty("worldline.modtest.provider");
            System.clearProperty("worldline.scenario.provider");
            Files.deleteIfExists(bundle);
            Files.deleteIfExists(left); Files.deleteIfExists(right);
            Files.deleteIfExists(mod);
            Files.deleteIfExists(secondMod); Files.deleteIfExists(leftResult);
            Files.deleteIfExists(rightResult);
            Files.deleteIfExists(scenario);
            Files.deleteIfExists(dslScenario); Files.deleteIfExists(runResult);
            Files.deleteIfExists(runTrace);
        }
        System.out.println("WorldlineCliTest passed");
    }

    public static final class FakeProvider implements ReplayProvider {
        @Override public String runtimeId() { return "test-runtime"; }
        @Override public ReplayReport replay(ReproductionBundle bundle) {
            return new ReplayReport(runtimeId(), 0, "tick0=ok");
        }
    }
    public static final class FakeModTestRunner implements ModTestRunner {
        @Override public ModTestResult run(Path modJar, long seed, int ticks) {
            try {
                ModArtifact artifact = ModLoader.inspect(modJar, "b1.7.3", "1");
                CanonicalStateDocument trace = CanonicalStateDocument.parse(
                        "v2|seed=" + seed + "|schema=x|tick0=" + ticks);
                return ModTestResult.createExecuted(artifact, trace, seed, ticks);
            } catch (Exception error) { throw new IllegalStateException(error); }
        }
    }

    public static final class FakeScenarioRunner implements ScenarioRunner {
        @Override public CanonicalStateDocument run(Scenario scenario, long seed) {
            return CanonicalStateDocument.parse(
                    "v2|seed=" + seed + "|schema=x|tick0=" + scenario.size());
        }
    }

    public static final class CliSpec extends worldline.test.WorldlineSpec {
        @Override protected void define() {
            worldline.test.Worldline.test("CLI spec", context ->
                    worldline.test.Expect.expect(context.seed()).toEqual(173L));
        }
    }

    private static String repeat(char value, int count) {
        StringBuilder result = new StringBuilder(); while (result.length() < count) result.append(value);
        return result.toString();
    }
    private static void writeMod(Path path, String version, String runtime) throws Exception {
        String descriptor = "format=1\nid=worldline.probe\nversion=" + version + "\n"
                + "entrypoint=worldline.benchmark.ProbeMod\nworldline.api=1\nruntime="
                + runtime + "\n";
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(path))) {
            output.putNextEntry(new JarEntry("META-INF/worldline-mod.properties"));
            output.write(descriptor.getBytes(StandardCharsets.UTF_8)); output.closeEntry();
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
