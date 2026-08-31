import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Non-qualifying adapter for validating the Rust executor beside legacy Runtime Fabric paths. */
public final class RustExecutorShadow {
    private RustExecutorShadow() { }

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--self-test")),
                    "usage: java tools/containers/RustExecutorShadow.java --self-test");
            selfTest();
        } catch (Exception error) {
            System.err.println("Rust executor shadow failed: " + error.getMessage()); System.exit(1);
        }
    }

    static WorldlineExecutionReceipt.Verified execute(Path executable, Path planPath,
            WorldlineExecutionPlan plan) throws Exception {
        require(executable.isAbsolute() && Files.isRegularFile(executable),
                "WORLDLINE_EXECUTOR is not an absolute regular file");
        require(planPath.isAbsolute() && planPath.normalize().equals(planPath), "plan path is not normalized");
        plan.write(planPath); Path launcherLog = planPath.resolveSibling(planPath.getFileName() + ".launcher.log");
        Process process = new ProcessBuilder(invocation(executable, planPath, plan))
                .directory(planPath.getParent().toFile()).redirectErrorStream(true)
                .redirectOutput(launcherLog.toFile()).start();
        long waitMillis = Math.addExact(plan.limits().timeoutMs(), 30_000L);
        if (!process.waitFor(waitMillis, TimeUnit.MILLISECONDS)) {
            ProcessTreeTermination.kill(process, "native executor");
            throw new IllegalStateException("native executor exceeded its outer timeout");
        }
        WorldlineExecutionReceipt.Verified verified;
        try { verified = WorldlineExecutionReceipt.verify(plan); }
        catch (Exception error) {
            String output = Files.readString(launcherLog, StandardCharsets.UTF_8);
            throw new IllegalStateException("native receipt verification failed\n" + output, error);
        }
        require(process.exitValue() == cliExit(verified), "native executor CLI exit drift");
        return verified;
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-executor-contract-").toAbsolutePath().normalize();
        try {
            WorldlineExecutionPlan plan = directPlan(root);
            byte[] canonical = plan.canonicalBytes();
            require(canonical[canonical.length - 1] == '\n' && new String(canonical, StandardCharsets.UTF_8)
                    .startsWith("{\"schema\":\"worldline.execution-plan.v1\""), "plan canonical encoding drift");
            synthetic(plan); WorldlineExecutionReceipt.verify(plan);
            WorldlineExecutionPlan linux = containedPlan(root, "linux_cgroup");
            synthetic(linux); WorldlineExecutionReceipt.verify(linux);
            Path receipt = plan.outputs().receipt(); String valid = Files.readString(receipt);
            Files.writeString(receipt, valid.replace("{\"schema\"", "{\"unknown\":true,\"schema\""));
            expectFailure(() -> WorldlineExecutionReceipt.verify(plan)); Files.writeString(receipt, valid);
            Files.writeString(plan.outputs().stdout(), "tampered", StandardCharsets.UTF_8);
            expectFailure(() -> WorldlineExecutionReceipt.verify(plan));
            liveProbe(root);
            System.out.println("Rust executor shadow contract self-test passed");
        } finally { delete(root); }
    }

    private static WorldlineExecutionPlan directPlan(Path root) {
        Path output = root.resolve("synthetic");
        return new WorldlineExecutionPlan("shadow-contract", root, List.of(java(), "-version"),
                WorldlineExecutionPlan.Environment.empty(),
                new WorldlineExecutionPlan.Limits(5_000, null, null, null, null, null, null),
                new WorldlineExecutionPlan.Isolation("direct", "inherit", null),
                new WorldlineExecutionPlan.Outputs(output, output.resolve("stdout.log"),
                        output.resolve("stderr.log"), output.resolve("metrics.json"),
                        output.resolve("receipt.json")));
    }

    private static WorldlineExecutionPlan containedPlan(Path root, String backend) {
        Path output = root.resolve(backend);
        return new WorldlineExecutionPlan("shadow-" + backend.replace('_', '-'), root,
                List.of(java(), "-version"), WorldlineExecutionPlan.Environment.empty(),
                new WorldlineExecutionPlan.Limits(5_000, 512L << 20,
                        backend.equals("windows_job") ? 4 : null,
                        backend.equals("linux_cgroup") ? 128 : null, 100, 1L << 20, 1L << 20),
                new WorldlineExecutionPlan.Isolation(backend, "inherit", ProcessHandle.current().pid()),
                new WorldlineExecutionPlan.Outputs(output, output.resolve("stdout.log"),
                        output.resolve("stderr.log"), output.resolve("metrics.json"),
                        output.resolve("receipt.json")));
    }

    private static void synthetic(WorldlineExecutionPlan plan) throws Exception {
        Files.createDirectories(plan.outputs().root()); Files.write(plan.outputs().stdout(), new byte[0]);
        Files.write(plan.outputs().stderr(), new byte[0]);
        boolean direct = plan.isolation().backend().equals("direct");
        String measurement = plan.isolation().backend().equals("linux_cgroup") ? "cgroup_peak"
                : direct ? "unavailable" : "job_commit";
        String accounting = direct ? "null" : "1";
        String metrics = "{\"schema\":\"worldline.execution-metrics.v1\",\"elapsed_ms\":1,"
                + "\"peak_memory_bytes\":" + accounting + ",\"memory_measurement\":\"" + measurement + "\","
                + "\"peak_processes\":" + accounting + ",\"cpu_user_100ns\":" + accounting
                + ",\"cpu_kernel_100ns\":" + accounting + ",\"io_read_bytes\":" + accounting
                + ",\"io_write_bytes\":" + accounting + ",\"stdout_observed_bytes\":0,"
                + "\"stderr_observed_bytes\":0,\"stdout_truncated\":false,\"stderr_truncated\":false,"
                + "\"telemetry_qualification\":\"observational\"}";
        ExecutionJson.writeAtomic(plan.outputs().metrics(), ExecutionJson.bytes(metrics));
        String empty = ExecutionJson.sha256(new byte[0]);
        String receipt = "{\"schema\":\"worldline.execution-receipt.v1\",\"request_sha256\":"
                + ExecutionJson.quote(plan.requestSha256()) + ",\"executor_version\":\"self-test\","
                + "\"backend\":" + ExecutionJson.quote(plan.isolation().backend())
                + ",\"exit_code\":0,\"termination\":\"exited\","
                + "\"stdout_sha256\":" + ExecutionJson.quote(empty) + ",\"stderr_sha256\":"
                + ExecutionJson.quote(empty) + ",\"metrics_sha256\":"
                + ExecutionJson.quote(ExecutionJson.sha256(plan.outputs().metrics()))
                + ",\"stdout_observed_bytes\":0,\"stderr_observed_bytes\":0,"
                + "\"stdout_truncated\":false,\"stderr_truncated\":false,"
                + "\"descendants_remaining\":" + (direct ? "null" : "0") + ",\"error\":null}";
        ExecutionJson.writeAtomic(plan.outputs().receipt(), ExecutionJson.bytes(receipt));
    }

    private static void liveProbe(Path root) throws Exception {
        String configured = System.getenv("WORLDLINE_EXECUTOR"); if (configured == null || configured.isBlank()) return;
        String os = System.getProperty("os.name").toLowerCase();
        boolean windows = os.contains("win"), linux = os.contains("linux"), contained = windows || linux;
        Path output = root.resolve("live"); List<String> command = List.of(java(), "-Xms16m", "-Xmx64m", "-version");
        WorldlineExecutionPlan plan = new WorldlineExecutionPlan(
                "shadow-live", root, command, WorldlineExecutionPlan.Environment.empty(),
                new WorldlineExecutionPlan.Limits(10_000, contained ? 512L << 20 : null,
                        windows ? 4 : null, linux ? 128 : null, contained ? 100 : null,
                        contained ? 1L << 20 : null, contained ? 1L << 20 : null),
                new WorldlineExecutionPlan.Isolation(windows ? "windows_job" : linux ? "linux_cgroup" : "direct",
                        "inherit", contained ? ProcessHandle.current().pid() : null),
                new WorldlineExecutionPlan.Outputs(output, output.resolve("stdout.log"), output.resolve("stderr.log"),
                        output.resolve("metrics.json"), output.resolve("receipt.json")));
        WorldlineExecutionReceipt.Verified result = execute(Path.of(configured).toAbsolutePath().normalize(),
                root.resolve("live-plan.json"), plan);
        require("exited".equals(result.termination()) && Long.valueOf(0).equals(result.exitCode()),
                "live native executor probe failed: " + result);
        if (windows) legacyDifferential(root, command, plan);
    }

    private static List<String> invocation(Path executable, Path planPath, WorldlineExecutionPlan plan) {
        if (!plan.isolation().backend().equals("linux_cgroup"))
            return List.of(executable.toString(), "execute", planPath.toString());
        return List.of("systemd-run", "--user", "--scope", "--quiet", "--collect", "-p", "Delegate=yes",
                "--", executable.toString(), "execute", planPath.toString());
    }

    private static void legacyDifferential(Path root, List<String> command, WorldlineExecutionPlan rust)
            throws Exception {
        Path legacy = root.resolve("legacy"); Files.createDirectories(legacy);
        Path log = legacy.resolve("console.log"), metrics = legacy.resolve("metrics.properties");
        List<String> invocation = new ArrayList<>(List.of(WindowsJobBootstrap.ensure().toString(),
                "--memory", Long.toString(512L << 20), "--cpu-rate", "10000",
                "--active-processes", "4", "--timeout-seconds", "10", "--parent-pid",
                Long.toString(ProcessHandle.current().pid()), "--cwd", root.toString(), "--log",
                log.toString(), "--metrics", metrics.toString(), "--"));
        invocation.addAll(command); Process process = new ProcessBuilder(invocation).directory(root.toFile()).start();
        require(process.waitFor(20, TimeUnit.SECONDS) && process.exitValue() == 0,
                "legacy Windows Job differential failed");
        Properties values = new Properties(); try (var reader = Files.newBufferedReader(metrics)) { values.load(reader); }
        require("windows-job".equals(values.getProperty("backend")) && "0".equals(values.getProperty("exit.code")),
                "legacy Windows Job metrics drift");
        require(Arrays.equals(Files.readAllBytes(log), Files.readAllBytes(rust.outputs().stderr()))
                && Files.size(rust.outputs().stdout()) == 0, "legacy/Rust output differential");
    }

    private static int cliExit(WorldlineExecutionReceipt.Verified value) {
        if (value.termination().equals("timed_out")) return 124;
        if (value.termination().equals("spawn_failed") || value.termination().equals("wait_failed")
                || value.termination().equals("owner_exited")) return 125;
        if (value.termination().equals("exited") && value.exitCode() >= 0 && value.exitCode() < 124)
            return value.exitCode().intValue();
        return 1;
    }

    private static String java() { boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", "java" + (windows ? ".exe" : "")).toString(); }
    private static void delete(Path root) throws Exception { try (var paths = Files.walk(root)) {
        for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(path); } }
    private static void expectFailure(Checked action) throws Exception { try { action.run();
        throw new IllegalStateException("invalid executor evidence was accepted"); } catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) { if (!value) throw new IllegalArgumentException(message); }
    @FunctionalInterface private interface Checked { void run() throws Exception; }
}
