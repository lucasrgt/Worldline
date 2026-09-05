import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
/** Production matrix and default regression against an immutable previous renderer. */
public final class M793AeroProductionQualification {
    private static final String ID = "m793-aero-production-qualification";
    static final String TRACE = "v4|sdk=public|aero=current14975c9,baseline88adc8e|versions=B,C,C,B|orders=off-on,on-off|sessions=8|cases=cold,1,20,64,128,256,transition,custom,nested,identity,blocks,chunks|hold=180|discard=60-except-cold|terrain-prewarm=5-views-no-dirty-exact8x15-no-models|captures=3-per-arm-case|pixels=exact|gain20-64=0.90|other=1.03|p99=1.05|allocation=1.02|staging=16MiB|hitches=no-increase|diagnostic=separate-inclusive-stage-cpu|surface=owned-rgba8-depth24|vanilla-occlusion=off|spikelog=off";
    public void execute(String id) throws Exception {
        M793Performance.selfTest(); M793Artifact.selfTest();
        SmokeSupport.require(ID.equals(id), "M793 identity drift");
        Path root = Path.of("").toAbsolutePath().normalize(), smoke = root.resolve("smokes/" + ID);
        Path build = root.resolve(".worldline/build/smoke/" + ID);
        Properties config = new Properties(); SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        SmokeSupport.require("8".equals(config.getProperty("sessions"))
            && "180".equals(config.getProperty("hold.frames"))
            && "0".equals(config.getProperty("maximum.changed.pixels")), "M793 design drift");
        SmokeSupport.require(M793Runtime.sha256(TRACE).equals(config.getProperty("expected.signature")), "M793 signature drift");
        SmokeSupport.recreate(root, build);
        List<M793Performance> profiles = new ArrayList<M793Performance>();
        List<M793Artifact> defaults = new ArrayList<M793Artifact>();
        StringBuilder evidence = new StringBuilder("id=" + ID + "\n");
        boolean visual = true, work = true;
        for (int group = 0; group < 4; group++) {
            boolean current = group == 1 || group == 2;
            Properties pin = new Properties(); pin.putAll(config);
            if (!current) pin.setProperty("aero.revision", config.getProperty("baseline.revision"));
            M793Runtime runtime = new M793Runtime(root, smoke, build, pin,
                root.resolve(config.getProperty(current ? "aero.path" : "baseline.path")).normalize());
            runtime.verifyCheckout(); runtime.verifyExtension(); runtime.buildAero();
            Path template = build.resolve("template");
            if (group == 0) runtime.runClient(template, true, "prepare");
            for (int order = 0; order < 2; order++) {
                String name = order == 0 ? "off-on" : "on-off";
                Path game = build.resolve("group" + group + "-" + (current ? "current-" : "baseline-") + name);
                M793Runtime.copyWorld(template.resolve("saves/WorldlineAero"), game.resolve("saves/WorldlineAero"));
                runtime.runClient(game, false, name);
                M793Artifact off = M793Artifact.read(game, "replay-off"), on = M793Artifact.read(game, "replay-on");
                off.verify(); on.verify(); defaults.add(off);
                M793VisualPair pair = M793VisualPair.compare(group * 2 + order, off, on);
                visual &= pair.changedPixels() == 0; work &= off.culled == 0 && on.culled == 0;
                evidence.append(pair.summary()).append('\n');
                profiles.add(M793Performance.read(game, current, order == 0));
                Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
            }
        }
        for (int i = 2; i < 6; i++) {
            M793VisualPair pair = M793VisualPair.compare(8 + i, defaults.get(i & 1), defaults.get(i));
            visual &= pair.changedPixels() == 0; evidence.append("baseline.").append(pair.summary()).append('\n');
        }
        boolean performance = M793Performance.evaluate(profiles, evidence);
        M793Runtime diagnostic = new M793Runtime(root, smoke, build, config,
            root.resolve(config.getProperty("aero.path")).normalize());
        diagnostic.verifyCheckout(); diagnostic.buildAero();
        Path game = build.resolve("diagnostic-off-on");
        M793Runtime.copyWorld(build.resolve("template/saves/WorldlineAero"), game.resolve("saves/WorldlineAero"));
        diagnostic.runClient(game, false, "diagnostic-off-on");
        evidence.append("visual.exact=").append(visual).append("\nno.culls=").append(work)
            .append("\nperformance.passes=").append(performance).append("\ntrace=").append(TRACE).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        SmokeSupport.require(visual && work && performance, "M793 production qualification rejected; see " + build.resolve("evidence.txt"));
        System.out.println("WORLDLINE_M793_SIGNAL=" + config.getProperty("expected.signal"));
        System.out.println("WORLDLINE_M793_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M793_SIGNATURE=" + config.getProperty("expected.signature"));
    }
}
