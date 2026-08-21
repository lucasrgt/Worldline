import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Runs the M17 three-scenario vanilla/governor/adaptive matrix. */
public final class AeroSchedulerHardeningCycle {
    private static final String ID = "m17-scheduler-hardening";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Properties config = new Properties();

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/AeroSchedulerHardeningCycle.java " + ID);
            System.exit(2);
        }
        try { new AeroSchedulerHardeningCycle().execute(); }
        catch (Exception error) {
            System.err.println("Aero scheduler hardening failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        Path smoke = root.resolve("smokes").resolve(ID); load(smoke.resolve("smoke.properties"));
        require(ID.equals(value("id")), "smoke descriptor id mismatch");
        verifyProfile();
        List<String> scenarios = list("scenarios");
        require(scenarios.size() == 3, "M17 requires three scenarios");
        Path checkout = root.resolve(value("aero.path")).normalize(); verifyCheckout(checkout);
        Path test = checkout.resolve("stationapi/test"), run = test.resolve("run").normalize();
        Path save = run.resolve("saves/WorldlineAero").normalize();
        require(save.startsWith(run) && !save.equals(run), "unsafe Aero save path");
        Path build = root.resolve(".worldline/smokes").resolve(ID).normalize(); recreate(build);
        Map<String, Path> snapshots = snapshots(test, save, build, scenarios);
        for (String scenario : scenarios) {
            String density = scenario(scenario, "density"), path = scenario(scenario, "path");
            String view = scenario(scenario, "view"), freeze = scenario(scenario, "freeze.tick");
            restore(save, snapshots.get(density));
            capture(test, build, scenario, "baseline", density, path, view, freeze, true);
            restore(save, snapshots.get(density));
            capture(test, build, scenario, "adaptive", density, path, view, freeze, true);
            restore(save, snapshots.get(density));
            capture(test, build, scenario, "governor", density, path, view, freeze, false);
        }
        delete(save); for (Path snapshot : snapshots.values()) delete(snapshot);
        Path classes = build.resolve("classes"); Files.createDirectories(classes);
        run(root, command("javac", "-encoding", "UTF-8", "--release", "8", "-Xlint:all,-options",
                "-Werror", "-d", classes.toString(),
                root.resolve("adapters/aero-model-lib/src/main/java/worldline/aero/AeroDiagnostics.java").toString(),
                root.resolve("adapters/aero-model-lib/src/main/java/worldline/aero/AeroChunkReadiness.java").toString(),
                root.resolve("adapters/aero-model-lib/src/main/java/worldline/aero/AeroFrameOracle.java").toString(),
                smoke.resolve("src/worldline/smoke/m17/FrameDiff.java").toString(),
                smoke.resolve("src/worldline/smoke/m17/RunMetrics.java").toString(),
                smoke.resolve("src/worldline/smoke/m17/SchedulerHardeningSmoke.java").toString()));
        String output = output(root, command("java", "-classpath", classes.toString(),
                "worldline.smoke.m17.SchedulerHardeningSmoke", build.toString(),
                String.join(",", scenarios), value("analysis.frames"), value("adaptive.budget.us")));
        require(output.contains("WORLDLINE_M17_SCHEDULER_HARDENING=PASS"), "M17 proof absent");
        require(line(output, "evidence.sha256=").equals(value("expected.signature")),
                "M17 signature drift");
        String evidence = "id=" + ID + "\naero.revision=" + value("aero.revision")
                + "\nseed=" + value("seed") + "\n" + output;
        Files.write(build.resolve("evidence.txt"), evidence.getBytes(StandardCharsets.UTF_8));
        System.out.println("Aero scheduler hardening cycle passed"); System.out.print(output);
        System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
    }

    private Map<String, Path> snapshots(Path test, Path save, Path build,
            List<String> scenarios) throws Exception {
        Map<String, Path> result = new LinkedHashMap<>();
        for (String scenario : scenarios) {
            String density = scenario(scenario, "density");
            if (result.containsKey(density)) continue;
            Path snapshot = build.resolve("snapshot-" + density);
            delete(save);
            capture(test, build, "seed-" + density, "baseline", density,
                    "stationary", "0", "20", true);
            copy(save, snapshot); result.put(density, snapshot);
        }
        return result;
    }

    private void restore(Path save, Path snapshot) throws IOException {
        delete(save); copy(snapshot, save);
    }

    private void capture(Path test, Path build, String scenario, String policy,
            String density, String path, String view, String freeze, boolean oracle) throws Exception {
        require(scenario.matches("[a-z0-9-]+"), "unsafe scenario name");
        String name = scenario + "-" + policy;
        System.out.println("M17 capture " + name);
        Path raw = build.resolve(name + "-raw.log"), probe = build.resolve(name + "-probe.log");
        Path aero = build.resolve(name + "-aero.log"), frame = build.resolve(name + "-frame.log");
        Path image = build.resolve(name + "-frame.png");
        Files.deleteIfExists(raw); Files.deleteIfExists(probe); Files.deleteIfExists(aero);
        Files.deleteIfExists(frame); Files.deleteIfExists(image);
        String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
        List<String> command = command(test.resolve(wrapper).toString(), "--no-daemon", "--init-script",
                root.resolve(value("runner")).toString(), "runClient",
                "-PworldlineTicks=" + (oracle ? "10000" : value("governor.ticks")),
                "-PworldlineWarmupTicks=0", "-PworldlineSpikeMs=1",
                "-PworldlineMode=m17" + policy, "-PworldlineDensity=" + density,
                "-PworldlineMinBEs=" + (density.equals("empty") ? "0" : "500"),
                "-PworldlinePath=" + path, "-PworldlineViewDistance=" + view,
                "-PworldlineFreezeTick=" + freeze, "-PworldlineFrameOracle=" + oracle,
                "-PworldlineOracleStableFrames=" + value("analysis.frames"),
                "-PworldlineAdaptiveMax=" + value("adaptive.max"),
                "-PworldlineBudgetUs=" + value("adaptive.budget.us"), "-PworldlineLog=" + raw);
        if (oracle) command.add("-PworldlineFrameOutput=" + image);
        String text = output(test, command);
        require(text.contains("[WorldlineCapture] complete") && text.contains("BUILD SUCCESSFUL"),
                "Aero " + name + " capture failed\n" + text);
        List<String> probes = new ArrayList<>(), frames = new ArrayList<>(), oracleRows = new ArrayList<>();
        boolean recording = false;
        for (String row : text.split("\\R")) {
            if (row.contains("[WorldlineCapture] ready")) { recording = true; continue; }
            if (recording && row.startsWith("[WorldlineChunkProbe]")) probes.add(row);
            if (recording && row.startsWith("[Aero_")) frames.add(row);
            int marker = row.indexOf("[WorldlineFrameOracle]");
            if (recording && marker >= 0) oracleRows.add(row.substring(marker));
            if (row.contains("[WorldlineCapture] complete")) break;
        }
        int minimum = Integer.parseInt(value("analysis.frames"));
        require(probes.size() >= minimum && frames.size() >= minimum,
                "too few measured frames for " + name);
        if (oracle) require(oracleRows.size() == 1 && !oracleRows.get(0).contains("timeout"),
                "invalid " + name + " frame oracle: " + oracleRows);
        Files.write(probe, probes, StandardCharsets.UTF_8);
        Files.write(aero, frames, StandardCharsets.UTF_8);
        if (oracle) Files.write(frame, oracleRows, StandardCharsets.UTF_8);
    }

    private void verifyCheckout(Path checkout) throws Exception {
        require(Files.isDirectory(checkout.resolve(".git")), "Aero checkout missing");
        require(output(root, command("git", "-C", checkout.toString(), "remote", "get-url", "origin"))
                .trim().equals(value("aero.repository")), "unexpected Aero origin");
        require(output(root, command("git", "-C", checkout.toString(), "rev-parse", "HEAD"))
                .trim().equals(value("aero.revision")), "unexpected Aero revision");
        require(output(root, command("git", "-C", checkout.toString(), "status", "--porcelain"))
                .trim().isEmpty(), "Aero checkout has tracked changes");
    }

    private void verifyProfile() throws IOException {
        Properties profile = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(root.resolve(value("profile")),
                StandardCharsets.UTF_8)) { profile.load(reader); }
        require("false".equals(profile.getProperty("default.enabled")), "profile must default off");
        require("lab-only-no-go".equals(profile.getProperty("shipping.status")),
                "profile shipping status drifted");
        require(value("adaptive.max").equals(profile.getProperty("max.batch"))
                && value("adaptive.budget.us").equals(profile.getProperty("budget.us")),
                "profile scheduler parameters drifted");
    }

    private void recreate(Path path) throws IOException { delete(path); Files.createDirectories(path); }
    private void copy(Path source, Path target) throws IOException {
        require(Files.isDirectory(source), "snapshot source missing " + source);
        require(target.startsWith(root) && !target.equals(root), "unsafe snapshot target " + target);
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path item : paths.collect(Collectors.toList())) {
                Path destination = target.resolve(source.relativize(item));
                if (Files.isDirectory(item)) Files.createDirectories(destination);
                else Files.copy(item, destination);
            }
        }
    }
    private void delete(Path path) throws IOException {
        if (!Files.exists(path)) return;
        require(path.startsWith(root) && !path.equals(root), "unsafe delete path " + path);
        try (Stream<Path> paths = Files.walk(path)) {
            for (Path item : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
                Files.delete(item);
        }
    }
    private void load(Path path) throws IOException { try (java.io.Reader reader =
            Files.newBufferedReader(path, StandardCharsets.UTF_8)) { config.load(reader); } }
    private String value(String key) { String result = config.getProperty(key);
        require(result != null && !result.trim().isEmpty(), "missing smoke property " + key);
        return result.trim(); }
    private String scenario(String name, String key) { return value(name + "." + key); }
    private List<String> list(String key) { return Arrays.stream(value(key).split(","))
            .map(String::trim).filter(item -> !item.isEmpty()).collect(Collectors.toList()); }
    private String line(String text, String prefix) { return Arrays.stream(text.split("\\R"))
            .filter(row -> row.startsWith(prefix)).findFirst().orElseThrow(
                () -> new IllegalStateException("missing " + prefix)).substring(prefix.length()); }
    private List<String> command(String... values) { return new ArrayList<>(Arrays.asList(values)); }
    private void run(Path directory, List<String> command) throws Exception { output(directory, command); }
    private String output(Path directory, List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start();
        String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException(command.get(0) + " failed\n" + text);
        return text;
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
