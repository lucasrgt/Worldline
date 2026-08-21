import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Properties;

/**
 * Qualifies the public scenario DSL: authoring, strict validation, and
 * deterministic controlled execution through the repository launcher.
 */
public final class ScenarioDslCycle {
    private static final String ID = "m14-scenario-dsl";
    private static final long SEED = 4242L;
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/ScenarioDslCycle.java " + ID); System.exit(2);
        }
        try { new ScenarioDslCycle().execute(); }
        catch (Exception error) { System.err.println("M14 DSL cycle failed: " + error.getMessage()); System.exit(1); }
    }

    private void execute() throws Exception {
        recreate();
        Path scenario = build.resolve("dsl.wlscenario");
        Path invalid = build.resolve("invalid.wlscenario");
        Path first = build.resolve("first.wltrace"), second = build.resolve("second.wltrace");
        Result created = launcher("scenario", "create", scenario.toString(), "observe:before",
                "block:8,65,8:20", "tick:2", "reseed:101", "observe:after");
        require(created.code == 0 && created.text.contains("WORLDLINE_SCENARIO_CREATE=PASS")
                && created.text.contains("steps=5"), "DSL scenario creation failed");
        Result validated = launcher("scenario", "validate", scenario.toString());
        require(validated.code == 0 && validated.text.contains("WORLDLINE_SCENARIO_VALIDATE=PASS")
                && validated.text.contains("dsl=worldline-scenario-dsl/1")
                && validated.text.contains("2=TICK:tick:2") && validated.text.contains("1=BLOCK:block:8,65,8:20:0"),
                "DSL validation failed");
        Result badCreate = launcher("scenario", "create", invalid.toString(), "tick", "warp:3");
        require(badCreate.code == 0, "invalid scenario creation failed");
        Result badValidate = launcher("scenario", "validate", invalid.toString());
        require(badValidate.code == 1 && badValidate.text.contains("unknown scenario step"),
                "out-of-grammar step was not rejected");
        Result firstRun = launcher("scenario", "run", scenario.toString(), Long.toString(SEED),
                first.toString());
        Result secondRun = launcher("scenario", "run", scenario.toString(), Long.toString(SEED),
                second.toString());
        require(firstRun.code == 0 && secondRun.code == 0, "controlled scenario run failed");
        require(Arrays.equals(Files.readAllBytes(first), Files.readAllBytes(second)),
                "scenario execution is not deterministic");
        String traceHash = line(firstRun.text, "trace.sha256=");
        String report = "steps=5\nseed=" + SEED + "\nverbs=observe,block,tick,reseed"
                + "\nrejected=warp\nrun.deterministic=true\ntrace.sha256=" + traceHash + "\n";
        String signature = sha256(report);
        Properties expected = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(
                root.resolve("smokes").resolve(ID).resolve("smoke.properties"))) {
            expected.load(reader);
        }
        require(signature.equals(expected.getProperty("expected.signature")),
                "M14 DSL evidence diverged: " + signature);
        Files.write(build.resolve("evidence.txt"), report.getBytes(StandardCharsets.UTF_8));
        System.out.println("M14 scenario DSL cycle passed");
        System.out.println("  verbs: observe, block, tick, reseed (tap covered by unit suite)");
        System.out.println("  deterministic trace SHA-256: " + traceHash);
        System.out.println("  evidence SHA-256: " + signature);
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

    private Result launcher(String... arguments) throws Exception {
        java.util.List<String> command = new java.util.ArrayList<>(
                Arrays.asList("java", "tools/replay/Replay.java"));
        command.addAll(Arrays.asList(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new Result(process.waitFor(), output);
    }

    private String line(String text, String prefix) {
        return text.lines().filter(row -> row.startsWith(prefix)).findFirst()
                .orElseThrow(() -> new IllegalStateException("missing " + prefix)).substring(prefix.length());
    }

    private String sha256(String text) throws Exception { byte[] hash = MessageDigest.getInstance("SHA-256")
            .digest(text.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
            for (byte value : hash) result.append(String.format("%02x", value & 255)); return result.toString(); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }
    private static final class Result { final int code; final String text;
        Result(int code, String text) { this.code = code; this.text = text; } }
}
