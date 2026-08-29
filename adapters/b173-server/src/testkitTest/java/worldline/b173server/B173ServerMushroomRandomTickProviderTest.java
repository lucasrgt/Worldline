package worldline.b173server;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import worldline.api.RemoteItemStack;
import worldline.test.TestRuntimeProviders;
import worldline.test.TestRuntimeRequest;
import worldline.testkit.BlockRandomTickSpreadPlan;
import worldline.testkit.BlockRandomTickSpreadScenario;
import worldline.testkit.ConformanceLayer;

/** Static provider, arena-row, and bounded-RNG contract checks. */
public final class B173ServerMushroomRandomTickProviderTest {
    private B173ServerMushroomRandomTickProviderTest() { }

    public static void main(String[] arguments) throws Exception {
        require(TestRuntimeProviders.discover(
                B173ServerMushroomRandomTickTestRuntimeProvider.RUNTIME_ID).getClass()
                == B173ServerMushroomRandomTickTestRuntimeProvider.class,
                "mushroom random-tick provider discovery drifted");
        List<BlockRandomTickSpreadScenario> rows =
                B173MushroomRandomTickScenarioFactory.rows();
        require(rows.size() == 2 && rows.get(0).state().legacyId() == 39
                && rows.get(1).state().legacyId() == 40
                && rows.stream().allMatch(row -> row.claims().size() == 5
                        && row.claims().stream().allMatch(claim ->
                                claim.layer() == ConformanceLayer.ARCHETYPE)
                        && row.sources().size() == 30 && row.targets().size() == 19
                        && row.maxWindows() == 40 && row.windowTicks() == 400),
                "mushroom random-tick rows drifted");
        require(rows.get(0).lightProbe().equals(B173MushroomRandomTickStructure.LIGHT_PROBE)
                && rows.get(0).control().equals(B173MushroomRandomTickStructure.CONTROL)
                && rows.get(0).sourceToRemove().equals(
                        B173MushroomRandomTickStructure.LIGHT_PROBE),
                "mushroom arena causal probes drifted");
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put(BlockRandomTickSpreadPlan.PLACEMENT_SLOT_OPTION, "5:41:39:32:0");
        options.put(BlockRandomTickSpreadPlan.BREAK_SLOT_OPTION, "8:44:278:1:0");
        B173MushroomRandomTickLoadout loadout = B173MushroomRandomTickLoadout.from(
                new TestRuntimeRequest(B173MushroomRandomTickArena.SEED, Paths.get("."), null,
                        "official mushrooms > brown", options));
        require(loadout.placementHotbar == 5 && loadout.breakHotbar == 8
                && loadout.placement.equals(new RemoteItemStack(39, 32, 0)),
                "mushroom random-tick loadout drifted");
        rejects(() -> B173MushroomRandomTickLoadout.from(new TestRuntimeRequest(
                B173MushroomRandomTickArena.SEED, Paths.get("."), null, "missing",
                java.util.Collections.emptyMap())), "missing spread placement");
        B173ServerMushroomRandomTickTestRuntimeProvider provider =
                new B173ServerMushroomRandomTickTestRuntimeProvider();
        rejects(() -> provider.open(new TestRuntimeRequest(B173MushroomRandomTickArena.SEED,
                Paths.get("."), Paths.get("mod.jar"))), "does not load server mods");
        rejects(() -> provider.open(new TestRuntimeRequest(1L, Paths.get("."), null)),
                "seed drift");
        System.out.println("B173ServerMushroomRandomTickProviderTest passed");
    }
    private static void rejects(Checked action, String fragment) throws Exception {
        try { action.run(); throw new AssertionError("invalid mushroom input was accepted"); }
        catch (IllegalArgumentException error) {
            require(error.getMessage().contains(fragment),
                    "unexpected mushroom rejection: " + error.getMessage());
        }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private interface Checked { void run() throws Exception; }
}
