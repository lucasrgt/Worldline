import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipFile;

/** Preserves branch, patch, untracked source, logs, and receipts without deleting a worktree. */
final class SwarmEvidenceArchive {
    private SwarmEvidenceArchive() { }

    static Result save(Path directory, String id, Path worktree, String branch, String base,
            String head, String tree, String state, String status, List<Path> logs, Path receipt,
            Result repositoryBundle)
            throws Exception {
        Files.createDirectories(directory);
        Path output = directory.resolve(id + "-" + head.substring(0, 12) + ".zip");
        if (Files.exists(output)) {
            try (ZipFile zip = new ZipFile(output.toFile())) {
                require(zip.getEntry("manifest.properties") != null
                        && zip.getEntry("working-tree.patch") != null,
                        "existing evidence archive is incomplete: " + output);
            }
            return new Result(output.toString(), sha256(output));
        }
        try {
            String patch = SwarmProcess.output(worktree, List.of("diff", "--binary", "HEAD"), 180);
            String untracked = SwarmProcess.output(worktree,
                    List.of("ls-files", "--others", "--exclude-standard", "-z"), 180);
            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(output))) {
                addText(zip, "manifest.properties", "schema=1\nid=" + id + "\nstate=" + state
                        + "\nworktree=" + worktree + "\nbranch=" + branch + "\nbase=" + base
                        + "\nhead=" + head + "\ntree=" + tree + "\nrepository.bundle="
                        + repositoryBundle.path + "\nrepository.bundle.sha256="
                        + repositoryBundle.sha256 + "\ncreated=" + Instant.now() + "\n");
                addText(zip, "status.txt", status);
                addText(zip, "working-tree.patch", patch);
                for (String relative : untracked.split("\u0000")) if (!relative.isBlank()) {
                    Path source = worktree.resolve(relative).normalize();
                    require(source.startsWith(worktree), "untracked path escaped worktree: " + relative);
                    if (Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS))
                        addFile(zip, source, "untracked/" + slash(relative));
                }
                for (Path log : logs) if (Files.isRegularFile(log, LinkOption.NOFOLLOW_LINKS))
                    addFile(zip, log, "evidence/" + log.getFileName());
                if (Files.isRegularFile(receipt, LinkOption.NOFOLLOW_LINKS)
                        && logs.stream().noneMatch(receipt::equals))
                    addFile(zip, receipt, "evidence/" + receipt.getFileName());
                addPrivateTree(zip, worktree.resolve(".worldline/reports"), "private/reports/");
                addPrivateTree(zip, worktree.resolve(".worldline/smoke-logs"), "private/smoke-logs/");
            }
        } catch (Exception error) { Files.deleteIfExists(output); throw error; }
        return new Result(output.toString(), sha256(output));
    }

    static Result saveRepositoryBundle(Path root, Path directory) throws Exception {
        Files.createDirectories(directory);
        Path output = directory.resolve("milestone-branches.bundle");
        if (Files.exists(output)) {
            SwarmProcess.run(root, List.of("bundle", "verify", output.toString()), 600);
            return new Result(output.toString(), sha256(output));
        }
        try {
            SwarmProcess.run(root, List.of("bundle", "create", output.toString(),
                    "--branches=codex/milestone-m*"), 600);
            SwarmProcess.run(root, List.of("bundle", "verify", output.toString()), 600);
        } catch (Exception error) { Files.deleteIfExists(output); throw error; }
        return new Result(output.toString(), sha256(output));
    }

    private static void addPrivateTree(ZipOutputStream zip, Path directory, String prefix)
            throws IOException {
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) return;
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory)) {
            for (Path entry : entries) {
                String name = prefix + entry.getFileName();
                if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS))
                    addPrivateTree(zip, entry, name + "/");
                else if (Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS)) addFile(zip, entry, name);
            }
        }
    }

    private static void addText(ZipOutputStream zip, String name, String text) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(text.getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
    }

    private static void addFile(ZipOutputStream zip, Path source, String name) throws IOException {
        zip.putNextEntry(new ZipEntry(slash(name)));
        try (InputStream input = new BufferedInputStream(Files.newInputStream(source))) {
            input.transferTo(zip);
        }
        zip.closeEntry();
    }

    static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = new BufferedInputStream(Files.newInputStream(path))) {
            byte[] buffer = new byte[8192]; int count;
            while ((count = input.read(buffer)) >= 0) if (count > 0) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static String slash(String value) { return value.replace('\\', '/'); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    record Result(String path, String sha256) {
        static Result empty() { return new Result("", ""); }
    }
}
