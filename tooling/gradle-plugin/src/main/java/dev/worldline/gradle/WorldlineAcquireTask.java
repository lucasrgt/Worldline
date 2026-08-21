package dev.worldline.gradle;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

/** Explicit, hash-verified acquisition; never runs as an implicit test dependency. */
public abstract class WorldlineAcquireTask extends DefaultTask {
    private static final URI CLIENT = URI.create(
            "https://launcher.mojang.com/v1/objects/43db9b498cb67058d2e12d394e6507722e71bb45/client.jar");
    private static final URI SERVER = URI.create(
            "https://vault.omniarchive.uk/archive/java/server-beta/b1.7/b1.7.3.jar");
    @TaskAction public void acquire() {
        WorldlineExtension extension = getProject().getExtensions().getByType(WorldlineExtension.class);
        OraclePaths paths = OraclePaths.resolve(getProject(), extension);
        acquire(CLIENT, paths.client, OraclePaths.CLIENT_BYTES, OraclePaths.CLIENT_SHA256, "client");
        if (Boolean.parseBoolean(String.valueOf(getProject().findProperty("worldline.acquireServer"))))
            acquire(SERVER, paths.server, OraclePaths.SERVER_BYTES, OraclePaths.SERVER_SHA256, "server");
    }
    private void acquire(URI source, Path target, long bytes, String hash, String label) {
        if (Files.isRegularFile(target)) {
            OracleVerifier.verify(target, bytes, hash);
            getLogger().lifecycle("Worldline {} oracle already verified: {}", label, target); return;
        }
        try {
            Files.createDirectories(target.getParent()); Path temporary = Files.createTempFile(
                    target.getParent(), "worldline-" + label + "-", ".part");
            try {
                getLogger().lifecycle("Downloading the explicit b1.7.3 {} oracle...", label);
                try (InputStream input = source.toURL().openStream()) {
                    Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING);
                }
                OracleVerifier.verify(temporary, bytes, hash);
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            } finally { Files.deleteIfExists(temporary); }
            getLogger().lifecycle("Worldline {} oracle acquired and verified: {}", label, target);
        } catch (Exception error) { throw new GradleException("oracle acquisition failed", error); }
    }
}
