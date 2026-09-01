package worldline.b173server;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.test.TestRuntimeProviders;
import worldline.test.TestRuntimeRequest;
import worldline.testkit.BlockCollisionExpectation;
import worldline.testkit.BlockCollisionPlan;
import worldline.testkit.BlockCollisionScenario;
import worldline.testkit.ConformanceLayer;

/** Static checks for the public collision provider and official scenario rows. */
public final class B173ServerCollisionProviderTest {
    private B173ServerCollisionProviderTest() { }

    public static void main(String[] arguments) throws Exception {
        require(TestRuntimeProviders.discover(
                B173ServerCollisionTestRuntimeProvider.RUNTIME_ID).getClass()
                == B173ServerCollisionTestRuntimeProvider.class,
                "collision provider service discovery drifted");
        List<BlockCollisionScenario> rows = B173CollisionScenarioFactory.staticEnvelopeFamily();
        require(rows.size() == 5 && rows.stream().allMatch(row ->
                row.claim().layer() == ConformanceLayer.ARCHETYPE),
                "collision family membership or routing drifted");
        require(rows.get(0).subject().endsWith("/001") && rows.get(0).probes().size() == 3
                && rows.get(0).probes().get(2).expected()
                        == BlockCollisionExpectation.PASSABLE,
                "full-cube collision envelope drifted");
        require(rows.get(1).placements().get(0).expected().equals(new BlockState(44, 0))
                && rows.get(1).probes().get(1).deltaY() == 0.5D
                && rows.get(1).probes().get(1).expected()
                        == BlockCollisionExpectation.PASSABLE,
                "slab collision envelope drifted");
        require(rows.get(2).placements().get(0).expected().equals(new BlockState(53, 2))
                && rows.get(2).probes().get(1).deltaZ() == 0.6D
                && rows.get(3).placements().size() == 2
                && rows.get(3).placementSlot().before().count() == 2
                && rows.get(3).probes().get(1).expected()
                        == BlockCollisionExpectation.BLOCKED,
                "stairs or fence envelope drifted");
        require(rows.get(4).placements().get(0).expected().equals(new BlockState(50, 5))
                && rows.get(4).probes().get(0).expected()
                        == BlockCollisionExpectation.PASSABLE,
                "no-collision attachment envelope drifted");
        List<BlockCollisionScenario> special =
                B173SpecialCollisionPhysicalScenarioFactory.collisions();
        require(special.size() == 4
                && special.get(0).probes().get(0).expected()
                        == BlockCollisionExpectation.PASSABLE
                && special.get(1).probes().get(0).expected()
                        == BlockCollisionExpectation.PASSABLE
                && special.get(2).supportState().equals(new BlockState(12, 0))
                && special.get(3).probes().get(2).deltaY() == 0.9375D,
                "special-collision envelope rows drifted");
        List<BlockCollisionScenario> utilities =
                B173TileUtilityPhysicalScenarioFactory.collisions();
        require(utilities.size() == 6 && utilities.stream().allMatch(row ->
                row.claim().layer() == ConformanceLayer.SINGULAR && row.probes().size() == 3)
                && utilities.get(0).placements().get(0).expected().equals(new BlockState(23, 2))
                && utilities.get(5).subject().endsWith("/084"),
                "tile-utility collision rows drifted");
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put(BlockCollisionPlan.PLACEMENT_SLOT_OPTION, "1:37:85:2:0");
        B173CollisionLoadout loadout = B173CollisionLoadout.from(new TestRuntimeRequest(
                B173CollisionArena.SEED, Paths.get("."), null,
                "official static collision > fence", options));
        require(loadout.hotbar == 1 && loadout.item.equals(new RemoteItemStack(85, 2, 0)),
                "collision runtime option did not select its loadout");
        rejects(() -> B173CollisionLoadout.from(new TestRuntimeRequest(
                B173CollisionArena.SEED, Paths.get("."), null, "missing",
                java.util.Collections.<String, String>emptyMap())), "placement slot");
        B173ServerCollisionTestRuntimeProvider provider =
                new B173ServerCollisionTestRuntimeProvider();
        rejects(() -> provider.open(new TestRuntimeRequest(B173CollisionArena.SEED,
                Paths.get("."), Paths.get("mod.jar"))), "server mods");
        rejects(() -> provider.open(new TestRuntimeRequest(1L,
                Paths.get("."), null)), "requires seed");
        System.out.println("B173ServerCollisionProviderTest passed");
    }

    private static void rejects(Checked action, String fragment) throws Exception {
        try { action.run(); throw new AssertionError("invalid collision input was accepted"); }
        catch (IllegalArgumentException error) {
            require(error.getMessage().contains(fragment),
                    "unexpected collision rejection: " + error.getMessage());
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private interface Checked { void run() throws Exception; }
}
