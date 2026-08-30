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
    static final String VERSION = "0.3.1";
    static final String API_SHA256 = "8a54dab3199a38ac1ae5f6f4180d790ab19590157092f95edda5d9e37bb86834";
    static final String RUNNER_SHA256 = "151453eb55e5a031ed1611ab162fb2cd14eb2d4b7f5030ffa0b7bddc0686bfa0";
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
