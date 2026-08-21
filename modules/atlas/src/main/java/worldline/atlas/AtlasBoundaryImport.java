package worldline.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.SemanticMapping;
import worldline.semantics.AdapterManifest;
import worldline.semantics.SemanticCatalog;
import worldline.semantics.SemanticGraph;

/**
 * One boundary per semantic-graph token. Adapter sites classify control;
 * LWJGL-only tokens stay native/non-deterministic.
 */
final class AtlasBoundaryImport {
    private AtlasBoundaryImport() {}

    static List<AtlasRecord> load(SemanticCatalog catalog, List<AdapterManifest> adapters) {
        SemanticGraph graph = SemanticGraph.of(catalog);
        Set<String> intercepted = intercepted(catalog, adapters);
        List<AtlasRecord> records = new ArrayList<AtlasRecord>();
        for (String token : graph.tokens()) {
            records.add(boundary(catalog, token, intercepted));
        }
        return Collections.unmodifiableList(records);
    }

    private static AtlasRecord boundary(SemanticCatalog catalog, String token,
            Set<String> intercepted) {
        boolean worldline = false, nativeOwner = false;
        for (SemanticMapping mapping : catalog.mappings()) {
            if (!uses(mapping, token)) continue;
            if (mapping.owner().startsWith("worldline/")) worldline = true;
            if (mapping.owner().startsWith("org/lwjgl/")) nativeOwner = true;
        }
        String control;
        String status;
        if (nativeOwner && !worldline) {
            control = "NATIVE";
            status = AtlasStatus.NATIVE_NONDETERMINISTIC;
        } else if (virtualized(token) && (worldline || intercepted.contains(token))) {
            control = "VIRTUALIZED";
            status = AtlasStatus.STRONG;
        } else if (intercepted.contains(token)) {
            control = "INTERCEPTED";
            status = AtlasStatus.STRONG;
        } else if (worldline) {
            control = "CONTROLLED";
            status = AtlasStatus.STRONG;
        } else {
            control = "OBSERVED";
            status = AtlasStatus.OBSERVATIONAL;
        }
        List<String> refs = new ArrayList<String>();
        String subsystem = AtlasSubsystems.forBoundary(token);
        if (!subsystem.isEmpty()) refs.add("atlas.subsystem." + subsystem);
        List<String> evidence = new ArrayList<String>();
        evidence.add("semantic-graph");
        if (intercepted.contains(token)) evidence.add("adapter-manifest");
        return AtlasRecord.of("atlas.boundary." + token, AtlasKind.BOUNDARY, status,
                AtlasSchema.CLIENT, AtlasSchema.SCOPE, token, control, 0, evidence, refs);
    }

    private static Set<String> intercepted(SemanticCatalog catalog, List<AdapterManifest> adapters) {
        Set<String> tokens = new HashSet<String>();
        for (AdapterManifest manifest : adapters) {
            for (AdapterManifest.Site site : manifest.sites()) {
                SemanticMapping mapping = catalog.role(site.role());
                tokens.addAll(mapping.reads());
                tokens.addAll(mapping.writes());
                tokens.addAll(mapping.deps());
            }
        }
        return tokens;
    }

    private static boolean uses(SemanticMapping mapping, String token) {
        return mapping.reads().contains(token) || mapping.writes().contains(token)
                || mapping.deps().contains(token);
    }

    private static boolean virtualized(String token) {
        return "CLOCK".equals(token) || "RNG".equals(token) || "INPUT".equals(token)
                || "FILESYSTEM".equals(token) || "NETWORK".equals(token)
                || "SCHEDULER".equals(token);
    }
}
