package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import worldline.minimization.Scenario;
import worldline.profiling.ProfileBudget;
import worldline.profiling.ProfileReport;
import worldline.profiling.ProfiledRunner;
import worldline.profiling.TickProfiledRun;

/** Per-tick wall-clock profiling with optional machine-relative budget gate. */
final class ProfileCommand {
    private ProfileCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error)
            throws IOException {
        if (arguments.length < 3 || arguments.length > 4 || !"profile".equals(arguments[0])) {
            return WorldlineCli.usage(error);
        }
        Scenario scenario = ScenarioCommands.read(arguments[1]);
        long seed = Checks.seed(arguments[2]);
        ProfileBudget budget = arguments.length == 4
                ? ProfileBudget.parse(Paths.get(arguments[3])) : null;
        ProfiledRunner runner = Checks.provider("worldline.profile.provider",
                "worldline.b173.B173ProfiledRunner", ProfiledRunner.class);
        TickProfiledRun run = runner.profile(scenario, seed);
        ProfileReport report = ProfileReport.of(scenario, seed, run);
        printSummary(output, run, report);
        if (budget == null) return 0;
        List<String> violations = budget.violations(run.profile());
        if (violations.isEmpty()) {
            output.println("WORLDLINE_PROFILE_BUDGET=PASS"); return 0;
        }
        output.println("WORLDLINE_PROFILE_BUDGET=EXCEEDED");
        for (String violation : violations) output.println("violation=" + violation);
        return 3;
    }

    private static void printSummary(PrintStream output, TickProfiledRun run,
            ProfileReport report) {
        output.println("WORLDLINE_PROFILE=PASS");
        output.println("ticks=" + run.profile().ticks());
        output.println("trace.sha256=" + run.trace().signature());
        output.println("tick.mean.nanos=" + run.profile().mean());
        output.println("tick.median.nanos=" + run.profile().median());
        output.println("tick.p95.nanos=" + run.profile().p95());
        output.println("mod.share.percent=" + run.profile().modSharePercent());
        output.println("report.sha256=" + report.sha256());
    }
}
