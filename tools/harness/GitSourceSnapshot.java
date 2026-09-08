import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Materializes only tracked source blobs from one exact Git commit. */
final class GitSourceSnapshot implements AutoCloseable {
    private final Path container;
    private final Path root;
    private final Set<Path> tracked;
    private final String commit;

    private GitSourceSnapshot(Path container, Path root, Set<Path> tracked, String commit) {
        this.container = container; this.root = root; this.tracked = Set.copyOf(tracked);
        this.commit = commit;
    }

    static GitSourceSnapshot open(Path repository, String requested) throws Exception {
        require(requested.matches("[A-Za-z0-9._/~^+-]+") && !requested.startsWith("-")
                && !requested.contains(".."), "unsafe Git snapshot ref");
        String commit = capture(repository, List.of("git", "rev-parse", "--verify",
                requested + "^{commit}")).trim();
        require(commit.matches("[0-9a-f]{40}|[0-9a-f]{64}"), "Git snapshot ref is not a commit");
        Set<String> expected = tree(repository, commit);
        Path base = repository.resolve(".worldline/input-graph-snapshots");
        Files.createDirectories(base); Path container = Files.createTempDirectory(base, "source-");
        Path root = container.resolve("root"); Files.createDirectories(root);
        try {
            Set<Path> tracked = archive(repository, commit, root, container.resolve("source.zip"),
                    container.resolve("git.err"));
            Set<String> actual = new HashSet<>();
            for (Path path : tracked) actual.add(root.relativize(path).toString().replace('\\', '/'));
            require(actual.equals(expected), "Git snapshot archive/tree mismatch");
            return new GitSourceSnapshot(container, root, tracked, commit);
        } catch (Exception error) {
            SafeTreeDelete.delete(container); throw error;
        }
    }

    Path root() { return root; }
    Set<Path> tracked() { return tracked; }
    String commit() { return commit; }

    @Override public void close() throws IOException { SafeTreeDelete.delete(container); }

    private static Set<String> tree(Path repository, String commit) throws Exception {
        byte[] bytes = captureBytes(repository, List.of("git", "ls-tree", "-r", "-z", commit));
        Set<String> paths = new HashSet<>();
        for (String row : new String(bytes, StandardCharsets.UTF_8).split("\\x00")) {
            if (row.isEmpty()) continue; int tab = row.indexOf('\t');
            require(tab > 0 && row.substring(0, tab).matches("100(?:644|755) blob [0-9a-f]{40,64}"),
                    "Git snapshot contains a non-regular entry");
            String path = row.substring(tab + 1).replace('\\', '/'); safe(path);
            require(paths.add(path), "duplicate Git snapshot path");
        }
        require(!paths.isEmpty(), "Git snapshot is empty"); return Set.copyOf(paths);
    }

    private static Set<Path> archive(Path repository, String commit, Path root, Path source, Path errors)
            throws Exception {
        Process process = new ProcessBuilder("git", "archive", "--format=zip",
                "--output=" + source, commit)
                .directory(repository.toFile()).redirectError(errors.toFile()).start();
        process.getOutputStream().close();
        if (!process.waitFor(120, TimeUnit.SECONDS)) {
            process.descendants().forEach(ProcessHandle::destroyForcibly); process.destroyForcibly();
            throw new IOException("Git archive timed out");
        }
        require(process.exitValue() == 0, "Git archive failed: "
                + Files.readString(errors, StandardCharsets.UTF_8));
        Set<Path> paths = new HashSet<>();
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(source))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName().replace('\\', '/'); safe(name);
                Path target = root.resolve(name).normalize();
                require(target.startsWith(root), "Git archive entry escaped snapshot root");
                if (entry.isDirectory()) Files.createDirectories(target);
                else {
                    Files.createDirectories(target.getParent());
                    Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING);
                    require(paths.add(target.toAbsolutePath().normalize()), "duplicate Git archive entry");
                }
                zip.closeEntry();
            }
        }
        Files.delete(source); Files.deleteIfExists(errors); return Set.copyOf(paths);
    }

    private static String capture(Path repository, List<String> command) throws Exception {
        return new String(captureBytes(repository, command), StandardCharsets.UTF_8);
    }
    private static byte[] captureBytes(Path repository, List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(repository.toFile())
                .redirectErrorStream(true).start(); process.getOutputStream().close();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output);
        require(process.waitFor(60, TimeUnit.SECONDS) && process.exitValue() == 0,
                "Git snapshot command failed: " + String.join(" ", command));
        return output.toByteArray();
    }
    private static void safe(String path) throws IOException {
        require(!path.isBlank() && !Path.of(path).isAbsolute() && !path.startsWith("/")
                && !path.contains("../") && path.indexOf('\0') < 0, "unsafe Git snapshot path");
    }
    private static void require(boolean value, String message) throws IOException {
        if (!value) throw new IOException(message);
    }
}
