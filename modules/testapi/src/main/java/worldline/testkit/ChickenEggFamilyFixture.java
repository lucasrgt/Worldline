package worldline.testkit;

import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobSpawn;
import worldline.api.RemoteObjectSpawn;

/** Executes the complete qualified type-93 chicken and player-thrown egg family scene. */
public final class ChickenEggFamilyFixture {
    private static final String SUBJECT = "b1.7.3:entity/093";
    private static final RemoteItemStack EGG = new RemoteItemStack(344, 1, 0);

    private ChickenEggFamilyFixture() { }

    public static ChickenEggFamilyEvidence execute(EntityConformancePlan plan,
            ChickenEggFamilyScenario scenario) {
        if (plan == null || scenario == null) throw new NullPointerException("chicken egg family");
        EntityConformanceCase claim = plan.caseFor(SUBJECT, "spawn-materialization");
        if (claim.layer() != ConformanceLayer.UNIVERSAL) {
            throw new IllegalArgumentException("chicken spawn must be universal");
        }
        ChickenEggFamilyObservation value = scenario.observe();
        if (value == null || value.actorEntityId() <= 0) {
            throw new IllegalStateException("chicken egg-family scene absent");
        }
        RemoteMobSpawn chicken = value.chicken();
        if (chicken == null || chicken.entityId() <= 0 || chicken.legacyType() != 93
                || chicken.entityId() == value.actorEntityId()) {
            throw new IllegalStateException("chicken Packet24 identity drifted");
        }
        RemoteDroppedItem laid = value.laidEgg();
        if (laid != null && (laid.entityId() <= 0 || !EGG.equals(laid.item()))) {
            throw new IllegalStateException("bounded optional laid egg drifted");
        }
        RemoteObjectSpawn thrown = value.thrownEgg();
        if (thrown == null || thrown.entityId() <= 0 || thrown.type() != 62
                || thrown.entityId() == chicken.entityId()
                || thrown.entityId() == value.actorEntityId()
                || (thrown.throwerId() != 0 && thrown.throwerId() != value.actorEntityId())
                || Math.abs(thrown.x() - value.platformX()) > 8D
                || Math.abs(thrown.z() - value.platformZ()) > 8D) {
            throw new IllegalStateException("thrown egg Packet23 boundary drifted");
        }
        return new ChickenEggFamilyEvidence(claim, value);
    }
}
