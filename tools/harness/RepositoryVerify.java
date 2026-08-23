import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Repository verification engine entered through Gate. */
final class RepositoryVerify {
    private static final Pattern CODE = Pattern.compile("\\\"code\\\"\\s*:\\s*(\\d+)");
    private static final Pattern REPORT = Pattern.compile(
            "\\{\\\"stats\\\":\\{(.*?)\\},\\\"name\\\":\\\"([^\\\"]+)\\\"", Pattern.DOTALL);

    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path build = root.resolve(".worldline/build");
    private final Properties config = new Properties();
    private final boolean requireLocalArtifacts;
    private final boolean runSmoke;
    private final VerifyReport report;

    private RepositoryVerify(boolean requireLocalArtifacts, boolean runSmoke) {
        this(requireLocalArtifacts, runSmoke,
                runSmoke ? "smoke" : requireLocalArtifacts ? "runtime" : "verify");
    }

    private RepositoryVerify(boolean requireLocalArtifacts, boolean runSmoke, String profile) {
        this.requireLocalArtifacts = requireLocalArtifacts;
        this.runSmoke = runSmoke;
        this.report = new VerifyReport(root, profile);
    }

    public static void main(String[] arguments) {
        if (Arrays.equals(arguments, new String[] {"--orchestrator"})) {
            orchestrator(); return;
        }
        if (arguments.length == 2 && "--milestone-static".equals(arguments[0])) {
            milestonePhase(arguments[1], false); return;
        }
        if (arguments.length == 2 && "--milestone-runtime".equals(arguments[0])) {
            milestonePhase(arguments[1], true); return;
        }
        if (arguments.length == 2 && "--candidate".equals(arguments[0])) {
            VerifyReport report = new VerifyReport(
                    Path.of("").toAbsolutePath().normalize(), "candidate:" + arguments[1]);
            try {
                report.step("candidate", () -> CandidateCheck.execute(arguments[1]));
                report.finish("passed", null);
            }
            catch (Exception error) {
                report.finish("failed", error);
                System.err.println("candidate verification failed: " + error.getMessage());
                System.exit(1);
            }
            return;
        }
        if (Arrays.equals(arguments, new String[] {"--pin-smokes"})) {
            SmokePin.main(arguments); return;
        }
        boolean runtime = Arrays.equals(arguments, new String[] {"--runtime"});
        boolean smoke = Arrays.equals(arguments, new String[] {"--smoke"});
        if (arguments.length > 0 && !runtime && !smoke) {
            System.err.println("usage: java tools/harness/Verify.java [--runtime|--smoke]");
            System.exit(2);
        }
        RepositoryVerify verify = new RepositoryVerify(runtime || smoke, smoke);
        try {
            verify.execute(); System.out.println("verify passed"); verify.report.finish("passed", null);
        } catch (Exception error) {
            verify.report.finish("failed", error);
            System.err.println("verify failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void orchestrator() {
        RepositoryVerify verify = new RepositoryVerify(false, false, "orchestrator");
        try {
            OrchestratorCheck.Context context = OrchestratorCheck.preflight(verify.root);
            verify.execute();
            verify.report.step("orchestrator-receipt", () -> OrchestratorCheck.qualify(verify.root, context));
            System.out.println("orchestrator gate passed"); verify.report.finish("passed", null);
        } catch (Exception error) {
            verify.report.finish("failed", error);
            System.err.println("orchestrator gate failed: " + error.getMessage()); System.exit(1);
        }
    }

    private static void milestonePhase(String id, boolean runtime) {
        String phase = runtime ? "runtime" : "static";
        VerifyReport report = new VerifyReport(Path.of("").toAbsolutePath().normalize(),
                "milestone:" + id + ":" + phase);
        try {
            if (System.getenv("WORLDLINE_GATE_ACTIVE") == null)
                throw new IllegalStateException("milestone phases are Gate-internal");
            report.step("milestone-" + phase, () -> {
                if (runtime) MilestoneCheck.runtimePhase(id);
                else MilestoneCheck.staticPhase(id);
            });
            report.finish("passed", null);
        } catch (Exception error) {
            report.finish("failed", error);
            System.err.println("milestone " + phase + " phase failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        System.out.println("Worldline repository verification");
        report.step("configuration", this::loadConfiguration);
        report.step("gate-self-test", () -> run(Arrays.asList(
                "java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"), "Gate", "--self-test")));
        report.step("smoke-discovery", () -> run(Arrays.asList(
                "java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"), "SmokeDiscoveryCheck")));
        report.step("smoke-cache-self-test", () -> run(Arrays.asList(
                "java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"), "SmokeReceiptCacheTest")));
        report.step("release", () -> run(Arrays.asList(
                "java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"), "ReleaseCheck")));
        report.step("optimization", () -> {
            run(Arrays.asList("java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"),
                    "OptimizationCatalogCheckTest"));
            run(Arrays.asList("java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"),
                    "OptimizationCatalogCheck"));
        });
        report.step("behavior-contracts", () -> run(Arrays.asList(
                "java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"),
                "BehaviorCompletenessCheck")));
        report.step("adapter-kinds", () -> run(Arrays.asList(
                "java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"), "AdapterKindCheck")));
        if (runSmoke) {
            run(Arrays.asList("java", "tools/toolchains/Bootstrap.java", "retromcp"));
        }
        report.step("runtime-inputs", this::verifyRuntimeInputs);
        List<String> modules = values("modules");
        validateModuleOrder(modules);
        report.step("source-policy", () -> {
            enforceBudget("product", productionRoots(modules));
            enforceBudget("harness", Collections.singletonList(root.resolve("tools")));
            enforceBudget("smoke", Collections.singletonList(root.resolve("smokes")));
            enforceBudget("adapter", Collections.singletonList(root.resolve("adapters")));
            new SourceQualityCheck(root).execute();
        });
        recreateBuildDirectory();
        report.step("integration-tools", () -> {
            IntegrationToolsCheck.execute(root, build); OrchestratorPolicyCheck.execute();
        });
        report.step("smoke-runners", () -> new SmokeRunnerBuild(root, build).compile());
        List<Path> outputs = report.value("modules",
                () -> new ModuleBuild(root, build, config, modules).compileAll());
        report.step("portable-adapters", () -> {
            new PortableAdapterCheck(root, build, required("java.release")).execute();
            run(Arrays.asList("java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"),
                    "ForeignUiContractCheck"));
        });
        report.step("milestone-surfaces", () -> {
            Path api = outputs.get(modules.indexOf("api"));
            Path testmodel = outputs.get(modules.indexOf("testmodel"));
            Path testapi = outputs.get(modules.indexOf("testapi"));
            for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
                MilestoneContract contract = new MilestoneContract(root, smoke.id, build);
                contract.validateAtlas(api); contract.validateTestKit(api, testmodel, testapi);
            }
            System.out.println("  milestone Atlas + TestKit surfaces agree with descriptors");
        });
        report.step("tests", () -> new TestBuild(root, build, config, modules, outputs).compileAndRun());
        if (runSmoke) {
            report.step("smokes", this::runSmokeSuite);
        }
    }

    private void runSmokeSuite() throws Exception {
        run(Arrays.asList("java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"), "SmokeSuite"));
    }

    private void loadConfiguration() throws IOException {
        Path path = root.resolve("harness.properties");
        try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            config.load(reader);
        }
    }

    private void verifyRuntimeInputs() throws Exception {
        List<String> command = new ArrayList<>(Arrays.asList(
                "java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"), "RuntimeCheck"));
        if (requireLocalArtifacts) {
            command.add("--required");
        }
        run(command);
    }

    private List<String> values(String key) {
        String raw = required(key).trim();
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }

    private String required(String key) {
        String value = config.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("missing harness property: " + key);
        }
        return value;
    }

    private void validateModuleOrder(List<String> modules) {
        if (modules.isEmpty()) {
            throw new IllegalStateException("at least one module is required");
        }
        List<String> seen = new ArrayList<>();
        for (String module : modules) {
            Path main = moduleRoot(module).resolve("src/main/java");
            if (!Files.isDirectory(main)) {
                throw new IllegalStateException("missing production source root: " + relative(main));
            }
            for (String dependency : values("module." + module + ".dependencies")) {
                if (!seen.contains(dependency)) {
                    throw new IllegalStateException(
                            "module " + module + " depends on undeclared, unknown, or later module " + dependency);
                }
            }
            seen.add(module);
        }
    }

    private List<Path> productionRoots(List<String> modules) {
        return modules.stream()
                .map(module -> moduleRoot(module).resolve("src/main/java"))
                .collect(Collectors.toList());
    }

    private Path moduleRoot(String module) {
        return root.resolve("modules").resolve(module);
    }

    private void enforceBudget(String name, List<Path> roots) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("tokei");
        roots.forEach(path -> command.add(path.toString()));
        command.addAll(Arrays.asList("--output", "json"));
        String json = capture(command);
        String java = languageSection(json);
        int reports = java.indexOf("\"reports\"");
        long total = codeLines(reports < 0 ? java : java.substring(0, reports));
        long maxFile = Long.parseLong(required(name + ".max.file"));
        Matcher files = REPORT.matcher(java);
        while (files.find()) {
            long lines = codeLines(files.group(1));
            if (lines > maxFile) {
                throw new IllegalStateException(
                        name + " file budget exceeded: " + files.group(2) + " has " + lines + "/" + maxFile);
            }
        }
        System.out.println("  " + name + " lines: " + total + " (max file " + maxFile + ")");
    }

    private String languageSection(String json) {
        int start = json.indexOf("\"Java\":{");
        if (start < 0) {
            throw new IllegalStateException("tokei JSON did not contain Java");
        }
        return json.substring(start + 8);
    }

    private long codeLines(String json) {
        Matcher matcher = CODE.matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("tokei JSON did not contain a code count");
        }
        return Long.parseLong(matcher.group(1));
    }

    private void recreateBuildDirectory() throws IOException {
        if (Files.exists(build)) {
            if (!build.startsWith(root) || build.equals(root)) {
                throw new IllegalStateException("refusing to delete unsafe build path: " + build);
            }
            try (Stream<Path> paths = Files.walk(build)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                    Files.delete(path);
                }
            }
        }
        Files.createDirectories(build);
    }

    private void run(List<String> command) throws Exception {
        String output = capture(command);
        if (!output.trim().isEmpty()) {
            System.out.print(output);
        }
    }

    private String capture(List<String> command) throws Exception {
        Process process;
        try {
            process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        } catch (IOException error) {
            throw new IllegalStateException("could not start " + command.get(0) + ": " + error.getMessage(), error);
        }
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exit = process.waitFor();
        if (exit != 0) {
            throw new IllegalStateException(command.get(0) + " exited " + exit + "\n" + output);
        }
        return output;
    }

    private String relative(Path path) {
        return root.relativize(path).toString();
    }
}
