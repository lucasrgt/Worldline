import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Exact explainable input graph for one immutable compiled-module cache entry. */
final class ModuleInputGraph {
    private ModuleInputGraph() { }

    static Result build(Path repository, Properties config, String module) throws Exception {
        return build(repository.toAbsolutePath().normalize(), config, module, new HashSet<>());
    }

    static String identity(Path root, String module, String release, List<Path> sources,
            List<String> dependencies) throws Exception {
        return assemble(root, module, release, sources, dependencies).identity();
    }

    static List<Path> sources(Path root, String module) throws Exception {
        Path directory = root.resolve("modules").resolve(module).resolve("src/main/java");
        try (Stream<Path> paths = Files.walk(directory)) {
            List<Path> files = paths.filter(path -> path.toString().endsWith(".java"))
                    .sorted().collect(Collectors.toList());
            require(!files.isEmpty(), "no Java sources under " + directory); return files;
        }
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-module-input-graph-");
        try {
            Path base = source(root, "base", "Base.java");
            Path app = source(root, "app", "App.java");
            Files.writeString(base, "class Base {}\n", StandardCharsets.UTF_8);
            Files.writeString(app, "class App extends Base {}\n", StandardCharsets.UTF_8);
            Properties config = new Properties(); config.setProperty("java.release", "21");
            config.setProperty("module.base.dependencies", "");
            config.setProperty("module.app.dependencies", "base");
            Result graph = build(root, config, "app");
            require(graph.identity().matches("[0-9a-f]{64}") && graph.nodes().size() == 5,
                    "module input graph did not seal exact inputs");
            require(graph.json().contains("worldline.module-input-graph.v1")
                    && graph.json().contains("modules/app/src/main/java/App.java")
                    && graph.json().contains("\"key\":\"base\"")
                    && !graph.json().contains("class App"),
                    "module input graph schema or content redaction drifted");
            rejected(() -> build(root, config, "missing"), "unknown module graph was accepted");
            config.setProperty("module.base.dependencies", "app");
            rejected(() -> build(root, config, "app"), "cyclic module graph was accepted");
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  module input graph self-test: passed");
    }

    private static Result build(Path root, Properties config, String module, Set<String> stack)
            throws Exception {
        require(module.matches("[a-z0-9]+(?:-[a-z0-9]+)*")
                && config.getProperty("module." + module + ".dependencies") != null,
                "unknown module input graph: " + module);
        require(stack.add(module), "cyclic module input graph: " + module);
        try {
            String release = config.getProperty("module." + module + ".release",
                    required(config, "java.release"));
            List<String> dependencyIds = list(config.getProperty(
                    "module." + module + ".dependencies"));
            List<String> dependencies = new ArrayList<>();
            for (String dependency : dependencyIds)
                dependencies.add(build(root, config, dependency, stack).identity());
            return labelDependencies(assemble(root, module, release,
                    sources(root, module), dependencies), dependencyIds);
        } finally { stack.remove(module); }
    }

    private static Result assemble(Path root, String module, String release, List<Path> sources,
            List<String> dependencies) throws Exception {
        MessageDigest identity = HexDigest.sha256(); List<Node> nodes = new ArrayList<>();
        scalar(identity, nodes, "identity", "module", module);
        scalar(identity, nodes, "toolchain", "release", release);
        scalar(identity, nodes, "toolchain", "java.version", System.getProperty("java.version"));
        for (int index = 0; index < dependencies.size(); index++) {
            String digest = dependencies.get(index); require(digest.matches("[0-9a-f]{64}"),
                    "invalid module dependency identity");
            update(identity, digest); nodes.add(new Node("dependency",
                    Integer.toString(index), digest));
        }
        for (Path source : sources) {
            Path path = source.toAbsolutePath().normalize();
            require(path.startsWith(root) && Files.isRegularFile(path),
                    "module input source escaped repository");
            String relative = root.relativize(path).toString().replace('\\', '/');
            byte[] bytes = Files.readAllBytes(path); update(identity, relative); identity.update(bytes);
            nodes.add(new Node("source", relative, digest(bytes)));
        }
        return new Result(module, HexFormat.of().formatHex(identity.digest()), List.copyOf(nodes));
    }

    private static Result labelDependencies(Result result, List<String> names) {
        List<Node> nodes = new ArrayList<>(); int dependency = 0;
        for (Node node : result.nodes()) nodes.add(node.kind().equals("dependency")
                ? new Node(node.kind(), names.get(dependency++), node.sha256()) : node);
        require(dependency == names.size(), "module dependency labels drifted");
        return new Result(result.module(), result.identity(), List.copyOf(nodes));
    }

    private static void scalar(MessageDigest identity, List<Node> nodes,
            String kind, String key, String value) throws Exception {
        update(identity, value); nodes.add(new Node(kind, key, digest(value.getBytes(StandardCharsets.UTF_8))));
    }
    private static String digest(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(HexDigest.sha256().digest(bytes));
    }
    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
    }
    private static Path source(Path root, String module, String name) throws Exception {
        Path directory = root.resolve("modules").resolve(module).resolve("src/main/java");
        Files.createDirectories(directory); return directory.resolve(name);
    }
    private static List<String> list(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Stream.of(raw.split(",")).map(String::trim).filter(value -> !value.isEmpty()).toList();
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(),
                "missing module input property: " + key); return value;
    }
    private static void rejected(Action action, String message) throws Exception {
        boolean rejected = false;
        try { action.run(); } catch (IllegalStateException expected) { rejected = true; }
        require(rejected, message);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    record Result(String module, String identity, List<Node> nodes) {
        String json() {
            StringBuilder text = new StringBuilder("{\n  \"schema\":\"worldline.module-input-graph.v1\",\n")
                    .append("  \"module\":\"").append(module).append("\",\n  \"identity_sha256\":\"")
                    .append(identity).append("\",\n  \"nodes\":[\n");
            for (int index = 0; index < nodes.size(); index++) {
                Node node = nodes.get(index); text.append("    {\"kind\":\"").append(node.kind)
                        .append("\",\"key\":\"").append(node.key).append("\",\"sha256\":\"")
                        .append(node.sha256).append("\"}").append(index + 1 == nodes.size() ? '\n' : ",\n");
            }
            return text.append("  ]\n}\n").toString();
        }
    }
    record Node(String kind, String key, String sha256) { }
    @FunctionalInterface private interface Action { void run() throws Exception; }
}
