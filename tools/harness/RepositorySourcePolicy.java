import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Runs source budgets, migration attestations, and maintained-source quality checks. */
final class RepositorySourcePolicy {
    private final Path root;
    private final RepositoryConfiguration config;
    private final List<String> modules;
    RepositorySourcePolicy(Path root, RepositoryConfiguration config, List<String> modules) {
        this.root = root; this.config = config; this.modules = modules;
    }
    void execute() throws Exception {
        budget("product", config.productionRoots(modules));
        budget("harness", VerificationRoots.read(root));
        budget("adapter", Collections.singletonList(root.resolve("adapters")));
        FilesWalkPolicy.execute(root);
        parallel(List.of(
            () -> {
            DataDrivenCycleCheck.execute(root); CompositeCycleCheck.execute(root);
            TelemetryPinCheck.execute(root); SchemaPinCheck.execute(root);
            SmokeDescriptorSchemaCheck.execute(root); MilestoneIdUniquenessCheck.execute(root);
            SmokeLane.validate(root);
            },
            () -> {
            NeighborTestKitPinCheck.execute(root);
            SupportFaceTestKitPinCheck.execute(root);
            BoundedDropTestKitPinCheck.execute(root);
            LifecycleClaimTestKitPinCheck.execute(root);
            TestKitReleasePinCheck.execute(root);
            },
            () -> {
            FormattingPinCheck.execute(root);
            SharedHelperPinCheck.execute(root); UnicodePinCheck.execute(root);
            AdapterSplitPinCheck.execute(root);
            },
            () -> {
            ProviderDiscoveryPinCheck.execute(root); GuiWorkbenchPinCheck.execute(root);
            BehaviorFamilyPinCheck.execute(root);
            BehaviorIdentityCheck.execute(root);
            },
            () -> {
            TrainPinCheck.execute(root);
            },
            () -> {
            new DocumentationCatalog(root).execute();
            new ReadmeStatus(root).check();
            BehaviorMapSchemaCheck.execute(root); RetryMigrationCheck.execute(root);
            FixedWaitMigrationCheck.execute(root); new SourceQualityCheck(root).execute();
            }
        ));
    }
    private static void parallel(List<VerifyReport.Checked> actions) throws Exception {
        try (var executor = Executors.newFixedThreadPool(actions.size())) {
            List<Future<Void>> futures = new ArrayList<>();
            for (VerifyReport.Checked action : actions)
                futures.add(executor.submit((Callable<Void>) () -> { action.run(); return null; }));
            for (Future<Void> future : futures) try { future.get(); }
            catch (ExecutionException error) {
                Throwable cause = error.getCause();
                if (cause instanceof Exception exception) throw exception;
                throw error;
            }
        }
    }
    private void budget(String name, List<Path> roots) throws Exception {
        List<String> command = new ArrayList<>(); command.add("tokei");
        roots.forEach(path -> command.add(path.toString()));
        command.addAll(Arrays.asList("--output", "json"));
        TokeiReport java = TokeiReport.required(ProcessCapture.require(root, command,
                ProcessCapture.environmentTimeout()), "Java");
        long maximum = Long.parseLong(config.required(name + ".max.file"));
        for (TokeiReport.FileReport file : java.files())
            require(file.code() <= maximum, name + " file budget exceeded: " + file.name()
                    + " has " + file.code() + "/" + maximum);
        System.out.println("  " + name + " lines: " + java.code() + " (max file " + maximum + ")");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
