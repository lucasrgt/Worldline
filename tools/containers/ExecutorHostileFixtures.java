import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Qualifying legacy/Rust differential for hostile Windows process fixtures. */
final class ExecutorHostileFixtures {
    private static final long MEMORY = 512L << 20;
    private static final int PROCESSES = 8;
    private static final long OUTPUT_LIMIT = 2L << 20;

    private ExecutorHostileFixtures() { }

    static void execute(Path root, Path executor) throws Exception {
        Path base = root.resolve(".worldline/executor-hostile-fixtures");
        require(!Files.exists(base), "hostile fixture residue exists");
        Path temporary = base.resolve("private temp"); Files.createDirectories(temporary);
        ExecutionEnvironmentManifest environment = ExecutionEnvironmentManifest.hostile(temporary);
        StringBuilder evidence = new StringBuilder("schema=worldline.executor-hostile-fixtures.v1\n");
        try {
            for (Scenario scenario : scenarios()) {
                Result legacy = legacy(root, base.resolve("legacy-" + scenario.id), scenario, environment);
                Native nativeResult = rust(root, executor, base.resolve("rust-" + scenario.id),
                        scenario, environment);
                compare(scenario, legacy, nativeResult);
                evidence.append(scenario.id).append('=').append(nativeResult.verified.termination())
                        .append(',').append(nativeResult.verified.exitCode()).append(',')
                        .append(ExecutionJson.sha256(nativeResult.bytes)).append(',')
                        .append(nativeResult.bytes.length).append('\n');
            }
            System.out.println("  hostile executor fixtures: " + scenarios().size()
                    + " legacy/Rust differentials; evidence="
                    + ExecutionJson.sha256(ExecutionJson.bytes(evidence.toString())));
        } finally { ProcessTreeTermination.delete(root, base, "hostile fixture workspace"); }
    }

    static void selfTest() {
        List<Scenario> values = scenarios();
        require(values.size() == 7 && values.stream().map(Scenario::id).distinct().count() == values.size(),
                "hostile scenario catalog drift");
        require(values.stream().anyMatch(value -> value.timeoutMs == 1_000L)
                && values.stream().anyMatch(value -> value.arguments.contains("262144")),
                "hostile boundary coverage drift");
    }

    private static Native rust(Path root, Path executor, Path output, Scenario scenario,
            ExecutionEnvironmentManifest environment) throws Exception {
        WorldlineExecutionPlan plan = plan(root, output, command(root, scenario), scenario, environment);
        WorldlineExecutionReceipt.Verified verified = RustExecutorShadow.execute(executor,
                output.resolveSibling(output.getFileName() + "-plan.json"), plan);
        require(!verified.stdoutTruncated() && !verified.stderrTruncated()
                && Long.valueOf(0).equals(verified.descendantsRemaining()),
                "Rust hostile fixture retained descendants or truncated output: " + scenario.id);
        return new Native(verified, stream(scenario, plan.outputs().stdout(), plan.outputs().stderr()));
    }

    private static Result legacy(Path root, Path output, Scenario scenario,
            ExecutionEnvironmentManifest environment) throws Exception {
        Files.createDirectories(output); Path log = output.resolve("console.log");
        Path metrics = output.resolve("metrics.properties");
        List<String> invocation = new ArrayList<>(List.of(WindowsJobBootstrap.ensure().toString(),
                "--memory", Long.toString(MEMORY), "--cpu-rate", "10000", "--active-processes",
                Integer.toString(PROCESSES), "--timeout-seconds", Long.toString(scenario.timeoutMs / 1_000),
                "--parent-pid", Long.toString(ProcessHandle.current().pid()), "--cwd", root.toString(),
                "--log", log.toString(), "--metrics", metrics.toString(), "--"));
        invocation.addAll(command(root, scenario)); ProcessBuilder builder = new ProcessBuilder(invocation)
                .directory(root.toFile()); environment.apply(builder); Process process = builder.start();
        try {
            if (!process.waitFor(scenario.timeoutMs / 1_000 + 20, TimeUnit.SECONDS)) {
                ProcessTreeTermination.kill(process, "legacy hostile fixture");
                throw new IllegalStateException("legacy hostile fixture outer timeout: " + scenario.id);
            }
        } catch (InterruptedException error) {
            ProcessTreeTermination.kill(process, "legacy hostile fixture");
            Thread.currentThread().interrupt(); throw error;
        }
        Properties values = new Properties(); try (var reader = Files.newBufferedReader(metrics)) {
            values.load(reader);
        }
        require("0".equals(values.getProperty("processes.active")),
                "legacy hostile fixture retained descendants: " + scenario.id);
        boolean timedOut = Boolean.parseBoolean(values.getProperty("timed.out"));
        String termination = timedOut ? "timed_out" : "exited";
        Long exitCode = timedOut ? null : Long.valueOf(values.getProperty("exit.code"));
        return new Result(termination, exitCode, Files.readAllBytes(log),
                Long.parseLong(values.getProperty("processes.total")));
    }

    private static WorldlineExecutionPlan plan(Path root, Path output, List<String> command,
            Scenario scenario, ExecutionEnvironmentManifest environment) {
        return new WorldlineExecutionPlan("hostile-" + scenario.id, root, command, environment.environment(),
                new WorldlineExecutionPlan.Limits(scenario.timeoutMs, MEMORY, PROCESSES, null, 100,
                        OUTPUT_LIMIT, OUTPUT_LIMIT),
                new WorldlineExecutionPlan.Isolation("windows_job", "inherit",
                        ProcessHandle.current().pid()), WorldlineExecutionPlan.Filesystem.inherit(),
                new WorldlineExecutionPlan.Outputs(output, output.resolve("stdout.log"),
                        output.resolve("stderr.log"), output.resolve("metrics.json"),
                        output.resolve("manifest.json"), output.resolve("receipt.json")));
    }

    private static List<String> command(Path root, Scenario scenario) {
        List<String> command = new ArrayList<>(List.of(java(), "-Xms16m", "-Xmx128m", "-XX:-UsePerfData",
                root.resolve("tools/containers/ExecutorFixtureProbe.java").toString()));
        command.addAll(scenario.arguments); return command;
    }

    private static byte[] stream(Scenario scenario, Path stdout, Path stderr) throws Exception {
        byte[] selected = Files.readAllBytes(scenario.stderr ? stderr : stdout);
        require(Files.size(scenario.stderr ? stdout : stderr) == 0L,
                "hostile fixture wrote to an unexpected stream: " + scenario.id);
        return selected;
    }

    private static void compare(Scenario scenario, Result legacy, Native nativeResult) {
        require(legacy.termination.equals(nativeResult.verified.termination())
                && java.util.Objects.equals(legacy.exitCode, nativeResult.verified.exitCode()),
                "hostile termination differential: " + scenario.id);
        require(Arrays.equals(legacy.bytes, nativeResult.bytes),
                "hostile output differential: " + scenario.id);
        require(legacy.processes >= scenario.minimumProcesses,
                "legacy process accounting drift: " + scenario.id);
    }

    private static List<Scenario> scenarios() {
        return List.of(
                new Scenario("normal", List.of("normal", "lambda-λ path with spaces"), 10_000L, false, 1),
                new Scenario("stderr", List.of("stderr", "beta-β"), 10_000L, true, 1),
                new Scenario("nonzero", List.of("exit", "23"), 10_000L, false, 1),
                new Scenario("bulk", List.of("bulk", "262144"), 10_000L, false, 1),
                new Scenario("environment", List.of("environment"), 10_000L, false, 1),
                new Scenario("tree-timeout", List.of("tree-root"), 1_000L, false, 3),
                new Scenario("repeat-normal", List.of("normal", "restart-after-timeout"), 10_000L, false, 1));
    }

    private static String java() {
        return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    private record Scenario(String id, List<String> arguments, long timeoutMs,
            boolean stderr, long minimumProcesses) { }
    private record Result(String termination, Long exitCode, byte[] bytes, long processes) { }
    private record Native(WorldlineExecutionReceipt.Verified verified, byte[] bytes) { }
}
