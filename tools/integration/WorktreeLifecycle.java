import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Audits worktrees and archives only clean, integrated worktrees with a verified bundle. */
public final class WorktreeLifecycle {
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try { new WorktreeLifecycle().execute(arguments); }
        catch (Exception error) {
            System.err.println("worktree lifecycle failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute(String[] arguments) throws Exception {
        require(arguments.length > 0, usage());
        if ("audit".equals(arguments[0])) {
            String base = arguments.length == 3 && "--base".equals(arguments[1]) ? arguments[2] : "HEAD";
            require(arguments.length == 1 || arguments.length == 3, usage());
            audit(base);
        } else if ("archive".equals(arguments[0])) {
            archive(arguments);
        } else if ("prune".equals(arguments[0])) {
            require(arguments.length == 1, usage());
            System.out.print(git(root, "worktree", "prune", "--dry-run", "--verbose"));
        } else throw new IllegalArgumentException(usage());
    }

    private void audit(String reference) throws Exception {
        String base = git(root, "rev-parse", "--verify", reference + "^{commit}").trim();
        List<Worktree> worktrees = discover();
        int workers = Math.max(1, Math.min(16, integerEnvironment("WORLDLINE_WORKTREE_WORKERS", 8)));
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        List<Future<State>> futures = new ArrayList<>();
        for (Worktree worktree : worktrees) futures.add(executor.submit(() -> inspect(worktree, base)));
        executor.shutdown();
        int dirty = 0, integrated = 0, removable = 0;
        StringBuilder json = new StringBuilder("{\n  \"schema\":1,\n  \"created\":\"")
                .append(Instant.now()).append("\",\n  \"base\":\"").append(base).append("\",\n  \"worktrees\":[\n");
        for (int index = 0; index < worktrees.size(); index++) {
            Worktree worktree = worktrees.get(index);
            State state = futures.get(index).get();
            boolean exists = state.exists, isDirty = state.dirty, isIntegrated = state.integrated;
            boolean safe = exists && !isDirty && isIntegrated && !worktree.path.equals(root);
            if (isDirty) dirty++;
            if (isIntegrated) integrated++;
            if (safe) removable++;
            json.append("    {\"path\":\"").append(escape(worktree.path.toString()))
                    .append("\",\"head\":\"").append(worktree.head).append("\",\"branch\":\"")
                    .append(escape(worktree.branch)).append("\",\"exists\":").append(exists)
                    .append(",\"dirty\":").append(isDirty).append(",\"integrated\":")
                    .append(isIntegrated).append(",\"archiveEligible\":").append(safe).append("}")
                    .append(index + 1 == worktrees.size() ? "\n" : ",\n");
        }
        json.append("  ]\n}\n");
        Path report = root.resolve(".worldline/reports/worktrees.json");
        Files.createDirectories(report.getParent());
        Files.writeString(report, json, StandardCharsets.UTF_8);
        System.out.println("worktree audit: total=" + worktrees.size() + ", dirty=" + dirty
                + ", integrated=" + integrated + ", archive-eligible=" + removable);
        System.out.println("  report: " + root.relativize(report));
    }

    private State inspect(Worktree worktree, String base) throws Exception {
        boolean exists = Files.isDirectory(worktree.path);
        boolean dirty = exists && !git(worktree.path, "status", "--porcelain").isBlank();
        boolean integrated = status(root, "merge-base", "--is-ancestor", worktree.head, base) == 0;
        return new State(exists, dirty, integrated);
    }

    private void archive(String[] arguments) throws Exception {
        Path target = null, bundles = null; String baseRef = "HEAD";
        for (int index = 1; index < arguments.length; index++) {
            require(index + 1 < arguments.length, usage());
            switch (arguments[index]) {
                case "--path" -> target = Path.of(arguments[++index]).toAbsolutePath().normalize();
                case "--bundles" -> bundles = Path.of(arguments[++index]).toAbsolutePath().normalize();
                case "--base" -> baseRef = arguments[++index];
                default -> throw new IllegalArgumentException(usage());
            }
        }
        require(target != null && bundles != null, usage());
        Path selected = target;
        require(!selected.equals(root) && selected.getNameCount() > 2, "refusing broad/current worktree target");
        Worktree worktree = discover().stream().filter(item -> item.path.equals(selected)).findFirst()
                .orElseThrow(() -> new IllegalStateException("not a registered worktree: " + selected));
        require(Files.isDirectory(selected), "worktree is missing: " + selected);
        require(git(selected, "status", "--porcelain").isBlank(), "worktree is dirty: " + selected);
        String base = git(root, "rev-parse", "--verify", baseRef + "^{commit}").trim();
        require(status(root, "merge-base", "--is-ancestor", worktree.head, base) == 0,
                "worktree head is not integrated into " + baseRef);
        require(!worktree.branch.isBlank(), "detached worktree cannot be bundled by branch");
        Files.createDirectories(bundles);
        String safeName = worktree.branch.replaceAll("[^A-Za-z0-9._-]", "-");
        Path bundle = bundles.resolve(safeName + "-" + worktree.head.substring(0, 12) + ".bundle");
        require(!Files.exists(bundle), "bundle already exists: " + bundle);
        git(root, "bundle", "create", bundle.toString(), worktree.branch);
        git(root, "bundle", "verify", bundle.toString());
        git(root, "worktree", "remove", selected.toString());
        System.out.println("archived worktree " + selected + " to " + bundle);
        System.out.println("branch retained: " + worktree.branch);
    }

    private List<Worktree> discover() throws Exception {
        List<Worktree> result = new ArrayList<>();
        Path path = null; String head = "", branch = "";
        for (String line : git(root, "worktree", "list", "--porcelain").lines().toList()) {
            if (line.isBlank()) {
                if (path != null) result.add(new Worktree(path, head, branch));
                path = null; head = ""; branch = "";
            } else if (line.startsWith("worktree ")) path = Path.of(line.substring(9)).toAbsolutePath().normalize();
            else if (line.startsWith("HEAD ")) head = line.substring(5);
            else if (line.startsWith("branch ")) branch = line.substring(7).replaceFirst("^refs/heads/", "");
        }
        if (path != null) result.add(new Worktree(path, head, branch));
        return result;
    }

    private static String git(Path directory, String... arguments) throws Exception {
        Path log = Files.createTempFile("worldline-worktree-git-", ".log");
        Process process = command(directory, arguments).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            if (!process.waitFor(120, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IllegalStateException("git timed out: " + String.join(" ", arguments));
            }
            String output = Files.readString(log, StandardCharsets.UTF_8);
            require(process.exitValue() == 0, "git " + String.join(" ", arguments) + " failed:\n" + output);
            return output;
        } finally { Files.deleteIfExists(log); }
    }

    private static int status(Path directory, String... arguments) throws Exception {
        Process process = command(directory, arguments).redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException("git timed out: " + String.join(" ", arguments));
        }
        return process.exitValue();
    }

    private static ProcessBuilder command(Path directory, String... arguments) {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        return new ProcessBuilder(command).directory(directory.toFile());
    }

    private static String usage() {
        return "usage: java tools/integration/WorktreeLifecycle.java audit [--base REF] | prune | "
                + "archive --path PATH --bundles DIR [--base REF]";
    }

    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    private static int integerEnvironment(String name, int fallback) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) return fallback;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException error) { throw new IllegalArgumentException(name + " must be an integer"); }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private record Worktree(Path path, String head, String branch) {}
    private record State(boolean exists, boolean dirty, boolean integrated) {}
}
