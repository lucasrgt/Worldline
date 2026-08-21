package worldline.cli;

import java.io.PrintStream;
import java.nio.file.Paths;
import worldline.atlas.AtlasDelta;
import worldline.atlas.AtlasGaps;
import worldline.atlas.AtlasGraph;
import worldline.atlas.AtlasQuery;
import worldline.atlas.AtlasRecord;
import worldline.atlas.AtlasStore;

/** Generated Atlas inspection. Validation does not launch Minecraft. */
final class AtlasCommand {
    private AtlasCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error) {
        if (arguments.length == 2 && "status".equals(arguments[1])) return status(output, error);
        if (arguments.length == 3 && "show".equals(arguments[1]))
            return show(arguments[2], output, error);
        if (arguments.length == 3 && "search".equals(arguments[1]))
            return search(arguments[2], output, error);
        if (arguments.length == 2 && "gaps".equals(arguments[1])) return gaps(output, error);
        if (arguments.length == 2 && "coverage".equals(arguments[1]))
            return coverage(output, error);
        if (arguments.length == 3 && "evidence".equals(arguments[1]))
            return evidence(arguments[2], output, error);
        if (arguments.length == 3 && "graph".equals(arguments[1]))
            return graph(arguments[2], output, error);
        if (arguments.length == 2 && "export".equals(arguments[1])) return export(output, error);
        if (arguments.length == 4 && "changed".equals(arguments[1]) && "--since".equals(arguments[2]))
            return changed(arguments[3], output, error);
        error.println("usage: worldline atlas status");
        error.println("   or: worldline atlas show <id>");
        error.println("   or: worldline atlas search <term>");
        error.println("   or: worldline atlas gaps");
        error.println("   or: worldline atlas coverage");
        error.println("   or: worldline atlas evidence <id>");
        error.println("   or: worldline atlas graph <id>");
        error.println("   or: worldline atlas export");
        error.println("   or: worldline atlas changed --since <Mn>");
        return 2;
    }

    private static int status(PrintStream output, PrintStream error) {
        try {
            AtlasStore store = load();
            output.print("WORLDLINE_ATLAS=PASS\n");
            output.print(AtlasQuery.status(store));
            return 0;
        } catch (RuntimeException failure) {
            return fail(error, failure);
        }
    }

    private static int show(String id, PrintStream output, PrintStream error) {
        try {
            output.print("WORLDLINE_ATLAS_SHOW=PASS\n");
            output.print(load().get(id).canonical());
            return 0;
        } catch (RuntimeException failure) {
            return fail(error, failure);
        }
    }

    private static int search(String term, PrintStream output, PrintStream error) {
        try {
            output.print("WORLDLINE_ATLAS_SEARCH=PASS\n");
            output.print(AtlasQuery.search(load().search(term)));
            return 0;
        } catch (RuntimeException failure) {
            return fail(error, failure);
        }
    }

    private static int gaps(PrintStream output, PrintStream error) {
        try {
            output.print("WORLDLINE_ATLAS_GAPS=PASS\n");
            output.print(AtlasQuery.gaps(AtlasGaps.list(load())));
            return 0;
        } catch (RuntimeException failure) {
            return fail(error, failure);
        }
    }

    private static int coverage(PrintStream output, PrintStream error) {
        try {
            output.print("WORLDLINE_ATLAS_COVERAGE=PASS\n");
            output.print(AtlasQuery.coverage(load()));
            return 0;
        } catch (RuntimeException failure) {
            return fail(error, failure);
        }
    }

    private static int evidence(String id, PrintStream output, PrintStream error) {
        try {
            AtlasRecord record = load().get(id);
            output.print("WORLDLINE_ATLAS_EVIDENCE=PASS\n");
            output.print(AtlasQuery.evidence(record));
            return 0;
        } catch (RuntimeException failure) {
            return fail(error, failure);
        }
    }

    private static int graph(String id, PrintStream output, PrintStream error) {
        try {
            output.print("WORLDLINE_ATLAS_GRAPH=PASS\n");
            output.print(AtlasGraph.render(load(), id));
            return 0;
        } catch (RuntimeException failure) {
            return fail(error, failure);
        }
    }

    private static int export(PrintStream output, PrintStream error) {
        try {
            AtlasStore store = load();
            output.print("WORLDLINE_ATLAS_EXPORT=PASS\n");
            output.print("sha256=" + store.sha256() + "\n");
            output.print(store.canonical());
            return 0;
        } catch (RuntimeException failure) {
            return fail(error, failure);
        }
    }

    private static int changed(String since, PrintStream output, PrintStream error) {
        try {
            output.print("WORLDLINE_ATLAS_CHANGED=PASS\n");
            output.print(AtlasDelta.since(load(), since));
            return 0;
        } catch (RuntimeException failure) {
            return fail(error, failure);
        }
    }

    private static AtlasStore load() {
        return AtlasStore.standard(Paths.get("."));
    }

    private static int fail(PrintStream error, RuntimeException failure) {
        error.println("worldline command failed: " + failure.getMessage());
        return 1;
    }
}
