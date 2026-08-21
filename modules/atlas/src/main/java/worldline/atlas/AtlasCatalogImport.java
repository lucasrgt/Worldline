package worldline.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;
import worldline.semantics.SemanticCatalog;

/** Generates one Atlas role record per closed catalog mapping. */
final class AtlasCatalogImport {
    private AtlasCatalogImport() {}

    static List<AtlasRecord> load(SemanticCatalog catalog) {
        List<AtlasRecord> records = new ArrayList<AtlasRecord>();
        for (SemanticMapping mapping : catalog.mappings()) {
            String status = mapping.known() ? AtlasStatus.STRONG : AtlasStatus.EXPERIMENTAL;
            String artifact = mapping.owner().startsWith("worldline/")
                    ? AtlasSchema.WORLDLINE : AtlasSchema.CLIENT;
            List<String> refs = new ArrayList<String>();
            String subsystem = AtlasSubsystems.forCategory(mapping.category());
            if (!subsystem.isEmpty()) refs.add("atlas.subsystem." + subsystem);
            records.add(AtlasRecord.of("atlas.role." + mapping.role(), AtlasKind.ROLE, status,
                    artifact, AtlasSchema.SCOPE, mapping.owner() + "." + mapping.name(), "", 0,
                    mapping.evidence(), refs));
        }
        return Collections.unmodifiableList(records);
    }
}
