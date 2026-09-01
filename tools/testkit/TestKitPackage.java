import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

/** Builds deterministic ignored TestKit 0.x authoring and runner distributions. */
public final class TestKitPackage {
    private static final String VERSION = "0.3.1";
    private static final String GROUP = "io.github.lucasrgt.worldline";
    private static final List<String> API = Arrays.asList("api", "extensions", "testmodel", "testapi");
    private static final List<String> RUNNER = Arrays.asList("optimization", "api", "extensions", "invariants",
            "semantics", "trace", "kernel", "reproduction", "mods", "analysis", "modtest",
            "minimization", "testmodel", "testapi", "testkit", "cli");
    private TestKitPackage() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length > 1) throw new IllegalArgumentException("usage: TestKitPackage.java [output]");
        Path root = Paths.get("").toAbsolutePath().normalize();
        Path output = root.resolve(arguments.length == 0 ? ".worldline/dist/testkit" : arguments[0])
                .normalize();
        require(output.startsWith(root.resolve(".worldline")), "output must stay under .worldline");
        Path classes = root.resolve(".worldline/build/classes");
        Path adapters = root.resolve(".worldline/build/adapter-classes");
        for (String module : RUNNER) require(Files.isDirectory(classes.resolve(module)),
                "missing compiled module " + module + "; run Gate first");
        Files.createDirectories(output);
        Path api = output.resolve("worldline-test-api-" + VERSION + ".jar");
        Path runner = output.resolve("worldline-test-runner-" + VERSION + ".jar");
        Path provider = output.resolve(
                "worldline-test-provider-b173-server-lifecycle-" + VERSION + ".jar");
        build(api, classes, API, null); build(runner, classes, RUNNER, "worldline.cli.WorldlineCli");
        buildRoots(provider, Arrays.asList(adapters.resolve("b173-server"),
                adapters.resolve("b173-server-testkit")));
        maven(output.resolve("maven"), api, "worldline-test-api");
        maven(output.resolve("maven"), runner, "worldline-test-runner");
        maven(output.resolve("maven"), provider,
                "worldline-test-provider-b173-server-lifecycle", true);
        String apiHash = sha256(api), runnerHash = sha256(runner), providerHash = sha256(provider);
        Files.write(output.resolve("checksums.properties"), Arrays.asList(
                "format=1", api.getFileName() + "=" + apiHash,
                runner.getFileName() + "=" + runnerHash,
                provider.getFileName() + "=" + providerHash), StandardCharsets.US_ASCII);
        Files.write(output.resolve("worldline-test.ps1"), Arrays.asList(
                "$ErrorActionPreference = 'Stop'",
                "java -jar (Join-Path $PSScriptRoot '" + runner.getFileName() + "') @args",
                "exit $LASTEXITCODE"), StandardCharsets.UTF_8);
        Files.write(output.resolve("worldline-test.cmd"), Arrays.asList("@echo off",
                "java -jar \"%~dp0" + runner.getFileName() + "\" %*"), StandardCharsets.US_ASCII);
        System.out.println("WORLDLINE_TESTKIT_PACKAGE=PASS");
        System.out.println("api=" + api); System.out.println("runner=" + runner);
        System.out.println("provider=" + provider);
        System.out.println("api.sha256=" + apiHash);
        System.out.println("runner.sha256=" + runnerHash);
        System.out.println("provider.sha256=" + providerHash);
    }

    private static void build(Path target, Path classes, List<String> modules, String main) throws Exception {
        Set<String> names = new LinkedHashSet<>(); List<Entry> entries = new ArrayList<>();
        for (String module : modules) {
            Path moduleRoot = classes.resolve(module).toRealPath();
            require(Files.isRegularFile(moduleRoot.resolve(".complete")),
                    "compiled module is not an immutable cache entry: " + module);
            int before = entries.size();
            try (Stream<Path> stream = Files.walk(moduleRoot)) {
                for (Path path : (Iterable<Path>) stream.filter(Files::isRegularFile)
                        .filter(file -> !file.getFileName().toString().equals(".complete"))::iterator) {
                    String name = moduleRoot.relativize(path).toString().replace('\\', '/');
                    require(names.add(name), "duplicate distribution entry: " + name);
                    require(entries.size() < 20_000, "distribution contains too many entries");
                    byte[] bytes = Files.readAllBytes(path);
                    require(bytes.length <= 8_388_608, "entry too large: " + name);
                    entries.add(new Entry(name, bytes));
                }
            }
            require(entries.size() > before, "compiled module contains no distribution entries: " + module);
        }
        Collections.sort(entries); Files.createDirectories(target.getParent());
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            write(jar, "META-INF/MANIFEST.MF", manifest(main));
            for (Entry entry : entries) write(jar, entry.name, entry.bytes);
        }
    }
    private static void buildRoots(Path target, List<Path> roots) throws Exception {
        Set<String> names = new LinkedHashSet<>(); List<Entry> entries = new ArrayList<>();
        for (Path configured : roots) {
            Path source = configured.toRealPath(); int before = entries.size();
            try (Stream<Path> stream = Files.walk(source)) {
                for (Path path : (Iterable<Path>) stream.filter(Files::isRegularFile)::iterator) {
                    String name = source.relativize(path).toString().replace('\\', '/');
                    require(names.add(name), "duplicate provider entry: " + name);
                    byte[] bytes = Files.readAllBytes(path);
                    require(bytes.length <= 8_388_608, "provider entry too large: " + name);
                    entries.add(new Entry(name, bytes));
                }
            }
            require(entries.size() > before, "provider root contains no entries: " + configured);
        }
        Collections.sort(entries); Files.createDirectories(target.getParent());
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            write(jar, "META-INF/MANIFEST.MF", manifest(null));
            for (Entry entry : entries) write(jar, entry.name, entry.bytes);
        }
    }
    private static byte[] manifest(String main) {
        String value = "Manifest-Version: 1.0\r\nImplementation-Title: Worldline TestKit\r\n"
                + "Implementation-Version: " + VERSION + "\r\n"
                + (main == null ? "" : "Main-Class: " + main + "\r\n") + "\r\n";
        return value.getBytes(StandardCharsets.US_ASCII);
    }
    private static void maven(Path repository, Path jar, String artifact) throws IOException {
        maven(repository, jar, artifact, false);
    }
    private static void maven(Path repository, Path jar, String artifact,
            boolean dependsOnApi) throws IOException {
        Path directory = repository.resolve(GROUP.replace('.', '/')).resolve(artifact).resolve(VERSION);
        Files.createDirectories(directory);
        Files.copy(jar, directory.resolve(artifact + "-" + VERSION + ".jar"),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        String pom = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 "
                + "https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
                + "  <modelVersion>4.0.0</modelVersion>\n  <groupId>" + GROUP + "</groupId>\n"
                + "  <artifactId>" + artifact + "</artifactId>\n  <version>" + VERSION + "</version>\n"
                + "  <name>Worldline TestKit</name>\n  <licenses><license><name>MIT</name>"
                + "<url>https://opensource.org/license/mit</url></license></licenses>\n"
                + (dependsOnApi ? "  <dependencies><dependency><groupId>" + GROUP
                        + "</groupId><artifactId>worldline-test-api</artifactId><version>"
                        + VERSION + "</version></dependency></dependencies>\n" : "")
                + "</project>\n";
        Files.writeString(directory.resolve(artifact + "-" + VERSION + ".pom"), pom,
                StandardCharsets.UTF_8);
    }
    private static void write(JarOutputStream jar, String name, byte[] bytes) throws IOException {
        JarEntry entry = new JarEntry(name); entry.setTime(0L); jar.putNextEntry(entry);
        jar.write(bytes); jar.closeEntry();
    }
    private static String sha256(Path path) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
        StringBuilder value = new StringBuilder();
        for (byte item : digest) value.append(String.format("%02x", item & 255)); return value.toString();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private static final class Entry implements Comparable<Entry> {
        final String name; final byte[] bytes;
        Entry(String name, byte[] bytes) { this.name = name; this.bytes = bytes; }
        @Override public int compareTo(Entry other) { return name.compareTo(other.name); }
    }
}
