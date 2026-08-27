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

/** Static checks for the public state-domain provider and official door row. */
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
                && door.domain().size() == 12 && door.steps().size() == 8
                && door.placementSlot().before().legacyId() == 324
                && door.placementSlot().before().count() == 4
                && door.placementSlot().after() == null,
                "wooden-door state-domain row drifted");
        for (int metadata = 0; metadata <= 11; metadata++) {
            require(door.domain().contains(new BlockState(64, metadata)),
                    "door metadata absent from public domain: " + metadata);
        }
        require(door.finalStates().size() == 8
                && door.finalStates().values().contains(new BlockState(64, 4))
                && door.finalStates().values().contains(new BlockState(64, 7))
                && door.finalStates().values().contains(new BlockState(64, 8))
                && door.finalStates().values().contains(new BlockState(64, 11)),
                "door final open-state grid drifted");
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put(BlockStateDomainPlan.PLACEMENT_SLOT_OPTION, "1:37:324:4:0");
        B173StateDomainLoadout loadout = B173StateDomainLoadout.from(new TestRuntimeRequest(
                B173StateDomainScenarioFactory.SEED, Paths.get("."), null,
                "official state domain > wooden door", options));
        require(loadout.hotbar == 1 && loadout.item.equals(
                new worldline.api.RemoteItemStack(324, 4, 0)),
                "state-domain runtime option did not select its loadout");
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
