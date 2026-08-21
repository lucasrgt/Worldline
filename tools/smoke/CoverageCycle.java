import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Qualifies dynamic scenario coverage against the closed semantic catalog:
 * category classification across every public verb, role extraction from an
 * executed trace, and threshold gating through the neutral launcher path.
 */
public final class CoverageCycle {
    private static final String ID = "m18-coverage";
    private static final String SEED = "4242";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/CoverageCycle.java " + ID); System.exit(2);
        }
        try { new CoverageCycle().execute(); }
        catch (Exception error) { System.err.println("M18 coverage cycle failed: " + error.getMessage()); System.exit(1); }
    }

    private void execute() throws Exception {
        recreate();
        Path scenario = build.resolve("spectrum.wlscenario");
        Result created = launcher("scenario", "create", scenario.toString(),
                "observe:before", "reseed:5", "tap:2", "block:8,65,8:20",
                "tick:3", "observe:after");
        require(created.code == 0 && created.text.contains("steps=6"),
                "coverage scenario creation failed");
        Result bare = launcher("coverage", scenario.toString());
        require(bare.code == 0 && bare.text.contains("WORLDLINE_COVERAGE=PASS"),
                "bare coverage failed");
        require(bare.text.contains("categories=[rng, input, tick, world, lab]")
                && bare.text.contains("percent=20/24"), "category classification drifted");
        Path trace = build.resolve("spectrum.wltrace");
        Result executed = launcher("scenario", "run", scenario.toString(), SEED,
                trace.toString());
        require(executed.code == 0, "controlled execution for coverage failed");
        Result withTrace = launcher("coverage", scenario.toString(), trace.toString());
        require(withTrace.code == 0 && withTrace.text.contains("roles=[BLOCK_ID_READ]"),
                "trace role extraction drifted: " + withTrace.text);
        require(lineOf(withTrace.text, "report.sha256=")
                .matches("[0-9a-f]{64}"), "missing report digest");
        require(Files.isRegularFile(scenario.resolveSibling("spectrum.wlscenario.traced.wlcover")),
                ".wlcover artifact was not written");
        cleanCovers();
        Result below = launcher("coverage", scenario.toString(), trace.toString(), "50");
        require(below.code == 3, "threshold gate did not fail closed");
        cleanCovers();
        Result met = launcher("coverage", scenario.toString(), trace.toString(), "20");
        require(met.code == 0, "met threshold was rejected");
        String report = "scenario.steps=6\ncategories=rng,input,tick,world,lab"
                + "\npercent=20\nroles=BLOCK_ID_READ\nthreshold.fail=50"
                + "\nthreshold.pass=20\n";
        String signature = sha256(report);
        Properties expected = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(
                root.resolve("smokes").resolve(ID).resolve("smoke.properties"))) {
            expected.load(reader);
        }
        require(signature.equals(expected.getProperty("expected.signature")),
                "M18 coverage evidence diverged: " + signature);
        Files.write(build.resolve("evidence.txt"), report.getBytes(StandardCharsets.UTF_8));
        System.out.println("M18 coverage cycle passed");
        System.out.println("  verbs mapped to rng/input/tick/world/lab (5 of 24 categories)");
        System.out.println("  executed-trace role: BLOCK_ID_READ; thresholds 50 fail / 20 pass");
        System.out.println("  evidence SHA-256: " + signature);
    }

    private Result launcher(String... arguments) throws Exception {
        List<String> command = new java.util.ArrayList<>(
                Arrays.asList("java", "tools/replay/Replay.java"));
        command.addAll(Arrays.asList(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new Result(process.waitFor(), output.replace('\r', '\n'));
    }

    private String lineOf(String text, String prefix) {
        for (String row : text.split("\n", -1)) {
            if (row.startsWith(prefix)) return row.substring(prefix.length());
        }
        throw new IllegalStateException("missing " + prefix);
    }

    private void cleanCovers() throws Exception {
        try (java.util.stream.Stream<Path> paths = Files.list(build)) {
            for (Path path : paths.filter(path -> path.toString().endsWith(".wlcover"))
                    .collect(java.util.stream.Collectors.toList())) Files.delete(path);
        }
    }

    private void recreate() throws Exception {
        if (Files.exists(build)) {
            try (java.util.stream.Stream<Path> paths = Files.walk(build)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder())
                        .collect(java.util.stream.Collectors.toList())) Files.delete(path);
            }
        }
        Files.createDirectories(build);
    }

    private String sha256(String text) throws Exception { byte[] hash = MessageDigest.getInstance("SHA-256")
            .digest(text.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
            for (byte value : hash) result.append(String.format("%02x", value & 255)); return result.toString(); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }
    private static final class Result { final int code; final String text;
        Result(int code, String text) { this.code = code; this.text = text; } }
}
