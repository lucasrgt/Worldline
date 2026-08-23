package worldline.symbolgraph;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/** Runtime entry point for the cumulative SEM-M11 through SEM-M13 mapping gates. */
public final class MappingBatchMain {
    private MappingBatchMain() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 11) {
            System.err.println("usage: MappingBatchMain <client.jar> <server.jar> <intermediary.jar>"
                    + " <nostalgia.jar> <feather.jar> <retromcp.properties> <retromcp.tiny>"
                    + " <touch-root> <m11.properties> <m12.properties> <m13.properties>");
            System.exit(2);
        }
        Path client = Paths.get(arguments[0]), server = Paths.get(arguments[1]);
        Path intermediaryPath = Paths.get(arguments[2]), nostalgiaPath = Paths.get(arguments[3]);
        Path featherPath = Paths.get(arguments[4]), retroPin = Paths.get(arguments[5]);
        Path retroPath = Paths.get(arguments[6]), touchRoot = Paths.get(arguments[7]);
        MappingPin.load(retroPin).verify(retroPath);
        TinyMapping intermediary = MappingArchive.read(intermediaryPath, "mappings/mappings.tiny");
        TinyMapping nostalgia = MappingArchive.read(nostalgiaPath, "mappings/mappings.tiny");
        TinyMapping feather = MappingArchive.read(featherPath, "mappings/mappings.tiny");
        TinyMapping retro;
        try (Reader reader = Files.newBufferedReader(retroPath, StandardCharsets.UTF_8)) {
            retro = new TinyV2Reader().read(reader);
        }
        MappingCoverageReport coverage = MappingCoverageReport.create(client, server,
                intermediaryPath, nostalgiaPath, retroPin, retroPath);
        SymbolGraph base = new SymbolGraphBuilder().build(intermediary, nostalgia);
        SymbolGraph graph = new RetroMcpImport().apply(base, intermediary, retro).graph();
        Set<String> touches = MappingTouchIndex.read(touchRoot);
        int[] targets = {25, 50, 100};
        for (int index = 0; index < targets.length; index++) {
            MappingBatchReport report = MappingBatchReport.create(
                    coverage, intermediary, nostalgia, feather, graph, touches, targets[index]);
            MappingBatchGate.verify(report, Paths.get(arguments[index + 8]));
            System.out.println("  SEM-M" + (index + 11) + " mapping batch: "
                    + report.metric("selected.total") + "/" + report.metric("qualified.total")
                    + " qualified; report " + report.sha256());
        }
    }
}
