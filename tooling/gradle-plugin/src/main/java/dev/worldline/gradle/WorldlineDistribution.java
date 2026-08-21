package dev.worldline.gradle;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

/** Hash-pinned TestKit release resolution with an explicit offline override. */
final class WorldlineDistribution {
    static final String VERSION = "0.2.1";
    static final String API_SHA256 = "811e72bb1cf49c075c8936d953aeb6abddff0fb7943bef850358051aaeaec861";
    static final String RUNNER_SHA256 = "69ecddc1aab517006ecd88febf0f7faec60bf404a0dcfa32795c0209f43a069f";
    private static final String RELEASE = "https://github.com/lucasrgt/Worldline/releases/download/testkit-v"
            + VERSION + "/";
    private WorldlineDistribution() {}
    static java.io.File artifact(Project project, String name, String hash) {
        String file = "worldline-test-" + name + "-" + VERSION + ".jar";
        Object override = project.findProperty("worldline.distributionDir");
        Path target = override == null
                ? project.getGradle().getGradleUserHomeDir().toPath().resolve("caches/worldline-testkit")
                        .resolve(VERSION).resolve(file)
                : project.file(override).toPath().resolve(file);
        if (Files.isRegularFile(target)) { verify(target, hash); return target.toFile(); }
        if (override != null) throw new GradleException("Worldline distribution artifact absent: " + target);
        download(URI.create(RELEASE + file), target, hash); return target.toFile();
    }
    private static void download(URI source, Path target, String hash) {
        try {
            Files.createDirectories(target.getParent()); Path temporary = Files.createTempFile(
                    target.getParent(), target.getFileName().toString(), ".part");
            try {
                try (InputStream input = source.toURL().openStream()) {
                    Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING);
                }
                verify(temporary, hash); Files.move(temporary, target,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } finally { Files.deleteIfExists(temporary); }
        } catch (Exception error) { throw new GradleException("cannot provision Worldline artifact " + source, error); }
    }
    private static void verify(Path path, String expected) {
        try {
            if (!OracleVerifier.digest(path).equals(expected))
                throw new GradleException("Worldline artifact hash mismatch: " + path);
        } catch (java.io.IOException error) { throw new GradleException("cannot hash " + path, error); }
    }
}
