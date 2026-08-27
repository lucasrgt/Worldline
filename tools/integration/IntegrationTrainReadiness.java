import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Performs exact, source-launchable readiness before an integration train consumes evidence. */
public final class IntegrationTrainReadiness {
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try { new IntegrationTrainReadiness().execute(arguments); }
        catch (Exception error) {
            System.err.println("integration train readiness failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute(String[] arguments) throws Exception {
        require(arguments.length == 7, "readiness requires base, id, ref, head, tree, worktree, mode");
        String base = arguments[0], id = arguments[1], reference = arguments[2];
        String head = arguments[3], tree = arguments[4];
        Path worktree = Path.of(arguments[5]).toAbsolutePath().normalize();
        boolean reconcile = Boolean.parseBoolean(arguments[6]);
        require(git(worktree, "status", "--porcelain=v1", "--untracked-files=all").isBlank(),
                "worktree contains tracked or untracked changes: " + worktree);
        require(git(root, "rev-list", "--parents", "-n", "1", head).trim()
                .equals(head + " " + base), reference + " is not a direct single-parent commit over base");
        if (reconcile) runCoordinatorGate(worktree);
        else verifyQualified(base, id, reference, head, tree, worktree);
        System.out.println("train candidate readiness passed: " + id);
    }

    private void verifyQualified(String base, String id, String reference, String head,
            String tree, Path worktree) throws Exception {
        Path directory = root.resolve("coordination/handoffs");
        List<Path> matching = new ArrayList<>();
        if (Files.isDirectory(directory)) try (var paths = Files.list(directory)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                if (head.equals(properties(path).getProperty("head"))) matching.add(path);
            }
        }
        require(matching.size() == 1, "expected exactly one handoff for " + id);
        Properties handoff = properties(matching.get(0));
        String branch = reference.replaceFirst("^refs/heads/", "");
        String receiptName = ".worldline/reports/milestones/" + id + ".json";
        Path receipt = worktree.resolve(receiptName);
        require(branch.equals(handoff.getProperty("branch"))
                && worktree.toString().replace('\\', '/').equals(handoff.getProperty("worktree"))
                && head.equals(handoff.getProperty("head")) && base.equals(handoff.getProperty("base"))
                && receiptName.equals(handoff.getProperty("receipt.path"))
                && "qualified".equals(handoff.getProperty("disposition")),
                "handoff identity drifted for " + id);
        require(Files.isRegularFile(receipt)
                && digest(receipt).equals(handoff.getProperty("receipt.sha256")),
                "handoff receipt digest drifted for " + id);
        String json = Files.readString(receipt, StandardCharsets.UTF_8);
        require(id.equals(jsonString(json, "id")) && "passed".equals(jsonString(json, "status"))
                && head.equals(jsonString(json, "head")) && tree.equals(jsonString(json, "tree"))
                && base.equals(jsonString(json, "base")), "milestone receipt identity drifted");
        require(!Files.readString(worktree.resolve("smokes/" + id + "/smoke.properties"),
                StandardCharsets.UTF_8).contains("scaffold.status="), "scaffold cannot enter a train");
    }

    private static void runCoordinatorGate(Path worktree) throws Exception {
        Process process = new ProcessBuilder(javaTool(), "tools/harness/Gate.java", "--train-readiness")
                .directory(worktree.toFile()).inheritIO().start();
        require(process.waitFor(10, TimeUnit.MINUTES), "train readiness gate timed out");
        require(process.exitValue() == 0, "train coordinator readiness failed");
    }

    private static Properties properties(Path path) throws Exception {
        require(Files.isRegularFile(path), "missing properties: " + path);
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }

    private static String jsonString(String json, String key) {
        var matcher = java.util.regex.Pattern.compile("\"" + java.util.regex.Pattern.quote(key)
                + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        require(matcher.find(), "missing receipt field: " + key); return matcher.group(1);
    }
    private static String digest(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }
    private static String git(Path directory, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Path log = Files.createTempFile("worldline-train-ready-git-", ".log");
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            require(process.waitFor(60, TimeUnit.SECONDS), "git command timed out");
            String output = Files.readString(log, StandardCharsets.UTF_8);
            require(process.exitValue() == 0, "git command failed: " + output); return output;
        } finally { Files.deleteIfExists(log); }
    }
    private static String javaTool() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", "java" + (windows ? ".exe" : ""))
                .toString();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
