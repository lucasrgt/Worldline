package worldline.testkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePaintingSpawn;

/** Executes spawn, opposed orientation and support-loss as one painting capability. */
public final class PaintingLifecycleFixture {
    private static final RemoteItemStack PAINTING_ITEM = new RemoteItemStack(321, 1, 0);

    private PaintingLifecycleFixture() { }

    public static PaintingLifecycleEvidence execute(EntityConformancePlan plan, String subject,
            List<PaintingSpawnExpectation> expectations, PaintingLifecycleScenario scenario) {
        if (plan == null || expectations == null || scenario == null) {
            throw new NullPointerException("painting lifecycle");
        }
        if (expectations.size() != 2
                || expectations.get(0) == null || expectations.get(1) == null
                || expectations.get(0).direction() == expectations.get(1).direction()) {
            throw new IllegalArgumentException("painting lifecycle requires two distinct facings");
        }
        EntityConformanceCase spawnClaim = plan.caseFor(subject, "spawn-materialization");
        EntityConformanceCase orientationClaim = plan.caseFor(subject, "interaction-state");
        EntityConformanceCase supportClaim = plan.caseFor(subject, "environment-response");
        requireLayer(spawnClaim, ConformanceLayer.UNIVERSAL);
        requireLayer(orientationClaim, ConformanceLayer.SINGULAR);
        requireLayer(supportClaim, ConformanceLayer.SINGULAR);

        List<RemotePaintingSpawn> spawns = new ArrayList<RemotePaintingSpawn>();
        for (PaintingSpawnExpectation expectation : expectations) {
            RemotePaintingSpawn observed = scenario.materialize(expectation);
            if (!expectation.matches(observed)) {
                throw new IllegalStateException("Packet25 painting pose drifted");
            }
            spawns.add(observed);
        }
        if (spawns.get(0).entityId() == spawns.get(1).entityId()) {
            throw new IllegalStateException("opposed paintings reused one entity identity");
        }

        RemotePaintingSpawn supported = spawns.get(0);
        scenario.removeSupport(supported);
        if (scenario.awaitDestroy(supported.entityId()) != supported.entityId()) {
            throw new IllegalStateException("Packet29 painting identity drifted");
        }
        RemoteDroppedItem drop = scenario.awaitDrop(PAINTING_ITEM);
        if (drop == null || drop.entityId() <= 0 || !PAINTING_ITEM.equals(drop.item())) {
            throw new IllegalStateException("painting support-loss drop drifted");
        }
        return new PaintingLifecycleEvidence(Arrays.asList(spawnClaim, orientationClaim,
                supportClaim), expectations, spawns, drop);
    }

    private static void requireLayer(EntityConformanceCase claim, ConformanceLayer layer) {
        if (claim.layer() != layer) {
            throw new IllegalArgumentException(claim.template().id() + " must be " + layer);
        }
    }
}
