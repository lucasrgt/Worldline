package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import worldline.fuzz.DifferentialFuzzer;
import worldline.fuzz.FuzzPlan;
import worldline.fuzz.FuzzReport;
import worldline.fuzz.FuzzSubjectProvider;

/** Differential fuzzing command over public-grammar scenarios. */
final class FuzzCommand {
    private FuzzCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error)
            throws IOException {
        if (arguments.length < 5 || arguments.length > 7) return WorldlineCli.usage(error);
        Path outDir = Paths.get(arguments[1]);
        long seed = Checks.seed(arguments[2]);
        int cases = positive(arguments[3], 4096);
        int steps = positive(arguments[4], worldline.minimization.Scenario.MAX_STEPS);
        List<Path> jars = new java.util.ArrayList<>();
        for (int index = 5; index < arguments.length; index++) {
            jars.add(Paths.get(arguments[index]));
        }
        FuzzSubjectProvider provider = Checks.provider("worldline.fuzz.provider",
                "worldline.b173.B173FuzzSubjects", FuzzSubjectProvider.class);
        List<worldline.fuzz.FuzzSubject> subjects = provider.subjects(jars);
        List<String> labels = new java.util.ArrayList<>();
        for (worldline.fuzz.FuzzSubject subject : subjects) labels.add(subject.label());
        FuzzPlan plan = FuzzPlan.generate(seed, cases, steps);
        DifferentialFuzzer.Result result = DifferentialFuzzer.fuzz(subjects, plan,
                seed, true, 200);
        Files.createDirectories(outDir);
        FuzzReport report = FuzzReport.of(seed, plan.size(), steps, labels, result);
        Path reportPath = outDir.resolve("fuzz-report.txt");
        Files.write(reportPath, report.bytes(), java.nio.file.StandardOpenOption.CREATE_NEW,
                java.nio.file.StandardOpenOption.WRITE);
        for (int index = 0; index < result.findings().size(); index++) {
            writeFinding(outDir, index, result.findings().get(index));
        }
        report.print(output);
        output.println("report=" + reportPath);
        return result.findings().isEmpty() ? 0 : 3;
    }

    private static void writeFinding(Path outDir, int index,
            worldline.fuzz.FuzzFinding finding) throws IOException {
        worldline.minimization.Scenario scenario = finding.minimized() != null
                ? finding.minimized() : finding.original();
        Path path = outDir.resolve("finding-" + index + ".wlscenario");
        Files.write(path, scenario.bytes(), java.nio.file.StandardOpenOption.CREATE_NEW,
                java.nio.file.StandardOpenOption.WRITE);
    }

    private static int positive(String text, int limit) {
        Checks.require(text != null && text.matches("[1-9][0-9]{0,3}")
                && Integer.parseInt(text) <= limit, "invalid numeric argument: " + text);
        return Integer.parseInt(text);
    }
}
