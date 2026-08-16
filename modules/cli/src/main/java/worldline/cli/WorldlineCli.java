package worldline.cli;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import worldline.analysis.TraceDiff;
import worldline.analysis.TraceRenderer;
import worldline.mods.ModArtifact;
import worldline.mods.ModLoader;
import worldline.modtest.ModTestComparison;
import worldline.modtest.ModTestResult;
import worldline.minimization.Scenario;
import worldline.reproduction.ReplayProvider;
import worldline.reproduction.ReplayReport;
import worldline.reproduction.ReproductionBundle;
import worldline.trace.CanonicalStateDocument;

/** Stable command-line entrypoint for replay, trace, and mod package operations. */
public final class WorldlineCli {
    private static final String DEFAULT_PROVIDER = "worldline.b173.B173ReplayProvider";
    private static final String MOD_RUNTIME = "b1.7.3", MOD_API = "1";

    private WorldlineCli() {}

    public static void main(String[] arguments) {
        int status = run(arguments, System.out, System.err);
        if (status != 0) System.exit(status);
    }

    public static int run(String[] arguments, PrintStream output, PrintStream error) {
        if (arguments == null) return usage(error);
        try {
            if (arguments.length == 2 && "replay".equals(arguments[0]))
                return replay(arguments[1], output);
            if (arguments.length == 3 && "trace".equals(arguments[0])
                    && "show".equals(arguments[1])) return show(arguments[2], output);
            if (arguments.length == 4 && "trace".equals(arguments[0])
                    && "diff".equals(arguments[1])) return diff(arguments[2], arguments[3], output);
            if (arguments.length == 3 && "mod".equals(arguments[0])
                    && "inspect".equals(arguments[1])) return inspectMod(arguments[2], output);
            if (arguments.length == 6 && "mod".equals(arguments[0])
                    && "test".equals(arguments[1]) && "record".equals(arguments[2]))
                return recordModTest(arguments[3], arguments[4], arguments[5], output);
            if (arguments.length == 5 && "mod".equals(arguments[0])
                    && "test".equals(arguments[1]) && "diff".equals(arguments[2]))
                return diffModTests(arguments[3], arguments[4], output);
            if (arguments.length >= 3 && "scenario".equals(arguments[0])
                    && "create".equals(arguments[1]))
                return createScenario(arguments[2], Arrays.asList(arguments).subList(3, arguments.length), output);
            if (arguments.length == 3 && "scenario".equals(arguments[0])
                    && "inspect".equals(arguments[1])) return inspectScenario(arguments[2], output);
            return usage(error);
        } catch (IOException | ReflectiveOperationException | RuntimeException failure) {
            error.println("worldline command failed: " + failure.getMessage()); return 1;
        }
    }

    private static int replay(String path, PrintStream output)
            throws IOException, ReflectiveOperationException {
        ReproductionBundle bundle = ReproductionBundle.parse(Files.readAllBytes(Paths.get(path)));
        String type = System.getProperty("worldline.replay.provider", DEFAULT_PROVIDER);
        ReplayProvider provider = Class.forName(type).asSubclass(ReplayProvider.class)
                .getDeclaredConstructor().newInstance();
        require(provider.runtimeId().equals(bundle.runtimeId()), "no provider for " + bundle.runtimeId());
        ReplayReport report = replayQuietly(provider, bundle);
        require(report.runtimeId().equals(bundle.runtimeId()), "replay provider returned wrong runtime");
        output.println("WORLDLINE_REPLAY=PASS");
        output.println("bundle.sha256=" + bundle.sha256());
        output.println("snapshot.sha256=" + bundle.snapshot().sha256());
        output.println("runtime=" + report.runtimeId()); output.println("tick=" + report.tick());
        output.println("state=" + report.state()); return 0;
    }

    private static int show(String path, PrintStream output) throws IOException {
        output.print("WORLDLINE_TRACE_SHOW=PASS\n");
        output.print(TraceRenderer.render(readTrace(path))); return 0;
    }

    private static int diff(String left, String right, PrintStream output) throws IOException {
        TraceDiff difference = TraceDiff.compare(readTrace(left), readTrace(right));
        output.print("WORLDLINE_TRACE_DIFF=" + (difference.diverged() ? "DIVERGED\n" : "EQUAL\n"));
        output.print(difference.render()); return difference.diverged() ? 3 : 0;
    }

    private static CanonicalStateDocument readTrace(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        require(bytes.length > 0 && bytes.length <= CanonicalStateDocument.MAX_CHARACTERS,
                "invalid trace file size");
        String value = new String(bytes, StandardCharsets.UTF_8);
        require(Arrays.equals(bytes, value.getBytes(StandardCharsets.UTF_8)), "trace is not strict UTF-8");
        return CanonicalStateDocument.parse(value);
    }

    private static int inspectMod(String path, PrintStream output) throws IOException {
        ModArtifact artifact = ModLoader.inspect(Paths.get(path), MOD_RUNTIME, MOD_API);
        output.println("WORLDLINE_MOD_INSPECT=" + (artifact.compatible() ? "PASS" : "INCOMPATIBLE"));
        output.println("id=" + artifact.descriptor().id());
        output.println("version=" + artifact.descriptor().version());
        output.println("entrypoint=" + artifact.descriptor().entrypoint());
        output.println("runtime=" + artifact.descriptor().runtime());
        output.println("worldline.api=" + artifact.descriptor().worldlineApi());
        output.println("artifact.sha256=" + artifact.sha256());
        output.println("compatibility=" + artifact.compatibility());
        return artifact.compatible() ? 0 : 3;
    }

    private static int recordModTest(String modPath, String tracePath, String resultPath,
            PrintStream output) throws IOException {
        ModArtifact artifact = ModLoader.inspect(Paths.get(modPath), MOD_RUNTIME, MOD_API);
        require(artifact.compatible(), "cannot record an incompatible mod");
        ModTestResult result = ModTestResult.create(artifact, readTrace(tracePath));
        Files.write(Paths.get(resultPath), result.bytes(), StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        output.println("WORLDLINE_MOD_TEST_RECORD=PASS");
        output.println("mod=" + result.modId() + "@" + result.modVersion());
        output.println("artifact.sha256=" + result.artifactSha256());
        output.println("trace.sha256=" + result.trace().signature());
        output.println("result.sha256=" + result.sha256()); return 0;
    }

    private static int diffModTests(String leftPath, String rightPath, PrintStream output)
            throws IOException {
        ModTestComparison comparison = ModTestComparison.compare(
                readModTest(leftPath), readModTest(rightPath));
        output.println("WORLDLINE_MOD_TEST_DIFF="
                + (comparison.behaviorDiverged() ? "DIVERGED" : "EQUAL"));
        output.print(comparison.render()); return comparison.behaviorDiverged() ? 3 : 0;
    }

    private static ModTestResult readModTest(String path) throws IOException {
        long size = Files.size(Paths.get(path));
        require(size > 0 && size <= ModTestResult.MAX_BYTES, "invalid mod test result size");
        return ModTestResult.parse(Files.readAllBytes(Paths.get(path)));
    }

    private static int createScenario(String path, java.util.List<String> steps, PrintStream output)
            throws IOException {
        Scenario scenario = Scenario.of(steps);
        Files.write(Paths.get(path), scenario.bytes(), StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        output.println("WORLDLINE_SCENARIO_CREATE=PASS");
        output.println("steps=" + scenario.size());
        output.println("scenario.sha256=" + scenario.sha256()); return 0;
    }

    private static int inspectScenario(String path, PrintStream output) throws IOException {
        Scenario scenario = readScenario(path);
        output.println("WORLDLINE_SCENARIO_INSPECT=PASS");
        output.println("steps=" + scenario.size());
        output.println("scenario.sha256=" + scenario.sha256());
        for (int index = 0; index < scenario.size(); index++)
            output.println(index + "=" + scenario.step(index));
        return 0;
    }

    private static Scenario readScenario(String path) throws IOException {
        long size = Files.size(Paths.get(path));
        require(size > 0 && size <= Scenario.MAX_BYTES, "invalid scenario size");
        return Scenario.parse(Files.readAllBytes(Paths.get(path)));
    }

    private static int usage(PrintStream error) {
        error.println("usage: worldline replay <bundle.wlrb>");
        error.println("   or: worldline trace show <trace.wltrace>");
        error.println("   or: worldline trace diff <left.wltrace> <right.wltrace>");
        error.println("   or: worldline mod inspect <mod.jar>");
        error.println("   or: worldline mod test record <mod.jar> <trace.wltrace> <result.wlmtest>");
        error.println("   or: worldline mod test diff <left.wlmtest> <right.wlmtest>");
        error.println("   or: worldline scenario create <output.wlscenario> [step ...]");
        error.println("   or: worldline scenario inspect <scenario.wlscenario>"); return 2;
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
