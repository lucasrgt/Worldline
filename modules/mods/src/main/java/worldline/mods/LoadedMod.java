package worldline.mods;

import java.io.IOException;
import java.net.URLClassLoader;

/** Loaded mod instance and the isolated class-loader resource that owns it. */
public final class LoadedMod<T> implements AutoCloseable {
    private final ModArtifact artifact;
    private final T instance;
    private final URLClassLoader loader;

    LoadedMod(ModArtifact artifact, T instance, URLClassLoader loader) {
        this.artifact = artifact;
        this.instance = instance;
        this.loader = loader;
    }

    public ModArtifact artifact() { return artifact; }
    public T instance() { return instance; }

    @Override
    public void close() throws IOException { loader.close(); }
}
