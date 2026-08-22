package worldline.symbolgraph;

import java.nio.file.Paths;

public final class OfficialBytecodeAuditMain {
    private OfficialBytecodeAuditMain() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 3) {
            System.err.println("usage: OfficialBytecodeAuditMain <official.jar> <intermediary.jar>"
                    + " <clientOfficial|serverOfficial>");
            System.exit(2);
        }
        OfficialJarInventory official = OfficialJarInventory.read(Paths.get(arguments[0]));
        TinyMapping intermediary = MappingArchive.read(Paths.get(arguments[1]), "mappings/mappings.tiny");
        OfficialBytecodeAudit.Report report = new OfficialBytecodeAudit()
                .compare(official, intermediary, arguments[2]);
        System.out.print(report.render(arguments[2]));
        int shown = 0;
        for (OfficialSymbolKey missing : report.missingSymbols()) {
            if (shown++ == 20) break;
            System.out.println("missing.sample=" + missing.canonical());
        }
    }
}
