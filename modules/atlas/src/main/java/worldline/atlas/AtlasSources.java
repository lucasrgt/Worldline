package worldline.atlas;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import worldline.semantics.AdapterManifest;
import worldline.semantics.SemanticCatalog;

/** Assembles generated Atlas records from authoritative Worldline artifacts. */
final class AtlasSources {
    private AtlasSources() {}

    static AtlasStore load(Path root) {
        if (root == null) throw new NullPointerException("root");
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
            records.addAll(AtlasFieldImport.load());
            records.addAll(AtlasHypothesisImport.load());
            records.addAll(AtlasEcosystemImport.load());
            records.addAll(AtlasCoverage.units(records));
            return AtlasStore.of(records, catalog, root);
        } catch (IOException error) {
            throw new IllegalStateException(error);
        }
    }
}
