import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/** Qualifies Aero as an external Java 8 Worldline TestKit CPU-path consumer. */
public final class M774AeroProfilerCpuAdoptionCycle {
    private static final String ID = "m774-aero-profiler-cpu-adoption";
    private static final String TRACE = "v1|consumer=aero-model-lib|testkit=0.3.1|"
            + "java=8|gradle=8.14.4|runs=2|tests=3+3|morph=boxed-reference|"
            + "scheduler=bounded-visible-first+debt-fair|oracle=none";
    private static final String SIGNAL = "consumer=aero-model-lib,testkit=0.3.1,"
            + "java=8,gradle=8.14.4,runs=2,tests=3+3,morph=boxed-reference,"
            + "scheduler=bounded-visible-first+debt-fair,oracle=none";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties();

    private M774AeroProfilerCpuAdoptionCycle() {}

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: M774AeroProfilerCpuAdoptionCycle " + ID);
            System.exit(2);
        }
        try { new M774AeroProfilerCpuAdoptionCycle().execute(); }
        catch (Exception error) {
            System.err.println("M774 Aero CPU adoption failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        verifyAero(aero);
        verifyDistribution();
        SmokeSupport.recreate(root, build);
        Path gradle = gradleExecutable();
        String first = run(gradle, aero, "first");
        String second = run(gradle, aero, "second");
        verifyRun(first, "first");
        verifyRun(second, "second");
        Path xml = aero.resolve("tests/worldline/build/test-results/"
                + "worldlineTest/TEST-worldline.xml");
        String report = Files.readString(xml, StandardCharsets.UTF_8);
        SmokeSupport.require(report.contains("tests=\"3\"")
                && report.contains("failures=\"0\"") && report.contains("skipped=\"0\""),
                "M774 JUnit census drift");
        for (String test : List.of("parallel morph storage matches a boxed reference",
                "chunk scheduling is bounded and visibility-first",
                "chunk debt prevents hidden-work starvation")) {
            SmokeSupport.require(report.contains(test), "M774 missing differential: " + test);
        }
        String signature = sha256(TRACE);
        SmokeSupport.require(SIGNAL.equals(SmokeSupport.value(config, "expected.signal")),
                "M774 signal drift");
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
                "M774 signature drift: " + signature);
        Files.writeString(build.resolve("evidence.txt"), "id=" + ID + "\n" + SIGNAL.replace(',', '\n')
                + "\ntrace=" + TRACE + "\nsignature=" + signature + "\n",
                StandardCharsets.UTF_8);
        System.out.println("M774 Aero Worldline CPU consumer passed");
        System.out.println("WORLDLINE_M774_SIGNAL=" + SIGNAL);
        System.out.println("WORLDLINE_M774_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M774_SIGNATURE=" + signature);
    }

    private String run(Path gradle, Path aero, String label) throws Exception {
        Path project = aero.resolve("tests/worldline");
        List<String> command = new ArrayList<String>();
        command.add(gradle.toString());
        command.addAll(List.of("-p", project.toString(), "--include-build",
                root.resolve("tooling/gradle-plugin").toString(),
                "--project-prop=worldline.distributionDir="
                        + root.resolve(".worldline/dist/testkit").toString().replace('\\', '/'),
                "worldlineDoctor", "worldlineTest", "--rerun-tasks", "--no-daemon"));
        Path log = build.resolve(label + ".log");
        Process process = new ProcessBuilder(command).directory(aero.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        int timeout = Integer.parseInt(SmokeSupport.value(config, "timeout.seconds"));
        if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
            process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                    .forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
            throw new IllegalStateException("M774 " + label + " timed out; log=" + log);
        }
        String output = Files.readString(log, StandardCharsets.UTF_8);
        System.out.print(output);
        SmokeSupport.require(process.exitValue() == 0, "M774 " + label + " failed; log=" + log);
        return output;
    }

    private void verifyRun(String output, String label) {
        SmokeSupport.require(output.contains("WORLDLINE TEST  v0.3.1")
                && output.contains("3 selected") && output.contains("3 passed")
                && output.contains("WORLDLINE_TEST=PASS") && output.contains("tests=3")
                && output.contains("host-only mode; runtime provider disabled")
                && output.contains("BUILD SUCCESSFUL"), "M774 " + label + " output drift");
    }

    private void verifyAero(Path aero) throws Exception {
        SmokeSupport.require(Files.isDirectory(aero.resolve(".git"))
                || Files.isRegularFile(aero.resolve(".git")), "M774 Aero checkout absent");
        String revision = capture(aero, List.of("git", "rev-parse", "HEAD")).strip();
        SmokeSupport.require(revision.equals(SmokeSupport.value(config, "aero.revision")),
                "M774 Aero revision drift: " + revision);
        String tracked = capture(aero, List.of("git", "ls-files", "tests/worldline"));
        SmokeSupport.require(tracked.lines().count() == 6L && !tracked.contains(".jar"),
                "M774 external suite topology drift");
    }

    private void verifyDistribution() throws Exception {
        Path distribution = root.resolve(".worldline/dist/testkit");
        for (String artifact : List.of("api", "runner")) {
            Path jar = distribution.resolve("worldline-test-" + artifact + "-0.3.1.jar");
            SmokeSupport.require(Files.isRegularFile(jar), "M774 TestKit artifact absent: " + jar);
            SmokeSupport.require(SmokeSupport.digest(jar, "SHA-256").equals(
                    SmokeSupport.value(config, artifact + ".sha256")),
                    "M774 TestKit artifact drift: " + artifact);
        }
    }

    private Path gradleExecutable() throws Exception {
        String override = System.getenv("WORLDLINE_GRADLE_8144");
        if (override != null && !override.isBlank()) return Path.of(override);
        Path distributions = Path.of(System.getProperty("user.home"), ".gradle", "wrapper",
                "dists", "gradle-8.14.4-bin");
        String executable = System.getProperty("os.name").startsWith("Windows")
                ? "gradle.bat" : "gradle";
        try (Stream<Path> paths = Files.walk(distributions)) {
            return paths.filter(path -> path.getFileName().toString().equals(executable))
                    .findFirst().orElseThrow(() -> new IllegalStateException(
                            "Gradle 8.14.4 absent; set WORLDLINE_GRADLE_8144"));
        }
    }

    private String capture(Path directory, List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        SmokeSupport.require(process.waitFor() == 0, "M774 command failed: " + command);
        return output;
    }

    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
