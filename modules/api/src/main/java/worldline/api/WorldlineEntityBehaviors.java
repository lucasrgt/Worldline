package worldline.api;

/** Qualified cross-registry entity boundaries. */
public final class WorldlineEntityBehaviors {
    public static final WorldlineBehavior PHYSICAL_ENVELOPE = WorldlineBehavior.define(
            "entity-physical-envelope", WorldlineFamily.ENTITY,
            "Canonical concrete EntityList dimensions, AABBs, and contact dispositions");

    private WorldlineEntityBehaviors() { }
}
