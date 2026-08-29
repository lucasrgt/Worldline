package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Properties;
import worldline.analysis.AtlasRequest;
import worldline.analysis.AtlasRunner;
import worldline.atlas.AtlasDelta;
import worldline.atlas.AtlasGaps;
import worldline.atlas.AtlasGraph;
import worldline.atlas.AtlasContext;
import worldline.atlas.AtlasContextQuery;
import worldline.atlas.AtlasHit;
import worldline.atlas.AtlasIndex;
import worldline.atlas.AtlasQuery;
import worldline.atlas.AtlasRecord;
import worldline.atlas.AtlasStore;

/** Generated Atlas inspection. Validation does not launch Minecraft. */
final class AtlasCommand {
    private AtlasCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error) throws IOException {
        if (arguments.length == 4 && arguments[1].matches("-?[0-9]+"))
            return renderSeed(arguments, output);
        if (arguments.length == 2 && "status".equals(arguments[1])) return status(output, error);
        if (arguments.length == 3 && "show".equals(arguments[1]))
            return show(arguments[2], output, error);
        if (arguments.length == 3 && "search".equals(arguments[1]))
            return search(arguments[2], output, error);
        if (arguments.length == 3 && "index".equals(arguments[1]))
            return index(arguments[2], output, error);
        if (arguments.length >= 3 && "context".equals(arguments[1]))
            return context(arguments, output, error);
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
        error.println("   or: worldline atlas index <query>");
        error.println("   or: worldline atlas context <query> [--format=json] [--budget=N] [--depth=N]");
        error.println("   or: worldline atlas gaps");
        error.println("   or: worldline atlas coverage");
        error.println("   or: worldline atlas evidence <id>");
        error.println("   or: worldline atlas graph <id>");
        error.println("   or: worldline atlas export");
        error.println("   or: worldline atlas changed --since <Mn>");
        error.println("   or: worldline atlas <seed> <radius-1..4> <output.html>");
        return 2;
    }

    private static int renderSeed(String[] arguments, PrintStream output) throws IOException {
        long seed = Checks.seed(arguments[1]);
        Checks.require(arguments[2].matches("[1-4]"), "radius must be 1..4");
        int radius = Integer.parseInt(arguments[2]);
        Path workspace = Files.createTempDirectory("worldline-seed-atlas-");
        AtlasRunner runner = Checks.provider("worldline.atlas.provider",
                "worldline.b173server.B173AtlasRunner", AtlasRunner.class);
        String html = runner.render(new AtlasRequest(seed, radius, serverJar(), workspace));
        Files.write(Paths.get(arguments[3]), html.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        output.println("WORLDLINE_SEED_ATLAS=PASS");
        output.println("seed=" + seed);
        output.println("radius=" + radius);
        output.println("page.sha256=" + Checks.sha256(html.getBytes(StandardCharsets.UTF_8)));
        return 0;
    }

    private static Path serverJar() throws IOException {
        Properties artifact = new Properties();
        Path properties = Paths.get("artifacts", "minecraft-b1.7.3-server.properties");
        try (java.io.Reader reader = Files.newBufferedReader(properties)) {
            artifact.load(reader);
        }
        String relative = artifact.getProperty("local.path");
        Checks.require(relative != null && !relative.trim().isEmpty(),
                "server artifact local.path missing");
        return Paths.get(relative.trim());
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

    private static int index(String query, PrintStream output, PrintStream error) {
        try {
            output.print("WORLDLINE_ATLAS_INDEX=PASS\n");
            output.print(AtlasContextQuery.index(AtlasIndex.search(load(), query, 50)));
            return 0;
        } catch (RuntimeException failure) {
            return fail(error, failure);
        }
    }

    private static int context(String[] arguments, PrintStream output, PrintStream error) {
        try {
            int budget = 64, depth = 1; boolean json = false;
            for (int index = 3; index < arguments.length; index++) {
                String option = arguments[index];
                if ("--json".equals(option) || "--format=json".equals(option)) json = true;
                else if (option.startsWith("--budget=")) budget = number(option, "--budget=");
                else if (option.startsWith("--depth=")) depth = number(option, "--depth=");
                else throw new IllegalArgumentException("unknown atlas context option " + option);
            }
            List<AtlasHit> hits = AtlasContext.build(load(), arguments[2], budget, depth);
            output.print("WORLDLINE_ATLAS_CONTEXT=PASS\n");
            output.print(json ? AtlasContextQuery.json(arguments[2], hits)
                    : AtlasContextQuery.text(arguments[2], hits));
            return 0;
        } catch (RuntimeException failure) {
            return fail(error, failure);
        }
    }

    private static int number(String option, String prefix) {
        try { return Integer.parseInt(option.substring(prefix.length())); }
        catch (NumberFormatException failure) {
            throw new IllegalArgumentException("invalid " + prefix.substring(2, prefix.length() - 1));
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
