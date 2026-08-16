package worldline.mods;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Inspects and loads descriptor-selected entrypoints from local mod JARs. */
public final class ModLoader {
    public static final long MAX_JAR_BYTES = 67_108_864L;

    private ModLoader() {}

    public static ModArtifact inspect(Path path, String runtime, String worldlineApi)
            throws IOException {
        if (path == null || runtime == null || worldlineApi == null) {
            throw new NullPointerException("mod inspection arguments");
        }
        Path real = path.toRealPath();
        require(Files.isRegularFile(real) && Files.size(real) > 0
                && Files.size(real) <= MAX_JAR_BYTES, "invalid mod JAR size");
        ModDescriptor descriptor = ModDescriptor.read(real);
        ModCompatibility compatibility = ModCompatibility.compare(
                runtime.equals(descriptor.runtime()), worldlineApi.equals(descriptor.worldlineApi()));
        return new ModArtifact(real, descriptor, digest(real), compatibility);
    }

    public static <T> LoadedMod<T> load(Path path, String runtime, String worldlineApi,
            Class<T> entrypointType) throws IOException, ReflectiveOperationException {
        if (entrypointType == null) throw new NullPointerException("entrypointType");
        ModArtifact artifact = inspect(path, runtime, worldlineApi);
        require(artifact.compatible(), "incompatible mod: " + artifact.compatibility());
        URL source = artifact.path().toUri().toURL();
        URLClassLoader loader = new URLClassLoader(new URL[] {source}, entrypointType.getClassLoader());
        try {
            Class<? extends T> type = Class.forName(artifact.descriptor().entrypoint(), true, loader)
                    .asSubclass(entrypointType);
            require(type.getProtectionDomain().getCodeSource() != null
                    && source.equals(type.getProtectionDomain().getCodeSource().getLocation()),
                    "mod entrypoint was not loaded from the inspected JAR");
            T instance = type.getDeclaredConstructor().newInstance();
            require(artifact.sha256().equals(digest(artifact.path())),
                    "mod JAR changed during entrypoint loading");
            return new LoadedMod<>(artifact, instance, loader);
        } catch (IOException | ReflectiveOperationException | RuntimeException | Error failure) {
            try { loader.close(); } catch (IOException closeFailure) { failure.addSuppressed(closeFailure); }
            throw failure;
        }
    }

    private static String digest(Path path) throws IOException {
        MessageDigest digest;
        try { digest = MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException error) { throw new IllegalStateException("SHA-256 unavailable", error); }
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            for (int count; (count = input.read(buffer)) >= 0;) digest.update(buffer, 0, count);
        }
        StringBuilder result = new StringBuilder(64);
        for (byte value : digest.digest()) result.append(String.format("%02x", value & 0xff));
        return result.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
