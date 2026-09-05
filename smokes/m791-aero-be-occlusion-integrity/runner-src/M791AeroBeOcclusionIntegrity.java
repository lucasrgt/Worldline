import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Qualifies Aero's conservative 3D BE occlusion candidate. */
public final class M791AeroBeOcclusionIntegrity {
    private static final String ID = "m791-aero-be-occlusion-integrity";
    private static final String[] ORDERS = {"off-on", "on-off"};
    private static final String TRACE = "v3|extension=ultra-journey-sdk-v1|"
        + "scene=4chunks-512-mixed-2layers+double-slab|sessions=2|orders=off-on+on-off|"
        + "cameras=covered-up8x45+reveal4x90|hold=20|camera-settle=3+confirm=2|advanced-opengl=on|"
        + "world=frozen-tick+time6000+clear-weather+no-entities+no-clouds+no-demo-population|"
        + "contrast=be-cell-occlusion-off-vs-on|other-culls=off|"
        + "oracle=exact-rgba+activation+submission-reduction-including-pages|captures=12-per-arm";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/build/smoke").resolve(ID);
    private final Properties config = new Properties();

    public void execute(String id) throws Exception {
        SmokeSupport.require(ID.equals(id), "M791 coordinator identity drift");
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M791Runtime runtime = new M791Runtime(root, smoke, build, config, aero);
        runtime.verifyCheckout();
        SmokeSupport.recreate(root, build);
        runtime.verifyExtension();
        runtime.buildAero();
        Path template = build.resolve("template");
        runtime.runClient(template, true, "prepare");
        Path sourceWorld = template.resolve("saves/WorldlineAero");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M791 template world absent");
        List<M791Session> sessions = new ArrayList<M791Session>();
        for (int index = 0; index < ORDERS.length; index++) {
            Path game = build.resolve("session" + (index + 1) + "-" + ORDERS[index]);
            M791Runtime.copyWorld(sourceWorld, game.resolve("saves/WorldlineAero"));
            runtime.runClient(game, false, ORDERS[index]);
            M791Artifact off = M791Artifact.read(game, "occlusion-off");
            M791Artifact on = M791Artifact.read(game, "occlusion-on");
            off.verify();
            on.verify();
            sessions.add(new M791Session(index + 1, off, on));
        }
        List<M791VisualPair> pairs = List.of(
            M791VisualPair.compare(1, sessions.get(0).off(), sessions.get(1).on()),
            M791VisualPair.compare(2, sessions.get(0).on(), sessions.get(1).off()));
        M791Result result = M791Result.evaluate(sessions, pairs);
        double maximum = Double.parseDouble(SmokeSupport.value(config, "maximum.work.ratio"));
        String signature = M791Runtime.sha256(TRACE);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
            "M791 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M791Session session : sessions) evidence.append(session.summary()).append('\n');
        for (M791VisualPair pair : pairs) evidence.append(pair.summary()).append('\n');
        evidence.append(result.summary()).append("\ntrace=").append(TRACE)
            .append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        SmokeSupport.require(result.passes(maximum), "M791 gate failed: " + result.summary());
        System.out.println("M791 Aero BE occlusion integrity passed");
        System.out.println("WORLDLINE_M791_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M791_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M791_SIGNATURE=" + signature);
    }

    private void verifyDesign() {
        SmokeSupport.require("2".equals(SmokeSupport.value(config, "sessions"))
            && "12".equals(SmokeSupport.value(config, "checkpoints"))
            && "20".equals(SmokeSupport.value(config, "hold.frames"))
            && "512".equals(SmokeSupport.value(config, "machines"))
            && "4".equals(SmokeSupport.value(config, "machine.chunks"))
            && "0".equals(SmokeSupport.value(config, "maximum.changed.pixels")),
            "M791 acquisition design drift");
    }
}
