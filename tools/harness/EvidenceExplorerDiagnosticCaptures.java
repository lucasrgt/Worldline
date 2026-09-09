import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Bounded content identities for local non-campaign failure diagnostics. */
final class EvidenceExplorerDiagnosticCaptures {
    private static final List<FamilyRoot> FAMILIES = List.of(
            new FamilyRoot(Path.of(".worldline/executor-server-failures"),
                    "server-qualification-failure"),
            new FamilyRoot(Path.of(".worldline/executor-gui-failures"),
                    "gui-native-qualification-failure"),
            new FamilyRoot(Path.of(".worldline/executor-aero-failures"),
                    "gui-aero-qualification-failure"));
    private static final Pattern DIRECTORY = Pattern.compile(
            "(legacy|rust)-([a-z0-9]+(?:-[a-z0-9]+)*)-"
                    + "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    private static final int MAX_CAPTURES = 128;
    private static final int MAX_FILES = 11;
    private static final long MAX_FILE_BYTES = 16L << 20;
    private static final long MAX_CAPTURE_BYTES = 32L << 20;

    private EvidenceExplorerDiagnosticCaptures() { }

    static Snapshot build(Path repository) throws Exception {
        List<Capture> captures = new ArrayList<>(); int malformed = 0, total = 0;
        for (FamilyRoot family : FAMILIES) {
            Path root = repository.resolve(family.root);
            if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) continue;
            require(directory(root), "invalid qualification diagnostic capture root");
            List<Path> paths = children(root); total += paths.size();
            require(total <= MAX_CAPTURES, "qualification diagnostic capture count exceeds policy");
            for (Path path : paths) {
                Capture capture = capture(repository, path, family.family);
                if (capture == null) malformed++; else captures.add(capture);
            }
        }
        captures.sort(Comparator.comparing(Capture::id));
        int complete = (int) captures.stream().filter(Capture::complete).count();
        int orphan = (int) captures.stream().filter(Capture::orphan).count();
        long files = captures.stream().mapToLong(value -> value.artifacts.size()).sum();
        long bytes = captures.stream().mapToLong(Capture::bytes).sum();
        return new Snapshot(captures.size(), complete, captures.size() - complete, orphan,
                malformed, files, bytes, List.copyOf(captures));
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-diagnostic-captures-");
        try {
            Path smoke = root.resolve("smokes/m20-server-bootstrap/smoke.properties");
            Files.createDirectories(smoke.getParent()); Files.writeString(smoke, "fixture\n");
            for (String id : List.of("m10-native-render", "m778-aero-culling-visual-integrity")) {
                Path descriptor = root.resolve("smokes").resolve(id).resolve("smoke.properties");
                Files.createDirectories(descriptor.getParent()); Files.writeString(descriptor, "fixture\n");
            }
            Path captures = root.resolve(FAMILIES.get(0).root); Files.createDirectories(captures);
            Path complete = Files.createDirectories(captures.resolve(
                    "legacy-m20-server-bootstrap-00000000-0000-0000-0000-000000000001"));
            Files.writeString(complete.resolve("failure.txt"), "unsafe <failure>\n");
            Files.writeString(complete.resolve("m20-server-bootstrap.log"), "secret log content\n");
            Path partial = Files.createDirectories(captures.resolve(
                    "rust-m130-nether-login-00000000-0000-0000-0000-000000000002"));
            Files.writeString(partial.resolve("stderr.log"), "private stderr\n");
            Files.writeString(captures.resolve("unexpected.txt"), "malformed\n");
            Path gui = Files.createDirectories(root.resolve(FAMILIES.get(1).root).resolve(
                    "rust-m10-native-render-00000000-0000-0000-0000-000000000003"));
            Files.writeString(gui.resolve("failure.txt"), "private GUI failure\n");
            Files.writeString(gui.resolve("stderr.log"), "private GUI stderr\n");
            Path aero = Files.createDirectories(root.resolve(FAMILIES.get(2).root).resolve(
                    "legacy-m778-aero-culling-visual-integrity-00000000-0000-0000-0000-000000000004"));
            Files.writeString(aero.resolve("failure.txt"), "private Aero failure\n");
            Files.writeString(aero.resolve("console.log"), "private Aero console\n");
            Snapshot value = build(root);
            require(value.captureCount == 4 && value.completeCount == 3 && value.partialCount == 1
                    && value.orphanCount == 1 && value.malformedCount == 1 && value.fileCount == 7
                    && value.captures.stream().allMatch(item -> item.id.matches("[0-9a-f]{64}")),
                    "diagnostic capture summary drifted");
            String json = EvidenceExplorerDiagnosticFormat.json(value);
            require(!json.contains("secret log content") && !json.contains("private stderr")
                    && !json.contains("private GUI") && !json.contains("private Aero")
                    && !json.contains(root.toString()) && json.contains("m130-nether-login")
                    && json.contains("gui-native-qualification-failure")
                    && json.contains("gui-aero-qualification-failure"),
                    "diagnostic capture projection exposed local content or lost identity");
        } finally { SafeTreeDelete.delete(root); }
    }

    private static Capture capture(Path repository, Path path, String family) throws Exception {
        if (!directory(path)) return null;
        Matcher matcher = DIRECTORY.matcher(path.getFileName().toString());
        if (!matcher.matches()) return null;
        String backend = matcher.group(1), workload = matcher.group(2);
        List<Path> paths = children(path); if (paths.size() > MAX_FILES) return null;
        Map<String, Artifact> artifacts = new LinkedHashMap<>(); long bytes = 0L;
        for (Path file : paths) {
            if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)
                    || Files.isSymbolicLink(file)) return null;
            String kind = kind(file.getFileName().toString(), workload);
            if (kind == null || artifacts.containsKey(kind)) return null;
            long size = Files.size(file);
            if (size > MAX_FILE_BYTES || (bytes += size) > MAX_CAPTURE_BYTES) return null;
            artifacts.put(kind, new Artifact(kind, sha256(file), size));
        }
        List<Artifact> ordered = artifacts.values().stream()
                .sorted(Comparator.comparing(Artifact::kind)).toList();
        boolean complete = artifacts.containsKey("failure-metadata");
        Path descriptor = repository.resolve("smokes").resolve(workload).resolve("smoke.properties");
        boolean orphan = !Files.isRegularFile(descriptor, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(descriptor);
        String id = identity(family, backend, workload, complete, orphan, ordered);
        return new Capture(id, family, backend, workload,
                complete, orphan, ordered.size(), bytes, List.copyOf(ordered));
    }

    private static String kind(String name, String workload) {
        if (name.equals("failure.txt")) return "failure-metadata";
        if (name.equals(workload + ".log")) return "smoke-log";
        if (name.equals("console.log")) return "console";
        if (name.equals("metrics.properties")) return "legacy-metrics";
        if (name.equals("stdout.log")) return "stdout";
        if (name.equals("stderr.log")) return "stderr";
        if (name.equals("metrics.json")) return "metrics";
        if (name.equals("manifest.json")) return "manifest";
        if (name.equals("receipt.json")) return "receipt";
        if (name.equals(workload + "-plan.json") || name.equals("rust-plan.json")) return "plan";
        return name.equals("verify.json") ? "verify-report" : null;
    }

    private static String identity(String family, String backend, String workload, boolean complete,
            boolean orphan, List<Artifact> artifacts) {
        StringBuilder value = new StringBuilder("schema=worldline.diagnostic-capture.v1\n")
                .append("family=").append(family).append("\nbackend=").append(backend)
                .append("\nworkload=").append(workload).append("\ncomplete=").append(complete)
                .append("\norphan=").append(orphan).append('\n');
        for (Artifact artifact : artifacts) value.append(artifact.kind).append('=')
                .append(artifact.sha256).append(':').append(artifact.bytes).append('\n');
        return sha256(value.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static List<Path> children(Path root) throws Exception {
        List<Path> values = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path path : stream) values.add(path);
        }
        values.sort(Comparator.comparing(path -> path.getFileName().toString())); return values;
    }
    private static boolean directory(Path path) {
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path);
    }
    private static String sha256(Path path) throws Exception {
        MessageDigest digest = HexDigest.sha256();
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[16 * 1024]; int count;
            while ((count = input.read(buffer)) >= 0) if (count > 0) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
    private static String sha256(byte[] value) {
        try { return HexFormat.of().formatHex(HexDigest.sha256().digest(value)); }
        catch (Exception error) { throw new IllegalStateException(error); }
    }
    private static Snapshot empty() {
        return new Snapshot(0, 0, 0, 0, 0, 0L, 0L, List.of());
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }

    record Snapshot(int captureCount, int completeCount, int partialCount, int orphanCount,
            int malformedCount, long fileCount, long byteCount, List<Capture> captures) { }
    record Capture(String id, String family, String backend, String workload, boolean complete,
            boolean orphan, int fileCount, long bytes, List<Artifact> artifacts) { }
    record Artifact(String kind, String sha256, long bytes) { }
    private record FamilyRoot(Path root, String family) { }
}
