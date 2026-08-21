package dev.worldline.gradle;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

/** Actionable local diagnosis without launching Minecraft. */
public abstract class WorldlineDoctorTask extends DefaultTask {
    private Path source, repository; private OraclePaths paths; private boolean runtimeRequired;
    void configure(Path source, Path repository, OraclePaths paths, boolean runtimeRequired) {
        this.source = source; this.repository = repository; this.paths = paths;
        this.runtimeRequired = runtimeRequired;
    }
    @TaskAction public void diagnose() {
        boolean failed = false; getLogger().lifecycle("WORLDLINE DOCTOR  runtime=b1.7.3");
        if (Runtime.version().feature() >= 21) passed("JDK " + Runtime.version().feature() + " tooling");
        else { getLogger().error("x JDK 21 or newer is required"); failed = true; }
        if (Files.isDirectory(source)) passed("Java test source " + source); else { missing("test source", source); failed = true; }
        failed |= !oracle("client", paths.client, OraclePaths.CLIENT_BYTES,
                OraclePaths.CLIENT_SHA256, runtimeRequired);
        oracle("server", paths.server, OraclePaths.SERVER_BYTES, OraclePaths.SERVER_SHA256, false);
        if (tracked(paths.client) || tracked(paths.server)) {
            getLogger().error("x official JAR is tracked by Git"); failed = true;
        } else passed("official JARs are outside Git tracking");
        if (!runtimeRequired) passed("host-only mode; runtime provider disabled");
        else if (paths.clientPresent()) passed("controlled-client tests available");
        if (failed) throw new GradleException("Worldline doctor found blocking problems");
    }
    private boolean oracle(String name, Path path, long bytes, String hash, boolean required) {
        if (!Files.isRegularFile(path)) {
            getLogger().lifecycle("{} {} oracle absent: {}", required ? "x" : "o", name, path);
            return !required;
        }
        try { OracleVerifier.verify(path, bytes, hash); passed(name + " oracle verified: " + path); return true; }
        catch (RuntimeException error) { getLogger().error("x {}", error.getMessage()); return false; }
    }
    private boolean tracked(Path path) {
        if (!Files.exists(path)) return false;
        try {
            Path absolute = path.toAbsolutePath().normalize();
            if (!absolute.startsWith(repository)) return false;
            Process process = new ProcessBuilder("git", "-C", repository.toString(),
                    "ls-files", "--error-unmatch", repository.relativize(absolute).toString())
                    .redirectErrorStream(true).start();
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                process.getInputStream().transferTo(bytes); return process.waitFor() == 0
                        && !bytes.toString(StandardCharsets.UTF_8).trim().isEmpty();
            }
        } catch (Exception error) { throw new GradleException("cannot inspect Git tracking", error); }
    }
    private void passed(String message) { getLogger().lifecycle("+ {}", message); }
    private void missing(String label, Path path) { getLogger().error("x {} absent: {}", label, path); }
}
