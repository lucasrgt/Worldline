package worldline.atlas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Evidence joins must preserve producer signatures and never synthesize a missing producer. */
final class AtlasSemanticEvidenceLinksTest {
    private AtlasSemanticEvidenceLinksTest() { }

    static void verify(AtlasStore store) {
        producer(store, "lab-cycle", "scheduling", "SCHEDULER_ADVANCE");
        producer(store, "controlled-client-tick", "lifecycle", "CLOSE");
        producer(store, "m17-profile", "profiling", "PROFILE_BUDGET");
        producer(store, "m69-peer-swing", "animation", "ANIMATION_PEER_OBSERVATION");
        AtlasRecord role = record("atlas.role.CLOSE", AtlasKind.ROLE,
                Collections.singletonList("exact"),
                Collections.singletonList("atlas.subsystem.lifecycle"));
        AtlasRecord unsigned = record("atlas.experiment.exact", AtlasKind.EXPERIMENT,
                Collections.singletonList("smoke.properties"), Collections.<String>emptyList());
        List<AtlasRecord> joined = AtlasSemanticEvidenceLinks.link(Arrays.asList(role, unsigned));
        require(AtlasCoverage.filled(joined, "lifecycle", "TESTABILITY"), "exact producer join");
        require(!AtlasCoverage.filled(joined, "lifecycle", "ORACLE"), "unsigned remains unsigned");
        require(!AtlasCoverage.filled(joined, "lifecycle", "REPRODUCIBILITY"), "no invented signature");
        require(joined.get(1).evidence().equals(unsigned.evidence())
                && joined.get(1).status().equals(unsigned.status()), "producer evidence preserved");
        require(AtlasSemanticEvidenceLinks.link(joined).get(1).canonical()
                .equals(joined.get(1).canonical()), "join idempotent");
        AtlasRecord similar = record("atlas.experiment.exact-extra", AtlasKind.EXPERIMENT,
                Collections.singletonList("expected.signature=" + repeat('a', 64)),
                Collections.<String>emptyList());
        List<AtlasRecord> missing = AtlasSemanticEvidenceLinks.link(Arrays.asList(role, similar));
        require(missing.size() == 2 && missing.get(1).refs().isEmpty(), "no inferred producer IDs");
        require(!AtlasCoverage.filled(missing, "lifecycle", "ORACLE"), "no evidence from similar ID");
        require(!AtlasCoverage.filled(missing, "lifecycle", "TESTABILITY"), "missing producer stays gap");
    }

    private static void producer(AtlasStore store, String id, String subsystem, String role) {
        AtlasRecord experiment = store.get("atlas.experiment." + id);
        require(experiment.refs().contains("atlas.role." + role)
                && experiment.refs().contains("atlas.subsystem." + subsystem), "producer lineage " + id);
        require(store.get("atlas.role." + role).evidence().contains(id), "explicit semantic evidence");
        List<AtlasRecord> isolated = new ArrayList<AtlasRecord>();
        isolated.add(experiment);
        require(AtlasCoverage.filled(isolated, subsystem, "ORACLE"), "signed producer " + id);
    }

    private static AtlasRecord record(String id, String kind, List<String> evidence, List<String> refs) {
        return AtlasRecord.of(id, kind, AtlasStatus.OBSERVATIONAL, AtlasSchema.WORLDLINE,
                AtlasSchema.SCOPE, "fixture", "", 0, evidence, refs);
    }

    private static String repeat(char value, int count) {
        char[] result = new char[count]; Arrays.fill(result, value); return new String(result);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
