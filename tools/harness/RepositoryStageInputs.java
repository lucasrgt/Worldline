import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Complete input sets for content-cached repository verification stages. */
final class RepositoryStageInputs {
    private final Path root;
    RepositoryStageInputs(Path root) { this.root = root; }

    List<Path> harness() { return paths("tools/harness"); }
    List<Path> harnessFeatures() {
        return paths("tools/harness", "README.md", "changelog", "smokes", "quality");
    }
    List<Path> release() {
        return paths("tools/harness/ReleaseCheck.java", "release", "README.md", "changelog",
                "docs/ROADMAP.md", "modules/api/src/main/java/worldline/api/WorldlineVersion.java");
    }
    List<Path> optimization() {
        return paths("tools/harness/OptimizationCatalogCheck.java",
                "tools/harness/OptimizationCatalogCheckTest.java", "modules/optimization",
                "optimization", "docs");
    }
    List<Path> behavior() {
        return paths("tools/harness/BehaviorCompletenessCheck.java", "behavior", "smokes",
                "modules", "adapters");
    }
    List<Path> sourcePolicy() {
        return paths("harness.properties", "quality", ".editorconfig", ".gitattributes",
                "modules", "tools/harness", "tools/integration", "tools/smoke", "smokes", "adapters");
    }
    List<Path> integration() {
        return paths("tools/integration", "tools/harness/IntegrationToolsCheck.java",
                "tools/harness/OrchestratorPolicyCheck.java", "tools/harness/MiniJson.java",
                "tools/harness/ProcessCapture.java");
    }
    List<Path> adapters() {
        return paths("adapters", "modules/api", "modules/analysis", "modules/trace",
                "tools/harness/PortableAdapterCheck.java", "tools/harness/ForeignUiContractCheck.java");
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
