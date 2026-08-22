package worldline.atlas;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import worldline.minimization.Scenario;

/** Indexes the canonical scenario envelope. SHA-256 remains the scenario identity. */
final class AtlasScenarioImport {
    private AtlasScenarioImport() {}

    static List<AtlasRecord> load() {
        Scenario probe = Scenario.of(Collections.singletonList("atlas-index"));
        String header = new String(probe.bytes(), StandardCharsets.UTF_8);
        int end = header.indexOf('\n');
        if (end <= 0) throw new IllegalStateException("scenario header");
        List<String> evidence = Collections.singletonList("worldline-scenario-1");
        List<String> refs = Collections.singletonList("atlas.experiment.m9-scenario-minimization");
        AtlasRecord record = AtlasRecord.of("atlas.scenario.worldline-scenario-1",
                AtlasKind.SCENARIO, AtlasStatus.STRONG, AtlasSchema.WORLDLINE, AtlasSchema.SCOPE,
                header.substring(0, end), "", 0, evidence, refs);
        return Collections.singletonList(record);
    }
}
