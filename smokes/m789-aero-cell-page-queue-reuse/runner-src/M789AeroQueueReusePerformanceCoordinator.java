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

/** Classifies bounded transient Cell Page queue reuse. */
public final class M789AeroQueueReusePerformanceCoordinator {
    private static final String ID = "m789-aero-cell-page-queue-reuse";
    private static final String[] RUNS = {"round1-baseline", "round1-reuse",
        "round2-reuse", "round2-baseline", "round3-reuse", "round3-baseline",
        "round4-baseline", "round4-reuse"};
    private static final String[] ARMS = {"baseline", "reuse", "reuse", "baseline",
        "reuse", "baseline", "baseline", "reuse"};
    private static final String TRACE = "v1|scene=576-static-four-towers-four-chunks+fixed-membership-brightness|"
        + "jvms=8-fresh-four-counterbalanced-pairs|route=1200-min20s-orbit+traverse+spin+teleport|"
        + "warm=480-route-frames+both-arms-hot+template-pages-stable|"
        + "render=ordered-template-baseline-vs-bounded-queue-reuse|"
        + "cell-pages=min1+flatten-off+ttl100000+budget8+cap-unbounded|pool=256-pages+256-instances|"
        + "dispatch=native-fixture-render-suppressed|"
        + "submission=576-queue+one-controlled-flush-per-frame-both-arms|"
        + "captures=24-full-rgba-isolated-fixture+blank-retry<=24|"
        + "metrics=wall+p50+p95+p99+fps+allocation+heap+isolated-render+pages+pool+display-lists+hitches|"
        + "oracle=exact-cross-arm-rgba+same-arm-repeatability+hot-template+bounded-owner-free-pool+"
        + "both-order-strata+allocation-wins>=3+fps>=0.97+p99<=1.05+alloc<=0.90+"
        + "queue-alloc<=0.05+render<=1.05+"
        + "hitch<=5000ppm|decision=benefit-confirmed-or-mixed-tradeoff";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/build/smoke").resolve(ID);
    private final Properties config = new Properties();

    public void execute(String id) throws Exception {
        SmokeSupport.require(ID.equals(id), "M789 coordinator identity drift");
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M789Runtime runtime = new M789Runtime(smoke, config, aero);
        runtime.verifyCheckout();
        SmokeSupport.recreate(root, build);
        runtime.buildAero();
        Path seed = build.resolve("seed");
        runtime.runClient(seed, true, "prepare");
        Path sourceWorld = seed.resolve("saves/WorldlineAeroQueueReuse");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M789 fixture world absent");
        List<M789Artifact> artifacts = new ArrayList<M789Artifact>();
        for (int index = 0; index < RUNS.length; index++) {
            Path game = build.resolve(RUNS[index]);
            M789Runtime.copyWorld(sourceWorld,
                game.resolve("saves/WorldlineAeroQueueReuse"));
            runtime.runClient(game, false, ARMS[index]);
            M789Artifact artifact = M789Artifact.read(game, ARMS[index]);
            artifact.verify();
            artifacts.add(artifact);
        }
        List<M789Pair> pairs = List.of(
            new M789Pair(1, true, artifacts.get(0), artifacts.get(1)),
            new M789Pair(2, false, artifacts.get(3), artifacts.get(2)),
            new M789Pair(3, false, artifacts.get(5), artifacts.get(4)),
            new M789Pair(4, true, artifacts.get(6), artifacts.get(7)));
        List<M789Visual> visuals = List.of(
            M789Visual.compare("round.1", artifacts.get(0), artifacts.get(1)),
            M789Visual.compare("round.2", artifacts.get(3), artifacts.get(2)),
            M789Visual.compare("round.3", artifacts.get(5), artifacts.get(4)),
            M789Visual.compare("round.4", artifacts.get(6), artifacts.get(7)),
            M789Visual.compare("repeat.baseline", artifacts.get(0), artifacts.get(6)),
            M789Visual.compare("repeat.reuse", artifacts.get(1), artifacts.get(7)));
        M789Result result = M789Gate.evaluate(
            SmokeSupport.product(root, "profiling"), pairs, visuals, config);
        SmokeSupport.require(result.integrityPass(),
            "M789 correctness/guardrail gate failed: " + result.summary());
        String signature = M789Runtime.sha256(TRACE);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
            "M789 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M789Pair pair : pairs) evidence.append(pair.summary()).append('\n');
        for (M789Visual visual : visuals) evidence.append(visual.summary()).append('\n');
        evidence.append(result.summary()).append("\ntrace=").append(TRACE)
            .append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M789 Aero queue-reuse performance classification passed");
        System.out.println("WORLDLINE_M789_DECISION=" + result.decision());
        System.out.println("WORLDLINE_M789_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M789_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M789_SIGNATURE=" + signature);
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
            && SmokeSupport.value(config, "minimum.allocation.winning.pairs").equals("3"),
            "M789 acquisition design drift");
    }
}
