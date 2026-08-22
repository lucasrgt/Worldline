package worldline.symbolgraph;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class RetroMcpImportMain {
    private RetroMcpImportMain() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 5) {
            System.err.println("usage: RetroMcpImportMain <intermediary.jar> <nostalgia.jar>"
                    + " <retromcp.properties> <retromcp.tiny> <output.tsv>");
            System.exit(2);
        }
        TinyMapping inventory = MappingArchive.read(Paths.get(arguments[0]), "mappings/mappings.tiny");
        TinyMapping nostalgia = MappingArchive.read(Paths.get(arguments[1]), "mappings/mappings.tiny");
        TinyMapping retro;
        Path retroPath = Paths.get(arguments[3]);
        MappingPin.load(Paths.get(arguments[2])).verify(retroPath);
        try (Reader reader = Files.newBufferedReader(retroPath, StandardCharsets.UTF_8)) {
            retro = new TinyV2Reader().read(reader);
        }
        SymbolGraph base = new SymbolGraphBuilder().build(inventory, nostalgia);
        RetroMcpImport.Result result = new RetroMcpImport().apply(base, inventory, retro);
        Path output = Paths.get(arguments[4]).toAbsolutePath().normalize();
        Path parent = output.getParent();
        if (parent != null) Files.createDirectories(parent);
        Files.write(output, result.graph().render().getBytes(StandardCharsets.UTF_8));
        System.out.println("matched=" + result.matched());
        System.out.println("unmatched=" + result.unmatched().size());
        System.out.println("clientServerNameDifferences=" + result.nameDifferences().size());
        System.out.println("inventoryMissingRetroMcp=" + result.missing().size());
        System.out.println("symbols=" + result.graph().records().size());
        System.out.println("sha256=" + result.graph().sha256());
        int shown = 0;
        for (String unmatched : result.unmatched()) {
            if (shown++ == 20) break;
            System.out.println("unmatched.sample=" + unmatched);
        }
        for (SymbolKey missing : result.missing()) System.out.println("missing=" + missing.canonical());
    }
}
