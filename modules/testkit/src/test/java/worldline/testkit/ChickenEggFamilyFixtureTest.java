package worldline.testkit;

import java.util.Collections;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobSpawn;
import worldline.api.RemoteObjectSpawn;

/** Proves chicken-family identity, optional laying honesty and thrown-egg boundaries. */
public final class ChickenEggFamilyFixtureTest {
    private ChickenEggFamilyFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("ChickenEggFamilyFixtureTest passed");
    }

    static void execute() {
        EntityConformancePlan plan = plan(ConformanceLayer.UNIVERSAL);
        ChickenEggFamilyEvidence withoutLay = ChickenEggFamilyFixture.execute(plan,
                () -> observation(10, null, 12, 11, 4.5D, 4.5D));
        ChickenEggFamilyEvidence withLay = ChickenEggFamilyFixture.execute(plan,
                () -> observation(100, drop(102, 344), 103, 101, 8.5D, -2.5D));
        require(withoutLay.claim().layer() == ConformanceLayer.UNIVERSAL,
                "chicken spawn route drifted");
        require(withoutLay.equals(withLay) && withoutLay.hashCode() == withLay.hashCode(),
                "optional laying or runtime identities leaked into evidence");
        reject(() -> ChickenEggFamilyFixture.execute(plan,
                () -> new ChickenEggFamilyObservation(mob(1, 92), null,
                        object(2, 62, 0, 0D, 0D), 3, 0D, 0D)));
        reject(() -> ChickenEggFamilyFixture.execute(plan,
                () -> new ChickenEggFamilyObservation(mob(1, 93), drop(2, 288),
                        object(3, 62, 0, 0D, 0D), 4, 0D, 0D)));
        reject(() -> ChickenEggFamilyFixture.execute(plan,
                () -> new ChickenEggFamilyObservation(mob(1, 93), null,
                        object(3, 61, 0, 0D, 0D), 4, 0D, 0D)));
        reject(() -> ChickenEggFamilyFixture.execute(plan,
                () -> new ChickenEggFamilyObservation(mob(1, 93), null,
                        object(3, 62, 5, 0D, 0D), 4, 0D, 0D)));
        reject(() -> ChickenEggFamilyFixture.execute(plan,
                () -> new ChickenEggFamilyObservation(mob(1, 93), null,
                        object(3, 62, 4, 8.03125D, 0D), 4, 0D, 0D)));
        reject(() -> ChickenEggFamilyFixture.execute(plan(ConformanceLayer.ARCHETYPE),
                () -> observation(1, null, 3, 2, 0D, 0D)));
    }

    private static ChickenEggFamilyObservation observation(int chicken, RemoteDroppedItem laid,
            int thrown, int actor, double x, double z) {
        return new ChickenEggFamilyObservation(mob(chicken, 93), laid,
                object(thrown, 62, actor, x, z), actor, x, z);
    }

    private static RemoteMobSpawn mob(int entity, int type) {
        return new RemoteMobSpawn(entity, type, 32, 2048, 32, 0, 0, 2, 0);
    }

    private static RemoteDroppedItem drop(int entity, int item) {
        return new RemoteDroppedItem(entity, new RemoteItemStack(item, 1, 0),
                1D, 64D, 1D, 0D, 0D, 0D);
    }

    private static RemoteObjectSpawn object(int entity, int type, int thrower,
            double x, double z) {
        return new RemoteObjectSpawn(entity, type, (int) Math.round(x * 32D), 2048,
                (int) Math.round(z * 32D), thrower, 0, 0, 0);
    }

    private static EntityConformancePlan plan(ConformanceLayer layer) {
        EntityConformanceProfile chicken = new EntityConformanceProfile(
                "b1.7.3:entity/093", Collections.singletonList("egg-layer"), false,
                Collections.singletonMap("spawn-materialization", layer));
        return new EntityConformancePlan(Collections.singletonList(chicken),
                Collections.singletonList(new EntityConformanceTemplate(
                        "spawn-materialization", ConformanceLayer.UNIVERSAL)));
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid chicken egg-family evidence accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
