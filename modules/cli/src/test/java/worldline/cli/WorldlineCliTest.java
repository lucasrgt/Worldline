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
        String previous = System.getProperty("worldline.replay.provider");
        try {
            ReproductionBundle value = ReproductionBundle.create("test-runtime", "1.2.3",
                    repeat('a', 64), repeat('b', 40), RuntimeSnapshot.of(new byte[] {1}));
            Files.write(bundle, value.bytes());
            writeMod(mod, "b1.7.3");
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
                    && output.toString().contains("field=y") && output.toString().contains("right=9"),
                    "CLI divergent trace diff failed");
            output.reset(); error.reset();
            status = WorldlineCli.run(new String[] {"mod", "inspect", mod.toString()},
                    new PrintStream(output), new PrintStream(error));
            require(status == 0 && output.toString().contains("WORLDLINE_MOD_INSPECT=PASS")
                    && output.toString().contains("id=worldline.probe")
                    && output.toString().contains("compatibility=COMPATIBLE"),
                    "CLI mod inspection failed");
            require(WorldlineCli.run(new String[0], System.out, new PrintStream(error)) == 2,
                    "CLI usage did not fail");
        } finally {
            if (previous == null) System.clearProperty("worldline.replay.provider");
            else System.setProperty("worldline.replay.provider", previous);
            Files.deleteIfExists(bundle);
            Files.deleteIfExists(left); Files.deleteIfExists(right);
            Files.deleteIfExists(mod);
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
    private static void writeMod(Path path, String runtime) throws Exception {
        String descriptor = "format=1\nid=worldline.probe\nversion=1.0.0\n"
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
