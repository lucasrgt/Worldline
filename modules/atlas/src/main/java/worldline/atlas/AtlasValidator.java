package worldline.atlas;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.invariants.BlockConservation;
import worldline.invariants.DurabilityConservation;
import worldline.invariants.EntitySpawn;
import worldline.invariants.HealthConservation;
import worldline.invariants.ItemConservation;
import worldline.invariants.TimeMonotonic;
import worldline.semantics.SemanticCatalog;

/** Fail-closed Atlas checks that do not launch Minecraft. */
public final class AtlasValidator {
    private AtlasValidator() {}

    public static void validate(List<AtlasRecord> records, SemanticCatalog catalog, Path root) {
        if (records == null || catalog == null) throw new NullPointerException("atlas");
        Set<String> ids = new HashSet<String>();
        for (AtlasRecord record : records) {
            if (!ids.add(record.id())) throw new IllegalArgumentException("duplicate id " + record.id());
        }
        for (AtlasRecord record : records) {
            checkVerified(record);
            checkKind(record, catalog, root);
            for (String ref : record.refs()) {
                if (!ids.contains(ref)) throw new IllegalArgumentException("broken ref " + ref);
            }
        }
    }

    private static void checkVerified(AtlasRecord record) {
        if (!AtlasStatus.VERIFIED.equals(record.status())) return;
        if (!qualifying(record)) {
            throw new IllegalArgumentException("VERIFIED without qualifying evidence " + record.id());
        }
    }

    private static boolean qualifying(AtlasRecord record) {
        for (String item : record.evidence()) {
            if ("invariant-engine".equals(item) || invariantName(item)) return true;
            if (item.startsWith("expected.signature=")) {
                String hash = item.substring("expected.signature=".length());
                if (AtlasSchema.shaToken(hash)) return true;
            }
            if (AtlasSchema.shaToken(item)) return true;
        }
        return false;
    }

    private static void checkKind(AtlasRecord record, SemanticCatalog catalog, Path root) {
        String token = AtlasKind.token(record.id());
        if (AtlasKind.ROLE.equals(record.kind())) catalog.role(token);
        if (AtlasKind.INVARIANT.equals(record.kind()) && !invariantName(token)) {
            throw new IllegalArgumentException("unknown invariant " + token);
        }
        if (AtlasKind.COVERAGE_UNIT.equals(record.kind()) && record.denominator() < 1) {
            throw new IllegalArgumentException("coverage unit denominator " + record.id());
        }
        if (AtlasKind.SUBSYSTEM.equals(record.kind()) && !AtlasSubsystems.known(token)) {
            throw new IllegalArgumentException("unknown subsystem " + token);
        }
        if (AtlasKind.EXPERIMENT.equals(record.kind()) && root != null) {
            checkExperiment(token, root);
        }
    }

    private static void checkExperiment(String token, Path root) {
        if (token.startsWith("symbols-map.")) {
            String folder = token.substring("symbols-map.".length());
            Path map = root.resolve("smokes").resolve(folder).resolve("symbols.map");
            if (!Files.isRegularFile(map)) throw new IllegalArgumentException("missing " + map);
            return;
        }
        Path smoke = root.resolve("smokes").resolve(token).resolve("smoke.properties");
        if (!Files.isRegularFile(smoke)) {
            throw new IllegalArgumentException("unknown experiment " + token);
        }
    }

    static boolean invariantName(String value) {
        return ItemConservation.NAME.equals(value) || EntitySpawn.NAME.equals(value)
                || BlockConservation.NAME.equals(value) || HealthConservation.NAME.equals(value)
                || DurabilityConservation.NAME.equals(value) || TimeMonotonic.NAME.equals(value);
    }
}
