import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Qualifies the SPI-discovered TestKit provider against two fresh StationAPI process pairs. */
public final class StationApiWorldControlsCycle {
    private static final String ID = "b173-stationapi-world-controls-cycle";
    private static final String SIGNAL = "provider=stationapi-b1.7.3,profile=local-world,sessions=2,"
            + "testkit=2-pass,atoms=3,isolation=fresh-client+world,profiler=2-sealed-wlpr";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties descriptor = new Properties(), serverArtifact = new Properties();
    private final Properties clientArtifact = new Properties();

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: StationApiWorldControlsCycle " + ID);
            System.exit(2);
        }
        try { new StationApiWorldControlsCycle().execute(); }
        catch (Exception error) {
            System.err.println("StationAPI world controls StationAPI TestKit driver failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), descriptor);
        SmokeSupport.load(root.resolve("artifacts/minecraft-b1.7.3-server.properties"), serverArtifact);
        SmokeSupport.load(root.resolve("artifacts/minecraft-b1.7.3-client.properties"), clientArtifact);
        SmokeSupport.require(ID.equals(SmokeSupport.value(descriptor, "id")), "descriptor id drift");
        Path server = root.resolve(SmokeSupport.value(serverArtifact, "local.path")).normalize();
        SmokeSupport.verifyArtifact(server, serverArtifact);
        Path client = root.resolve(SmokeSupport.value(clientArtifact, "local.path")).normalize();
        SmokeSupport.verifyArtifact(client, clientArtifact);
        SmokeSupport.require(SmokeSupport.value(descriptor, "server.jar.sha256")
                .equals(SmokeSupport.value(serverArtifact, "expected.sha256")), "server pin drift");
        SmokeSupport.require(SmokeSupport.value(descriptor, "client.jar.sha256")
                .equals(SmokeSupport.value(clientArtifact, "expected.sha256")), "client pin drift");
        Path checkout = root.resolve(SmokeSupport.value(descriptor, "stationapi.path")).normalize();
        verifyCheckout(checkout);
        SmokeSupport.recreate(root, build);
        Path classes = SmokeSupport.product(root, "api").getParent();
        Path adapter = build.resolve("adapter-classes"), specs = build.resolve("spec-classes");
        Files.createDirectories(adapter);
        Files.createDirectories(specs);
        List<Path> testApi = Arrays.asList(classes.resolve("api"), classes.resolve("testmodel"),
                classes.resolve("testapi"));
        compile(adapter, "21", testApi,
                root.resolve("adapters/stationapi/src/main/java"));
        Path service = adapter.resolve("META-INF/services/worldline.test.TestRuntimeProvider");
        Files.createDirectories(service.getParent());
        Files.writeString(service, "worldline.stationapi.StationApiTestRuntimeProvider\n",
                StandardCharsets.UTF_8);
        List<Path> specApi = new ArrayList<Path>(testApi); specApi.add(adapter); specApi.add(classes.resolve("testkit"));
        compile(specs, "8", specApi, root.resolve("adapters/stationapi/src/test/java"));
        specApi.add(specs); compile(specs, "8", specApi, smoke.resolve("spec-src"));
        String output = runTestKit(checkout, server, client, classes, adapter, specs);
        verifyOutput(output);
        verifyProfilerArtifacts();
        verifyCheckout(checkout);
        List<String> observations = output.lines().filter(line -> line.startsWith("WORLDLINE_STATIONAPI_CONTROLS="))
                .map(line -> line.substring("WORLDLINE_STATIONAPI_CONTROLS=".length())).toList();
        SmokeSupport.require(observations.size() == 2 && observations.get(0).equals(observations.get(1)),
                "StationAPI control replicas differ");
        Files.writeString(build.resolve("evidence.txt"), observations.get(0) + "\n", StandardCharsets.UTF_8);
        String signature = SmokeSupport.digest(build.resolve("evidence.txt"), "SHA-256");
        SmokeSupport.require(SIGNAL.equals(SmokeSupport.value(descriptor, "expected.signal")), "control signal drift");
        SmokeSupport.require(signature.equals(SmokeSupport.value(descriptor, "expected.signature")),
                "control signature drift: " + signature);
        System.out.println("StationAPI world controls StationAPI TestKit driver passed");
        System.out.println("  signal: " + SIGNAL);
        System.out.println("  signature: " + signature);
    }

    private String runTestKit(Path checkout, Path server, Path client, Path classes,
            Path adapter, Path specs) throws Exception {
        List<Path> runtime = new ArrayList<Path>();
        runtime.add(specs);
        runtime.add(adapter);
        runtime.add(classes.resolve("optimization"));
        runtime.add(classes.resolve("api"));
        runtime.add(classes.resolve("invariants"));
        runtime.add(classes.resolve("semantics"));
        runtime.add(classes.resolve("trace"));
        runtime.add(classes.resolve("kernel"));
        runtime.add(classes.resolve("reproduction"));
        runtime.add(classes.resolve("mods"));
        runtime.add(classes.resolve("analysis"));
        runtime.add(classes.resolve("modtest"));
        runtime.add(classes.resolve("minimization"));
        runtime.add(classes.resolve("testmodel"));
        runtime.add(classes.resolve("testapi"));
        runtime.add(classes.resolve("testkit"));
        runtime.add(classes.resolve("cli"));
        List<String> command = new ArrayList<String>(Arrays.asList(javaTool(),
                "-Dworldline.stationapi.localWorld=true",
                "-Dworldline.stationapi.checkout=" + checkout,
                "-Dworldline.stationapi.init=" + smoke.resolve("stationapi-driver.init.gradle"),
                "-Dworldline.stationapi.serverJar=" + server,
                "-Dworldline.stationapi.clientJar=" + client,
                "-Dworldline.stationapi.clientSha256=" + SmokeSupport.value(descriptor, "client.jar.sha256"),
                "-Dworldline.stationapi.timeoutSeconds=" + SmokeSupport.value(descriptor, "session.timeout.seconds"),
                "-classpath", join(runtime), "worldline.cli.WorldlineCli", "test", "run",
                specs.toString(), "worldline.stationapi.test.StationApiWorldControlSpec",
                "--provider=stationapi-b1.7.3", "--world=" + build.resolve("worlds"),
                "--artifacts=" + build.resolve("results"), "--snapshots=" + build.resolve("snapshots"),
                "--runtime-lock=" + build.resolve("testkit-runtime.lock"), "--reporter=agent",
                "--timeout=600000"));
        return capture(command, Integer.parseInt(SmokeSupport.value(descriptor, "timeout.seconds")));
    }

    private void verifyOutput(String output) {
        SmokeSupport.require(output.contains("WORLDLINE_TEST=PASS") && output.contains("tests=2"),
                "StationAPI TestKit cases did not pass\n" + output);
        for (String id : Arrays.asList("s01", "s02")) {
            SmokeSupport.require(count(output, "WORLDLINE_STATIONAPI_SESSION_OPEN=" + id) == 1,
                    "missing fresh StationAPI session " + id);
        }
        SmokeSupport.require(count(output, "WORLDLINE_STATIONAPI_SESSION_CLOSE=CLOSED") == 2,
                "StationAPI sessions were not both closed");
    }

    private void verifyProfilerArtifacts() throws Exception {
        for (String id : Arrays.asList("s01", "s02")) {
            Path artifact = build.resolve("profiler-" + id + ".wlpr");
            SmokeSupport.require(Files.isRegularFile(artifact) && Files.size(artifact) > 64L,
                    "missing StationAPI profiler artifact: " + artifact);
            String inspection = SmokeSupport.capture(root, Arrays.asList(javaTool(),
                    root.resolve("tools/replay/Replay.java").toString(), "profiler", "inspect",
                    artifact.toString()));
            SmokeSupport.require(inspection.contains("WORLDLINE_PROFILER_INSPECT=PASS"),
                    "invalid StationAPI profiler artifact: " + artifact);
        }
    }

    private void verifyCheckout(Path checkout) throws Exception {
        SmokeSupport.require(Files.isDirectory(checkout.resolve(".git")), "StationAPI checkout absent");
        String origin = SmokeSupport.capture(root, Arrays.asList("git", "-C", checkout.toString(),
                "remote", "get-url", "origin")).trim();
        String head = SmokeSupport.capture(root, Arrays.asList("git", "-C", checkout.toString(),
                "rev-parse", "HEAD")).trim();
        String status = SmokeSupport.capture(root, Arrays.asList("git", "-C", checkout.toString(),
                "status", "--porcelain")).trim();
        SmokeSupport.require(origin.equals(SmokeSupport.value(descriptor, "stationapi.repository")),
                "unexpected StationAPI origin");
        SmokeSupport.require(head.equals(SmokeSupport.value(descriptor, "stationapi.revision")),
                "unexpected StationAPI revision");
        SmokeSupport.require(status.isEmpty(), "StationAPI checkout has tracked changes");
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

    private String capture(List<String> command, int seconds) throws Exception {
        Path log = build.resolve("testkit.log");
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
            process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                    .forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
            throw new IllegalStateException("TestKit command timed out; log=" + log);
        }
        String output = Files.readString(log, StandardCharsets.UTF_8);
        System.out.print(output);
        SmokeSupport.require(process.exitValue() == 0, "TestKit command failed; log=" + log);
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
    private static int count(String text, String needle) {
        int count = 0, offset = 0;
        while ((offset = text.indexOf(needle, offset)) >= 0) {
            count++;
            offset += needle.length();
        }
        return count;
    }
    private static String javaTool() {
        return Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name", "").startsWith("Windows") ? "java.exe" : "java").toString();
    }
}
