package worldline.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import worldline.api.RuntimeSnapshot;
import worldline.reproduction.ReplayProvider;
import worldline.reproduction.ReplayReport;
import worldline.reproduction.ReproductionBundle;

public final class WorldlineCliTest {
    private WorldlineCliTest() {}

    public static void main(String[] arguments) throws Exception {
        Path bundle = Files.createTempFile("worldline-cli-test", ".wlrb");
        Path left = Files.createTempFile("worldline-cli-left", ".wltrace");
        Path right = Files.createTempFile("worldline-cli-right", ".wltrace");
        Path mod = Files.createTempFile("worldline-cli-mod", ".jar");
        Path secondMod = Files.createTempFile("worldline-cli-mod-two", ".jar");
        Path leftResult = Files.createTempFile("worldline-cli-left", ".wlmtest");
        Path rightResult = Files.createTempFile("worldline-cli-right", ".wlmtest");
        Path scenario = Files.createTempFile("worldline-cli", ".wlscenario");
        String previous = System.getProperty("worldline.replay.provider");
        try {
            ReproductionBundle value = ReproductionBundle.create("test-runtime", "1.2.3",
                    repeat('a', 64), repeat('b', 40), RuntimeSnapshot.of(new byte[] {1}));
            Files.write(bundle, value.bytes());
            writeMod(mod, "1.0.0", "b1.7.3");
            writeMod(secondMod, "1.1.0", "b1.7.3");
            Files.delete(leftResult); Files.delete(rightResult); Files.delete(scenario);
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
            require(WorldlineCli.run(new String[0], System.out, new PrintStream(error)) == 2,
                    "CLI usage did not fail");
        } finally {
            if (previous == null) System.clearProperty("worldline.replay.provider");
            else System.setProperty("worldline.replay.provider", previous);
            Files.deleteIfExists(bundle);
            Files.deleteIfExists(left); Files.deleteIfExists(right);
            Files.deleteIfExists(mod);
            Files.deleteIfExists(secondMod); Files.deleteIfExists(leftResult);
            Files.deleteIfExists(rightResult);
            Files.deleteIfExists(scenario);
        }
        System.out.println("WorldlineCliTest passed");
    }

    public static final class FakeProvider implements ReplayProvider {
        @Override public String runtimeId() { return "test-runtime"; }
        @Override public ReplayReport replay(ReproductionBundle bundle) {
            return new ReplayReport(runtimeId(), 0, "tick0=ok");
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
