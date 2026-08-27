import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Compiles the exact standalone Ox Alpha launcher closure before execution. */
public final class OxAlphaLauncher {
    private OxAlphaLauncher() {
    }

    public static void main(String[] arguments) {
        try {
            Path root = Path.of("").toAbsolutePath().normalize();
            Path output = root.resolve(".worldline/build/ox-alpha-launcher");
            Files.createDirectories(output);
            List<String> compile = List.of(javaTool("javac"), "-encoding", "UTF-8", "--release", "21",
                    "-Xlint:all,-options", "-Werror", "-d", output.toString(),
                    root.resolve("tools/integration/OxAlphaProfile.java").toString(),
                    root.resolve("tools/integration/OxAlphaRequest.java").toString(),
                    root.resolve("tools/integration/OxAlphaLegacyAdoption.java").toString(),
                    root.resolve("tools/integration/OxAlphaTelemetry.java").toString(),
                    root.resolve("tools/integration/OxAlphaProviderFailure.java").toString(),
                    root.resolve("tools/integration/OxAlphaProviderLogMonitor.java").toString(),
                    root.resolve("tools/integration/OxAlphaProviderCapture.java").toString(),
                    root.resolve("tools/integration/OxAlphaTerminalMonitor.java").toString(),
                    root.resolve("tools/integration/OxAlphaProcessFixture.java").toString(),
                    root.resolve("tools/integration/OxAlphaWorkerSelfTest.java").toString(),
                    root.resolve("tools/integration/OxAlphaWorker.java").toString(),
                    root.resolve("tools/integration/OxAlphaLauncher.java").toString());
            require(run(root, compile, 120) == 0, "Ox Alpha launcher closure did not compile");
            List<String> command = new ArrayList<>(List.of(javaTool("java"), "-cp", output.toString(),
                    "OxAlphaWorker"));
            command.addAll(List.of(arguments));
            System.exit(run(root, command, launcherSeconds(arguments)));
        } catch (Exception exception) {
            System.err.println("Ox Alpha source launcher failed: " + exception.getMessage());
            System.exit(1);
        }
    }

    static int run(Path root, List<String> command, int seconds) throws Exception {
        Process process = new ProcessBuilder(command).directory(root.toFile()).inheritIO().start();
        try {
            process.getOutputStream().close();
            if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
                stop(process);
                throw new IllegalStateException(command.get(0) + " timed out");
            }
            return process.exitValue();
        } catch (Exception failure) {
            if (process.isAlive()) {
                stop(process);
            }
            throw failure;
        }
    }

    private static void stop(Process process) throws Exception {
        List<ProcessHandle> observed = new ArrayList<>();
        process.toHandle().descendants().forEach(observed::add);
        process.destroyForcibly();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            List<ProcessHandle> additions = new ArrayList<>();
            observed.forEach(handle -> handle.descendants().forEach(additions::add));
            additions.stream().filter(handle -> !observed.contains(handle)).forEach(observed::add);
            observed.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroyForcibly);
            if (!process.isAlive() && observed.stream().noneMatch(ProcessHandle::isAlive)) {
                return;
            }
            Thread.sleep(50);
        }
        require(!process.isAlive() && observed.stream().noneMatch(ProcessHandle::isAlive),
                "source launcher process and observed descendants did not stop");
    }

    private static int launcherSeconds(String[] arguments) {
        int workerSeconds = 3600;
        for (int index = 0; index + 1 < arguments.length; index += 2) {
            if (arguments[index].equals("--timeout-seconds")) {
                workerSeconds = Integer.parseInt(arguments[index + 1]);
            }
        }
        return Math.addExact(workerSeconds, 100);
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : ""))
                .toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
