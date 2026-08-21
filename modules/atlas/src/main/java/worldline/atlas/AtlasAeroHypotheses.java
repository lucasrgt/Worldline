package worldline.atlas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Closed Aero and scheduler negatives. Statuses stay honest: rejected vs open. */
final class AtlasAeroHypotheses {
    private AtlasAeroHypotheses() {}

    static List<AtlasRecord> load() {
        List<AtlasRecord> records = new ArrayList<AtlasRecord>();
        records.add(item("aero-historical-spike", AtlasStatus.UNKNOWN, "historical-aero-hitch",
                "NON_CLAIM",
                "M12,M13,M18,M19,M71-M88",
                "atlas.subsystem.aero", "atlas.experiment.m12-aero-reproduction",
                "atlas.experiment.m18-save-attribution"));
        records.add(item("aero-dense-amplification", AtlasStatus.UNKNOWN, "dense-empty-amplification",
                "NOT_ESTABLISHED", "M13", "atlas.subsystem.aero",
                "atlas.experiment.m13-aero-differential"));
        records.add(item("aero-compile-governor", AtlasStatus.REJECTED, "compile-budget-governor",
                "REJECTED", "M13", "atlas.subsystem.aero",
                "atlas.experiment.m13-aero-differential"));
        records.add(item("aero-fixed-two-rebuild", AtlasStatus.REJECTED, "fixed-two-rebuild-scheduler",
                "REJECTED", "M15", "atlas.subsystem.aero",
                "atlas.experiment.m15-chunk-contract"));
        records.add(item("aero-adaptive-scheduler", AtlasStatus.REJECTED, "visible-first-adaptive-scheduler",
                "REJECTED", "M17", "atlas.subsystem.aero",
                "atlas.experiment.m17-scheduler-hardening"));
        records.add(item("aero-autosave-caused-spike", AtlasStatus.UNKNOWN, "autosave-as-spike-cause",
                "OPEN", "M18,M19", "atlas.subsystem.aero", "atlas.subsystem.saves",
                "atlas.experiment.m18-save-attribution", "atlas.experiment.m19-forced-autosave"));
        records.add(item("aero-m71-m88-causal-spike", AtlasStatus.UNKNOWN, "membership-ladder-causality",
                "NON_CLAIM", "M71-M88", "atlas.subsystem.aero",
                "atlas.experiment.m80-natural-membership-rebuild"));
        records.add(item("l1-save-mesh-offload", AtlasStatus.UNKNOWN, "snapshot-worker-commit",
                "CONTRACT_ONLY", "parallel-notes", "atlas.subsystem.saves",
                "atlas.subsystem.rendering"));
        return Collections.unmodifiableList(records);
    }

    private static AtlasRecord item(String token, String status, String subject, String control,
            String evidence, String... refs) {
        return AtlasHypothesisImport.item(token, status, subject, control, evidence, refs);
    }
}
