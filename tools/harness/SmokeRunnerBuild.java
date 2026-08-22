import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Compiles every discovered smoke coordinator against the shared support API. */
final class SmokeRunnerBuild {
    private final Path root, build, cache;

    SmokeRunnerBuild(Path root, Path build) {
        this.root = root; this.build = build;
        String control = System.getenv("WORLDLINE_GATE_CONTROL");
        this.cache = (control == null || control.isBlank() ? root.resolve(".worldline/cache")
                : Path.of(control).resolve("cache")).resolve("smoke-runners");
    }

    void compile() throws Exception {
        List<Path> sources;
        try (Stream<Path> paths = Files.list(root.resolve("tools/smoke"))) {
            sources = paths.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        }
        String digest = digest(sources);
        Path entry = cache.resolve(digest), complete = entry.resolve(".complete");
        Files.createDirectories(cache);
        boolean hit = Files.isRegularFile(complete);
        if (!hit) compileCached(sources, digest, entry, complete);
        Path target = build.resolve("smoke-runner-classes");
        copy(entry, target);
        System.out.println("  " + (hit ? "cached" : "compiled") + " " + sources.size() + " smoke runners");
    }

    private void compileCached(List<Path> sources, String digest, Path entry, Path complete) throws Exception {
        Path lockPath = cache.resolve(digest + ".lock");
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE); FileLock lock = channel.lock()) {
            if (!lock.isValid()) throw new IllegalStateException("invalid smoke runner cache lock");
            if (Files.isRegularFile(complete)) return;
            Path temporary = cache.resolve(digest + ".tmp-" + ProcessHandle.current().pid());
            delete(temporary); Files.createDirectories(temporary);
            Path arguments = temporary.resolve("javac.args");
            List<String> lines = new ArrayList<>(List.of("-encoding", "UTF-8", "--release", "21",
                    "-Xlint:all,-options", "-Werror", "-classpath", quote(harnessClasspath()),
                    "-d", quote(temporary.toString())));
            sources.forEach(path -> lines.add(quote(path.toString())));
            Files.write(arguments, lines, StandardCharsets.UTF_8);
            Process process = new ProcessBuilder(javaTool("javac"), "@" + arguments)
                    .directory(root.toFile()).inheritIO().start();
            if (!process.waitFor(300, TimeUnit.SECONDS)) {
                process.destroyForcibly(); throw new IllegalStateException("smoke runner compilation timed out");
            }
            if (process.exitValue() != 0) throw new IllegalStateException("smoke runner compilation failed");
            Files.delete(arguments); Files.writeString(temporary.resolve(".complete"), digest + "\n");
            delete(entry); Files.move(temporary, entry);
        }
    }

    private String digest(List<Path> sources) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(System.getProperty("java.version").getBytes(StandardCharsets.UTF_8));
        digest.update(Files.readAllBytes(root.resolve("tools/harness/SmokeSupport.java")));
        for (Path source : sources) {
            digest.update(root.relativize(source).toString().getBytes(StandardCharsets.UTF_8));
            digest.update(Files.readAllBytes(source));
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private String harnessClasspath() {
        String value = System.getenv("WORLDLINE_HARNESS_CP");
        if (value == null || value.isBlank()) throw new IllegalStateException("missing harness classpath");
        return value;
    }

    private static String quote(String value) { return "\"" + value.replace("\\", "/") + "\""; }

    private static void copy(Path source, Path target) throws IOException {
        delete(target); Files.createDirectories(target);
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : paths.sorted().collect(Collectors.toList())) {
                Path relative = source.relativize(path); if (relative.toString().equals(".complete")) continue;
                Path destination = target.resolve(relative);
                if (Files.isDirectory(path)) Files.createDirectories(destination);
                else Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void delete(Path target) throws IOException {
        if (!Files.exists(target)) return;
        try (Stream<Path> paths = Files.walk(target)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
                Files.deleteIfExists(path);
        }
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : "")).toString();
    }
}
