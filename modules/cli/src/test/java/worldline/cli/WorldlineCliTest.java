package worldline.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import worldline.api.RuntimeSnapshot;
import worldline.reproduction.ReplayProvider;
import worldline.reproduction.ReplayReport;
import worldline.reproduction.ReproductionBundle;

public final class WorldlineCliTest {
    private WorldlineCliTest() {}

    public static void main(String[] arguments) throws Exception {
        Path bundle = Files.createTempFile("worldline-cli-test", ".wlrb");
        String previous = System.getProperty("worldline.replay.provider");
        try {
            ReproductionBundle value = ReproductionBundle.create("test-runtime", "1.2.3",
                    repeat('a', 64), repeat('b', 40), RuntimeSnapshot.of(new byte[] {1}));
            Files.write(bundle, value.bytes());
            System.setProperty("worldline.replay.provider", FakeProvider.class.getName());
            ByteArrayOutputStream output = new ByteArrayOutputStream(), error = new ByteArrayOutputStream();
            int status = WorldlineCli.run(new String[] {"replay", bundle.toString()},
                    new PrintStream(output, true, "UTF-8"), new PrintStream(error, true, "UTF-8"));
            String text = output.toString(StandardCharsets.UTF_8.name());
            require(status == 0 && error.size() == 0 && text.contains("WORLDLINE_REPLAY=PASS")
                    && text.contains("state=tick0=ok"), "CLI replay failed");
            require(WorldlineCli.run(new String[0], System.out, new PrintStream(error)) == 2,
                    "CLI usage did not fail");
        } finally {
            if (previous == null) System.clearProperty("worldline.replay.provider");
            else System.setProperty("worldline.replay.provider", previous);
            Files.deleteIfExists(bundle);
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
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
