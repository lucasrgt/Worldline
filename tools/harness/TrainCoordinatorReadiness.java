import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Fails before train qualification when coordinator-owned generated surfaces are stale. */
final class TrainCoordinatorReadiness {
    private TrainCoordinatorReadiness() { }

    static void execute(Path root) throws Exception {
        require(git(root, "status", "--porcelain=v1", "--untracked-files=all").isBlank(),
                "train worktree contains tracked or untracked changes");
        new ReadmeStatus(root).check();
        new DocumentationCatalog(root).execute();
        System.out.println("train coordinator readiness passed");
    }

    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Path log = Files.createTempFile("worldline-train-readiness-", ".log");
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            require(process.waitFor(60, TimeUnit.SECONDS), "train readiness git command timed out");
            String output = Files.readString(log, StandardCharsets.UTF_8);
            require(process.exitValue() == 0, "train readiness git command failed: " + output);
            return output;
        } finally { Files.deleteIfExists(log); }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
