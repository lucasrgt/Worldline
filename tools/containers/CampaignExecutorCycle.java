import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Runs one Java-owned campaign shard inside the native executor. */
public final class CampaignExecutorCycle {
    private static final long MEMORY = 2L << 30;
    private static final long OUTPUT_LIMIT = 1L << 20;

    private CampaignExecutorCycle() { }

    public static void main(String[] arguments) {
        try { execute(arguments); }
        catch (Exception error) {
            System.err.println("campaign executor failed: " + error.getMessage()); System.exit(1);
        }
    }

    private static void execute(String[] arguments) throws Exception {
        require(arguments.length > 0 && !List.of(arguments).contains("--executor-plan"),
                "campaign executor requires ordinary Nightly campaign arguments");
        Path root = Path.of("").toAbsolutePath().normalize();
        Path classes = root.resolve(".worldline/gate/classes");
        require(Files.isRegularFile(classes.resolve("NightlyQualityCampaign.class")),
                "run the canonical Gate before the native campaign");
        Path runs = root.resolve(".worldline/reports/nightly-quality/executor-runs");
        Files.createDirectories(runs);
        Path run = Files.createTempDirectory(runs, "run-").toAbsolutePath().normalize();
        Path planPath = run.resolve("plan.json"), output = run.resolve("output"), temporary = run.resolve("tmp");
        Files.createDirectory(temporary);
        List<String> command = new ArrayList<>(List.of(javaTool(), "-cp", classes.toString(),
                "NightlyQualityCampaign"));
        command.addAll(List.of(arguments)); command.addAll(List.of("--executor-plan", planPath.toString()));
        WorldlineExecutionPlan plan = plan(root, planPath, output, temporary, command, budget(arguments));
        WorldlineExecutionReceipt.Verified verified = RustExecutorShadow.execute(executor(), planPath, plan);
        require("exited".equals(verified.termination()) && Long.valueOf(0L).equals(verified.exitCode()),
                "native campaign shard did not exit successfully");
        require(!verified.stdoutTruncated() && !verified.stderrTruncated(),
                "native campaign output was truncated");
        System.out.print(Files.readString(plan.outputs().stdout(), StandardCharsets.UTF_8));
        System.out.println("nightly native executor passed: request=" + plan.requestSha256()
                + ", evidence=" + root.relativize(run));
    }

    private static WorldlineExecutionPlan plan(Path root, Path planPath, Path output,
            Path temporary, List<String> command, int budget) {
        boolean windows = isWindows();
        return new WorldlineExecutionPlan("nightly-quality-shard", root, command,
                ExecutionEnvironmentManifest.campaign(temporary, planPath).environment(),
                new WorldlineExecutionPlan.Limits((budget + 60L) * 1_000L, MEMORY,
                        windows ? 8 : null, windows ? null : 128, 100, OUTPUT_LIMIT, OUTPUT_LIMIT),
                new WorldlineExecutionPlan.Isolation(windows ? "windows_job" : "linux_cgroup",
                        "inherit", ProcessHandle.current().pid()), WorldlineExecutionPlan.Filesystem.inherit(),
                new WorldlineExecutionPlan.Outputs(output, output.resolve("stdout.log"),
                        output.resolve("stderr.log"), output.resolve("metrics.json"),
                        output.resolve("manifest.json"), output.resolve("receipt.json")));
    }

    static void selfTest() {
        Path root = Path.of("").toAbsolutePath().normalize(), run = root.resolve(".worldline/campaign-self-test");
        Path planPath = run.resolve("plan.json"), output = run.resolve("output"), temporary = run.resolve("tmp");
        WorldlineExecutionPlan plan = plan(root, planPath, output, temporary,
                List.of(javaTool(), "-version", "--executor-plan", planPath.toString()), 10);
        require(plan.limits().timeoutMs() == 70_000L && plan.canonicalBytes().length > 0,
                "campaign executor plan drifted");
        expectFailure(() -> budget(new String[] {"--budget-seconds", "9"}));
    }

    private static int budget(String[] arguments) {
        Integer value = null;
        for (int index = 0; index < arguments.length; index++) if (arguments[index].equals("--budget-seconds")) {
            require(value == null && index + 1 < arguments.length, "invalid campaign budget option");
            try { value = Integer.parseInt(arguments[++index]); }
            catch (NumberFormatException error) {
                throw new IllegalArgumentException("campaign budget is not numeric");
            }
        }
        require(value != null && value >= 10 && value <= 7_200, "campaign budget must be between 10 and 7200");
        return value;
    }

    private static Path executor() {
        String value = System.getenv("WORLDLINE_EXECUTOR");
        require(value != null && !value.isBlank(), "WORLDLINE_EXECUTOR is required");
        Path path = Path.of(value).toAbsolutePath().normalize();
        require(path.toString().equals(value) && Files.isRegularFile(path), "WORLDLINE_EXECUTOR is invalid");
        return path;
    }
    private static String javaTool() { return Path.of(System.getProperty("java.home"), "bin",
            "java" + (isWindows() ? ".exe" : "")).toString(); }
    private static boolean isWindows() { return System.getProperty("os.name", "")
            .toLowerCase(Locale.ROOT).contains("win"); }
    private static void expectFailure(Action action) { try { action.run();
        throw new IllegalStateException("invalid campaign executor input was accepted");
    } catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    @FunctionalInterface private interface Action { void run(); }
}
