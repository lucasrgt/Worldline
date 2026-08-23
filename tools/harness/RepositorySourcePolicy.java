import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        DataDrivenCycleCheck.execute(root); CompositeCycleCheck.execute(root);
        TelemetryPinCheck.execute(root); SchemaPinCheck.execute(root);
        SmokeDescriptorSchemaCheck.execute(root); TestKitReleasePinCheck.execute(root);
        FormattingPinCheck.execute(root);
        SharedHelperPinCheck.execute(root); UnicodePinCheck.execute(root); AdapterSplitPinCheck.execute(root);
        ProviderDiscoveryPinCheck.execute(root); GuiWorkbenchPinCheck.execute(root);
        new DocumentationCatalog(root).execute();
        BehaviorMapSchemaCheck.execute(root); RetryMigrationCheck.execute(root);
        FixedWaitMigrationCheck.execute(root); new SourceQualityCheck(root).execute();
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
