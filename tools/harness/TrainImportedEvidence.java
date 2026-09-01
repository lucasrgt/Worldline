import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

/** Locates and validates qualified milestone receipts stored in clean worktrees. */
final class TrainImportedEvidence extends TrainPinSupport {
    private TrainImportedEvidence() { }

    static boolean complete(Path swarm, String id) throws Exception {
        Path worktree = worktree(swarm, id);
        return Files.isRegularFile(worktree.resolve(".worldline/reports/milestones")
                        .resolve(id + ".json"))
                && Files.isRegularFile(worktree.resolve(".worldline/reports/smokes")
                        .resolve(id + ".properties"))
                && Files.isRegularFile(worktree.resolve(".worldline/smoke-logs")
                        .resolve(id + ".log"));
    }

    static Receipt read(Path root, Path swarm, String id) throws Exception {
        Path worktree = worktree(swarm, id);
        Path report = worktree.resolve(".worldline/reports/milestones").resolve(id + ".json");
        Path attestationPath = worktree.resolve(".worldline/reports/smokes")
                .resolve(id + ".properties");
        Path log = worktree.resolve(".worldline/smoke-logs").resolve(id + ".log");
        require(Files.isRegularFile(report) && Files.isRegularFile(attestationPath)
                && Files.isRegularFile(log), "missing milestone evidence: " + id);
        java.util.Map<String, Object> json = MiniJson.object(
                Files.readString(report, StandardCharsets.UTF_8));
        Properties attestation = load(attestationPath);
        String head = MiniJson.string(json, "head"), tree = MiniJson.string(json, "tree");
        String base = MiniJson.string(json, "base"), signature = MiniJson.string(json, "signature");
        String evidence = MiniJson.string(json, "evidence_sha256");
        require("passed".equals(MiniJson.string(json, "status"))
                        && head.equals(attestation.getProperty("head"))
                        && "passed".equals(attestation.getProperty("status"))
                        && evidence.equals(digest(Files.readAllBytes(log)))
                        && capture(worktree, "status", "--porcelain", "--untracked-files=all").isBlank()
                        && tree.equals(capture(root, "rev-parse", head + "^{tree}").strip())
                        && status(root, "merge-base", "--is-ancestor", base, head) == 0
                        && status(root, "merge-base", "--is-ancestor", base, "HEAD") == 0,
                "invalid milestone evidence: " + id);
        Properties source = load(worktree.resolve("smokes").resolve(id).resolve("smoke.properties"));
        Properties target = load(root.resolve("smokes").resolve(id).resolve("smoke.properties"));
        String main = source.containsKey("cycle.main") ? "cycle.main" : "worldline.main";
        for (String key : List.of("expected.signal", "expected.signature", main, "runner.source"))
            require(java.util.Objects.equals(source.getProperty(key), target.getProperty(key)),
                    "reconciled milestone behavior drift: " + id + " " + key);
        if (source.containsKey("oracle.main")) require(java.util.Objects.equals(
                source.getProperty("oracle.main"), target.getProperty("oracle.main")),
                "reconciled milestone oracle drift: " + id);
        return new Receipt(attestation.getProperty("fingerprint"), evidence,
                head, tree, base, signature);
    }

    private static Path worktree(Path swarm, String id) throws Exception {
        if (has(swarm, id)) return swarm;
        Path direct = swarm.resolve(id);
        if (has(direct, id)) return direct;
        try (var directories = Files.list(swarm)) {
            List<Path> matches = directories.filter(Files::isDirectory)
                    .filter(path -> has(path, id)).toList();
            require(matches.size() <= 1, "ambiguous milestone evidence: " + id);
            return matches.isEmpty() ? direct : matches.get(0);
        }
    }

    private static boolean has(Path worktree, String id) {
        return Files.isRegularFile(worktree.resolve(".worldline/reports/milestones")
                .resolve(id + ".json"));
    }

    record Receipt(String fingerprint, String evidence, String head, String tree,
            String base, String signature) { }
}
