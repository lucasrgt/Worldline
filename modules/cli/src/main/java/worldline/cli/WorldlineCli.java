package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import worldline.analysis.TraceDiff;
import worldline.invariants.InvariantFields;
import worldline.reproduction.ReplayProvider;
import worldline.reproduction.ReplayReport;
import worldline.reproduction.ReproductionBundle;

/** Stable command-line entrypoint for replay, trace, and mod package operations. */
public final class WorldlineCli {
    private static final String DEFAULT_PROVIDER = "worldline.b173.B173ReplayProvider";

    private WorldlineCli() {}

    public static void main(String[] arguments) {
        int status = run(arguments, System.out, System.err);
        if (status != 0) System.exit(status);
    }

    public static int run(String[] arguments, PrintStream output, PrintStream error) {
        if (arguments == null || arguments.length == 0) return usage(error);
        try {
            if ("init".equals(arguments[0]))
                return WorldlineProjectInit.run(Arrays.copyOfRange(arguments, 1, arguments.length), output);
            if ("doctor".equals(arguments[0]))
                return WorldlineProjectDoctor.run(Arrays.copyOfRange(arguments, 1, arguments.length), output);
            if ("migrate".equals(arguments[0]))
                return WorldlineProjectMigrate.run(Arrays.copyOfRange(arguments, 1, arguments.length), output);
            if ("test".equals(arguments[0]))
                return TestCommand.run(arguments, output, error);
            if (arguments.length == 2 && "replay".equals(arguments[0]))
                return replay(arguments[1], output);
            if (arguments.length == 3 && "trace".equals(arguments[0])
                    && "show".equals(arguments[1])) return show(arguments[2], output);
            if (arguments.length == 4 && "trace".equals(arguments[0])
                    && "diff".equals(arguments[1])) return diff(arguments[2], arguments[3], output);
            if ("trace".equals(arguments[0]) && arguments.length >= 4)
                return TraceHtmlCommand.run(arguments, output, error);
            if ("mod".equals(arguments[0]) && arguments.length >= 3)
                return mod(arguments, output, error);
            if ("scenario".equals(arguments[0]) && arguments.length >= 3)
                return ScenarioCommands.run(arguments, output, error);
            if ("fuzz".equals(arguments[0]))
                return FuzzCommand.run(arguments, output, error);
            if (arguments.length == 3 && "debug".equals(arguments[0]))
                return DebugCommand.run(arguments, output, error);
            if ("profile".equals(arguments[0]) && arguments.length >= 3)
                return ProfileCommand.run(arguments, output, error);
            if ("coverage".equals(arguments[0]) && arguments.length >= 2)
                return CoverageCommand.run(arguments, output, error);
            if ("census".equals(arguments[0]))
                return CensusCommand.run(arguments, output, error);
            if ("atlas".equals(arguments[0]))
                return AtlasCommand.run(arguments, output, error);
            if ("ui".equals(arguments[0]))
                return UiCommand.run(arguments, output, error);
            if (arguments.length >= 2 && "semantics".equals(arguments[0]))
                return SemanticsCommand.run(arguments, output, error);
            if ("mappings".equals(arguments[0]))
                return MappingCommand.run(arguments, output, error);
            return usage(error);
        } catch (IOException | ReflectiveOperationException | RuntimeException failure) {
            error.println("worldline command failed: " + failure.getMessage()); return 1;
        } catch (Exception failure) {
            error.println("worldline command failed: " + failure.getMessage()); return 1;
        }
    }

    private static int mod(String[] arguments, PrintStream output, PrintStream error)
            throws IOException {
        if (arguments.length == 3 && "inspect".equals(arguments[1]))
            return ModCommands.inspect(arguments[2], output);
        if (arguments.length == 6 && "test".equals(arguments[1]) && "record".equals(arguments[2]))
            return ModCommands.record(arguments[3], arguments[4], arguments[5], output);
        if (arguments.length == 5 && "test".equals(arguments[1]) && "diff".equals(arguments[2]))
            return ModCommands.diff(arguments[3], arguments[4], output);
        if (arguments.length == 7 && "test".equals(arguments[1]) && "run".equals(arguments[2]))
            return ModCommands.run(arguments[3], arguments[4], arguments[5], arguments[6], output);
        return usage(error);
    }

    private static int replay(String path, PrintStream output)
            throws IOException, ReflectiveOperationException {
        ReproductionBundle bundle = ReproductionBundle.parse(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get(path)));
        String type = System.getProperty("worldline.replay.provider", DEFAULT_PROVIDER);
        ReplayProvider provider = Class.forName(type).asSubclass(ReplayProvider.class)
                .getDeclaredConstructor().newInstance();
        Checks.require(provider.runtimeId().equals(bundle.runtimeId()),
                "no provider for " + bundle.runtimeId());
        ReplayReport report = replayQuietly(provider, bundle);
        Checks.require(report.runtimeId().equals(bundle.runtimeId()),
                "replay provider returned wrong runtime");
        output.println("WORLDLINE_REPLAY=PASS");
        output.println("bundle.sha256=" + bundle.sha256());
        output.println("snapshot.sha256=" + bundle.snapshot().sha256());
        output.println("runtime=" + report.runtimeId()); output.println("tick=" + report.tick());
        output.println("state=" + report.state()); return 0;
    }

    private static int show(String path, PrintStream output) throws IOException {
        output.print("WORLDLINE_TRACE_SHOW=PASS\n");
        output.print(worldline.analysis.TraceRenderer.render(Traces.read(path))); return 0;
    }

    private static int diff(String left, String right, PrintStream output) throws IOException {
        TraceDiff difference = TraceDiff.compare(Traces.read(left), Traces.read(right));
        output.print("WORLDLINE_TRACE_DIFF=" + (difference.diverged() ? "DIVERGED\n" : "EQUAL\n"));
        output.print(difference.render());
        explain(difference, output);
        return difference.diverged() ? 3 : 0;
    }

    static int usage(PrintStream error) {
        error.println("usage: worldline init [--runtime=b1.7.3] [--loader=NAME] [--template=NAME] [--host-only]");
        error.println("   or: worldline doctor [tests/worldline]");
        error.println("   or: worldline migrate [--root=PATH]");
        error.println("usage: worldline replay <bundle.wlrb>");
        error.println("   or: worldline trace show <trace.wltrace>");
        error.println("   or: worldline trace diff <left.wltrace> <right.wltrace>");
        error.println("   or: worldline trace html <left.wltrace> [right.wltrace] <output.html>");
        error.println("   or: worldline mod inspect <mod.jar>");
        error.println("   or: worldline mod test record <mod.jar> <trace.wltrace> <result.wlmtest>");
        error.println("   or: worldline mod test diff <left.wlmtest> <right.wlmtest>");
        error.println("   or: worldline mod test run <mod.jar> <seed> <ticks> <result.wlmtest>");
        error.println("   or: worldline scenario create <output.wlscenario> [step ...]");
        error.println("   or: worldline scenario inspect <scenario.wlscenario>");
        error.println("   or: worldline scenario validate <scenario.wlscenario>");
        error.println("   or: worldline scenario run <scenario.wlscenario> <seed> <trace.wltrace>");
        error.println("   or: worldline fuzz <out-dir> <seed> <cases> <max-steps> [left.jar] [right.jar]");
        error.println("   or: worldline debug <scenario.wlscenario> <seed>");
        error.println("   or: worldline profile <scenario.wlscenario> <seed> [budget.properties]");
        error.println("   or: worldline coverage <scenario.wlscenario> [trace.wltrace] [min-percent]");
        error.println("   or: worldline census <out-dir>");
        error.println("   or: worldline atlas <seed> <radius-1..4> <output.html>");
        error.println("   or: worldline ui <output.html>");
        error.println("   or: worldline semantics show");
        error.println("   or: worldline semantics graph");
        error.println("   or: worldline semantics category <name>");
        error.println("   or: worldline semantics role <ROLE>");
        error.println("   or: worldline semantics adapter [name]");
        error.println("   or: worldline semantics adapter check <repository-root>");
        error.println("   or: worldline mappings report <client.jar> <server.jar> <intermediary.jar>"
                + " <nostalgia.jar> <retromcp.properties> <retromcp.tiny>");
        error.println("   or: worldline mappings audit <client.jar> <server.jar> <intermediary.jar>"
                + " <nostalgia.jar> <retromcp.properties> <retromcp.tiny> <coverage.properties>");
        error.println("   or: worldline atlas status");
        error.println("   or: worldline atlas show <id>");
        error.println("   or: worldline atlas search <term>");
        error.println("   or: worldline atlas gaps");
        error.println("   or: worldline atlas coverage");
        error.println("   or: worldline atlas evidence <id>");
        error.println("   or: worldline atlas graph <id>");
        error.println("   or: worldline atlas export");
        error.println("   or: worldline atlas changed --since <Mn>"); return 2;
    }

    private static void explain(TraceDiff difference, PrintStream output) {
        String role = worldline.semantics.SemanticFields.role(difference.field());
        if (!role.isEmpty()) output.print("role=" + role + "\n");
        String rule = InvariantFields.rule(difference.field());
        if (!rule.isEmpty()) output.print("invariant=" + rule + "\n");
    }

    private static ReplayReport replayQuietly(ReplayProvider provider, ReproductionBundle bundle) {
        PrintStream previous = System.out;
        try {
            System.setOut(new PrintStream(new java.io.OutputStream() {
                @Override public void write(int value) { }
            }));
            return provider.replay(bundle);
        } finally { System.setOut(previous); }
    }
}
