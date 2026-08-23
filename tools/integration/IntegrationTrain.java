import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Audits isolated milestone branches before a coordinator integrates them. */
public final class IntegrationTrain {
    private static final Set<String> COORDINATOR_FILES = Set.of(
            "CHANGELOG.md", "README.md", "docs/ROADMAP.md", "docs/ARCHITECTURE.md",
            "release/worldline.properties",
            "modules/api/src/main/java/worldline/api/WorldlineVersion.java");
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try { new IntegrationTrain().execute(arguments); }
        catch (Exception error) {
            System.err.println("integration train failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute(String[] arguments) throws Exception {
        Arguments parsed = Arguments.parse(arguments);
        String base = git(root, "rev-parse", "--verify", parsed.base + "^{commit}").trim();
        List<Candidate> candidates = new ArrayList<>();
        for (String specification : parsed.candidates)
            candidates.add(inspect(base, specification, parsed.reconcile));
        if (!parsed.reconcile) verifyOwnership(candidates);
        verifyPairwiseMerges(candidates);
        if (!parsed.planOnly) verifyCandidates(base, candidates, parsed.reconcile);
        writeReport(base, candidates, !parsed.planOnly, parsed.reconcile);
        triage(parsed.reconcile ? candidates.get(0).head : base);
        System.out.println("integration train passed: base=" + shortSha(base)
                + ", candidates=" + candidates.size() + ", qualified=" + !parsed.planOnly);
    }

    private Candidate inspect(String base, String specification, boolean reconcile) throws Exception {
        int separator = specification.indexOf('=');
        require(separator > 0 && separator < specification.length() - 1,
                "candidate must be ID=REF: " + specification);
        String id = specification.substring(0, separator);
        String reference = specification.substring(separator + 1);
        require(id.matches("[a-z0-9]+(?:-[a-z0-9]+)*"), "invalid candidate id: " + id);
        String head = git(root, "rev-parse", "--verify", reference + "^{commit}").trim();
        require(status(root, "merge-base", "--is-ancestor", base, head) == 0,
                reference + " is not based on " + shortSha(base));
        List<String> paths = lines(git(root, "diff", "--name-only", base + "..." + head));
        require(!paths.isEmpty(), reference + " has no changes from base");
        if (reconcile) return new Candidate(id, reference, head, paths);
        require(paths.stream().anyMatch(path -> path.startsWith("smokes/" + id + "/")),
                reference + " does not own smokes/" + id + "/");
        for (String path : paths) require(!COORDINATOR_FILES.contains(path),
                reference + " modifies coordinator-owned " + path);
        return new Candidate(id, reference, head, paths);
    }

    private static void verifyOwnership(List<Candidate> candidates) {
        Map<String, String> owners = new HashMap<>();
        for (Candidate candidate : candidates) for (String path : candidate.paths) {
            String previous = owners.putIfAbsent(path, candidate.id);
            require(previous == null, "path owned by both " + previous + " and "
                    + candidate.id + ": " + path);
        }
    }

    private void verifyPairwiseMerges(List<Candidate> candidates) throws Exception {
        for (int left = 0; left < candidates.size(); left++) {
            for (int right = left + 1; right < candidates.size(); right++) {
                Candidate one = candidates.get(left), two = candidates.get(right);
                require(status(root, "merge-tree", "--write-tree", "--quiet", one.head, two.head) == 0,
                        "merge conflict between " + one.reference + " and " + two.reference);
            }
        }
    }

    private void verifyCandidates(String base, List<Candidate> candidates, boolean reconcile) throws Exception {
        Map<String, Path> worktrees = worktrees();
        List<Process> running = new ArrayList<>();
        for (Candidate candidate : candidates) {
            Path worktree = worktrees.get(candidate.head);
            require(worktree != null, "no registered worktree at " + shortSha(candidate.head)
                    + " for " + candidate.reference);
            require(git(worktree, "status", "--porcelain").isBlank(), "dirty worktree: " + worktree);
            List<String> command = reconcile
                    ? List.of(javaTool(), "tools/harness/Gate.java", "--smoke")
                    : List.of(javaTool(), "tools/harness/Gate.java", "--milestone", candidate.id);
            ProcessBuilder builder = new ProcessBuilder(command).directory(worktree.toFile()).inheritIO();
            builder.environment().put("WORLDLINE_CANDIDATE_BASE", base);
            running.add(builder.start());
        }
        try {
            for (int index = 0; index < running.size(); index++) {
                Process process = running.get(index);
                if (!process.waitFor(2, TimeUnit.HOURS)) {
                    destroy(process);
                    throw new IllegalStateException("candidate gate timed out: " + candidates.get(index).id);
                }
                require(process.exitValue() == 0, "candidate gate failed: " + candidates.get(index).id);
            }
        } catch (Exception error) {
            running.stream().filter(Process::isAlive).forEach(IntegrationTrain::destroy);
            throw error;
        }
    }

    private Map<String, Path> worktrees() throws Exception {
        Map<String, Path> result = new HashMap<>();
        Path current = null;
        for (String line : lines(git(root, "worktree", "list", "--porcelain"))) {
            if (line.startsWith("worktree ")) current = Path.of(line.substring(9));
            if (line.startsWith("HEAD ") && current != null) result.put(line.substring(5), current);
        }
        return result;
    }

    private void writeReport(String base, List<Candidate> candidates, boolean verified,
            boolean reconcile) throws IOException {
        StringBuilder json = new StringBuilder("{\n  \"schema\":1,\n  \"created\":\"")
                .append(Instant.now()).append("\",\n  \"base\":\"").append(base)
                .append("\",\n  \"mode\":\"").append(reconcile ? "reconcile" : "milestones")
                .append("\",\n  \"verified\":").append(verified).append(",\n  \"candidates\":[\n");
        for (int index = 0; index < candidates.size(); index++) {
            Candidate candidate = candidates.get(index);
            json.append("    {\"id\":\"").append(candidate.id).append("\",\"ref\":\"")
                    .append(escape(candidate.reference)).append("\",\"head\":\"")
                    .append(candidate.head).append("\",\"paths\":").append(candidate.paths.size()).append("}");
            json.append(index + 1 == candidates.size() ? "\n" : ",\n");
        }
        json.append("  ]\n}\n");
        Path report = root.resolve(".worldline/reports/integration-plan.json");
        Files.createDirectories(report.getParent());
        Files.writeString(report, json, StandardCharsets.UTF_8);
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

    private static String git(Path directory, String... arguments) throws Exception {
        ProcessBuilder builder = command(directory, arguments);
        Path log = Files.createTempFile("worldline-git-", ".log");
        Process process = builder.redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                destroy(process); throw new IllegalStateException("git timed out: " + String.join(" ", arguments));
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
            destroy(process); throw new IllegalStateException("git timed out: " + String.join(" ", arguments));
        }
        return process.exitValue();
    }

    private static ProcessBuilder command(Path directory, String... arguments) {
        List<String> command = new ArrayList<>(List.of("git"));
        for (String argument : arguments) command.add(argument);
        return new ProcessBuilder(command).directory(directory.toFile());
    }

    private static List<String> lines(String text) {
        return text.lines().map(String::trim).filter(line -> !line.isEmpty()).toList();
    }

    private static String javaTool() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", "java" + (windows ? ".exe" : "")).toString();
    }

    private static String shortSha(String sha) { return sha.substring(0, Math.min(12, sha.length())); }
    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    private static void destroy(Process process) {
        process.descendants().sorted(java.util.Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private record Candidate(String id, String reference, String head, List<String> paths) {}

    private record Arguments(String base, boolean planOnly, boolean reconcile,
            List<String> candidates) {
        static Arguments parse(String[] arguments) {
            String base = "HEAD"; boolean planOnly = false, reconcile = false;
            List<String> candidates = new ArrayList<>();
            for (int index = 0; index < arguments.length; index++) {
                if ("--base".equals(arguments[index]) && index + 1 < arguments.length) base = arguments[++index];
                else if ("--verify".equals(arguments[index])) {
                    // Compatibility: qualification is now the default.
                }
                else if ("--plan-only".equals(arguments[index])) planOnly = true;
                else if ("--reconcile".equals(arguments[index])) reconcile = true;
                else candidates.add(arguments[index]);
            }
            require(!candidates.isEmpty(),
                    "usage: java tools/integration/IntegrationTrain.java "
                    + "[--base REF] [--plan-only] [--reconcile] ID=REF...");
            require(!reconcile || candidates.size() == 1,
                    "--reconcile requires exactly one consolidated candidate");
            return new Arguments(base, planOnly, reconcile, candidates);
        }
    }
}
