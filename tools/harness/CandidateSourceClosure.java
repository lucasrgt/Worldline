import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/** Compiles the exact candidate source closure before a readiness PASS can be emitted. */
final class CandidateSourceClosure {
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final String id;
    private final Path build;
    private final Properties config;
    private final Properties descriptor;

    private CandidateSourceClosure(String id) throws Exception {
        this.id = id;
        this.build = root.resolve(".worldline/candidate-readiness").resolve(id);
        this.config = StrictProperties.load(root.resolve("harness.properties"));
        this.descriptor = StrictProperties.load(root.resolve("smokes").resolve(id)
                .resolve("smoke.properties"));
    }

    static void compile(String id) throws Exception {
        new CandidateSourceClosure(id).run();
    }

    private void run() throws Exception {
        SmokeDiscovery.Entry smoke = SmokeDiscovery.require(root, id);
        verifyCycleArtifact(root, descriptor, SmokeTrackedFiles.read(root));
        recreate();
        List<String> modules = values("modules");
        List<Path> outputs = new ModuleBuild(root, build, config, modules).compileAll();
        compileRunner(smoke);
        compileScenario(modules, outputs);
        compileOracle(outputs);
        System.out.println("  compiled exact pre-Candidate source closure");
    }

    static void selfTest() throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        Path test = root.resolve(".worldline/candidate-oracle-closure-self-test");
        SafeTreeDelete.delete(test);
        Files.createDirectories(test);
        try {
            Path contract = test.resolve("Contract.java");
            Path good = test.resolve("Good.java");
            Path bad = test.resolve("Bad.java");
            Files.writeString(contract, "interface Contract { void run(); }\n");
            Files.writeString(good,
                    "final class Good implements Contract { public void run() { } }\n");
            Files.writeString(bad, "import java.io.IOException;\n"
                    + "final class Bad implements Contract {\n"
                    + "  public void run() throws IOException { }\n}\n");
            Path classes = test.resolve("classes");
            Files.createDirectories(classes);
            compile(root, List.of(javaTool("javac"), "-d", classes.toString(),
                    contract.toString(), good.toString()), 60);
            boolean rejected = false;
            try {
                compile(root, List.of(javaTool("javac"), "-classpath", classes.toString(),
                        "-d", classes.toString(), bad.toString()), 60);
            } catch (Exception expected) {
                rejected = expected.getMessage().contains("overridden method does not throw");
            }
            require(rejected, "oracle checked-exception mismatch was not rejected");
            List<Path> selected = selectOutputs(List.of("api", "smoketest", "testkit"),
                    List.of(Path.of("api"), Path.of("smoketest"), Path.of("testkit")),
                    "api,testkit");
            require(selected.equals(List.of(Path.of("api"), Path.of("testkit"))),
                    "data-driven compile products were not selected exactly");
            artifactSelfTest(test);
        } finally {
            SafeTreeDelete.delete(test);
        }
    }

    private static void artifactSelfTest(Path test) throws Exception {
        Path artifacts = test.resolve("artifacts");
        Files.createDirectories(artifacts);
        Path artifact = artifacts.resolve("server.jar");
        Files.writeString(artifact, "synthetic official artifact", StandardCharsets.UTF_8);
        Path descriptor = artifacts.resolve("test.properties");
        Files.writeString(descriptor, "local.path=artifacts/server.jar\nexpected.bytes="
                + Files.size(artifact) + "\nexpected.sha1=" + SmokeSupport.digest(artifact, "SHA-1")
                + "\nexpected.sha256=" + SmokeSupport.digest(artifact, "SHA-256") + "\n",
                StandardCharsets.UTF_8);
        Properties smoke = new Properties();
        smoke.setProperty("cycle.artifact", "artifacts/test.properties");
        verifyCycleArtifact(test, smoke, Set.of(descriptor));
        boolean untrackedRejected = false;
        try {
            verifyCycleArtifact(test, smoke, Set.of());
        } catch (IllegalStateException expected) {
            untrackedRejected = expected.getMessage().contains("descriptor is not tracked");
        }
        require(untrackedRejected, "untracked cycle artifact descriptor passed readiness");
        Files.delete(artifact);
        boolean missingRejected = false;
        try {
            verifyCycleArtifact(test, smoke, Set.of(descriptor));
        } catch (IllegalStateException expected) {
            missingRejected = expected.getMessage().contains("official artifact absent");
        }
        require(missingRejected, "missing cycle artifact passed readiness");
    }

    private static void verifyCycleArtifact(Path root, Properties descriptor, Set<Path> tracked)
            throws Exception {
        String reference = descriptor.getProperty("cycle.artifact", "").trim();
        if (reference.isEmpty()) return;
        require(reference.matches("artifacts/[a-z0-9.-]+\\.properties"),
                "unsafe cycle artifact descriptor");
        Path artifactDescriptor = root.resolve(reference).normalize();
        require(artifactDescriptor.startsWith(root) && Files.isRegularFile(artifactDescriptor),
                "missing cycle artifact descriptor: " + reference);
        require(tracked.contains(artifactDescriptor.toAbsolutePath().normalize()),
                "cycle artifact descriptor is not tracked: " + reference);
        Properties artifact = StrictProperties.load(artifactDescriptor);
        Path binary = root.resolve(SmokeSupport.value(artifact, "local.path")).normalize();
        require(binary.startsWith(root) && !binary.equals(root), "unsafe cycle artifact path");
        SmokeSupport.verifyArtifact(binary, artifact);
    }

    private void compileRunner(SmokeDiscovery.Entry smoke) throws Exception {
        Path output = build.resolve("runner-classes");
        Files.createDirectories(output);
        execute(List.of(javaTool("javac"), "-encoding", "UTF-8", "--release", "21",
                "-Xlint:all,-options", "-Werror", "-classpath",
                System.getenv("WORLDLINE_HARNESS_CP"), "-d", output.toString(),
                root.resolve(smoke.runner).toString()), 180);
    }

    private void compileScenario(List<String> modules, List<Path> outputs) throws Exception {
        Path milestone = root.resolve("smokes").resolve(id);
        Path source = milestone.resolve("src");
        if (!Files.isDirectory(source)) {
            require(CandidateRuntimeBuild.owns(milestone, descriptor)
                    || "tooling".equals(descriptor.getProperty("candidate.kind")),
                    "missing candidate scenario sources");
            return;
        }
        boolean clientOnly = "client".equals(descriptor.getProperty("side"))
                || "client".equals(descriptor.getProperty("atlas.artifact"))
                || descriptor.getProperty("client.jar.sha256") != null
                        && descriptor.getProperty("server.jar.sha256") == null;
        List<Path> dependencies = new ArrayList<>();
        boolean mappedServer = !clientOnly && Files.isRegularFile(milestone.resolve("symbols.map"))
                && descriptor.getProperty("worldline.main") != null;
        if (clientOnly) {
            Path headless = build.resolve("headless-classes");
            Files.createDirectories(headless);
            List<String> stubs = new ArrayList<>(List.of(javaTool("javac"), "-encoding", "UTF-8",
                    "--release", "8", "-Xlint:all,-options", "-Werror", "-d", headless.toString()));
            stubs.addAll(javaFiles(root.resolve("adapters/b173-client/headless-src")));
            execute(stubs, 180);
            Path mapped = root.resolve("local/workspaces/b1.7.3/minecraft/bin");
            require(Files.isRegularFile(mapped.resolve("net/minecraft/client/Minecraft.class")),
                    "client candidate requires the prepared mapped workspace");
            dependencies.add(headless);
            dependencies.add(mapped);
            dependencies.addAll(jarFiles(root.resolve("local/workspaces/b1.7.3/libraries")));
        } else if (mappedServer) {
            Path mapped = root.resolve("local/workspaces/b1.7.3/minecraft_server/bin");
            if (!Files.isRegularFile(mapped.resolve("net/minecraft/src/World.class"))) return;
            dependencies.add(mapped);
            dependencies.addAll(jarFiles(root.resolve("local/workspaces/b1.7.3/libraries")));
        }
        dependencies.addAll(selectOutputs(modules, outputs,
                descriptor.getProperty("cycle.compile.products")));
        Path output = build.resolve("scenario-classes");
        Files.createDirectories(output);
        List<String> command = new ArrayList<>(List.of(javaTool("javac"), "-encoding", "UTF-8",
                "--release", "8", "-Xlint:all,-options", "-Werror", "-classpath",
                dependencies.stream().map(Path::toString)
                        .collect(Collectors.joining(System.getProperty("path.separator"))),
                "-d", output.toString()));
        command.addAll(javaFiles(root.resolve(clientOnly ? "adapters/b173-client/src/main/java"
                : "adapters/b173-server/src/main/java")));
        command.addAll(javaFiles(source));
        execute(command, 240);
    }

    private static List<Path> selectOutputs(List<String> modules, List<Path> outputs,
            String declared) {
        require(modules.size() == outputs.size(), "module output closure drifted");
        if (declared == null) return outputs;
        List<Path> selected = new ArrayList<>();
        for (String product : java.util.Arrays.stream(declared.split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).toList()) {
            int index = modules.indexOf(product);
            require(index >= 0, "unknown cycle.compile.products module: " + product);
            selected.add(outputs.get(index));
        }
        return selected;
    }

    private void compileOracle(List<Path> outputs) throws Exception {
        Path milestone = root.resolve("smokes").resolve(id);
        Path source = milestone.resolve("oracle-src");
        if (!Files.isDirectory(source)) return;
        boolean client = descriptor.getProperty("client.jar.sha256") != null
                && descriptor.getProperty("server.jar.sha256") == null;
        String side = client ? "client" : "server";
        Properties artifact = StrictProperties.load(root.resolve("artifacts")
                .resolve("minecraft-b1.7.3-" + side + ".properties"));
        String expected = descriptor.getProperty(side + ".jar.sha256");
        require(expected != null && expected.equals(artifact.getProperty("expected.sha256")),
                "candidate oracle artifact descriptor drift");
        Path jar = root.resolve(artifact.getProperty("local.path")).normalize();
        SmokeSupport.verifyArtifact(jar, artifact);
        List<Path> dependencies = new ArrayList<>(outputs);
        dependencies.add(jar);
        if (client) dependencies.addAll(jarFiles(root.resolve("local/workspaces/b1.7.3/libraries")));
        Path output = build.resolve("oracle-classes");
        Files.createDirectories(output);
        List<String> command = new ArrayList<>(List.of(javaTool("javac"), "-encoding", "UTF-8",
                "--release", "8", "-Xlint:all,-options", "-Werror", "-classpath",
                dependencies.stream().map(Path::toString)
                        .collect(Collectors.joining(System.getProperty("path.separator"))),
                "-d", output.toString()));
        command.addAll(javaFiles(source));
        execute(command, 240);
        System.out.println("  compiled official " + side + " oracle source closure");
    }

    private List<String> values(String key) {
        return java.util.Arrays.stream(config.getProperty(key, "").split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).toList();
    }

    private static List<String> javaFiles(Path root) throws Exception {
        return SafeTreeDelete.paths(root).stream().filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .sorted().map(Path::toString).toList();
    }

    private static List<Path> jarFiles(Path root) throws Exception {
        return SafeTreeDelete.paths(root).stream().filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".jar")).sorted().toList();
    }

    private void recreate() throws Exception {
        Path allowed = root.resolve(".worldline/candidate-readiness");
        require(build.startsWith(allowed) && !build.equals(allowed), "unsafe readiness build path");
        SafeTreeDelete.delete(build);
        Files.createDirectories(build);
    }

    private void execute(List<String> command, int timeout) throws Exception {
        String output = compile(root, command, timeout);
        if (!output.isBlank()) System.out.print(output);
    }

    private static String compile(Path root, List<String> command, int timeout) throws Exception {
        return ProcessCapture.require(root, command, timeout);
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin",
                name + (windows ? ".exe" : "")).toString();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
