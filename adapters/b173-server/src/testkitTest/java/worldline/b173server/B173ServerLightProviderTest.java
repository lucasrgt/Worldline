package worldline.b173server;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.test.TestRuntimeProviders;
import worldline.test.TestRuntimeRequest;
import worldline.testkit.BlockLightExpectation;
import worldline.testkit.BlockLightPlan;
import worldline.testkit.BlockLightScenario;
import worldline.testkit.ConformanceLayer;

/** Static checks for the public light provider and official scenario rows. */
public final class B173ServerLightProviderTest {
    private B173ServerLightProviderTest() { }

    public static void main(String[] arguments) throws Exception {
        require(TestRuntimeProviders.discover(B173ServerLightTestRuntimeProvider.RUNTIME_ID)
                .getClass() == B173ServerLightTestRuntimeProvider.class,
                "light provider service discovery drifted");
        List<BlockLightScenario> rows = B173LightScenarioFactory.staticTransportFamily();
        require(rows.size() == 7 && rows.stream().allMatch(row ->
                row.claim().layer() == ConformanceLayer.ARCHETYPE),
                "light family membership or routing drifted");
        require(rows.get(0).subject().endsWith("/020")
                && rows.get(0).probes().get(0).treatment().skyLight() == 15
                && rows.get(1).probes().get(0).treatment().skyLight() == 14
                && rows.get(2).probes().get(0).treatment().skyLight() == 12,
                "skylight attenuation matrix drifted");
        require(rows.get(3).placements().get(0).expected().equals(new BlockState(50, 5))
                && rows.get(3).probes().get(0).treatment().blockLight() == 14
                && rows.get(4).probes().get(0).treatment().blockLight() == 15
                && rows.get(5).probes().get(0).treatment().blockLight() == 7
                && rows.get(6).probes().get(2).treatment().blockLight() == 13,
                "block-light propagation matrix drifted");
        require(rows.get(4).probes().get(0).treatment().skyLight()
                == BlockLightExpectation.ANY_LIGHT,
                "opaque source sky plane should be out of contract");
        List<BlockLightScenario> special = B173SpecialCollisionPhysicalScenarioFactory.lights();
        require(special.size() == 4
                && special.get(0).probes().get(0).treatment().skyLight() == 14
                && special.get(2).supportState().equals(new BlockState(12, 0))
                && special.get(3).probes().get(0).treatment().skyLight() == 0,
                "special-collision light rows drifted");
        List<BlockLightScenario> utilities = B173TileUtilityPhysicalScenarioFactory.lights();
        require(utilities.size() == 6 && utilities.stream().allMatch(row ->
                row.claim().layer() == ConformanceLayer.SINGULAR
                && row.probes().get(0).treatment().blockLight() == 0)
                && utilities.get(2).subject().endsWith("/052")
                && utilities.get(2).probes().get(0).treatment().skyLight() == 15
                && utilities.stream().filter(row -> !row.subject().endsWith("/052"))
                        .allMatch(row -> row.probes().get(0).treatment().skyLight() == 0),
                "tile-utility light rows drifted");
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put(BlockLightPlan.PLACEMENT_SLOT_OPTION, "1:37:89:1:0");
        B173LightLoadout loadout = B173LightLoadout.from(new TestRuntimeRequest(
                B173LightArena.SEED, Paths.get("."), null,
                "official static light > glowstone", options));
        require(loadout.hotbar == 1 && loadout.item.equals(new RemoteItemStack(89, 1, 0)),
                "light runtime option did not select its loadout");
        rejects(() -> B173LightLoadout.from(new TestRuntimeRequest(B173LightArena.SEED,
                Paths.get("."), null, "missing", java.util.Collections.emptyMap())),
                "placement slot");
        B173ServerLightTestRuntimeProvider provider = new B173ServerLightTestRuntimeProvider();
        rejects(() -> provider.open(new TestRuntimeRequest(B173LightArena.SEED,
                Paths.get("."), Paths.get("mod.jar"))), "server mods");
        rejects(() -> provider.open(new TestRuntimeRequest(1L, Paths.get("."), null)),
                "requires seed");
        System.out.println("B173ServerLightProviderTest passed");
    }

    private static void rejects(Checked action, String fragment) throws Exception {
        try { action.run(); throw new AssertionError("invalid light input was accepted"); }
        catch (IllegalArgumentException error) {
            require(error.getMessage().contains(fragment),
                    "unexpected light rejection: " + error.getMessage());
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private interface Checked { void run() throws Exception; }
}
