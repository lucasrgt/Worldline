package worldline.atlas;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import worldline.extension.ExtensionCapabilities;
import worldline.extension.WorldlineExtensionDiscovery;
import worldline.extension.WorldlineExtensionPlan;
import worldline.semantics.AdapterManifest;
import worldline.semantics.SemanticCatalog;

/** Assembles generated Atlas records from authoritative Worldline artifacts. */
final class AtlasSources {
    private AtlasSources() {}

    static AtlasStore load(Path root) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) loader = AtlasSources.class.getClassLoader();
        return load(root, root, loader, hostCapabilities());
    }

    static AtlasStore load(Path root, Path extensionRoot, ClassLoader loader,
            ExtensionCapabilities capabilities) {
        if (root == null) throw new NullPointerException("root");
        if (extensionRoot == null || loader == null || capabilities == null)
            throw new NullPointerException("extension discovery");
        try {
            SemanticCatalog catalog = SemanticCatalog.standard();
            List<AdapterManifest> adapters = AdapterManifest.loadAll(root.resolve("adapters"),
                    catalog);
            List<AtlasRecord> records = new ArrayList<AtlasRecord>();
            records.addAll(AtlasCoverage.subsystems());
            records.addAll(AtlasCatalogImport.load(catalog));
            records.addAll(AtlasBoundaryImport.load(catalog, adapters));
            records.addAll(AtlasInvariantImport.load());
            records.addAll(AtlasMilestoneImport.load(root));
            records.addAll(AtlasScenarioImport.load());
            records.addAll(AtlasBehaviorImport.load(root));
            records.addAll(AtlasFunctionalCensusImport.load(root));
            List<WorldlineExtensionPlan> plans = WorldlineExtensionDiscovery.discover(
                    extensionRoot, loader, capabilities);
            records.addAll(AtlasExtensionImport.load(plans));
            records.addAll(AtlasFieldImport.load());
            records.addAll(AtlasHypothesisImport.load());
            records.addAll(AtlasEcosystemImport.load());
            records.addAll(AtlasCoverage.units(records));
            return AtlasStore.of(records, catalog, root);
        } catch (IOException error) {
            throw new IllegalStateException(error);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("extension entrypoint could not be loaded", error);
        }
    }

    private static ExtensionCapabilities hostCapabilities() {
        return ExtensionCapabilities.of(ExtensionCapabilities.TESTKIT_V1,
                ExtensionCapabilities.ATLAS_V1,
                ExtensionCapabilities.CUSTOM_CONTRACT_V1);
    }
}
