import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/** Classifies ordered Cell Page replay against exact direct rendering. */
public final class M788AeroOrderedTemplatePerformanceCoordinator {
    private static final String ID = "m788-aero-ordered-template-performance";
    private static final String[] RUNS = {"round1-direct", "round1-template",
        "round2-template", "round2-direct", "round3-template", "round3-direct",
        "round4-direct", "round4-template"};
    private static final String[] ARMS = {"direct", "template", "template", "direct",
        "template", "direct", "direct", "template"};
    private static final String TRACE = "v1|scene=576-static-four-towers-four-chunks+fixed-membership-brightness|"
        + "jvms=8-fresh-four-counterbalanced-pairs|route=1200-min20s-orbit+traverse+spin+teleport|"
        + "warm=480-route-frames+arm-hot+template-pages-stable|render=direct-vs-ordered-template|"
        + "cell-pages=min1+flatten-off+ttl100000+budget8+cap-unbounded|"
        + "dispatch=native-fixture-render-suppressed|"
        + "submission=576-direct-draws-vs-576-queue+one-controlled-flush-per-frame|"
        + "captures=24-full-rgba-isolated-fixture+blank-retry<=24|"
        + "metrics=wall+p50+p95+p99+fps+allocation+heap+isolated-render+pages+display-lists+hitches|"
        + "oracle=exact-cross-arm-rgba+same-arm-repeatability+hot-template+guardrails+"
        + "both-order-strata+frame-wins>=3+fps>=1.03+p99<=1.05+alloc<=1.05+render<=0.90+"
        + "hitch<=5000ppm|decision=benefit-confirmed-or-mixed-tradeoff";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/build/smoke").resolve(ID);
    private final Properties config = new Properties();

    public void execute(String id) throws Exception {
        SmokeSupport.require(ID.equals(id), "M788 coordinator identity drift");
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M788Runtime runtime = new M788Runtime(smoke, config, aero);
        runtime.verifyCheckout();
        SmokeSupport.recreate(root, build);
        runtime.buildAero();
        Path template = build.resolve("template");
        runtime.runClient(template, true, "prepare");
        Path sourceWorld = template.resolve("saves/WorldlineAeroOrderedTemplate");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M788 template world absent");
        List<M788Artifact> artifacts = new ArrayList<M788Artifact>();
        for (int index = 0; index < RUNS.length; index++) {
            Path game = build.resolve(RUNS[index]);
            M788Runtime.copyWorld(sourceWorld,
                game.resolve("saves/WorldlineAeroOrderedTemplate"));
            runtime.runClient(game, false, ARMS[index]);
            M788Artifact artifact = M788Artifact.read(game, ARMS[index]);
            artifact.verify();
            artifacts.add(artifact);
        }
        List<M788Pair> pairs = List.of(
            new M788Pair(1, true, artifacts.get(0), artifacts.get(1)),
            new M788Pair(2, false, artifacts.get(3), artifacts.get(2)),
            new M788Pair(3, false, artifacts.get(5), artifacts.get(4)),
            new M788Pair(4, true, artifacts.get(6), artifacts.get(7)));
        List<M788Visual> visuals = List.of(
            M788Visual.compare("round.1", artifacts.get(0), artifacts.get(1)),
            M788Visual.compare("round.2", artifacts.get(3), artifacts.get(2)),
            M788Visual.compare("round.3", artifacts.get(5), artifacts.get(4)),
            M788Visual.compare("round.4", artifacts.get(6), artifacts.get(7)),
            M788Visual.compare("repeat.direct", artifacts.get(0), artifacts.get(6)),
            M788Visual.compare("repeat.template", artifacts.get(1), artifacts.get(7)));
        M788Result result = M788Gate.evaluate(
            SmokeSupport.product(root, "profiling"), pairs, visuals, config);
        SmokeSupport.require(result.integrityPass(),
            "M788 correctness/guardrail gate failed: " + result.summary());
        String signature = M788Runtime.sha256(TRACE);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
            "M788 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M788Pair pair : pairs) evidence.append(pair.summary()).append('\n');
        for (M788Visual visual : visuals) evidence.append(visual.summary()).append('\n');
        evidence.append(result.summary()).append("\ntrace=").append(TRACE)
            .append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M788 Aero ordered-template performance classification passed");
        System.out.println("WORLDLINE_M788_DECISION=" + result.decision());
        System.out.println("WORLDLINE_M788_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M788_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M788_SIGNATURE=" + signature);
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "sessions").equals("8")
            && SmokeSupport.value(config, "retained.frames").equals("1200")
            && SmokeSupport.value(config, "minimum.millis").equals("20000")
            && SmokeSupport.value(config, "warm.frames").equals("480")
            && SmokeSupport.value(config, "checkpoints").equals("24")
            && SmokeSupport.value(config, "machines").equals("576")
            && SmokeSupport.value(config, "maximum.workload.drift.ratio").equals("0.02")
            && SmokeSupport.value(config, "maximum.blank.capture.rejections").equals("24")
            && SmokeSupport.value(config, "minimum.frame.winning.pairs").equals("3"),
            "M788 acquisition design drift");
    }
}
