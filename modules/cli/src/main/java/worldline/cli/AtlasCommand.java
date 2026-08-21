package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import worldline.analysis.AtlasRequest;
import worldline.analysis.AtlasRunner;

/** Renders a seed terrain atlas page through the official dedicated server. */
final class AtlasCommand {
    private AtlasCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error)
            throws IOException {
        if (arguments.length != 4 || !"atlas".equals(arguments[0])) {
            return WorldlineCli.usage(error);
        }
        long seed = Checks.seed(arguments[1]);
        Checks.require(arguments[2].matches("[1-4]"), "radius must be 1..4");
        int radius = Integer.parseInt(arguments[2]);
        Path serverJar = serverJar();
        Path workspace = Files.createTempDirectory("worldline-atlas-");
        AtlasRunner runner = Checks.provider("worldline.atlas.provider",
                "worldline.b173server.B173AtlasRunner", AtlasRunner.class);
        String html = runner.render(new AtlasRequest(seed, radius, serverJar, workspace));
        Files.write(Paths.get(arguments[3]), html.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        output.println("WORLDLINE_ATLAS=PASS");
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
}
