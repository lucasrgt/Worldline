package worldline.cli;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import worldline.test.WorldlineSpec;

/** Loads one explicitly named spec from one bounded JAR or class directory. */
final class TestSpecLoader implements Closeable {
    private static final long MAX_JAR_BYTES = 67_108_864L;
    private final URLClassLoader loader;
    private final URL source;
    private final Path sourcePath;

    TestSpecLoader(Path source) throws IOException { this(source, Collections.emptyList()); }
    TestSpecLoader(Path source, List<Path> classpath) throws IOException {
        Path real = source.toRealPath();
        if (!Files.isDirectory(real)) {
            require(Files.isRegularFile(real) && real.toString().endsWith(".jar")
                    && Files.size(real) > 0 && Files.size(real) <= MAX_JAR_BYTES,
                    "spec source must be a non-empty JAR or class directory");
        }
        sourcePath = real; this.source = real.toUri().toURL();
        List<URL> urls = new ArrayList<>(); urls.add(this.source);
        require(classpath.size() <= 256, "spec classpath contains more than 256 entries");
        for (Path entry : classpath) {
            Path dependency = entry.toRealPath();
            require(Files.isDirectory(dependency) || Files.isRegularFile(dependency)
                    && dependency.toString().endsWith(".jar") && Files.size(dependency) > 0
                    && Files.size(dependency) <= MAX_JAR_BYTES, "invalid spec classpath entry: " + entry);
            urls.add(dependency.toUri().toURL());
        }
        loader = new URLClassLoader(urls.toArray(new URL[0]), WorldlineSpec.class.getClassLoader());
    }

    WorldlineSpec load(String name) throws ReflectiveOperationException {
        require(name != null && name.matches(
                "[A-Za-z_$][A-Za-z0-9_$]*(?:\\.[A-Za-z_$][A-Za-z0-9_$]*)*"),
                "invalid spec class name");
        Class<? extends WorldlineSpec> type = Class.forName(name, true, loader).asSubclass(WorldlineSpec.class);
        require(type.getProtectionDomain().getCodeSource() != null
                && source.equals(type.getProtectionDomain().getCodeSource().getLocation()),
                "spec class was not loaded from the selected source");
        return type.getDeclaredConstructor().newInstance();
    }
    List<WorldlineSpec> loadAll(String name) throws ReflectiveOperationException, IOException {
        if (name != null && !"*".equals(name)) return Collections.singletonList(load(name));
        List<WorldlineSpec> specs = new ArrayList<>();
        for (String candidate : classNames()) {
            Class<?> type = Class.forName(candidate, false, loader);
            if (!WorldlineSpec.class.isAssignableFrom(type) || type == WorldlineSpec.class
                    || type.isInterface() || Modifier.isAbstract(type.getModifiers())) continue;
            @SuppressWarnings("unchecked")
            Class<? extends WorldlineSpec> spec = (Class<? extends WorldlineSpec>) type;
            require(spec.getProtectionDomain().getCodeSource() != null
                    && source.equals(spec.getProtectionDomain().getCodeSource().getLocation()),
                    "discovered spec was not loaded from the selected source");
            specs.add(spec.getDeclaredConstructor().newInstance());
        }
        require(!specs.isEmpty(), "no WorldlineSpec classes discovered"); return specs;
    }
    private List<String> classNames() throws IOException {
        List<String> values = new ArrayList<>();
        if (Files.isDirectory(sourcePath)) {
            try (Stream<Path> paths = Files.walk(sourcePath)) {
                for (Path path : (Iterable<Path>) paths::iterator) if (Files.isRegularFile(path))
                    add(values, sourcePath.relativize(path).toString().replace('\\', '/'));
            }
        } else try (JarFile jar = new JarFile(sourcePath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement(); if (!entry.isDirectory()) add(values, entry.getName());
            }
        }
        Collections.sort(values); return values;
    }
    private static void add(List<String> values, String name) {
        if (!name.endsWith(".class") || name.indexOf('$') >= 0 || name.equals("module-info.class")
                || name.endsWith("package-info.class")) return;
        require(values.size() < 10_000, "spec source contains more than 10000 classes");
        values.add(name.substring(0, name.length() - 6).replace('/', '.'));
    }
    @Override public void close() throws IOException { loader.close(); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
