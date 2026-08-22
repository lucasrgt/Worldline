package worldline.cli;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import worldline.symbolgraph.MappingAuditHtml;
import worldline.symbolgraph.MappingCoverageGate;
import worldline.symbolgraph.MappingCoverageReport;
import worldline.symbolgraph.MappingEvidenceReport;
import worldline.symbolgraph.MappingQualificationQueue;

/** Stable complete-game mapping audit and exact coverage gate. */
final class MappingCommand {
    private MappingCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error) throws Exception {
        boolean reportOnly = arguments.length == 8 && "report".equals(arguments[1]);
        boolean gated = arguments.length == 9 && "audit".equals(arguments[1]);
        boolean queue = arguments.length == 8 && "queue".equals(arguments[1]);
        boolean evidence = arguments.length == 9 && "evidence".equals(arguments[1]);
        boolean html = arguments.length == 10 && "html".equals(arguments[1]);
        if (!"mappings".equals(arguments[0])
                || (!reportOnly && !gated && !queue && !evidence && !html)) return usage(error);
        if (html) {
            MappingQualificationQueue source = MappingQualificationQueue.create(
                    Paths.get(arguments[2]), Paths.get(arguments[3]), Paths.get(arguments[4]),
                    Paths.get(arguments[5]), Paths.get(arguments[6]), Paths.get(arguments[7]));
            MappingEvidenceReport facts = MappingEvidenceReport.create(source, Paths.get(arguments[8]));
            String document = MappingAuditHtml.render(source, facts); Path target = Paths.get(arguments[9]);
            Files.write(target, document.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            output.println("WORLDLINE_MAPPINGS_HTML=PASS");
            output.println("queue.sha256=" + source.sha256()); output.println("output=" + target);
            return 0;
        }
        if (evidence) {
            MappingQualificationQueue source = MappingQualificationQueue.create(
                    Paths.get(arguments[2]), Paths.get(arguments[3]), Paths.get(arguments[4]),
                    Paths.get(arguments[5]), Paths.get(arguments[6]), Paths.get(arguments[7]));
            MappingEvidenceReport result = MappingEvidenceReport.create(source, Paths.get(arguments[8]));
            output.println("WORLDLINE_MAPPINGS_EVIDENCE=PASS"); output.print(result.render()); return 0;
        }
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
        error.println("   or: worldline mappings evidence <client.jar> <server.jar> <intermediary.jar>"
                + " <nostalgia.jar> <retromcp.properties> <retromcp.tiny> <evidence.tsv>");
        error.println("   or: worldline mappings html <client.jar> <server.jar> <intermediary.jar>"
                + " <nostalgia.jar> <retromcp.properties> <retromcp.tiny> <evidence.tsv> <output.html>");
        return 2;
    }
}
