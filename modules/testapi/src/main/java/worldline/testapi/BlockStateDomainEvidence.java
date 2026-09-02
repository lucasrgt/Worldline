package worldline.testapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockState;

/** Canonical public evidence for one causally exercised block metadata domain. */
public final class BlockStateDomainEvidence {
    private final String scenario, subject, claim;
    private final ConformanceLayer layer;
    private final List<BlockState> domain;
    private final List<String> steps;
    private final ReloadBoundary boundary;

    public BlockStateDomainEvidence(BlockStateDomainScenario scenario, List<String> steps,
            ReloadBoundary boundary) {
        this.scenario = scenario.id();
        subject = scenario.subject();
        claim = scenario.claim().claimId();
        layer = scenario.claim().layer();
        domain = Collections.unmodifiableList(new ArrayList<BlockState>(scenario.domain()));
        this.steps = Collections.unmodifiableList(new ArrayList<String>(steps));
        this.boundary = Objects.requireNonNull(boundary, "boundary");
    }

    public String scenario() { return scenario; }
    public String subject() { return subject; }
    public String claim() { return claim; }
    public ConformanceLayer layer() { return layer; }
    public List<BlockState> domain() { return domain; }
    public List<String> steps() { return steps; }
    public ReloadBoundary boundary() { return boundary; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.block-state-domain-evidence.v1\n");
        value.append("scenario=").append(scenario).append('\n');
        value.append("subject=").append(subject).append('\n');
        value.append("claim.state-domain=").append(claim).append('|').append(layer).append('\n');
        value.append("domain=");
        for (int index = 0; index < domain.size(); index++) {
            if (index > 0) value.append(',');
            BlockState state = domain.get(index);
            value.append(state.legacyId()).append(':').append(state.metadata());
        }
        value.append('\n');
        for (int index = 0; index < steps.size(); index++) {
            value.append("step.").append(index + 1).append('=').append(steps.get(index)).append('\n');
        }
        return value.append("reload=").append(boundary).append('\n').toString();
    }
}
