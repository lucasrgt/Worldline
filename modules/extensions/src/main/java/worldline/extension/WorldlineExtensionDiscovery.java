package worldline.extension;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Discovers project-owned manifests and loads their public entrypoints. */
public final class WorldlineExtensionDiscovery {
    private static final int MAX_EXTENSIONS = 128;
    private WorldlineExtensionDiscovery() {}

    public static List<WorldlineExtensionPlan> discover(Path projectRoot,
            ExtensionCapabilities hostCapabilities) throws IOException, ReflectiveOperationException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) loader = WorldlineExtensionDiscovery.class.getClassLoader();
        return discover(projectRoot, loader, hostCapabilities);
    }

    public static List<WorldlineExtensionPlan> discover(Path projectRoot, ClassLoader loader,
            ExtensionCapabilities hostCapabilities) throws IOException, ReflectiveOperationException {
        if (projectRoot == null || loader == null || hostCapabilities == null)
            throw new NullPointerException();
        Path root = projectRoot.toRealPath();
        Path directory = root.resolve("worldline/extensions");
        if (!Files.exists(directory)) return Collections.emptyList();
        if (!Files.isDirectory(directory)) throw new IllegalArgumentException(
                "worldline/extensions must be a directory");
        List<Path> manifests = new ArrayList<Path>();
        try (java.util.stream.Stream<Path> entries = Files.list(directory)) {
            for (Path entry : (Iterable<Path>) entries::iterator) {
                if (!Files.isDirectory(entry)) {
                    String name = entry.getFileName().toString();
                    if ("TEMPLATE.properties".equals(name)
                            || "SEMANTICS_TEMPLATE.properties".equals(name)) continue;
                    throw new IllegalArgumentException(
                            "extension entry must be a directory: " + entry.getFileName());
                }
                Path manifest = entry.resolve("manifest.properties");
                if (!Files.isRegularFile(manifest)) throw new IllegalArgumentException(
                        "missing extension manifest: " + entry.getFileName());
                manifests.add(manifest);
            }
        }
        if (manifests.size() > MAX_EXTENSIONS) throw new IllegalArgumentException(
                "more than " + MAX_EXTENSIONS + " extensions");
        Collections.sort(manifests, new Comparator<Path>() {
            @Override public int compare(Path left, Path right) {
                return left.toString().compareTo(right.toString());
            }
        });
        List<WorldlineExtensionPlan> plans = new ArrayList<WorldlineExtensionPlan>();
        for (Path path : manifests) plans.add(load(path, loader, hostCapabilities));
        return Collections.unmodifiableList(plans);
    }

    public static WorldlineExtensionPlan load(Path path, ClassLoader loader,
            ExtensionCapabilities hostCapabilities) throws IOException, ReflectiveOperationException {
        ExtensionManifest manifest = ExtensionManifest.load(path);
        if (!path.getParent().getFileName().toString().equals(manifest.id()))
            throw new IllegalArgumentException("manifest id does not match directory");
        if (!hostCapabilities.containsAll(manifest.requires()))
            throw new IllegalArgumentException("extension " + manifest.id()
                    + " requires unavailable capabilities " + manifest.requires().csv());
        WorldlineExtension extension = entrypoint(manifest.entrypoint(), loader);
        ExtensionPlanBuilder builder = new ExtensionPlanBuilder(manifest);
        extension.register(builder);
        return new WorldlineExtensionPlan(builder);
    }

    private static WorldlineExtension entrypoint(String name, ClassLoader loader)
            throws ReflectiveOperationException {
        try {
            return Class.forName(name, true, loader).asSubclass(WorldlineExtension.class)
                    .getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw error;
        }
    }
}
