import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Complete input sets for content-cached repository verification stages. */
final class RepositoryStageInputs {
    private final Path root;
    RepositoryStageInputs(Path root) { this.root = root; }

    List<Path> harness() { return paths("tools/harness"); }
    List<Path> smokeDiscovery() { return paths("smokes", "tools/harness/SmokeDiscovery.java",
            "tools/harness/SmokeDiscoveryCheck.java", "tools/harness/SmokeLane.java",
            "tools/harness/StrictProperties.java"); }
    List<Path> harnessCoreFeatures() { return paths("tools/harness", "README.md", "changelog"); }
    List<Path> harnessSmokeFeatures() { return paths("smokes", "quality",
            "tools/harness/SmokeScheduleHistory.java", "tools/harness/LaneDifferential.java",
            "tools/harness/LaneMatrixContract.java", "tools/harness/SmokeScheduleBaselineCheck.java",
            "tools/harness/SmokeStatementBudget.java", "tools/harness/SmokeProductRootTest.java"); }
    List<Path> harnessAeroFeatures() { return paths("quality/aero-scene-budgets.properties",
            "tools/harness/AeroSceneBudget.java", "tools/harness/AeroSceneBudgetTest.java",
            "tools/harness/AeroLogRow.java", "tools/harness/AeroLogRowTest.java"); }
    List<Path> release() {
        return paths("tools/harness/ReleaseCheck.java", "release", "README.md", "changelog",
                "docs/ROADMAP.md", "behavior/coverage.properties", "smokes/qualification.lock",
                "modules/api/src/main/java/worldline/api/WorldlineVersion.java",
                ".github/workflows/publish-testkit.yml");
    }
    List<Path> testKitArtifacts() {
        return paths("tools/testkit", "release/testkit.properties",
                "release/testkit-artifacts.lock", "harness.properties", "modules",
                "adapters/b173-server", "tools/harness/TestKitReleasePinCheck.java",
                "tools/harness/RepositoryVerify.java");
    }
    List<Path> optimization() {
        return paths("tools/harness/OptimizationCatalogCheck.java",
                "tools/harness/OptimizationCatalogCheckTest.java", "modules/optimization",
                "optimization", "docs");
    }
    List<Path> behavior() {
        return paths("tools/harness/BehaviorCompletenessCheck.java",
                "tools/harness/FunctionalCensusCheck.java", "behavior", "smokes", "modules", "adapters");
    }
    List<Path> sourcePolicy() {
        return paths("harness.properties", "quality", ".editorconfig", ".gitattributes",
                "README.md", "docs", "release", "changelog", "behavior/coverage.properties",
                "modules", "tools/harness", "tools/integration", "tools/testkit", "tools/smoke",
                "smokes", "adapters", ".github/workflows");
    }
    List<Path> integration() {
        return paths("tools/integration", "tools/harness/IntegrationToolsCheck.java",
                "tools/harness/OrchestratorPolicyCheck.java", "tools/harness/MiniJson.java",
                "tools/harness/ProcessCapture.java");
    }
    List<Path> adapters() {
        return paths("adapters", "modules/api", "modules/analysis", "modules/trace",
                "modules/testmodel", "modules/testapi",
                "tools/harness/PortableAdapterCheck.java", "tools/harness/ForeignUiContractCheck.java");
    }
    List<Path> atlasSynchronization() {
        return paths("smokes", "behavior", "adapters", "modules/api", "modules/invariants",
                "modules/semantics", "modules/trace", "modules/minimization", "modules/atlas",
                "tools/harness/MilestoneContract.java");
    }
    List<Path> tests() {
        return paths("modules", "harness.properties", "tools/harness");
    }
    List<Path> surfaces() {
        return paths("smokes", "behavior", "modules/api", "modules/testmodel", "modules/testapi",
                "tools/harness/MilestoneContract.java");
    }
    private List<Path> paths(String... values) {
        List<Path> result = new ArrayList<>();
        for (String value : values) result.add(root.resolve(value));
        return List.copyOf(result);
    }
}
