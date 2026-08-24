import java.io.*; import java.nio.charset.*; import java.nio.file.*;
import java.nio.file.attribute.*; import java.time.*; import java.util.*;
import java.util.concurrent.*;

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
        } else if ("triage".equals(arguments[0])) {
            String base = arguments.length == 3 && "--base".equals(arguments[1]) ? arguments[2] : "HEAD";
            require(arguments.length == 1 || arguments.length == 3, usage());
            triage(base);
        } else if ("archive".equals(arguments[0])) {
            archive(arguments);
        } else if ("--self-test".equals(arguments[0])) {
            require(arguments.length == 1, usage()); selfTestCleanup();
        } else if ("prune".equals(arguments[0])) {
            require(arguments.length == 1, usage());
            System.out.print(git(root, "worktree", "prune", "--dry-run", "--verbose"));
        } else throw new IllegalArgumentException(usage());
    }

    private void audit(String reference) throws Exception {
        String base = git(root, "rev-parse", "--verify", reference + "^{commit}").trim();
        List<Worktree> worktrees = discover();
        Set<String> receipts = receiptHeads(base);
        Set<String> retractions = WorktreeRetraction.ids(root, base);
        int workers = Math.max(1, Math.min(16, integerEnvironment("WORLDLINE_WORKTREE_WORKERS", 8)));
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        List<Future<State>> futures = new ArrayList<>();
        for (Worktree worktree : worktrees)
            futures.add(executor.submit(() -> inspect(worktree, base, receipts, retractions)));
        executor.shutdown();
        int dirty = 0, integrated = 0, retracted = 0, husks = 0, milestoneDirty = 0, removable = 0;
        StringBuilder json = new StringBuilder("{\n  \"schema\":1,\n  \"created\":\"")
                .append(Instant.now()).append("\",\n  \"base\":\"").append(base).append("\",\n  \"worktrees\":[\n");
        for (int index = 0; index < worktrees.size(); index++) {
            Worktree worktree = worktrees.get(index);
            State state = futures.get(index).get();
            boolean exists = state.exists, isDirty = state.dirty, isIntegrated = state.integrated;
            boolean safe = exists && !isDirty && (isIntegrated && !state.husk || state.retracted)
                    && !worktree.path.equals(root);
            if (isDirty) dirty++;
            if (isIntegrated) integrated++;
            if (state.retracted) retracted++;
            if (state.husk) husks++;
            if (state.milestoneDirty) milestoneDirty++;
            if (safe) removable++;
            json.append("    {\"path\":\"").append(escape(worktree.path.toString()))
                    .append("\",\"head\":\"").append(worktree.head).append("\",\"branch\":\"")
                    .append(escape(worktree.branch)).append("\",\"exists\":").append(exists)
                    .append(",\"dirty\":").append(isDirty).append(",\"integrated\":")
                    .append(isIntegrated).append(",\"husk\":").append(state.husk)
                    .append(",\"retracted\":").append(state.retracted)
                    .append(",\"milestoneDirty\":").append(state.milestoneDirty)
                    .append(",\"archiveEligible\":").append(safe).append("}")
                    .append(index + 1 == worktrees.size() ? "\n" : ",\n");
        }
        json.append("  ]\n}\n");
        Path report = root.resolve(".worldline/reports/worktrees.json");
        Files.createDirectories(report.getParent());
        Files.writeString(report, json, StandardCharsets.UTF_8);
        System.out.println("worktree audit: total=" + worktrees.size() + ", dirty=" + dirty
                + ", integrated=" + integrated + ", retracted=" + retracted + ", husks=" + husks
                + ", milestone-dirty="
                + milestoneDirty + ", archive-eligible=" + removable);
        System.out.println("  report: " + root.relativize(report));
    }

    private void triage(String reference) throws Exception {
        try {
            Class<?> type = Class.forName("BranchTriage");
            var method = type.getDeclaredMethod("write", Path.class, String.class);
            method.setAccessible(true); method.invoke(null, root, reference);
        } catch (ClassNotFoundException missing) {
            Path source = root.resolve("tools/integration/BranchTriage.java");
            Process process = new ProcessBuilder(javaTool(), source.toString(), "--base", reference)
                    .directory(root.toFile()).inheritIO().start();
            require(process.waitFor(120, TimeUnit.SECONDS) && process.exitValue() == 0,
                    "branch triage source launcher failed");
        }
    }

    private State inspect(Worktree worktree, String base, Set<String> receipts,
            Set<String> retractions) throws Exception {
        boolean exists = Files.isDirectory(worktree.path);
        String changes = exists ? git(worktree.path, "status", "--porcelain") : "";
        boolean dirty = !changes.isBlank();
        boolean integrated = integrated(worktree.head, base, receipts);
        if (worktree.branch.startsWith("codex/experiment-")) require(
                experimentDeferred(worktree), "experiment branch lacks a branch-bound NWC deferment: "
                        + worktree.branch);
        return new State(exists, dirty, integrated,
                WorktreeRetraction.matches(worktree.branch, worktree.path, retractions),
                husk(worktree), milestoneDirty(changes));
    }

    private boolean experimentDeferred(Worktree worktree) throws Exception {
        List<String> paths = git(root, "ls-tree", "-r", "--name-only", worktree.head,
                ".csm/nwc/deferments").lines().filter(line -> line.endsWith(".toml")).toList();
        boolean bound = paths.stream().anyMatch(path -> {
            try { return git(root, "show", worktree.head + ":" + path).contains(worktree.branch); }
            catch (Exception error) { throw new IllegalStateException(error); }
        });
        if (!bound) return false;
        require(Files.isDirectory(worktree.path), "experiment worktree is unavailable for NWC validation");
        return commandStatus(worktree.path, List.of("csm", "nwc", "check"), 60) == 0;
    }

    private void archive(String[] arguments) throws Exception {
        Path target = null, bundles = null; String baseRef = "HEAD";
        boolean allowHusk = false, allowRetracted = false;
        for (int index = 1; index < arguments.length; index++) {
            if ("--allow-husk".equals(arguments[index])) { allowHusk = true; continue; }
            if ("--allow-retracted".equals(arguments[index])) { allowRetracted = true; continue; }
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
        boolean integrated = integrated(worktree.head, base, receiptHeads(base));
        boolean retracted = WorktreeRetraction.matches(worktree.branch, worktree.path,
                WorktreeRetraction.ids(root, base));
        require(integrated || allowRetracted && retracted,
                "worktree head is neither integrated nor an explicitly allowed retraction");
        require(!worktree.branch.isBlank(), "detached worktree cannot be bundled by branch");
        require(allowHusk || !husk(worktree),
                "husk requires explicit --allow-husk: " + selected);
        Files.createDirectories(bundles);
        String safeName = worktree.branch.replaceAll("[^A-Za-z0-9._-]", "-");
        Path bundle = bundles.resolve(safeName + "-" + worktree.head.substring(0, 12) + ".bundle");
        require(!Files.exists(bundle), "bundle already exists: " + bundle);
        git(root, "bundle", "create", bundle.toString(), worktree.branch);
        git(root, "bundle", "verify", bundle.toString());
        Cleanup cleanup = privateCleanup(selected);
        git(root, "worktree", "remove", selected.toString());
        cacheDoctor();
        System.out.println("archived worktree " + selected + " to " + bundle);
        System.out.println("branch retained: " + worktree.branch);
        System.out.println("private cleanup: files=" + cleanup.files + ", bytes=" + cleanup.bytes
                + "; not recoverable from the tracked-source bundle");
    }

    private boolean integrated(String head, String base, Set<String> receipts) throws Exception {
        return status(root, "merge-base", "--is-ancestor", head, base) == 0 || receipts.contains(head);
    }

    private Set<String> receiptHeads(String base) throws Exception {
        String object = base + ":smokes/train-reconciliation.lock";
        if (status(root, "cat-file", "-e", object) != 0) return Set.of();
        Properties values = new Properties(); values.load(new StringReader(git(root, "show", object)));
        Set<String> result = new HashSet<>();
        for (String key : values.stringPropertyNames())
            if (key.startsWith("smoke.") && key.endsWith(".receipt.head")
                    && "milestone".equals(values.getProperty(key.replace(".receipt.head", ".kind"))))
                result.add(values.getProperty(key));
        return Set.copyOf(result);
    }

    private boolean husk(Worktree worktree) throws Exception {
        if (worktree.branch.isBlank() || !worktree.branch.matches(".*(?:^|[-/])m[0-9]+(?:[-/].*)?"))
            return false;
        Set<String> heads = new HashSet<>(git(root, "reflog", "show", "--format=%H", worktree.branch)
                .lines().filter(line -> !line.isBlank()).map(String::trim).toList());
        return heads.size() == 1 && heads.contains(worktree.head);
    }

    private static boolean milestoneDirty(String porcelain) {
        return porcelain.lines().map(line -> line.length() > 3 ? line.substring(3).trim() : "")
                .map(value -> value.contains(" -> ") ? value.substring(value.indexOf(" -> ") + 4) : value)
                .map(value -> value.replace('\\', '/')).anyMatch(value ->
                        value.matches("(?:smokes/m[0-9]+[^/]*/.*|docs/M[0-9]+.*[.]md)"));
    }

    private void selfTestCleanup() throws Exception {
        try {
            Class<?> type = Class.forName("WorktreePrivateCleanup");
            var method = type.getDeclaredMethod("selfTest"); method.setAccessible(true);
            method.invoke(null); return;
        } catch (ClassNotFoundException sourceLaunch) { }
        Process process = new ProcessBuilder(javaTool(), privateCleanupSource().toString(), "--self-test")
                .directory(root.toFile()).inheritIO().start();
        require(process.waitFor(120, TimeUnit.SECONDS), "worktree cleanup self-test timed out");
        require(process.exitValue() == 0, "worktree cleanup self-test failed");
    }

    private Cleanup privateCleanup(Path worktree) throws Exception {
        try {
            Class<?> type = Class.forName("WorktreePrivateCleanup");
            var prune = type.getDeclaredMethod("prune", Path.class); prune.setAccessible(true);
            Object result = prune.invoke(null, worktree);
            var files = result.getClass().getDeclaredMethod("files"); files.setAccessible(true);
            var bytes = result.getClass().getDeclaredMethod("bytes"); bytes.setAccessible(true);
            return new Cleanup((long) files.invoke(result), (long) bytes.invoke(result));
        } catch (ClassNotFoundException sourceLaunch) { }
        Path log = Files.createTempFile("worldline-private-cleanup-", ".log");
        Process process = new ProcessBuilder(javaTool(), privateCleanupSource().toString(), "prune",
                worktree.toString()).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            require(process.waitFor(120, TimeUnit.SECONDS), "worktree private cleanup timed out");
            String output = Files.readString(log, StandardCharsets.UTF_8).trim();
            require(process.exitValue() == 0, "worktree private cleanup failed: " + output);
            String row = output.lines().filter(line -> line.startsWith("files=")).findFirst()
                    .orElseThrow(() -> new IllegalStateException("missing private cleanup census"));
            String[] values = row.replace("files=", "").replace("bytes=", "").split(";");
            return new Cleanup(Long.parseLong(values[0]), Long.parseLong(values[1]));
        } finally { Files.deleteIfExists(log); }
    }

    private Path privateCleanupSource() {
        return root.resolve("tools/integration/WorktreePrivateCleanup.java");
    }

    private void cacheDoctor() throws Exception {
        Process process = new ProcessBuilder(javaTool(), root.resolve("tools/harness/Gate.java").toString(),
                "--cache-doctor").directory(root.toFile()).inheritIO().start();
        require(process.waitFor(600, TimeUnit.SECONDS), "post-archive cache doctor timed out");
        require(process.exitValue() == 0, "post-archive cache doctor failed");
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
        return commandStatus(directory, command(directory, arguments).command(), 60);
    }

    private static int commandStatus(Path directory, List<String> arguments, int seconds) throws Exception {
        Process process = new ProcessBuilder(arguments).directory(directory.toFile()).redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException(arguments.get(0) + " timed out");
        }
        return process.exitValue();
    }

    private static ProcessBuilder command(Path directory, String... arguments) {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(arguments));
        return new ProcessBuilder(command).directory(directory.toFile());
    }

    private static String usage() {
        return "usage: java tools/integration/WorktreeLifecycle.java audit|triage [--base REF] | prune | "
                + "archive --path PATH --bundles DIR [--base REF] [--allow-husk] [--allow-retracted]";
    }

    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    private static int integerEnvironment(String name, int fallback) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) return fallback;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException error) { throw new IllegalArgumentException(name + " must be an integer"); }
    }
    private static String javaTool() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", "java" + (windows ? ".exe" : "")).toString();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private record Worktree(Path path, String head, String branch) {}
    private record State(boolean exists, boolean dirty, boolean integrated, boolean retracted,
            boolean husk, boolean milestoneDirty) {}
    private record Cleanup(long files, long bytes) {}
}
