package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import worldline.mods.ModArtifact;
import worldline.mods.ModDependency;
import worldline.mods.ModLoader;
import worldline.modtest.ModTestComparison;
import worldline.modtest.ModTestResult;
import worldline.modtest.ModTestRunner;
import worldline.trace.CanonicalStateDocument;

/** Mod package inspection, attested runs, recording, and comparison commands. */
final class ModCommands {
    private static final String MOD_RUNTIME = "b1.7.3", MOD_API = "1";

    private ModCommands() {}

    static int inspect(String path, PrintStream output) throws IOException {
        ModArtifact artifact = ModLoader.inspect(Paths.get(path), MOD_RUNTIME, MOD_API);
        output.println("WORLDLINE_MOD_INSPECT=" + (artifact.compatible() ? "PASS" : "INCOMPATIBLE"));
        output.println("id=" + artifact.descriptor().id());
        output.println("version=" + artifact.descriptor().version());
        output.println("entrypoint=" + artifact.descriptor().entrypoint());
        output.println("runtime=" + artifact.descriptor().runtime());
        output.println("worldline.api=" + artifact.descriptor().worldlineApi());
        StringBuilder requires = new StringBuilder();
        for (ModDependency dependency : artifact.descriptor().requires()) {
            if (requires.length() > 0) requires.append(',');
            requires.append(dependency);
        }
        output.println("requires=" + requires);
        output.println("artifact.sha256=" + artifact.sha256());
        output.println("compatibility=" + artifact.compatibility());
        return artifact.compatible() ? 0 : 3;
    }

    static int record(String modPath, String tracePath, String resultPath, PrintStream output)
            throws IOException {
        ModArtifact artifact = ModLoader.inspect(Paths.get(modPath), MOD_RUNTIME, MOD_API);
        Checks.require(artifact.compatible(), "cannot record an incompatible mod");
        ModTestResult result = ModTestResult.create(artifact,
                Traces.read(tracePath));
        Files.write(Paths.get(resultPath), result.bytes(), StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        output.println("WORLDLINE_MOD_TEST_RECORD=PASS");
        output.println("mod=" + result.modId() + "@" + result.modVersion());
        output.println("artifact.sha256=" + result.artifactSha256());
        output.println("trace.sha256=" + result.trace().signature());
        output.println("result.sha256=" + result.sha256()); return 0;
    }

    static int run(String modPath, String seedText, String ticksText, String resultPath,
            PrintStream output) throws IOException {
        long seed = Checks.seed(seedText);
        int ticks = Checks.ticks(ticksText);
        ModTestRunner runner = Checks.provider("worldline.modtest.provider",
                "worldline.b173.B173ModTestRunner", ModTestRunner.class);
        ModTestResult result = runner.run(Paths.get(modPath), seed, ticks);
        Checks.require(result.executed(), "runner returned an unattested result");
        Files.write(Paths.get(resultPath), result.bytes(), StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        output.println("WORLDLINE_MOD_TEST_RUN=PASS");
        output.println("mod=" + result.modId() + "@" + result.modVersion());
        output.println("artifact.sha256=" + result.artifactSha256());
        output.println("execution=" + ModTestResult.EXECUTION);
        output.println("seed=" + result.seed());
        output.println("ticks=" + result.ticks());
        output.println("trace.sha256=" + result.trace().signature());
        output.println("result.sha256=" + result.sha256()); return 0;
    }

    static int diff(String leftPath, String rightPath, PrintStream output) throws IOException {
        ModTestComparison comparison = ModTestComparison.compare(
                read(leftPath), read(rightPath));
        output.println("WORLDLINE_MOD_TEST_DIFF="
                + (comparison.behaviorDiverged() ? "DIVERGED" : "EQUAL"));
        output.print(comparison.render());
        explain(comparison.traceDiff(), output);
        return comparison.behaviorDiverged() ? 3 : 0;
    }

    private static void explain(worldline.analysis.TraceDiff difference, PrintStream output) {
        String role = worldline.semantics.SemanticFields.role(difference.field());
        if (!role.isEmpty()) output.print("role=" + role + "\n");
        String rule = worldline.invariants.InvariantFields.rule(difference.field());
        if (!rule.isEmpty()) output.print("invariant=" + rule + "\n");
    }

    static ModTestResult read(String path) throws IOException {
        long size = Files.size(Paths.get(path));
        Checks.require(size > 0 && size <= ModTestResult.MAX_BYTES, "invalid mod test result size");
        return ModTestResult.parse(Files.readAllBytes(Paths.get(path)));
    }
}
