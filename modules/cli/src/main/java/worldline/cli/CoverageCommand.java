package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import worldline.coverage.CoverageReport;
import worldline.coverage.ScenarioCoverage;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioDsl;

/** Dynamic scenario coverage against the closed semantic catalog. */
final class CoverageCommand {
    private CoverageCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error)
            throws IOException {
        if (arguments.length < 2 || arguments.length > 4 || !"coverage".equals(arguments[0])) {
            return WorldlineCli.usage(error);
        }
        Scenario scenario = ScenarioCommands.read(arguments[1]);
        ScenarioDsl.validate(scenario);
        worldline.trace.CanonicalStateDocument trace = arguments.length >= 3
                ? Traces.read(arguments[2]) : null;
        Integer threshold = null;
        if (arguments.length == 4) {
            Checks.require(arguments[3].matches("[0-9]{1,3}"),
                    "invalid coverage threshold: " + arguments[3]);
            threshold = Integer.valueOf(arguments[3]);
        }
        ScenarioCoverage coverage = ScenarioCoverage.of(scenario, trace);
        CoverageReport report = CoverageReport.of(scenario, trace, coverage);
        output.println("WORLDLINE_COVERAGE=PASS");
        output.println("categories=" + coverage.categories());
        output.println("percent=" + coverage.percentCategories()
                + "/" + coverage.totalCategories());
        output.println("roles=" + (coverage.roles().isEmpty() ? "none" : coverage.roles()));
        output.println("report.sha256=" + report.sha256());
        Path out = Paths.get(arguments[1] + (trace == null ? "" : ".traced") + ".wlcover");        Files.write(out, report.bytes(), StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        output.println("report=" + out);
        return threshold == null || coverage.percentCategories() >= threshold ? 0 : 3;
    }
}
