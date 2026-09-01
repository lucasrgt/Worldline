import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Static, non-runtime qualification for one independently authored milestone. */
final class CandidateCheck {
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path build;
    private final Properties config = new Properties();
    private final Properties descriptor = new Properties();
    private final String id;

    private CandidateCheck(String id) {
        this.id = id;
        this.build = root.resolve(".worldline/candidates").resolve(id);
    }

    static void execute(String id) throws Exception { new CandidateCheck(id).run(); }

    private void run() throws Exception {
        System.out.println("Worldline candidate verification: " + id);
        CandidateReadiness.requireIfSupervised(id);
        load(root.resolve("harness.properties"), config);
        SmokeDiscovery.Entry smoke = SmokeDiscovery.require(root, id);
        validateMilestone(smoke);
        enforceBudgets(smoke);
        recreate(build);
        List<String> modules = values("modules");
        List<Path> outputs = new ModuleBuild(root, build, config, modules).compileAll();
        compileRunner(smoke);
        if ("tools/smoke/ExternalRuntimeRun.java".equals(smoke.runner)) {
            ExternalRuntimeCoordinatorBuild.compile(root, id, descriptor,
                build.resolve("runner-classes"), build.resolve("coordinator-classes"));
        }
        compileScenario(outputs);
        new MilestoneContract(root, id, build).validate();
        new TestBuild(root, build, config, modules, outputs).compileAndRun(affectedModules(modules));
        System.out.println("candidate passed: " + id + " (static only; runtime qualification pending)");
    }

    private void validateMilestone(SmokeDiscovery.Entry smoke) throws Exception {
        Path directory = root.resolve("smokes").resolve(id);
        require(Files.isDirectory(directory), "missing milestone directory: smokes/" + id);
        require(Files.isRegularFile(directory.resolve("MAP.md")), "missing milestone MAP.md");
        Path descriptor = directory.resolve("smoke.properties");
        require(Files.isRegularFile(descriptor), "candidate requires smoke.properties");
        load(descriptor, this.descriptor);
        boolean tooling = "tooling".equals(this.descriptor.getProperty("candidate.kind"));
        if (!tooling) require(this.descriptor.getProperty("expected.signature") != null,
                "missing expected.signature");
        Path runner = root.resolve(smoke.runner).normalize();
        require(Files.isRegularFile(runner), "missing runner: " + smoke.runner);
        boolean dataDriven = "tools/smoke/DataDrivenCycle.java".equals(smoke.runner)
                || "tools/smoke/CompositeCycle.java".equals(smoke.runner)
                || "tools/smoke/NativeInventoryRenderCycle.java".equals(smoke.runner);
        boolean externalRuntime = "tools/smoke/ExternalRuntimeRun.java".equals(smoke.runner);
        require("tools/smoke/Run.java".equals(smoke.runner) || dataDriven || externalRuntime
                        || Files.readString(runner, StandardCharsets.UTF_8).contains("\"" + id + "\""),
                "runner does not declare candidate id");
        if (smoke.runner.equals("tools/smoke/DataDrivenCycle.java")) DataDrivenCyclePlan.load(root, id);
        if (smoke.runner.equals("tools/smoke/CompositeCycle.java")) CompositeCyclePlan.load(root, id);
        Path source = directory.resolve("src");
        boolean runtimeBuild = CandidateRuntimeBuild.owns(directory, this.descriptor);
        if (!tooling && !runtimeBuild) require(Files.isDirectory(source) && !javaFiles(source).isEmpty(),
                "candidate has no smoke sources");
        if (runtimeBuild) {
            CandidateRuntimeBuild.validate(root, directory, this.descriptor);
        }
        String number = milestoneNumber(id);
        if (number != null && !tooling) {
            if ("1".equals(this.descriptor.getProperty("narrative.schema"))) {
                require(this.descriptor.getProperty("qualification.docs", "").equals(
                        this.descriptor.getProperty("qualification.cycle", "")),
                        "generated narrative must combine claim and cycle");
                return;
            }
            boolean document = false, cycle = false;
            try (Stream<Path> files = Files.list(root.resolve("docs"))) {
                for (Path file : files.collect(Collectors.toList())) {
                    String name = file.getFileName().toString();
                    if (name.startsWith("M" + number + "_") && name.endsWith(".md")) document = true;
                    if (name.equals("M" + number + "_CYCLE.md")) cycle = true;
                }
            }
            require(document && cycle, "candidate requires milestone and cycle documentation for M" + number);
        }
    }

    private void enforceBudgets(SmokeDiscovery.Entry smoke) throws Exception {
        new SmokeStatementBudget(root).candidate(root.resolve(smoke.runner),
                root.resolve("smokes").resolve(id));
        checkTokei("adapter", List.of(root.resolve("adapters")), 150);
    }

    private void checkTokei(String name, List<Path> roots, int maximum) throws Exception {
        List<String> command = new ArrayList<>(List.of("tokei"));
        roots.forEach(path -> command.add(path.toString()));
        command.addAll(List.of("--output", "json"));
        String output = capture(command, root, 60);
        TokeiReport report = TokeiReport.find(output, "Java");
        if (report == null) return;
        for (TokeiReport.FileReport file : report.files()) {
            if (file.code() > maximum)
                throw new IllegalStateException(name + " file budget exceeded: "
                        + file.name() + " has " + file.code() + "/" + maximum);
        }
    }

    private void compileRunner(SmokeDiscovery.Entry smoke) throws Exception {
        Path output = build.resolve("runner-classes"); Files.createDirectories(output);
        run(List.of(javaTool("javac"), "-encoding", "UTF-8", "--release", "21",
                "-Xlint:all,-options", "-Werror", "-classpath",
                System.getenv("WORLDLINE_HARNESS_CP"), "-d", output.toString(),
                root.resolve(smoke.runner).toString()), root, 180);
        System.out.println("  compiled candidate runner");
    }

    private void compileScenario(List<Path> outputs) throws Exception {
        Path source = root.resolve("smokes").resolve(id).resolve("src");
        if (!Files.isDirectory(source)) {
            require(CandidateRuntimeBuild.owns(root.resolve("smokes").resolve(id), descriptor)
                    || "tooling".equals(descriptor.getProperty("candidate.kind")),
                    "missing candidate scenario sources");
            System.out.println("  candidate scenario is owned by its frozen runtime build"); return;
        }
        boolean clientOnly = "client".equals(descriptor.getProperty("side"))
                || "client".equals(descriptor.getProperty("atlas.artifact"))
                || descriptor.getProperty("client.jar.sha256") != null
                        && descriptor.getProperty("server.jar.sha256") == null;
        List<Path> dependencies = new ArrayList<>();
        boolean mappedServer = !clientOnly
                && Files.isRegularFile(root.resolve("smokes").resolve(id).resolve("symbols.map"))
                && descriptor.getProperty("worldline.main") != null;
        if (clientOnly) {
            Path adapterRoot = root.resolve("adapters/b173-client");
            Path headless = build.resolve("headless-classes");
            Files.createDirectories(headless);
            List<String> stubs = new ArrayList<>(List.of(javaTool("javac"), "-encoding", "UTF-8",
                    "--release", "8", "-Xlint:all,-options", "-Werror", "-d", headless.toString()));
            stubs.addAll(javaFiles(adapterRoot.resolve("headless-src")).stream()
                    .map(Path::toString).collect(Collectors.toList()));
            run(stubs, root, 180);
            Path mapped = root.resolve("local/workspaces/b1.7.3/minecraft/bin");
            require(Files.isRegularFile(mapped.resolve("net/minecraft/client/Minecraft.class")),
                    "client candidate requires the prepared mapped workspace");
            dependencies.add(headless); dependencies.add(mapped);
            dependencies.addAll(jarFiles(root.resolve("local/workspaces/b1.7.3/libraries")));
        } else if (mappedServer) {
            Path mapped = root.resolve("local/workspaces/b1.7.3/minecraft_server/bin");
            if (!Files.isRegularFile(mapped.resolve("net/minecraft/src/World.class"))) {
                System.out.println("  mapped server candidate compilation deferred until the exact "
                        + "milestone prepares its pinned workspace");
                return;
            }
            dependencies.add(mapped);
            dependencies.addAll(jarFiles(root.resolve("local/workspaces/b1.7.3/libraries")));
        }
        dependencies.addAll(outputs);
        Path output = build.resolve("scenario-classes"); Files.createDirectories(output);
        List<String> command = new ArrayList<>(List.of(javaTool("javac"), "-encoding", "UTF-8",
                "--release", "8", "-Xlint:all,-options", "-Werror", "-classpath",
                dependencies.stream().map(Path::toString)
                        .collect(Collectors.joining(System.getProperty("path.separator"))),
                "-d", output.toString()));
        String configured = descriptor.getProperty("cycle.inputs", "").trim();
        List<String> inputs = configured.isEmpty()
                ? List.of(clientOnly ? "adapters/b173-client/src/main/java"
                        : "adapters/b173-server/src/main/java")
                : Stream.of(configured.split(",")).map(String::trim).toList();
        for (String input : inputs) {
            require(input.matches("(?:adapters|modules)/[a-z0-9-]+/src/(?:main|testkit)/java"),
                    "unsafe candidate cycle input: " + input);
            command.addAll(javaFiles(root.resolve(input)).stream()
                    .map(Path::toString).collect(Collectors.toList()));
        }
        command.addAll(javaFiles(source).stream().map(Path::toString).collect(Collectors.toList()));
        run(command, root, 240);
        System.out.println("  compiled " + (clientOnly ? "client" : "server")
                + " adapter and candidate smoke");
    }

    private List<String> values(String key) {
        String raw = required(key).trim();
        if (raw.isEmpty()) return List.of();
        return Stream.of(raw.split(",")).map(String::trim).filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }

    private Set<String> affectedModules(List<String> modules) throws Exception {
        String base = System.getenv("WORLDLINE_CANDIDATE_BASE");
        if (base == null || base.isBlank()) base = inferredBase();
        if (base.isBlank()) return new HashSet<>(modules);
        Set<String> changed = new HashSet<>();
        addLines(changed, capture(List.of("git", "diff", "--name-only", base + "...HEAD"), root, 60));
        addLines(changed, capture(List.of("git", "diff", "--name-only"), root, 60));
        addLines(changed, capture(List.of("git", "diff", "--cached", "--name-only"), root, 60));
        addLines(changed, capture(List.of("git", "ls-files", "--others", "--exclude-standard"), root, 60));
        if (changed.stream().anyMatch(path -> path.equals("harness.properties")
                || path.startsWith("tools/harness/"))) return new HashSet<>(modules);
        Set<String> affected = new HashSet<>();
        for (String path : changed) if (path.startsWith("modules/")) {
            String name = path.substring(8).split("/", 2)[0];
            if (modules.contains(name)) affected.add(name);
        }
        boolean grew;
        do {
            grew = false;
            for (String module : modules) {
                if (affected.contains(module)) continue;
                Set<String> dependencies = new HashSet<>(values("module." + module + ".dependencies"));
                String testKey = "module." + module + ".test.dependencies";
                if (config.getProperty(testKey) != null) dependencies.addAll(values(testKey));
                if (!java.util.Collections.disjoint(dependencies, affected)) grew |= affected.add(module);
            }
        } while (grew);
        return affected;
    }

    private String inferredBase() throws Exception {
        try {
            return capture(List.of("git", "merge-base", "HEAD", "refs/remotes/origin/main"), root, 60).trim();
        } catch (IllegalStateException error) {
            return "";
        }
    }

    private static void addLines(Set<String> target, String text) {
        text.lines().map(String::trim).filter(value -> !value.isEmpty())
                .map(value -> value.replace('\\', '/')).forEach(target::add);
    }

    private String required(String key) {
        String value = config.getProperty(key);
        if (value == null) throw new IllegalStateException("missing harness property: " + key);
        return value;
    }

    private static List<Path> javaFiles(Path source) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            return paths.filter(path -> path.toString().endsWith(".java"))
                    .sorted().collect(Collectors.toList());
        }
    }

    private static List<Path> jarFiles(Path source) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            return paths.filter(path -> path.toString().endsWith(".jar"))
                    .sorted().collect(Collectors.toList());
        }
    }

    private static String milestoneNumber(String id) {
        if (!id.startsWith("m")) return null;
        int end = 1; while (end < id.length() && Character.isDigit(id.charAt(end))) end++;
        return end == 1 ? null : id.substring(1, end);
    }

    private static String capture(List<String> command, Path directory, int timeout) throws Exception {
        Path output = Files.createTempFile("worldline-candidate-", ".log");
        Process process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true)
                .redirectOutput(output.toFile()).start();
        try {
            if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                destroy(process); throw new IllegalStateException(command.get(0) + " timed out");
            }
            String text = Files.readString(output, StandardCharsets.UTF_8);
            if (process.exitValue() != 0)
                throw new IllegalStateException(command.get(0) + " exited " + process.exitValue() + "\n" + text);
            return text;
        } finally { Files.deleteIfExists(output); }
    }

    private static void run(List<String> command, Path directory, int timeout) throws Exception {
        String output = capture(command, directory, timeout);
        if (!output.isBlank()) System.out.print(output);
    }

    private static void destroy(Process process) {
        process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }

    private static void recreate(Path target) throws IOException {
        Path allowed = Path.of("").toAbsolutePath().normalize().resolve(".worldline/candidates");
        require(target.startsWith(allowed) && !target.equals(allowed), "unsafe candidate build path");
        SafeTreeDelete.delete(target);
        Files.createDirectories(target);
    }

    private static void load(Path path, Properties properties) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : "")).toString();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
