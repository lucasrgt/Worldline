import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Compiles the exact lifecycle source closure before invoking the worktree tool. */
public final class WorktreeLifecycleLauncher {
    private WorktreeLifecycleLauncher() { }

    public static void main(String[] arguments) {
        try {
            int status = execute(arguments);
            if (status != 0) System.exit(status);
        } catch (Exception error) {
            System.err.println("worktree lifecycle launcher failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static int execute(String[] arguments) throws Exception {
        require(arguments.length > 0, "missing WorktreeLifecycle command");
        Path root = Path.of("").toAbsolutePath().normalize();
        Path output = root.resolve(".worldline/build/worktree-lifecycle");
        Files.createDirectories(output);
        List<Path> sources = List.of(
                root.resolve("tools/integration/WorktreeLifecycle.java"),
                root.resolve("tools/integration/WorktreeRetraction.java"),
                root.resolve("tools/integration/WorktreeArchiveDisposition.java"),
                root.resolve("tools/integration/WorktreePrivateCleanup.java"));
        List<String> compile = new ArrayList<>(List.of(javaTool("javac"), "-encoding", "UTF-8",
                "--release", "21", "-Xlint:all,-options", "-Werror", "-d", output.toString()));
        for (Path source : sources) {
            require(Files.isRegularFile(source), "missing lifecycle source: " + source);
            compile.add(source.toString());
        }
        require(run(root, compile, 120) == 0, "lifecycle source closure did not compile");
        List<String> command = new ArrayList<>(List.of(javaTool("java"), "-cp", output.toString(),
                "WorktreeLifecycle"));
        command.addAll(List.of(arguments));
        return run(root, command, 900);
    }

    private static int run(Path root, List<String> command, int seconds) throws Exception {
        Process process = new ProcessBuilder(command).directory(root.toFile()).inheritIO().start();
        if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException(command.get(0) + " timed out");
        }
        return process.exitValue();
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : ""))
                .toString();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
