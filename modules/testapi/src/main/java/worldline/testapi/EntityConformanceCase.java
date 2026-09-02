package worldline.testapi;

import java.util.Objects;

/** One generated persistent-entity claim routed to its executable proof layer. */
public final class EntityConformanceCase {
    private final String claimId;
    private final EntityConformanceProfile profile;
    private final EntityConformanceTemplate template;
    private final ConformanceLayer layer;

    public EntityConformanceCase(EntityConformanceProfile profile, EntityConformanceTemplate template,
            ConformanceLayer layer) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.template = Objects.requireNonNull(template, "template");
        this.layer = Objects.requireNonNull(layer, "layer");
        this.claimId = profile.subject() + "#" + template.id();
    }

    public String claimId() {
        return claimId;
    }

    public EntityConformanceProfile profile() {
        return profile;
    }

    public EntityConformanceTemplate template() {
        return template;
    }

    public ConformanceLayer layer() {
        return layer;
    }
}
