package worldline.cli;

import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.List;
import worldline.api.SemanticMapping;
import worldline.semantics.AdapterManifest;
import worldline.semantics.SemanticCatalog;
import worldline.semantics.SemanticGraph;

/** Neutral inspection of the closed b1.7.3 semantic catalog. */
final class SemanticsCommand {
    private SemanticsCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error) {
        if (arguments.length == 2 && "show".equals(arguments[1])) return show(output);
        if (arguments.length == 2 && "graph".equals(arguments[1])) return graph(output);
        if (arguments.length == 3 && "category".equals(arguments[1]))
            return category(arguments[2], output, error);
        if (arguments.length == 3 && "role".equals(arguments[1]))
            return role(arguments[2], output, error);
        if (arguments.length == 2 && "adapter".equals(arguments[1]))
            return adapters(output, error);
        if (arguments.length == 3 && "adapter".equals(arguments[1]))
            return adapter(arguments[2], output, error);
        error.println("usage: worldline semantics show");
        error.println("   or: worldline semantics graph");
        error.println("   or: worldline semantics category <name>");
        error.println("   or: worldline semantics role <ROLE>");
        error.println("   or: worldline semantics adapter [name]");
        return 2;
    }

    private static int show(PrintStream output) {
        SemanticCatalog catalog = SemanticCatalog.standard();
        output.print("WORLDLINE_SEMANTICS=PASS\n");
        output.print(catalog.render());
        return 0;
    }

    private static int graph(PrintStream output) {
        output.print("WORLDLINE_SEMANTICS_GRAPH=PASS\n");
        output.print(SemanticGraph.of(SemanticCatalog.standard()).render());
        return 0;
    }

    private static int category(String name, PrintStream output, PrintStream error) {
        try {
            SemanticCatalog catalog = SemanticCatalog.standard();
            output.println("WORLDLINE_SEMANTICS_CATEGORY=PASS");
            output.println("category=" + name);
            for (SemanticMapping mapping : catalog.category(name)) line(output, mapping);
            return 0;
        } catch (IllegalArgumentException failure) {
            error.println("worldline command failed: " + failure.getMessage());
            return 1;
        }
    }

    private static int role(String name, PrintStream output, PrintStream error) {
        try {
            line(output, SemanticCatalog.standard().role(name));
            return 0;
        } catch (IllegalArgumentException failure) {
            error.println("worldline command failed: " + failure.getMessage());
            return 1;
        }
    }

    private static int adapters(PrintStream output, PrintStream error) {
        try {
            output.println("WORLDLINE_SEMANTICS_ADAPTER=PASS");
            for (AdapterManifest manifest : load()) {
                output.println(manifest.adapter() + "=" + manifest.sites().size());
            }
            return 0;
        } catch (Exception failure) {
            error.println("worldline command failed: " + failure.getMessage());
            return 1;
        }
    }

    private static int adapter(String name, PrintStream output, PrintStream error) {
        try {
            for (AdapterManifest manifest : load()) {
                if (!manifest.adapter().equals(name)) continue;
                output.print("WORLDLINE_SEMANTICS_ADAPTER=PASS\n");
                output.print(manifest.render());
                return 0;
            }
            error.println("worldline command failed: unknown adapter " + name);
            return 1;
        } catch (Exception failure) {
            error.println("worldline command failed: " + failure.getMessage());
            return 1;
        }
    }

    private static List<AdapterManifest> load() throws Exception {
        return AdapterManifest.loadAll(Paths.get("adapters"), SemanticCatalog.standard());
    }

    private static void line(PrintStream output, SemanticMapping mapping) {
        output.println(mapping.canonical());
    }
}
