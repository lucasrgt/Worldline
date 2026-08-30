import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Qualifies SPI-discovered ModLoader and Forge providers against fresh real clients. */
public final class M767ModloaderForgeTestkitProviderCycle {
    private static final String ID = "m767-modloader-forge-testkit-provider";
    private static final String SIGNAL = "providers=modloader-b1.7.3+forge-b1.7.3,discovery=spi,"
            + "sessions=4,testkit=4-pass,ticks=4,isolation=fresh-singleplayer-client,"
            + "profiler=4-sealed-wlpr,shutdown=clean";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties descriptor = new Properties(), qualification = new Properties();

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: M767ModloaderForgeTestkitProviderCycle " + ID);
            System.exit(2);
        }
        try { new M767ModloaderForgeTestkitProviderCycle().execute(); }
        catch (Exception error) {
            System.err.println("M767 legacy TestKit provider failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), descriptor);
        SmokeSupport.load(root.resolve("adapters/modloader-forge/qualification.properties"), qualification);
        verifyPins();
        SmokeSupport.recreate(root, build);
        Path base = input("WORLDLINE_LEGACY_BASE_WORKSPACE", "local/workspaces/b1.7.3");
        Path artifacts = input("WORLDLINE_LEGACY_LOADER_ARTIFACTS", null);
        Path java8 = input("WORLDLINE_JAVA8_HOME", null);
        String prepared = capture("prepare", Arrays.asList(javaTool(), root.resolve(
                "tools/integration/LegacyProfilerQualificationLauncher.java").toString(),
                "--prepare-testkit-all", base.toString(), artifacts.toString(), java8.toString()), 900);
        for (String loader : Arrays.asList("modloader", "forge")) SmokeSupport.require(
                prepared.contains("WORLDLINE_LEGACY_TESTKIT_PREPARED=" + loader),
                "legacy client was not prepared: " + loader);

        Path classes = productClasses(), adapter = build.resolve("adapter-classes");
        Path specs = build.resolve("spec-classes");
        Files.createDirectories(adapter);
        Files.createDirectories(specs);
        List<Path> api = Arrays.asList(classes.resolve("api"), classes.resolve("testmodel"),
                classes.resolve("testapi"));
        compile(adapter, "21", api, root.resolve("adapters/modloader-forge/src/main/java"));
        Path service = adapter.resolve("META-INF/services/worldline.test.TestRuntimeProvider");
        Files.createDirectories(service.getParent());
        Files.copy(root.resolve("adapters/modloader-forge/src/main/resources/META-INF/services/"
                + "worldline.test.TestRuntimeProvider"), service);
        compile(specs, "8", api, smoke.resolve("spec-src"));
        for (String loader : Arrays.asList("modloader", "forge")) {
            String output = runTestKit(loader, java8, classes, adapter, specs);
            verifyOutput(loader, output);
            verifyArtifacts(loader);
        }
        String signature = sha256(SIGNAL);
        SmokeSupport.require(SIGNAL.equals(SmokeSupport.value(descriptor, "expected.signal")),
                "M767 signal drift");
        SmokeSupport.require(signature.equals(SmokeSupport.value(descriptor, "expected.signature")),
                "M767 signature drift: " + signature);
        Files.writeString(build.resolve("evidence.txt"), "id=" + ID + "\n"
                + SIGNAL.replace(',', '\n') + "\nsignature=" + signature + "\n", StandardCharsets.UTF_8);
        System.out.println("M767 ModLoader/Forge TestKit provider passed");
        System.out.println("  signal: " + SIGNAL);
        System.out.println("  signature: " + signature);
    }

    private String runTestKit(String loader, Path java8, Path classes, Path adapter, Path specs)
            throws Exception {
        List<Path> runtime = new ArrayList<Path>();
        runtime.add(specs);
        runtime.add(adapter);
        runtime.addAll(productRuntime(classes));
        Path workspace = root.resolve(".worldline/runtime/legacy-testkit/workspaces").resolve(loader);
        List<String> command = new ArrayList<String>(Arrays.asList(javaTool(),
                "-Dworldline.legacy." + loader + ".workspace=" + workspace,
                "-Dworldline.legacy.java8Home=" + java8,
                "-Dworldline.legacy.timeoutSeconds=" + SmokeSupport.value(descriptor,
                        "session.timeout.seconds"), "-classpath", join(runtime),
                "worldline.cli.WorldlineCli", "test", "run", specs.toString(),
                "worldline.legacy.test.LegacyDriverSpec", "--provider=" + loader + "-b1.7.3",
                "--world=" + build.resolve("worlds").resolve(loader),
                "--artifacts=" + build.resolve("results").resolve(loader),
                "--snapshots=" + build.resolve("snapshots").resolve(loader),
                "--runtime-lock=" + build.resolve(loader + "-runtime.lock"), "--reporter=agent",
                "--timeout=600000"));
        return capture(loader, command, Integer.parseInt(SmokeSupport.value(descriptor, "timeout.seconds")));
    }

    private void verifyOutput(String loader, String output) {
        SmokeSupport.require(output.contains("WORLDLINE_TEST=PASS") && output.contains("tests=2"),
                loader + " TestKit cases did not pass\n" + output);
        SmokeSupport.require(count(output, "WORLDLINE_LEGACY_SESSION_OPEN=" + loader + ":") == 2,
                "missing fresh sessions for " + loader);
        SmokeSupport.require(count(output, "WORLDLINE_LEGACY_SESSION_CLOSE=" + loader + ":CLOSED") == 2,
                "legacy sessions did not close: " + loader);
    }

    private void verifyArtifacts(String loader) throws Exception {
        String prefix = loader.substring(0, 1);
        for (int index = 1; index <= 2; index++) {
            String session = String.format("%s%02d", prefix, index);
            Path artifact = build.resolve("worlds").resolve(loader)
                    .resolve(loader + "-" + session).resolve("profiler.wlpr");
            SmokeSupport.require(Files.isRegularFile(artifact) && Files.size(artifact) > 64L,
                    "missing legacy profiler artifact " + artifact);
            String inspection = SmokeSupport.capture(root, Arrays.asList(javaTool(),
                    "-Djava.awt.headless=true", "-classpath", join(productRuntime(productClasses())),
                    "worldline.cli.WorldlineCli", "profiler", "inspect", artifact.toString()));
            SmokeSupport.require(inspection.contains("WORLDLINE_PROFILER_INSPECT=PASS"),
                    "invalid legacy profiler artifact " + artifact);
        }
    }

    private void verifyPins() {
        SmokeSupport.require("worldline.legacy-profiler-qualification.v1".equals(
                qualification.getProperty("schema")), "legacy qualification schema drift");
        for (String key : Arrays.asList("client.sha256", "modloader.sha256", "forge.sha256"))
            SmokeSupport.require(SmokeSupport.value(descriptor, key).equals(
                    SmokeSupport.value(qualification, key)), "M767 pin drift: " + key);
    }

    private Path input(String environment, String fallback) {
        String value = System.getenv(environment);
        Path path = value == null || value.isBlank()
                ? (fallback == null ? null : root.resolve(fallback)) : Path.of(value);
        SmokeSupport.require(path != null && Files.isDirectory(path), "missing " + environment);
        return path.toAbsolutePath().normalize();
    }

    private void compile(Path output, String release, List<Path> classpath, Path source) throws Exception {
        List<String> command = new ArrayList<String>(Arrays.asList("javac", "-encoding", "UTF-8",
                "--release", release, "-Xlint:all,-options", "-Werror", "-classpath", join(classpath),
                "-d", output.toString()));
        List<String> sources = SmokeSupport.javaFiles(source);
        SmokeSupport.require(!sources.isEmpty(), "no Java sources under " + source);
        command.addAll(sources);
        SmokeSupport.capture(root, command);
    }

    private String capture(String label, List<String> command, int seconds) throws Exception {
        Path log = build.resolve(label + ".log");
        Files.createDirectories(log.getParent());
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
            process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                    .forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
            throw new IllegalStateException(label + " timed out; log=" + log);
        }
        String output = Files.readString(log, StandardCharsets.ISO_8859_1);
        System.out.print(output);
        SmokeSupport.require(process.exitValue() == 0, label + " failed; log=" + log);
        return output;
    }

    private String join(List<Path> paths) {
        List<String> values = new ArrayList<String>();
        for (Path path : paths) {
            SmokeSupport.require(Files.exists(path), "missing " + path);
            values.add(path.toString());
        }
        return String.join(File.pathSeparator, values);
    }
    private Path productClasses() {
        String external = System.getenv("WORLDLINE_PRODUCT_ROOT");
        return external == null || external.isBlank() ? root.resolve(".worldline/build/classes")
                : Path.of(external);
    }
    private List<Path> productRuntime(Path classes) {
        List<Path> paths = new ArrayList<Path>();
        for (String module : Arrays.asList("optimization", "itemref", "api", "smoketest",
                "invariants", "symbolgraph", "semantics", "trace", "kernel", "reproduction",
                "mods", "analysis", "modtest", "minimization", "atlas", "testmodel", "testapi",
                "testkit", "fuzz", "profiling", "coverage", "cli")) paths.add(classes.resolve(module));
        return paths;
    }
    private static int count(String text, String needle) {
        int count = 0, offset = 0;
        while ((offset = text.indexOf(needle, offset)) >= 0) {
            count++;
            offset += needle.length();
        }
        return count;
    }
    private static String sha256(String value) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
    private static String javaTool() { return Path.of(System.getProperty("java.home"), "bin",
            System.getProperty("os.name", "").startsWith("Windows") ? "java.exe" : "java").toString(); }
}
