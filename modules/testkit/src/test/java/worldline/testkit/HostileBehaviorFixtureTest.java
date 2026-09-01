package worldline.testkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import worldline.api.BlockPosition;
import worldline.api.RemoteExplosion;
import worldline.api.RemoteMobSpawn;
import worldline.api.RemoteObjectSpawn;

/** Proves all four qualified hostile behavior rows and their failure boundaries. */
public final class HostileBehaviorFixtureTest {
    private HostileBehaviorFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("HostileBehaviorFixtureTest passed");
    }

    static void execute() {
        HostileBehaviorEvidence first = HostileBehaviorFixture.execute(plan(),
                () -> observation(10, 20, 30, 40, 50, 60, 70));
        HostileBehaviorEvidence second = HostileBehaviorFixture.execute(plan(),
                () -> observation(100, 200, 300, 400, 500, 600, 700));
        require(first.claims().size() == 4 && first.equals(second)
                && first.hashCode() == second.hashCode(),
                "hostile behavior identities leaked into evidence");
        reject(() -> HostileBehaviorFixture.execute(plan(),
                () -> observation(10, 20, 30, 40, 50, 60, 30)));
        reject(() -> HostileBehaviorFixture.execute(plan(),
                () -> wrongArrow(10, 20, 30, 40, 50, 60, 70)));
        reject(() -> HostileBehaviorFixture.execute(plan(),
                () -> wrongSpider(10, 20, 30, 40, 50, 60, 70)));
        reject(() -> HostileBehaviorFixture.execute(plan(),
                () -> wrongExplosion(10, 20, 30, 40, 50, 60, 70)));
        reject(() -> HostileBehaviorFixture.execute(plan(ConformanceLayer.ARCHETYPE),
                () -> observation(10, 20, 30, 40, 50, 60, 70)));
    }

    private static HostileBehaviorObservation observation(int actor, int zombie,
            int skeleton, int spider, int creeper, int firstArrow, int secondArrow) {
        BlockPosition dirt = new BlockPosition(4, 72, 4);
        BlockPosition wool = new BlockPosition(5, 72, 4);
        return new HostileBehaviorObservation(actor, 14000, mob(zombie, 54), mob(skeleton, 51),
                arrow(firstArrow, skeleton), arrow(secondArrow, skeleton), true, mob(spider, 52),
                true, true, 4, 5, mob(creeper, 50), true,
                new RemoteExplosion(4.5D, 72D, 4.5D, 3F, Arrays.asList(dirt, wool)),
                dirt, wool, true, true);
    }

    private static HostileBehaviorObservation wrongArrow(int actor, int zombie, int skeleton,
            int spider, int creeper, int firstArrow, int secondArrow) {
        HostileBehaviorObservation valid = observation(actor, zombie, skeleton, spider,
                creeper, firstArrow, secondArrow);
        return copy(valid, arrow(firstArrow, actor), valid.cobblestonePositiveY(),
                valid.creeperExplosion());
    }

    private static HostileBehaviorObservation wrongSpider(int actor, int zombie, int skeleton,
            int spider, int creeper, int firstArrow, int secondArrow) {
        HostileBehaviorObservation valid = observation(actor, zombie, skeleton, spider,
                creeper, firstArrow, secondArrow);
        return copy(valid, valid.firstArrow(), false, valid.creeperExplosion());
    }

    private static HostileBehaviorObservation wrongExplosion(int actor, int zombie, int skeleton,
            int spider, int creeper, int firstArrow, int secondArrow) {
        HostileBehaviorObservation valid = observation(actor, zombie, skeleton, spider,
                creeper, firstArrow, secondArrow);
        return copy(valid, valid.firstArrow(), true, new RemoteExplosion(0D, 0D, 0D, 4F,
                valid.creeperExplosion().destroyed()));
    }

    private static HostileBehaviorObservation copy(HostileBehaviorObservation value,
            RemoteObjectSpawn first, boolean cobble, RemoteExplosion explosion) {
        return new HostileBehaviorObservation(value.actorEntityId(), value.nightTime(),
                value.zombie(), value.skeleton(), first, value.secondArrow(),
                value.diamondArmorObserved(), value.spider(), cobble,
                value.planksPositiveY(), value.cobblestoneBlockId(), value.planksBlockId(),
                value.creeper(), value.proximityFuseObserved(), explosion, value.dirtCell(),
                value.woolCell(), value.dirtPersistedAir(), value.woolPersistedAir());
    }

    private static RemoteMobSpawn mob(int id, int type) {
        return new RemoteMobSpawn(id, type, 32, 2048, 32, 0, 0, 1, 0);
    }

    private static RemoteObjectSpawn arrow(int id, int skeleton) {
        return new RemoteObjectSpawn(id, 60, 32, 2048, 32, skeleton, 1, 1, 1);
    }

    private static EntityConformancePlan plan() { return plan(ConformanceLayer.SINGULAR); }
    private static EntityConformancePlan plan(ConformanceLayer creeperFuse) {
        List<EntityConformanceProfile> profiles = new ArrayList<EntityConformanceProfile>();
        profiles.add(profile("b1.7.3:entity/054", false, "spawn-materialization",
                ConformanceLayer.UNIVERSAL));
        profiles.add(profile("b1.7.3:entity/051", false, "interaction-state",
                ConformanceLayer.ARCHETYPE));
        profiles.add(profile("b1.7.3:entity/052", false, "movement-policy",
                ConformanceLayer.ARCHETYPE));
        profiles.add(profile("b1.7.3:entity/050", true, "tick-lifecycle", creeperFuse));
        return new EntityConformancePlan(profiles, Arrays.asList(
                new EntityConformanceTemplate("spawn-materialization", ConformanceLayer.UNIVERSAL),
                new EntityConformanceTemplate("interaction-state", ConformanceLayer.ARCHETYPE),
                new EntityConformanceTemplate("movement-policy", ConformanceLayer.ARCHETYPE),
                new EntityConformanceTemplate("tick-lifecycle", ConformanceLayer.ARCHETYPE)));
    }

    private static EntityConformanceProfile profile(String subject, boolean singular,
            String template, ConformanceLayer layer) {
        Map<String, ConformanceLayer> override = new LinkedHashMap<String, ConformanceLayer>();
        override.put(template, layer);
        return new EntityConformanceProfile(subject, Collections.singletonList("hostile"),
                singular, override);
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid hostile behavior matrix accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
