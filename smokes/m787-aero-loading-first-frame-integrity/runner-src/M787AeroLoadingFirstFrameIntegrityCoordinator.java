import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Qualifies vanilla loading order and cold Cell Page visual integrity. */
public final class M787AeroLoadingFirstFrameIntegrityCoordinator {
    private static final String ID = "m787-aero-loading-first-frame-integrity";
    private static final String[] RUNS = {"replica1-paired", "replica2-paired"};
    private static final String TRACE = "v2|scene=576-static-four-towers-four-chunks|"
        + "jvms=2-fresh-paired-replicas|loading=restored-world-one-title+building+simulating|"
        + "prebake=zero-render-world-during-loading|warm=none|frames=240|"
        + "captures=40-including-first16-consecutive+orbit+traverse+spin+teleport+close|"
        + "contrast=in-context-cell-pages-then-clear-direct-budget8+flatten-off|"
        + "camera=explicit-projection-modelview-at-capture|submission=576-controlled-every-frame|"
        + "hot=576-template-calls+zero-direct|"
        + "oracle=paired-full-rgba-exact+no-blank+cold-direct-fallback+page-convergence";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/build/smoke").resolve(ID);
    private final Properties config = new Properties();

    public void execute(String id) throws Exception {
        SmokeSupport.require(ID.equals(id), "M787 coordinator identity drift");
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M787Runtime runtime = new M787Runtime(smoke, config, aero);
        runtime.verifyCheckout();
        SmokeSupport.recreate(root, build);
        Path template = build.resolve("template");
        runtime.runClient(template, true, "prepare");
        Path sourceWorld = template.resolve("saves/WorldlineAeroColdEntry");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M787 template world absent");
        List<M787Artifact> artifacts = new ArrayList<>();
        for (String run : RUNS) {
            Path game = build.resolve(run);
            M787Runtime.copyWorld(sourceWorld, game.resolve("saves/WorldlineAeroColdEntry"));
            runtime.runClient(game, false, "paired");
            M787Artifact artifact = M787Artifact.read(game, "paired");
            artifact.verify();
            artifacts.add(artifact);
            if (artifacts.size() == 1) {
                M787VisualOracle firstPair = M787VisualOracle.evaluateFirstPair(artifacts);
                SmokeSupport.require(firstPair.passes(40),
                    "M787 first-pair visual gate failed: " + firstPair.summary());
            }
        }
        M787VisualOracle oracle = M787VisualOracle.evaluate(artifacts);
        SmokeSupport.require(oracle.passes(), "M787 visual gate failed: " + oracle.summary());
        String signature = M787Runtime.sha256(TRACE);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
            "M787 semantic signature drift: " + signature);
        Files.writeString(build.resolve("evidence.txt"), evidence(artifacts, oracle, signature),
            StandardCharsets.UTF_8);
        System.out.println("M787 Aero loading and first-frame integrity passed");
        System.out.println("WORLDLINE_M787_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M787_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M787_SIGNATURE=" + signature);
    }

    private String evidence(List<M787Artifact> artifacts, M787VisualOracle oracle,
            String signature) {
        StringBuilder result = new StringBuilder("id=").append(ID).append('\n');
        for (int index = 0; index < artifacts.size(); index++) {
            result.append("run.").append(index + 1).append('=')
                .append(artifacts.get(index).summary()).append('\n');
        }
        return result.append(oracle.summary()).append("\ntrace=").append(TRACE)
            .append("\nsignature=").append(signature).append('\n').toString();
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "sessions").equals("2")
            && SmokeSupport.value(config, "frames").equals("240")
            && SmokeSupport.value(config, "captures").equals("40")
            && SmokeSupport.value(config, "consecutive.cold.captures").equals("16")
            && SmokeSupport.value(config, "machines").equals("576")
            && SmokeSupport.value(config, "maximum.changed.pixels").equals("0"),
            "M787 acquisition design drift");
    }
}
