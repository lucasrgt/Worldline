import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Repository verification engine entered through Gate. */
final class RepositoryVerify {
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path build = root.resolve(".worldline/build");
    private final RepositoryConfiguration config = new RepositoryConfiguration(root);
    private final boolean requireLocalArtifacts;
    private final boolean runSmoke, pinnedSmoke;
    private final VerifyReport report;

    private RepositoryVerify(boolean requireLocalArtifacts, boolean runSmoke) {
        this(requireLocalArtifacts, runSmoke,
                runSmoke ? "smoke" : requireLocalArtifacts ? "runtime" : "verify", false);
    }

    private RepositoryVerify(boolean requireLocalArtifacts, boolean runSmoke, String profile) {
        this(requireLocalArtifacts, runSmoke, profile, false);
    }

    private RepositoryVerify(boolean requireLocalArtifacts, boolean runSmoke, String profile,
            boolean pinnedSmoke) {
        this.requireLocalArtifacts = requireLocalArtifacts;
        this.runSmoke = runSmoke;
        this.pinnedSmoke = pinnedSmoke;
        this.report = new VerifyReport(root, profile);
    }

    public static void main(String[] arguments) {
        if (arguments.length == 2 && "--pooled-smoke".equals(arguments[0])) {
            try { PooledSmokeCheck.execute(arguments[1]); }
            catch (Exception error) {
                System.err.println("pooled smoke failed: " + error.getMessage()); System.exit(1); }
            return;
        }
        if (arguments.length == 2 && "--lane-differential".equals(arguments[0])) {
            try { LaneDifferential.execute(arguments[1]); }
            catch (Exception error) {
                System.err.println("lane differential failed: " + error.getMessage()); System.exit(1); }
            return;
        }
        if (Arrays.equals(arguments, new String[] {"--smoke-plan"})) {
            SmokePoolPlan.main(new String[0]); return;
        }
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
        if (Arrays.equals(arguments, new String[] {"--pin-smokes"})) { SmokePin.main(arguments); return; }
        if (Arrays.equals(arguments, new String[] {"--accept-legacy-smoke-baseline"}))
            { SmokeBaselinePin.main(arguments); return; }
        boolean runtime = Arrays.equals(arguments, new String[] {"--runtime"});
        boolean smoke = Arrays.equals(arguments, new String[] {"--smoke"});
        boolean pinnedSmoke = Arrays.equals(arguments, new String[] {"--pinned-smoke"});
        if (arguments.length > 0 && !runtime && !smoke && !pinnedSmoke) {
            System.err.println("usage: java tools/harness/Gate.java [--runtime|--smoke]");
            System.exit(2);
        }
        RepositoryVerify verify = pinnedSmoke
                ? new RepositoryVerify(false, true, "pinned-smoke", true)
                : new RepositoryVerify(runtime || smoke, smoke);
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
        VerificationStageCache stages = new VerificationStageCache(root);
        RepositoryStageInputs inputs = new RepositoryStageInputs(root);
        report.step("configuration", config::load);
        report.step("gate-self-test", () -> new HarnessSelfTestCache(root).execute());
        report.step("smoke-discovery", () -> run(Arrays.asList(
                "java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"), "SmokeDiscoveryCheck")));
        cachedProcess(stages, inputs.harness(), "smoke-cache-self-test", "SmokeReceiptCacheTest");
        cachedProcess(stages, inputs.harness(), "smoke-retry-self-test", "SmokeRetryTest");
        cachedProcess(stages, inputs.harness(), "test-cache-self-test", "TestReceiptCacheTest");
        cachedProcess(stages, inputs.harness(), "json-parser-self-test", "MiniJsonTest");
        cachedProcess(stages, inputs.harness(), "fair-lock-self-test", "FairFileLeaseTest");
        cachedProcess(stages, inputs.harness(), "pre-push-self-test", "PrePushCheckTest");
        cachedProcess(stages, inputs.harness(), "verify-summary-self-test", "VerifySummaryTest");
        report.step("harness-feature-self-tests", () -> stages.execute("harness-feature-self-tests",
                inputs.harnessFeatures(), HarnessFeatureSelfTest::execute));
        report.step("release", () -> stages.execute("release", inputs.release(), () -> run(Arrays.asList(
                "java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"), "ReleaseCheck"))));
        report.step("optimization", () -> stages.execute("optimization", inputs.optimization(), () -> {
            run(Arrays.asList("java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"),
                    "OptimizationCatalogCheckTest"));
            run(Arrays.asList("java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"),
                    "OptimizationCatalogCheck"));
        }));
        report.step("behavior-contracts", () -> stages.execute("behavior-contracts", inputs.behavior(),
                () -> run(Arrays.asList(
                "java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"),
                "BehaviorCompletenessCheck"))));
        report.step("adapter-kinds", () -> run(Arrays.asList(
                "java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"), "AdapterKindCheck")));
        if (requireLocalArtifacts) {
            run(Arrays.asList("java", "tools/toolchains/Bootstrap.java", "retromcp"));
        }
        report.step("runtime-inputs", this::verifyRuntimeInputs);
        List<String> modules = config.modules();
        report.step("source-policy", () -> stages.execute("source-policy", inputs.sourcePolicy(),
                () -> new RepositorySourcePolicy(root, config, modules).execute()));
        report.step("runtime-fabric-self-test", () -> new RuntimeFabricCheck(root).execute());
        recreateBuildDirectory();
        report.step("integration-tools", () -> stages.execute("integration-tools", inputs.integration(), () -> {
            IntegrationToolsCheck.execute(root, build); OrchestratorPolicyCheck.execute();
        }));
        report.step("smoke-runners", () -> new SmokeRunnerBuild(root, build).compile());
        List<Path> outputs = report.value("modules",
                () -> new ModuleBuild(root, build, config.values(), modules).compileAll());
        report.step("portable-adapters", () -> stages.execute("portable-adapters", inputs.adapters(), () -> {
            new PortableAdapterCheck(root, build, config.required("java.release")).execute();
            run(Arrays.asList("java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"),
                    "ForeignUiContractCheck"));
        }));
        report.step("milestone-surfaces", () -> stages.execute("milestone-surfaces", inputs.surfaces(), () -> {
            Path api = outputs.get(modules.indexOf("api"));
            Path testmodel = outputs.get(modules.indexOf("testmodel"));
            Path testapi = outputs.get(modules.indexOf("testapi"));
            for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
                MilestoneContract contract = new MilestoneContract(root, smoke.id, build);
                contract.validateAtlas(api); contract.validateTestKit(api, testmodel, testapi);
            }
            System.out.println("  milestone Atlas + TestKit surfaces agree with descriptors");
        }));
        report.step("tests", () -> new TestBuild(root, build, config.values(), modules, outputs).compileAndRun());
        if (runSmoke) report.step("smokes", this::runSmokeSuite);
    }

    private void cachedProcess(VerificationStageCache cache, List<Path> inputs,
            String stage, String type) throws Exception {
        report.step(stage, () -> cache.execute(stage, inputs, () -> run(Arrays.asList(
                "java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"), type))));
    }

    private void runSmokeSuite() throws Exception {
        List<String> command = new ArrayList<>(Arrays.asList(
                "java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"), "SmokeSuite"));
        if (pinnedSmoke) command.add("--pinned-only");
        run(command);
    }

    private void verifyRuntimeInputs() throws Exception {
        List<String> command = new ArrayList<>(Arrays.asList(
                "java", "-cp", System.getenv("WORLDLINE_HARNESS_CP"), "RuntimeCheck"));
        if (requireLocalArtifacts) {
            command.add("--required");
        }
        run(command);
    }


    private void recreateBuildDirectory() throws IOException {
        if (Files.exists(build)) {
            if (!build.startsWith(root) || build.equals(root)) {
                throw new IllegalStateException("refusing to delete unsafe build path: " + build);
            }
            SafeTreeDelete.delete(build);
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
        return ProcessCapture.require(root, command, ProcessCapture.environmentTimeout());
    }

}
