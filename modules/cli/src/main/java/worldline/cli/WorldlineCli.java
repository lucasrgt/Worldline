package worldline.cli;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import worldline.reproduction.ReplayProvider;
import worldline.reproduction.ReplayReport;
import worldline.reproduction.ReproductionBundle;

/** Stable command-line entrypoint for portable Worldline reproduction bundles. */
public final class WorldlineCli {
    private static final String DEFAULT_PROVIDER = "worldline.b173.B173ReplayProvider";

    private WorldlineCli() {}

    public static void main(String[] arguments) {
        int status = run(arguments, System.out, System.err);
        if (status != 0) System.exit(status);
    }

    public static int run(String[] arguments, PrintStream output, PrintStream error) {
        if (arguments == null || arguments.length != 2 || !"replay".equals(arguments[0])) {
            error.println("usage: worldline replay <bundle.wlrb>"); return 2;
        }
        try {
            ReproductionBundle bundle = ReproductionBundle.parse(
                    Files.readAllBytes(Paths.get(arguments[1])));
            String type = System.getProperty("worldline.replay.provider", DEFAULT_PROVIDER);
            ReplayProvider provider = Class.forName(type).asSubclass(ReplayProvider.class)
                    .getDeclaredConstructor().newInstance();
            require(provider.runtimeId().equals(bundle.runtimeId()), "no provider for " + bundle.runtimeId());
            ReplayReport report = replayQuietly(provider, bundle);
            require(report.runtimeId().equals(bundle.runtimeId()), "replay provider returned wrong runtime");
            output.println("WORLDLINE_REPLAY=PASS");
            output.println("bundle.sha256=" + bundle.sha256());
            output.println("snapshot.sha256=" + bundle.snapshot().sha256());
            output.println("runtime=" + report.runtimeId());
            output.println("tick=" + report.tick());
            output.println("state=" + report.state());
            return 0;
        } catch (IOException | ReflectiveOperationException | RuntimeException failure) {
            error.println("worldline replay failed: " + failure.getMessage()); return 1;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static ReplayReport replayQuietly(ReplayProvider provider, ReproductionBundle bundle) {
        PrintStream previous = System.out;
        try {
            System.setOut(new PrintStream(new OutputStream() {
                @Override public void write(int value) { }
            }));
            return provider.replay(bundle);
        } finally { System.setOut(previous); }
    }
}
