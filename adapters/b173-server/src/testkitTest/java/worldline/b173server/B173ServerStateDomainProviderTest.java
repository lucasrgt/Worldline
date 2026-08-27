package worldline.b173server;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import worldline.api.BlockState;
import worldline.test.TestRuntimeProviders;
import worldline.test.TestRuntimeRequest;
import worldline.testkit.BlockStateDomainPlan;
import worldline.testkit.BlockStateDomainScenario;
import worldline.testkit.ConformanceLayer;

/** Static checks for the public state-domain provider and official scenario rows. */
public final class B173ServerStateDomainProviderTest {
    private B173ServerStateDomainProviderTest() { }

    public static void main(String[] arguments) throws Exception {
        require(TestRuntimeProviders.discover(
                B173ServerStateDomainTestRuntimeProvider.RUNTIME_ID).getClass()
                == B173ServerStateDomainTestRuntimeProvider.class,
                "state-domain provider service discovery drifted");
        BlockStateDomainScenario door = B173StateDomainScenarioFactory.woodenDoor();
        require(door.id().equals("wooden-door-complete-metadata")
                && door.claim().layer() == ConformanceLayer.SINGULAR
                && door.domain().size() == 16 && door.steps().size() == 8
                && door.placementSlot().before().legacyId() == 324
                && door.placementSlot().before().count() == 4
                && door.placementSlot().after() == null,
                "wooden-door state-domain row drifted");
        for (int metadata = 0; metadata <= 15; metadata++) {
            require(door.domain().contains(new BlockState(64, metadata)),
                    "door metadata absent from public domain: " + metadata);
        }
        require(door.finalStates().size() == 8
                && door.finalStates().values().contains(new BlockState(64, 4))
                && door.finalStates().values().contains(new BlockState(64, 7))
                && door.finalStates().values().contains(new BlockState(64, 12))
                && door.finalStates().values().contains(new BlockState(64, 15)),
                "door final open-state grid drifted");
        require(door.steps().get(0).observations().get(1).state().equals(new BlockState(64, 8))
                && door.steps().get(4).observations().get(1).state().equals(new BlockState(64, 12)),
                "door upper-half closed/open bit routing drifted");
        BlockStateDomainScenario furnace = B173StateDomainScenarioFactory.furnaceFacing();
        require(furnace.id().equals("furnace-facing-metadata")
                && furnace.claim().layer() == ConformanceLayer.SINGULAR
                && furnace.domain().size() == 4 && furnace.steps().size() == 4
                && furnace.placementSlot().before().equals(
                        new worldline.api.RemoteItemStack(61, 4, 0))
                && furnace.finalStates().size() == 4,
                "furnace state-domain row drifted");
        for (int metadata = 2; metadata <= 5; metadata++) {
            require(furnace.domain().contains(new BlockState(61, metadata)),
                    "furnace facing absent from public domain: " + metadata);
        }
        java.util.List<BlockStateDomainScenario> cardinal =
                B173StateDomainScenarioFactory.cardinalPlacementFamily();
        require(cardinal.size() == 7 && cardinal.get(0).subject().endsWith("/023")
                && cardinal.get(6).subject().endsWith("/091"),
                "cardinal placement family membership drifted");
        int states = 0;
        for (BlockStateDomainScenario scenario : cardinal) {
            require(scenario.steps().size() == 4 && scenario.finalStates().size() == 4
                    && scenario.placementSlot().before().count() == 4,
                    "cardinal state-domain row drifted: " + scenario.id());
            states += scenario.domain().size();
        }
        require(states == 25, "cardinal placement state count drifted");
        require(cardinal.get(0).claim().layer() == ConformanceLayer.SINGULAR
                && cardinal.get(1).claim().layer() == ConformanceLayer.ARCHETYPE
                && cardinal.get(2).claim().layer() == ConformanceLayer.SINGULAR
                && cardinal.get(3).claim().layer() == ConformanceLayer.SINGULAR
                && cardinal.get(4).claim().layer() == ConformanceLayer.ARCHETYPE
                && cardinal.get(5).claim().layer() == ConformanceLayer.ARCHETYPE
                && cardinal.get(6).claim().layer() == ConformanceLayer.ARCHETYPE,
                "cardinal placement conformance routing drifted");
        require(B173StateDomainScenarioFactory.chestPlacementMetadata().domain().equals(
                java.util.Collections.singletonList(new BlockState(54, 0))),
                "chest yaw-invariant server metadata drifted");
        require(B173StateDomainScenarioFactory.woodStairsFacing().domain().equals(
                java.util.Arrays.asList(new BlockState(53, 2), new BlockState(53, 1),
                        new BlockState(53, 3), new BlockState(53, 0))),
                "wood-stairs facing domain drifted");
        require(B173StateDomainScenarioFactory.pumpkinFacing().domain().equals(
                java.util.Arrays.asList(new BlockState(86, 2), new BlockState(86, 3),
                        new BlockState(86, 0), new BlockState(86, 1))),
                "pumpkin facing domain drifted");
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put(BlockStateDomainPlan.PLACEMENT_SLOT_OPTION, "1:37:324:4:0");
        B173StateDomainLoadout loadout = B173StateDomainLoadout.from(new TestRuntimeRequest(
                B173StateDomainScenarioFactory.SEED, Paths.get("."), null,
                "official state domain > wooden door", options));
        require(loadout.hotbar == 1 && loadout.item.equals(
                new worldline.api.RemoteItemStack(324, 4, 0)),
                "state-domain runtime option did not select its loadout");
        options.put(BlockStateDomainPlan.PLACEMENT_SLOT_OPTION, "1:37:61:4:0");
        loadout = B173StateDomainLoadout.from(new TestRuntimeRequest(
                B173StateDomainScenarioFactory.SEED, Paths.get("."), null,
                "official state domain > furnace", options));
        require(loadout.item.equals(new worldline.api.RemoteItemStack(61, 4, 0)),
                "state-domain runtime option did not accept furnace loadout");
        rejects(() -> B173StateDomainLoadout.from(new TestRuntimeRequest(
                B173StateDomainScenarioFactory.SEED, Paths.get("."), null,
                "missing", java.util.Collections.<String, String>emptyMap())), "placement slot");
        B173ServerStateDomainTestRuntimeProvider provider =
                new B173ServerStateDomainTestRuntimeProvider();
        rejects(() -> provider.open(new TestRuntimeRequest(B173StateDomainScenarioFactory.SEED,
                Paths.get("."), Paths.get("mod.jar"))), "server mods");
        rejects(() -> provider.open(new TestRuntimeRequest(1L,
                Paths.get("."), null)), "requires seed");
        System.out.println("B173ServerStateDomainProviderTest passed");
    }

    private static void rejects(Checked action, String fragment) throws Exception {
        try { action.run(); throw new AssertionError("invalid state-domain input was accepted"); }
        catch (IllegalArgumentException error) {
            require(error.getMessage().contains(fragment),
                    "unexpected state-domain rejection: " + error.getMessage());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private interface Checked { void run() throws Exception; }
}
