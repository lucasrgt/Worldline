package worldline.testapi;

import java.util.Objects;

/** One public generated execution case; its identity is a claim, never a milestone number. */
public final class BlockConformanceCase {
    private final String claimId;
    private final BlockConformanceProfile profile;
    private final BlockConformanceTemplate template;
    private final ConformanceLayer layer;

    public BlockConformanceCase(BlockConformanceProfile profile, BlockConformanceTemplate template,
            ConformanceLayer layer) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.template = Objects.requireNonNull(template, "template");
        this.layer = Objects.requireNonNull(layer, "layer");
        this.claimId = profile.subject() + "#" + template.id();
    }

    public String claimId() {
        return claimId;
    }

    public BlockConformanceProfile profile() {
        return profile;
    }

    public BlockConformanceTemplate template() {
        return template;
    }

    public ConformanceLayer layer() {
        return layer;
    }
}
