package worldline.cli;

import java.io.PrintStream;
import java.nio.file.Paths;
import worldline.symbolgraph.MappingCoverageGate;
import worldline.symbolgraph.MappingCoverageReport;
import worldline.symbolgraph.MappingQualificationQueue;

/** Stable complete-game mapping audit and exact coverage gate. */
final class MappingCommand {
    private MappingCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error) throws Exception {
        boolean reportOnly = arguments.length == 8 && "report".equals(arguments[1]);
        boolean gated = arguments.length == 9 && "audit".equals(arguments[1]);
        boolean queue = arguments.length == 8 && "queue".equals(arguments[1]);
        if (!"mappings".equals(arguments[0]) || (!reportOnly && !gated && !queue)) return usage(error);
        if (queue) {
            MappingQualificationQueue result = MappingQualificationQueue.create(
                    Paths.get(arguments[2]), Paths.get(arguments[3]), Paths.get(arguments[4]),
                    Paths.get(arguments[5]), Paths.get(arguments[6]), Paths.get(arguments[7]));
            output.println("WORLDLINE_MAPPINGS_QUEUE=PASS");
            output.print(result.render());
            return 0;
        }
        MappingCoverageReport report = MappingCoverageReport.create(
                Paths.get(arguments[2]), Paths.get(arguments[3]), Paths.get(arguments[4]),
                Paths.get(arguments[5]), Paths.get(arguments[6]), Paths.get(arguments[7]));
        if (gated) MappingCoverageGate.verify(report, Paths.get(arguments[8]));
        output.println(gated ? "WORLDLINE_MAPPINGS_AUDIT=PASS" : "WORLDLINE_MAPPINGS_REPORT=PASS");
        output.print(report.render());
        return 0;
    }

    private static int usage(PrintStream error) {
        error.println("usage: worldline mappings report <client.jar> <server.jar> <intermediary.jar>"
                + " <nostalgia.jar> <retromcp.properties> <retromcp.tiny>");
        error.println("   or: worldline mappings queue <client.jar> <server.jar> <intermediary.jar>"
                + " <nostalgia.jar> <retromcp.properties> <retromcp.tiny>");
        error.println("   or: worldline mappings audit <client.jar> <server.jar> <intermediary.jar>"
                + " <nostalgia.jar> <retromcp.properties> <retromcp.tiny> <coverage.properties>");
        return 2;
    }
}
