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

/** Zero-dependency repository gate. Run with: java tools/harness/Verify.java */
public final class Verify {
    private static final Pattern CODE = Pattern.compile("\\\"code\\\"\\s*:\\s*(\\d+)");
    private static final Pattern REPORT = Pattern.compile(
            "\\{\\\"stats\\\":\\{(.*?)\\},\\\"name\\\":\\\"([^\\\"]+)\\\"", Pattern.DOTALL);

    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path build = root.resolve(".worldline/build");
    private final Properties config = new Properties();
    private final boolean requireLocalArtifacts;
    private final boolean runSmoke;

    private Verify(boolean requireLocalArtifacts, boolean runSmoke) {
        this.requireLocalArtifacts = requireLocalArtifacts;
        this.runSmoke = runSmoke;
    }

    public static void main(String[] arguments) {
        boolean runtime = Arrays.equals(arguments, new String[] {"--runtime"});
        boolean smoke = Arrays.equals(arguments, new String[] {"--smoke"});
        if (arguments.length > 0 && !runtime && !smoke) {
            System.err.println("usage: java tools/harness/Verify.java [--runtime|--smoke]");
            System.exit(2);
        }
        try {
            new Verify(runtime || smoke, smoke).execute();
        } catch (Exception error) {
            System.err.println("verify failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        System.out.println("Worldline repository verification");
        loadConfiguration();
        run(Arrays.asList("java", "tools/harness/ReleaseCheck.java"));
        run(Arrays.asList("java", "tools/harness/OptimizationCatalogCheckTest.java"));
        run(Arrays.asList("java", "tools/harness/OptimizationCatalogCheck.java"));
        if (runSmoke) {
            run(Arrays.asList("java", "tools/toolchains/Bootstrap.java", "retromcp"));
        }
        verifyRuntimeInputs();
        List<String> modules = values("modules");
        validateModuleOrder(modules);
        enforceBudget("product", productionRoots(modules));
        enforceBudget("harness", Collections.singletonList(root.resolve("tools")));
        enforceBudget("smoke", Collections.singletonList(root.resolve("smokes")));
        enforceBudget("adapter", Collections.singletonList(root.resolve("adapters")));
        recreateBuildDirectory();
        List<Path> outputs = compileModules(modules);
        Path testOutput = compileTests(modules, outputs);
        runTests(outputs, testOutput);
        if (runSmoke) {
            run(Arrays.asList("java", "tools/smoke/Run.java", "deterministic-world-tick"));
            run(Arrays.asList("java", "tools/smoke/ClientCycle.java", "controlled-client-tick"));
            run(Arrays.asList("java", "tools/smoke/ApiCycle.java", "m3-domain-api"));
            run(Arrays.asList("java", "tools/smoke/SnapshotCycle.java", "m4-durable-snapshot"));
            run(Arrays.asList("java", "tools/smoke/BundleCycle.java", "m5-reproduction-bundle"));
            run(Arrays.asList("java", "tools/smoke/TraceCycle.java", "m6-trace-explorer"));
            run(Arrays.asList("java", "tools/smoke/ModCycle.java", "m7-mod-loading"));
            run(Arrays.asList("java", "tools/smoke/VersionCycle.java", "m8-mod-version-diff"));
            run(Arrays.asList("java", "tools/smoke/MinimizationCycle.java", "m9-scenario-minimization"));
            run(Arrays.asList("java", "tools/smoke/NativeRenderCycle.java", "m10-native-render"));
            run(Arrays.asList("java", "tools/smoke/AeroAttributionCycle.java", "m11-aero-attribution"));
            run(Arrays.asList("java", "tools/smoke/AeroReproductionCycle.java", "m12-aero-reproduction"));
            run(Arrays.asList("java", "tools/smoke/AeroDifferentialCycle.java", "m13-aero-differential"));
            run(Arrays.asList("java", "tools/smoke/AeroChunkBacklogCycle.java", "m14-chunk-backlog"));
            run(Arrays.asList("java", "tools/smoke/AeroChunkContractCycle.java", "m15-chunk-contract"));
            run(Arrays.asList("java", "tools/smoke/AeroAdaptiveChunkCycle.java", "m16-adaptive-chunks"));
            run(Arrays.asList("java", "tools/smoke/AeroSchedulerHardeningCycle.java",
                    "m17-scheduler-hardening"));
            run(Arrays.asList("java", "tools/smoke/AeroSaveAttributionCycle.java",
                    "m18-save-attribution"));
            run(Arrays.asList("java", "tools/smoke/AeroForcedAutosaveCycle.java",
                    "m19-forced-autosave"));
            run(Arrays.asList("java", "tools/smoke/ServerBootstrapCycle.java",
                    "m20-server-bootstrap"));
            run(Arrays.asList("java", "tools/smoke/ServerControlCycle.java",
                    "m21-server-control"));
            run(Arrays.asList("java", "tools/smoke/MultiplayerWireCycle.java",
                    "m22-multiplayer-wire"));
            run(Arrays.asList("java", "tools/smoke/PlayerPersistenceCycle.java",
                    "m23-player-persistence"));
            run(Arrays.asList("java", "tools/smoke/PlayPoseCycle.java",
                    "m24-play-pose"));
            run(Arrays.asList("java", "tools/smoke/PlayerMovementCycle.java",
                    "m25-player-movement"));
            run(Arrays.asList("java", "tools/smoke/NativeMultiplayerCycle.java",
                    "m26-native-multiplayer"));
            run(Arrays.asList("java", "tools/smoke/MultiplayerChatCycle.java", "m27-multiplayer-chat"));
            run(Arrays.asList("java", "tools/smoke/RemoteChunkCycle.java", "m28-remote-chunk"));
            run(Arrays.asList("java", "tools/smoke/RemoteChunkSnapshotCycle.java", "m29-remote-chunk-snapshot"));
	            run(Arrays.asList("java", "tools/smoke/RemoteWorldCacheCycle.java", "m30-remote-world-cache")); run(Arrays.asList("java", "tools/smoke/AcceptedPersonalTransactionCycle.java", "m55-accepted-personal-transaction")); run(Arrays.asList("java", "tools/smoke/RejectedTransactionRecoveryCycle.java", "m56-rejected-transaction-recovery")); run(Arrays.asList("java", "tools/smoke/PersonalCraftingCycle.java", "m57-personal-crafting")); run(Arrays.asList("java", "tools/smoke/WindowLifecycleCycle.java", "m58-window-lifecycle")); run(Arrays.asList("java", "tools/smoke/ChestTransferCycle.java", "m59-chest-transfer")); run(Arrays.asList("java", "tools/smoke/FurnaceSmeltCycle.java", "m60-furnace-smelt")); run(Arrays.asList("java", "tools/smoke/FurnaceOutputCycle.java", "m61-furnace-output")); run(Arrays.asList("java", "tools/smoke/WorkbenchWindowCycle.java", "m62-workbench-window")); run(Arrays.asList("java", "tools/smoke/WorkbenchPrepareCycle.java", "m63-workbench-prepare")); run(Arrays.asList("java", "tools/smoke/WorkbenchOutputCycle.java", "m64-workbench-output")); run(Arrays.asList("java", "tools/smoke/PeerArmorCycle.java", "m65-peer-armor")); run(Arrays.asList("java", "tools/smoke/PlayerCombatCycle.java", "m66-player-combat")); run(Arrays.asList("java", "tools/smoke/ChestRetrievalCycle.java", "m67-chest-retrieval")); run(Arrays.asList("java", "tools/smoke/AeroMultiplayerLoginCycle.java", "m68-aero-multiplayer-login")); run(Arrays.asList("java", "tools/smoke/PeerSwingCycle.java", "m69-peer-swing")); run(Arrays.asList("java", "tools/smoke/AeroCombatWindowCycle.java", "m70-aero-combat-window")); run(Arrays.asList("java", "tools/smoke/PairedAeroWindowCycle.java", "m71-paired-aero-window")); run(Arrays.asList("java", "tools/smoke/AeroServerContentCycle.java", "m72-aero-server-content")); run(Arrays.asList("java", "tools/smoke/AeroPairedContentCycle.java", "m73-paired-aero-content")); run(Arrays.asList("java", "tools/smoke/AeroFrameCensusCycle.java", "m74-complete-aero-census")); run(Arrays.asList("java", "tools/smoke/AeroDensityLadderCycle.java", "m75-aero-density-ladder")); run(Arrays.asList("java", "tools/smoke/RendererDecompositionCycle.java", "m76-renderer-decomposition")); run(Arrays.asList("java", "tools/smoke/DirectStageTimingCycle.java", "m77-direct-stage-timing")); run(Arrays.asList("java", "tools/smoke/PagedStageTimingCycle.java", "m78-paged-stage-timing")); run(Arrays.asList("java", "tools/smoke/ColdPageRebuildCycle.java", "m79-cold-page-rebuild")); run(Arrays.asList("java", "tools/smoke/NaturalMembershipRebuildCycle.java", "m80-natural-membership-rebuild")); run(Arrays.asList("java", "tools/smoke/NaturalMultipageRebuildCycle.java", "m81-natural-multipage-rebuild")); run(Arrays.asList("java", "tools/smoke/NaturalWaveLadderCycle.java", "m82-natural-wave-ladder")); run(Arrays.asList("java", "tools/smoke/PageTopologyContrastCycle.java", "m83-page-topology-contrast")); run(Arrays.asList("java", "tools/smoke/FourPageTopologyContrastCycle.java", "m84-four-page-topology-contrast"));
            run(Arrays.asList("java", "tools/smoke/IncrementalWorldCycle.java", "m31-incremental-world")); run(Arrays.asList("java", "tools/smoke/RemoteTerrainRenderCycle.java", "m32-remote-terrain-render")); run(Arrays.asList("java", "tools/smoke/ChunkTraversalCycle.java", "m33-chunk-traversal")); run(Arrays.asList("java", "tools/smoke/PoseCorrectionCycle.java", "m34-pose-correction")); run(Arrays.asList("java", "tools/smoke/MovementOutcomeCycle.java", "m35-movement-outcome")); run(Arrays.asList("java", "tools/smoke/RouteRecoveryCycle.java", "m36-route-recovery")); run(Arrays.asList("java", "tools/smoke/RoutePolicyCycle.java", "m37-route-policy")); run(Arrays.asList("java", "tools/smoke/ExplicitFallbackCycle.java", "m38-explicit-fallback")); run(Arrays.asList("java", "tools/smoke/RouteObservationCycle.java", "m39-route-observation")); run(Arrays.asList("java", "tools/smoke/ObserverControlCycle.java", "m40-observer-control")); run(Arrays.asList("java", "tools/smoke/RouteTerminationCycle.java", "m41-route-termination")); run(Arrays.asList("java", "tools/smoke/RouteCorrelationCycle.java", "m42-route-correlation")); run(Arrays.asList("java", "tools/smoke/CorrelatedBatchCycle.java", "m43-correlated-batch")); run(Arrays.asList("java", "tools/smoke/BatchObservationCycle.java", "m44-batch-observation")); run(Arrays.asList("java", "tools/smoke/EventBatchStopCycle.java", "m45-event-batch-stop")); run(Arrays.asList("java", "tools/smoke/BatchTerminalCycle.java", "m46-batch-terminal")); run(Arrays.asList("java", "tools/smoke/BatchCountsCycle.java", "m47-batch-counts")); run(Arrays.asList("java", "tools/smoke/InventoryObservationCycle.java", "m48-inventory-observation")); run(Arrays.asList("java", "tools/smoke/HeldItemPeerCycle.java", "m49-held-item-peer")); run(Arrays.asList("java", "tools/smoke/DropHeldItemCycle.java", "m50-drop-held-item")); run(Arrays.asList("java", "tools/smoke/DroppedItemSpawnCycle.java", "m51-dropped-item-spawn")); run(Arrays.asList("java", "tools/smoke/ItemCollectionCycle.java", "m52-item-collection")); run(Arrays.asList("java", "tools/smoke/HeldBlockPlacementCycle.java", "m53-held-block-placement")); run(Arrays.asList("java", "tools/smoke/ChestWindowCycle.java", "m54-chest-window"));
            run(Arrays.asList("java", "tools/smoke/GuiCycle.java", "gui-tree"));
            run(Arrays.asList("java", "tools/smoke/LabCycle.java", "lab-cycle"));
        }
        System.out.println("verify passed");
    }

    private void loadConfiguration() throws IOException {
        Path path = root.resolve("harness.properties");
        try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            config.load(reader);
        }
    }

    private void verifyRuntimeInputs() throws Exception {
        List<String> command = new ArrayList<>(Arrays.asList(
                "java", "tools/harness/RuntimeCheck.java"));
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

    private List<Path> compileModules(List<String> modules) throws Exception {
        List<Path> outputs = new ArrayList<>();
        for (String module : modules) {
            Path output = build.resolve("classes").resolve(module);
            Files.createDirectories(output);
            List<Path> dependencyOutputs = values("module." + module + ".dependencies").stream()
                    .map(dependency -> build.resolve("classes").resolve(dependency))
                    .collect(Collectors.toList());
            compile(javaFiles(moduleRoot(module).resolve("src/main/java")), output, dependencyOutputs);
            outputs.add(output);
            System.out.println("  compiled module " + module);
        }
        return outputs;
    }

    private Path compileTests(List<String> modules, List<Path> outputs) throws Exception {
        List<Path> tests = new ArrayList<>();
        for (String module : modules) {
            Path root = moduleRoot(module).resolve("src/test/java");
            if (Files.isDirectory(root)) {
                tests.addAll(javaFiles(root));
            }
        }
        if (tests.isEmpty()) {
            throw new IllegalStateException("no tests found");
        }
        Path output = build.resolve("test-classes");
        Files.createDirectories(output);
        compile(tests, output, outputs);
        System.out.println("  compiled tests");
        return output;
    }

    private void compile(List<Path> sources, Path output, List<Path> classpath) throws Exception {
        if (sources.isEmpty()) {
            throw new IllegalStateException("no Java sources for " + relative(output));
        }
        List<String> command = new ArrayList<>(Arrays.asList(
                "javac", "-encoding", "UTF-8", "--release", required("java.release"),
                "-Xlint:all,-options", "-Werror", "-d", output.toString()));
        if (!classpath.isEmpty()) {
            command.add("-classpath");
            command.add(joinPaths(classpath));
        }
        sources.forEach(path -> command.add(path.toString()));
        run(command);
    }

    private void runTests(List<Path> outputs, Path testOutput) throws Exception {
        List<Path> classpath = new ArrayList<>(outputs);
        classpath.add(testOutput);
        for (String suite : values("test.suites")) {
            run(Arrays.asList("java", "-ea", "-classpath", joinPaths(classpath), suite));
        }
    }

    private List<Path> javaFiles(Path sourceRoot) throws IOException {
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            return paths.filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    private String joinPaths(List<Path> paths) {
        return paths.stream().map(Path::toString).collect(Collectors.joining(System.getProperty("path.separator")));
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
