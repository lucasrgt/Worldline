package worldline.symbolgraph;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SymbolGraphMain {
    private SymbolGraphMain() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length < 2 || arguments.length > 3) {
            System.err.println("usage: SymbolGraphMain <intermediary.jar> <nostalgia.jar> [output.tsv]");
            System.exit(2);
        }
        TinyMapping inventory = MappingArchive.read(Paths.get(arguments[0]), "mappings/mappings.tiny");
        TinyMapping nostalgia = MappingArchive.read(Paths.get(arguments[1]), "mappings/mappings.tiny");
        SymbolGraph graph = new SymbolGraphBuilder().build(inventory, nostalgia);
        String rendered = graph.render();
        if (arguments.length == 3) {
            Path output = Paths.get(arguments[2]).toAbsolutePath().normalize();
            Path parent = output.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.write(output, rendered.getBytes(StandardCharsets.UTF_8));
        } else {
            System.out.print(rendered);
        }
        System.err.println("symbols=" + graph.records().size() + " sha256=" + graph.sha256()
                + " sides=" + graph.sideCounts());
    }
}
