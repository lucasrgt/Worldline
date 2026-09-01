package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;

/** Canonical public evidence for one gameplay-authored light transport profile. */
public final class BlockLightEvidence {
    private final String scenario, subject, claim;
    private final ConformanceLayer layer;
    private final List<String> controls, treatments, placements;
    private final ReloadBoundary boundary;

    BlockLightEvidence(BlockLightScenario scenario, List<String> controls,
            List<String> treatments, List<String> placements, ReloadBoundary boundary) {
        this.scenario = scenario.id(); this.subject = scenario.subject();
        this.claim = scenario.claim().claimId(); this.layer = scenario.claim().layer();
        this.controls = immutable(controls); this.treatments = immutable(treatments);
        this.placements = immutable(placements);
        this.boundary = Objects.requireNonNull(boundary, "boundary");
    }

    public String scenario() { return scenario; }
    public String subject() { return subject; }
    public String claim() { return claim; }
    public ConformanceLayer layer() { return layer; }
    public List<String> controls() { return controls; }
    public List<String> treatments() { return treatments; }
    public List<String> placements() { return placements; }
    public ReloadBoundary boundary() { return boundary; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.block-light-evidence.v1\n");
        value.append("scenario=").append(scenario).append('\n');
        value.append("subject=").append(subject).append('\n');
        value.append("claim.light-behavior=").append(claim).append('|').append(layer).append('\n');
        append(value, "placement", placements); append(value, "control", controls);
        append(value, "treatment", treatments);
        return value.append("reload=").append(boundary).append('\n').toString();
    }

    private static List<String> immutable(List<String> values) {
        return Collections.unmodifiableList(new ArrayList<String>(values));
    }
    private static void append(StringBuilder target, String key, List<String> values) {
        for (int index = 0; index < values.size(); index++) target.append(key).append('.')
                .append(index + 1).append('=').append(values.get(index)).append('\n');
    }
}
