package worldline.cli;

import java.io.PrintStream;
import worldline.semantics.SemanticCatalog;
import worldline.semantics.SemanticGraph;
import worldline.api.SemanticMapping;

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
        error.println("usage: worldline semantics show");
        error.println("   or: worldline semantics graph");
        error.println("   or: worldline semantics category <name>");
        error.println("   or: worldline semantics role <ROLE>");
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

    private static void line(PrintStream output, SemanticMapping mapping) {
        output.println(mapping.canonical());
    }
}
