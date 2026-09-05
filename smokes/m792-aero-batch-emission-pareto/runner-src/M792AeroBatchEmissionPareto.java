import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Exact raster and counterbalanced render/allocation frontier for emission replay. */
public final class M792AeroBatchEmissionPareto {
    private static final String ID = "m792-aero-batch-emission-pareto";
    static final String TRACE = "v2|candidate=bounded-draw-runs|sdk=public|machines=512-mixed-four-chunks|sessions=4|"
        + "orders=off-on,on-off,on-off,off-on|scenes=stationary,moving,advancing-diverse|"
        + "views=12|prewarm=12x120|hold=180|discard=60|pixels=exact|render-ratio=0.95|"
        + "moving-advancing-ratio=1.03|p99=1.05|allocation=1.02|staging=16MiB|hitches=no-increase";

    public void execute(String id) throws Exception {
        M792Performance.selfTest();
        SmokeSupport.require(ID.equals(id), "M792 identity drift");
        Path root = Path.of("").toAbsolutePath().normalize(), smoke = root.resolve("smokes/" + ID);
        Path build = root.resolve(".worldline/build/smoke/" + ID);
        Properties config = new Properties(); SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        SmokeSupport.require("4".equals(config.getProperty("sessions"))
            && "180".equals(config.getProperty("hold.frames"))
            && "0".equals(config.getProperty("maximum.changed.pixels")), "M792 design drift");
        SmokeSupport.require(M792Runtime.sha256(TRACE).equals(config.getProperty("expected.signature")),
            "M792 signature drift");
        M792Runtime runtime = new M792Runtime(root, smoke, build, config,
            root.resolve(config.getProperty("aero.path")).normalize());
        runtime.verifyCheckout(); SmokeSupport.recreate(root, build);
        runtime.verifyExtension(); runtime.buildAero();
        Path template = build.resolve("template"); runtime.runClient(template, true, "prepare");
        String[] orders = {"off-on", "on-off", "on-off", "off-on"};
        List<M792Performance> profiles = new ArrayList<M792Performance>();
        StringBuilder evidence = new StringBuilder("id=" + ID + "\n");
        boolean visual = true, work = true;
        for (int i = 0; i < orders.length; i++) {
            Path game = build.resolve("session" + (i + 1) + "-" + orders[i]);
            M792Runtime.copyWorld(template.resolve("saves/WorldlineAero"), game.resolve("saves/WorldlineAero"));
            runtime.runClient(game, false, orders[i]);
            M792Artifact off = M792Artifact.read(game, "replay-off"), on = M792Artifact.read(game, "replay-on");
            off.verify(); on.verify();
            M792VisualPair pair = M792VisualPair.compare(i + 1, off, on);
            visual &= pair.changedPixels() == 0;
            work &= off.culled == 0 && on.culled == 0;
            evidence.append(pair.summary()).append('\n');
            profiles.add(M792Performance.read(game, orders[i].equals("off-on"), config));
            Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        }
        boolean performance = M792Performance.evaluate(profiles, evidence);
        evidence.append("visual.exact=").append(visual).append("\nno.culls=").append(work)
            .append("\nperformance.passes=").append(performance).append("\ntrace=").append(TRACE).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        SmokeSupport.require(visual && work && performance, "M792 candidate rejected; see " + build.resolve("evidence.txt"));
        System.out.println("WORLDLINE_M792_SIGNAL=" + config.getProperty("expected.signal"));
        System.out.println("WORLDLINE_M792_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M792_SIGNATURE=" + config.getProperty("expected.signature"));
    }
}
