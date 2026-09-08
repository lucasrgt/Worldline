import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

/** Canonical, link-safe evidence for Candidate-owned compiled outputs. */
final class CandidateBuildEvidence {
    private static final List<String> ROOTS = List.of("runner-classes", "scenario-classes");
    private static final int MAX_FILES = 1_000;
    private static final long MAX_BYTES = 64L << 20;

    private CandidateBuildEvidence() { }

    static Evidence read(Path candidate) throws Exception {
        require(candidate.isAbsolute() && candidate.normalize().equals(candidate),
                "candidate output is not normalized");
        MessageDigest digest = ContainerHexDigest.sha256();
        int files = 0; long bytes = 0L;
        for (String name : ROOTS) {
            Path root = candidate.resolve(name); require(directory(root), "missing Candidate output " + name);
            List<Path> paths = new ArrayList<>();
            try (var stream = Files.walk(root)) { stream.skip(1).forEach(paths::add); }
            paths.sort(Comparator.comparing(path -> text(root.relativize(path))));
            for (Path path : paths) {
                require(!Files.isSymbolicLink(path), "Candidate evidence contains a link");
                if (directory(path)) continue;
                require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                        && path.getFileName().toString().endsWith(".class"),
                        "Candidate evidence contains a non-class file");
                byte[] content = Files.readAllBytes(path); files++; bytes = Math.addExact(bytes, content.length);
                require(files <= MAX_FILES && bytes <= MAX_BYTES, "Candidate evidence budget exceeded");
                update(digest, name + "/" + text(root.relativize(path))); update(digest, content);
            }
        }
        require(files > 0 && bytes > 0L, "Candidate evidence is empty");
        return new Evidence(HexFormat.of().formatHex(digest.digest()), files, bytes);
    }

    static void selfTest() throws Exception {
        Path base = Files.createTempDirectory("worldline-candidate-evidence-").toAbsolutePath().normalize();
        try {
            Path one = fixture(base.resolve("one"), new byte[] {1, 2, 3});
            Path two = fixture(base.resolve("two"), new byte[] {1, 2, 3});
            Evidence first = read(one), second = read(two);
            require(first.equals(second) && first.files == 2, "Candidate evidence is path-dependent");
            Files.write(two.resolve("scenario-classes/Probe.class"), new byte[] {3, 2, 1});
            require(!first.digest.equals(read(two).digest), "Candidate evidence ignored content drift");
        } finally { delete(base); }
    }

    private static Path fixture(Path root, byte[] scenario) throws Exception {
        Files.createDirectories(root.resolve("runner-classes"));
        Files.createDirectories(root.resolve("scenario-classes"));
        Files.write(root.resolve("runner-classes/Run.class"), new byte[] {4, 5});
        Files.write(root.resolve("scenario-classes/Probe.class"), scenario); return root;
    }
    private static void update(MessageDigest digest, String value) {
        update(digest, value.getBytes(StandardCharsets.UTF_8));
    }
    private static void update(MessageDigest digest, byte[] value) {
        digest.update(ByteBuffer.allocate(4).putInt(value.length).array()); digest.update(value);
    }
    private static boolean directory(Path path) {
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path);
    }
    private static String text(Path path) { return path.toString().replace('\\', '/'); }
    private static void delete(Path root) throws Exception { if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) { for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
            Files.deleteIfExists(path); } }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    record Evidence(String digest, int files, long bytes) { }
}
