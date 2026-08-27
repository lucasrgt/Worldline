import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.*;

/** Adopts one legacy retry without manufacturing a historical launcher receipt. */
public final class LegacyRetryAdoption {
    private static final int MAX_EVIDENCE_BYTES = 64 * 1024 * 1024;
    private static final Set<String> RECOVERY_STEPS = Set.of("adopt-legacy-retry",
            "provision-exact-artifact", "repair-process", "resume-same-milestone-and-worktree");

    private LegacyRetryAdoption() {
    }

    public static void main(String[] arguments) {
        try {
            Path receipt = adopt(Path.of("").toAbsolutePath().normalize(), Request.parse(arguments));
            System.out.println("Legacy retry adoption PASS: " + receipt);
            System.out.println("  receipt sha256: " + sha(receipt));
        } catch (Exception error) {
            System.err.println("Legacy retry adoption failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static Path adopt(Path root, Request request) throws Exception {
        require(request.id.matches("m[0-9]+-[a-z0-9-]+"), "invalid milestone id");
        require(request.owner.matches("[A-Za-z0-9:._-]+"), "invalid retry owner");
        require(request.newControlBase.matches("[0-9a-f]{40}"),
                "new control base must be an exact commit SHA");
        Properties disposition = properties(root.resolve(
                "coordination/swarm/dispositions/" + request.id + ".properties"));
        validateDisposition(request, disposition);
        Path worktree = exactPath(required(disposition, "worktree"));
        Path archive = exactPath(required(disposition, "archive"));
        String branch = required(disposition, "branch");
        String base = required(disposition, "base");
        String head = required(disposition, "head");
        String tree = required(disposition, "tree");
        String state = required(disposition, "prior.state");
        require(Files.isDirectory(worktree), "legacy worktree is missing");
        require(Files.isRegularFile(archive), "legacy archive is missing");
        String archiveSha = sha(archive);
        require(archiveSha.equalsIgnoreCase(required(disposition, "archive.sha256")),
                "legacy archive SHA-256 drifted");
        require(git(root, "cat-file", "-e", request.newControlBase + "^{commit}").isBlank(),
                "new control base is not a commit");
        require(status(root, "merge-base", "--is-ancestor", base, request.newControlBase) == 0,
                "new control base does not contain the legacy base");
        String liveStatus = git(worktree, "status", "--porcelain=v1", "--untracked-files=all");
        require(git(worktree, "branch", "--show-current").trim().equals(branch),
                "legacy worktree branch drifted");
        require(git(worktree, "rev-parse", "HEAD").trim().equals(head),
                "legacy worktree HEAD drifted");
        require(git(worktree, "rev-parse", "HEAD^{tree}").trim().equals(tree),
                "legacy worktree tree drifted");
        ArchiveIdentity archived = archive(archive);
        require(archived.id.equals(request.id) && archived.branch.equals(branch)
                && archived.worktree.equals(worktree) && archived.base.equals(base)
                && archived.head.equals(head) && archived.tree.equals(tree)
                && archived.state.equals(state), "legacy archive identity drifted");
        require(canonical(liveStatus).equals(canonical(archived.status)),
                "legacy worktree status drifted from the archive");
        SessionEvidence evidence = validateMode(request, worktree);
        Path reports = worktree.resolve(".worldline/reports/swarm");
        Files.createDirectories(reports);
        Path receipt = reports.resolve("legacy-retry-adoption-" + request.id + ".json");
        String json = receipt(request, disposition, worktree, archive, archiveSha, liveStatus, evidence);
        Files.writeString(receipt, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        return receipt;
    }

    private static void validateDisposition(Request request, Properties values) {
        require("1".equals(required(values, "schema"))
                && request.id.equals(required(values, "id")), "disposition identity drifted");
        require("RETRYABLE".equals(required(values, "disposition")),
                "legacy disposition is not RETRYABLE");
        require(Set.of("FAILED_GATE", "DIRTY_SUSPENDED").contains(required(values, "prior.state")),
                "legacy state is not adoptable");
        require("1".equals(required(values, "attempt"))
                && "2".equals(required(values, "max.attempts")),
                "legacy adoption only authorizes attempt 2 of 2");
        require(retainsWorktree(required(values, "next.action")),
                "legacy disposition does not retain the same worktree");
        require(required(values, "branch").equals("codex/milestone-" + request.id),
                "legacy branch identity drifted");
    }

    private static boolean retainsWorktree(String action) {
        if ("resume-same-session-and-worktree".equals(action)) {
            return true;
        }
        List<String> ordered = List.of(action.split(";", -1));
        Set<String> steps = new HashSet<>(ordered);
        return !steps.contains("") && steps.size() == ordered.size()
                && RECOVERY_STEPS.containsAll(steps)
                && steps.contains("adopt-legacy-retry")
                && steps.contains("resume-same-milestone-and-worktree");
    }

    private static SessionEvidence validateMode(Request request, Path worktree) throws Exception {
        if (request.mode.equals("resume-session")) {
            require(request.session != null && request.session.matches("[A-Za-z0-9_-]+"),
                    "resume-session requires an exact session");
            require(request.sessionEvidence != null && Files.isRegularFile(request.sessionEvidence),
                    "resume-session evidence is missing");
            require(request.sessionEvidence.toAbsolutePath().normalize().equals(
                            OpenCodeSessionExport.evidencePath(worktree, request.session)),
                    "resume-session evidence is not the canonical worktree export");
            require(sha(request.sessionEvidence).equalsIgnoreCase(request.sessionEvidenceSha),
                    "session evidence SHA-256 drifted");
            String text = evidenceText(request.sessionEvidence);
            OpenCodeSessionExport.validateEvidence(text, request.session, worktree);
            return new SessionEvidence(request.session, request.sessionEvidence.toString(),
                    request.sessionEvidenceSha.toLowerCase(), 0, 0, "");
        }
        require(request.mode.equals("process-recovery"), "invalid adoption mode");
        require(request.session == null && request.sessionEvidence == null
                && request.sessionEvidenceSha.isBlank(),
                "process recovery cannot claim a historical session");
        require(!request.noSessionReason.isBlank(), "process recovery requires a no-session reason");
        require(request.recoveryTimeoutSeconds > 0 && request.recoveryTimeoutSeconds <= 3600,
                "process recovery must have one bounded session of at most 3600 seconds");
        return new SessionEvidence(null, "", "", 1, request.recoveryTimeoutSeconds,
                request.noSessionReason);
    }

    private static ArchiveIdentity archive(Path path) throws Exception {
        try (ZipFile zip = new ZipFile(path.toFile())) {
            Map<String, String> values = rawManifest(text(zip, "manifest.properties"));
            String status = text(zip, "status.txt");
            return new ArchiveIdentity(required(values, "id"), exactPath(required(values, "worktree")),
                    required(values, "branch"), required(values, "base"), required(values, "head"),
                    required(values, "tree"), required(values, "state"), status);
        }
    }

    static void verifyArchiveWorktree(Path archive, Path worktree) throws Exception {
        require(LegacyRetryAdoption.archive(archive).worktree.equals(worktree.toAbsolutePath().normalize()),
                "archive worktree identity drifted");
    }

    static Map<String, String> rawManifest(String text) {
        Map<String, String> values = new HashMap<>();
        for (String line : canonical(text).split("\n", -1)) {
            if (line.isBlank() || line.startsWith("#") || line.startsWith("!")) {
                continue;
            }
            int separator = line.indexOf('=');
            require(separator > 0, "invalid archive manifest line");
            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            require(!key.isBlank() && values.putIfAbsent(key, value) == null,
                    "duplicate archive manifest key: " + key);
        }
        return Map.copyOf(values);
    }

    private static String receipt(Request request, Properties values, Path worktree, Path archive,
            String archiveSha, String status, SessionEvidence evidence) throws Exception {
        return "{\n  \"schema\":1,\n  \"id\":\"" + request.id + "\",\n  \"mode\":\""
                + request.mode + "\",\n  \"owner\":\"" + request.owner
                + "\",\n  \"prior_attempt\":1,\n  \"authorized_attempt\":2,\n  \"max_attempts\":2,"
                + "\n  \"new_control_base\":\"" + request.newControlBase
                + "\",\n  \"branch\":\"" + escape(required(values, "branch"))
                + "\",\n  \"worktree\":\"" + escape(worktree.toString())
                + "\",\n  \"base\":\"" + required(values, "base")
                + "\",\n  \"head\":\"" + required(values, "head")
                + "\",\n  \"tree\":\"" + required(values, "tree")
                + "\",\n  \"prior_state\":\"" + required(values, "prior.state")
                + "\",\n  \"status_sha256\":\"" + sha(canonical(status))
                + "\",\n  \"archive\":\"" + escape(archive.toString())
                + "\",\n  \"archive_sha256\":\"" + archiveSha
                + "\",\n  \"session\":" + nullable(evidence.session)
                + ",\n  \"session_evidence\":\"" + escape(evidence.path)
                + "\",\n  \"session_evidence_sha256\":\"" + evidence.sha
                + "\",\n  \"recovery_sessions_allowed\":" + evidence.recoverySessions
                + ",\n  \"recovery_timeout_seconds\":" + evidence.timeout
                + ",\n  \"no_session_reason\":\"" + escape(evidence.reason)
                + "\",\n  \"created\":\"" + Instant.now()
                + "\",\n  \"status\":\"PASS\"\n}\n";
    }

    private static String evidenceText(Path path) throws Exception {
        require(Files.size(path) <= MAX_EVIDENCE_BYTES, "session evidence is too large");
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static Properties properties(Path path) throws Exception {
        require(Files.isRegularFile(path), "missing disposition: " + path.getFileName());
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); }
        return values;
    }

    private static String text(ZipFile zip, String name) throws Exception {
        ZipEntry entry = zip.getEntry(name); require(entry != null, "archive lacks " + name);
        try (InputStream input = zip.getInputStream(entry)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor(120, TimeUnit.SECONDS) && process.exitValue() == 0,
                "git failed: " + String.join(" ", arguments));
        return output;
    }

    private static int status(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        require(process.waitFor(120, TimeUnit.SECONDS), "git timed out"); return process.exitValue();
    }

    static String sha(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192]; int count;
            while ((count = input.read(buffer)) >= 0) if (count > 0) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
    private static String sha(String text) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))); }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key, "").trim();
        require(!value.isBlank(), "missing " + key); return value; }
    private static String required(Map<String, String> values, String key) {
        String value = values.getOrDefault(key, "").trim();
        require(!value.isBlank(), "missing " + key);
        return value;
    }
    private static Path exactPath(String value) { return Path.of(value).toAbsolutePath().normalize(); }
    private static String canonical(String value) { return value.replace("\r\n", "\n"); }
    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    private static String nullable(String value) { return value == null ? "null" : "\"" + escape(value) + "\""; }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    record Request(String id, String owner, String newControlBase, String mode, String session,
            Path sessionEvidence, String sessionEvidenceSha, String noSessionReason,
            int recoveryTimeoutSeconds) {
        static Request parse(String[] arguments) {
            String id = "", owner = "", base = "", mode = "", session = null, sha = "", reason = "";
            Path evidence = null; int timeout = 0;
            for (int index = 0; index < arguments.length; index += 2) {
                require(index + 1 < arguments.length, "missing value for " + arguments[index]);
                String value = arguments[index + 1];
                switch (arguments[index]) {
                    case "--id" -> id = value; case "--owner" -> owner = value;
                    case "--new-control-base" -> base = value; case "--mode" -> mode = value;
                    case "--session" -> session = value;
                    case "--session-evidence" -> evidence = exactPath(value);
                    case "--session-evidence-sha256" -> sha = value;
                    case "--no-session-reason" -> reason = value;
                    case "--recovery-timeout-seconds" -> timeout = Integer.parseInt(value);
                    default -> throw new IllegalArgumentException("unknown argument: " + arguments[index]);
                }
            }
            return new Request(id, owner, base, mode, session, evidence, sha, reason, timeout);
        }
    }

    private record ArchiveIdentity(String id, Path worktree, String branch, String base,
            String head, String tree, String state, String status) { }
    private record SessionEvidence(String session, String path, String sha, int recoverySessions,
            int timeout, String reason) { }
}
