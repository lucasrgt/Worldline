import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;

/** Builds the pinned Aero source and freezes neutral logical/runtime attribution evidence. */
public final class AeroAttributionCycle {
    private static final String ID = "m11-aero-attribution";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Properties config = new Properties();

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/AeroAttributionCycle.java " + ID);
            System.exit(2);
        }
        try { new AeroAttributionCycle().execute(); }
        catch (Exception error) {
            System.err.println("Aero attribution cycle failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        Path smoke = root.resolve("smokes").resolve(ID);
        load(smoke.resolve("smoke.properties"));
        require(ID.equals(value("id")), "smoke descriptor id mismatch");
        Path checkout = root.resolve(value("aero.path")).normalize();
        require(checkout.startsWith(root.resolve("local").normalize()), "Aero checkout must be local");
        prepareCheckout(checkout);
        runCoreTests(checkout);
        Path station = checkout.resolve("stationapi");
        gradle(station, "build");
        gradle(station.resolve("test"), "compileJava");
        Path jar = checkout.resolve(value("aero.jar")).normalize();
        verifyArtifact(jar, checkout.resolve(value("aero.consumer.class")));
        boolean runtimeWarning = runRuntime(checkout.resolve("stationapi/test"));
        verifyNativeWork();

        Path build = root.resolve(".worldline/smokes").resolve(ID).normalize();
        Path classes = build.resolve("classes"); Files.createDirectories(classes);
        Path analysis = root.resolve(".worldline/build/classes/analysis");
        require(Files.isDirectory(analysis), "run repository compilation before M11");
        compile(classes, analysis,
                root.resolve("adapters/aero-model-lib/src/main/java/worldline/aero/AeroFrameLog.java"),
                smoke.resolve("src/worldline/smoke/m11/AeroAttributionSmoke.java"));
        String first = java(classes, analysis, null, "diagnose");
        String second = java(classes, analysis, null, "diagnose");
        require(first.equals(second), "attribution processes diverged");
        String signature = line(first, "WORLDLINE_ATTRIBUTION_SIGNATURE=");
        require(signature.equals(value("expected.signature")), "attribution signature drift: " + signature);
        require(first.contains("WORLDLINE_LOGICAL_CAUSE=LOGICAL_WORK")
                && first.contains("WORLDLINE_RUNTIME_CAUSE=RUNTIME_STALL"), "cause proof is absent");
        String loaded = java(classes, analysis, jar, "artifact");
        require(loaded.contains("WORLDLINE_AERO_LOAD=PASS")
                && loaded.contains("WORLDLINE_AERO_VERSION=" + value("aero.version")), "Aero load failed");
        require(line(loaded, "WORLDLINE_AERO_PROVENANCE=").replace('\\', '/').endsWith(
                "/stationapi/build/libs/aero-model-lib-" + value("aero.version") + ".jar"),
                "Aero loaded from the wrong artifact");
        Path evidence = writeEvidence(build, jar, signature, runtimeWarning);
        System.out.println("Aero attribution cycle passed");
        System.out.println("  source: " + value("aero.revision"));
        System.out.println("  core tests: 222 passed");
        System.out.println("  StationAPI library + consumer + loader: COMPATIBLE");
        System.out.println("  startup diagnostic: "
                + (runtimeWarning ? "chunk-bake UV requested before atlas readiness" : "none"));
        System.out.println("  isolated Aero class load: PASS");
        System.out.println("  logical spike / runtime stall: DISTINGUISHED");
        System.out.println("  evidence: " + root.relativize(evidence));
    }

    private void prepareCheckout(Path checkout) throws Exception {
        if (!Files.exists(checkout)) {
            Files.createDirectories(checkout.getParent());
            run(root, "git", "clone", "--no-checkout", value("aero.repository"), checkout.toString());
            run(root, "git", "-C", checkout.toString(), "fetch", "--depth", "1", "origin", value("aero.revision"));
            run(root, "git", "-C", checkout.toString(), "checkout", "--detach", value("aero.revision"));
        }
        require(Files.isDirectory(checkout.resolve(".git")), "Aero path is not a Git checkout");
        require(capture(root, command("git", "-C", checkout.toString(), "remote", "get-url", "origin"), null)
                .trim().equals(value("aero.repository")), "unexpected Aero origin");
        require(capture(root, command("git", "-C", checkout.toString(), "rev-parse", "HEAD"), null)
                .trim().equals(value("aero.revision")), "unexpected Aero revision");
        require(capture(root, command("git", "-C", checkout.toString(), "status", "--porcelain"), null)
                .trim().isEmpty(), "Aero checkout has tracked changes");
    }

    private void runCoreTests(Path checkout) throws Exception {
        List<String> command = command("powershell", "-ExecutionPolicy", "Bypass", "-File",
                checkout.resolve("modloader/tests/run.ps1").toString());
        String flags = "-Daero.maxAnimatedBE=128 -Daero.animBudget.hardCap=false";
        String output = capture(root, command, flags);
        require(output.contains("OK (222 tests)"), "Aero core test count/result drifted\n" + output);
    }

    private void gradle(Path directory, String task) throws Exception {
        String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
        String output = capture(directory, command(directory.resolve(wrapper).toString(), "--no-daemon", task), null);
        require(output.contains("BUILD SUCCESSFUL"), "Aero Gradle " + task + " did not succeed");
    }

    private void verifyArtifact(Path jar, Path consumer) throws Exception {
        require(Files.isRegularFile(jar) && Files.size(jar) > 0, "Aero StationAPI JAR missing");
        require(Files.isRegularFile(consumer), "Aero StationAPI consumer did not compile");
        try (JarFile archive = new JarFile(jar.toFile())) {
            require(archive.getEntry("aero/modellib/util/Aero_Profiler.class") != null, "Aero profiler missing");
            require(archive.getEntry("aero/modellib/Aero_FrameSpikeLogger.class") != null, "Aero logger missing");
            String descriptor = new String(archive.getInputStream(archive.getEntry("fabric.mod.json"))
                    .readAllBytes(), StandardCharsets.UTF_8);
            require(descriptor.contains("\"id\": \"aero-model-lib\"")
                    && descriptor.contains("\"version\": \"" + value("aero.version") + "\"")
                    && descriptor.contains("\"minecraft\": \"1.0.0-beta.7.3\""), "Aero descriptor drifted");
        }
    }

    private boolean runRuntime(Path directory) throws Exception {
        String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
        List<String> command = command(directory.resolve(wrapper).toString(), "--no-daemon", "--init-script",
                root.resolve(value("runner.workaround")).toString(), "runClientBenchmark",
                "-Pbench=" + value("benchmark.seconds"));
        String output = capture(directory, command, null);
        require(output.contains("aero-model-lib " + value("aero.version"))
                && output.contains("Setting up \"aerotest\" \"stationapi:event_bus_client\"")
                && output.contains("[AeroTestProfilerHook] installed")
                && output.contains("benchmark window elapsed"), "Aero StationAPI runtime load proof is absent");
        return output.contains("chunk-bake UV resolve failed");
    }

    private void verifyNativeWork() throws IOException {
        Path evidence = root.resolve(".worldline/smokes/m10-native-render/evidence.txt");
        require(Files.isRegularFile(evidence), "run M10 native render before M11");
        String text = new String(Files.readAllBytes(evidence), StandardCharsets.UTF_8);
        require(text.contains("render.work=" + value("native.work")), "native work evidence drifted");
    }

    private void compile(Path output, Path analysis, Path... sources) throws Exception {
        List<String> command = command("javac", "-encoding", "UTF-8", "--release", "8",
                "-Xlint:all,-options", "-Werror", "-classpath", analysis.toString(), "-d", output.toString());
        for (Path source : sources) command.add(source.toString());
        capture(root, command, null);
    }

    private String java(Path classes, Path analysis, Path jar, String mode) throws Exception {
        String cp = classes + System.getProperty("path.separator") + analysis;
        if (jar != null) cp += System.getProperty("path.separator") + jar;
        return capture(root, command("java", "-classpath", cp,
                "worldline.smoke.m11.AeroAttributionSmoke", mode), null);
    }

    private Path writeEvidence(Path build, Path jar, String signature, boolean runtimeWarning) throws Exception {
        Path evidence = build.resolve("evidence.txt");
        String text = "id=" + ID + "\naero.repository=" + value("aero.repository")
                + "\naero.revision=" + value("aero.revision") + "\naero.version=" + value("aero.version")
                + "\naero.jar.sha256=" + sha256(jar) + "\naero.core.tests=222/222"
                + "\naero.stationapi.build=PASS\naero.stationapi.consumer=PASS"
                + "\naero.stationapi.runtime.load=PASS\naero.classload=PASS"
                + "\naero.runtime.warning=" + (runtimeWarning ? "chunk-bake-uv-atlas-unready" : "none")
                + "\nnative.work=" + value("native.work") + "\nlogical.cause=LOGICAL_WORK"
                + "\nruntime.cause=RUNTIME_STALL\nattribution.sha256=" + signature + "\n";
        Files.write(evidence, text.getBytes(StandardCharsets.UTF_8)); return evidence;
    }

    private String sha256(Path path) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
        StringBuilder result = new StringBuilder();
        for (byte item : hash) result.append(String.format("%02x", item & 255));
        return result.toString();
    }

    private void load(Path path) throws IOException {
        try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { config.load(reader); }
    }
    private String value(String key) {
        String value = config.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing smoke property " + key);
        return value.trim();
    }
    private String line(String output, String prefix) {
        return Arrays.stream(output.split("\\R")).filter(row -> row.startsWith(prefix)).findFirst()
                .orElseThrow(() -> new IllegalStateException("missing output " + prefix)).substring(prefix.length());
    }
    private void run(Path directory, String... command) throws Exception {
        String output = capture(directory, new ArrayList<>(Arrays.asList(command)), null);
        if (!output.trim().isEmpty()) System.out.print(output);
    }
    private List<String> command(String... values) { return new ArrayList<>(Arrays.asList(values)); }
    private String capture(Path directory, List<String> command, String javaOptions) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true);
        if (javaOptions != null) builder.environment().put("JAVA_TOOL_OPTIONS", javaOptions);
        Process process = builder.start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException(command.get(0) + " failed\n" + output);
        return output;
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
