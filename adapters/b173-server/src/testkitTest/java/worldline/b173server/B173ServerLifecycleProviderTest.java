package worldline.b173server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import worldline.test.TestRuntimeProviders;
import worldline.test.TestRuntimeRequest;
import worldline.testkit.BlockLifecycleScenario;
import worldline.testkit.ConformanceLayer;

/** Static extension checks that do not require or distribute the official server. */
public final class B173ServerLifecycleProviderTest {
    private B173ServerLifecycleProviderTest() { }

    public static void main(String[] arguments) throws Exception {
        require(TestRuntimeProviders.discover(
                B173ServerLifecycleTestRuntimeProvider.RUNTIME_ID).getClass()
                == B173ServerLifecycleTestRuntimeProvider.class,
                "lifecycle provider service discovery drifted");
        List<BlockLifecycleScenario> rows = B173ServerLifecycleFixtures.scenarios();
        require(rows.size() == 26 && rows.get(0).id().equals("cobblestone")
                && rows.get(1).id().equals("dirt") && rows.get(2).id().equals("empty-chest")
                && rows.get(3).id().equals("stone") && rows.get(4).id().equals("planks")
                && rows.get(5).id().equals("sandstone") && rows.get(6).id().equals("brick")
                && rows.get(7).id().equals("gold-ore")
                && rows.get(15).id().equals("obsidian")
                && rows.get(16).id().equals("rail")
                && rows.get(17).id().equals("powered-rail")
                && rows.get(18).id().equals("detector-rail")
                && rows.get(19).id().equals("stone-pressure-plate")
                && rows.get(20).id().equals("wooden-pressure-plate")
                && rows.get(21).id().equals("empty-dispenser")
                && rows.get(22).id().equals("note-block")
                && rows.get(23).id().equals("crafting-table")
                && rows.get(24).id().equals("empty-furnace")
                && rows.get(25).id().equals("empty-jukebox"),
                "lifecycle fixture row order drifted");
        descriptorMatchesRows(rows.size());
        require(rows.get(0).drops().layer() == ConformanceLayer.ARCHETYPE
                && rows.get(1).drops().layer() == ConformanceLayer.ARCHETYPE
                && rows.get(2).drops().layer() == ConformanceLayer.SINGULAR,
                "lifecycle three-layer routing drifted");
        require(rows.stream().allMatch(row -> row.placementSlot().hotbarSlot() == 1
                        && row.placementSlot().inventorySlot() == 37
                        && row.breakSlot().hotbarSlot() == 2
                        && row.breakSlot().inventorySlot() == 38)
                && rows.get(0).breakSlot().before().legacyId() == 257
                && rows.get(7).breakSlot().before().legacyId() == 278
                && rows.get(18).breakSlot().before().legacyId() == 278
                && rows.get(20).breakSlot().before().legacyId() == 278
                && rows.get(21).placedState().metadata() == 2
                && rows.get(22).breakSlot().before().legacyId() == 258
                && rows.get(24).placedState().metadata() == 2
                && rows.get(25).breakSlot().before().legacyId() == 258,
                "lifecycle provisioned slots drifted");
        Map<String, String> fixture = new LinkedHashMap<String, String>();
        fixture.put(worldline.testkit.BlockLifecyclePlan.PLACEMENT_SLOT_OPTION, "1:37:57:1:0");
        fixture.put(worldline.testkit.BlockLifecyclePlan.BREAK_SLOT_OPTION, "2:38:278:1:0");
        B173LifecycleLoadout loadout = B173LifecycleLoadout.from(new TestRuntimeRequest(
                B173ServerLifecycleFixtures.SEED, Paths.get("."), null,
                "official block lifecycle > arbitrary-external-row", fixture));
        require(loadout.placement.legacyId() == 57 && loadout.tool.legacyId() == 278,
                "runtime lifecycle options did not select their loadout");
        rejects(new Checked() { @Override public void run() {
            B173LifecycleLoadout.from(new TestRuntimeRequest(B173ServerLifecycleFixtures.SEED,
                    Paths.get("."), null, "external", java.util.Collections.<String, String>emptyMap()));
        }}, "lacks placement");
        B173ServerLifecycleTestRuntimeProvider provider =
                new B173ServerLifecycleTestRuntimeProvider();
        rejects(new Checked() { @Override public void run() throws Exception {
            provider.open(new TestRuntimeRequest(B173ServerLifecycleFixtures.SEED,
                    Paths.get("."), Paths.get("mod.jar")));
        }}, "server mods");
        rejects(new Checked() { @Override public void run() throws Exception {
            provider.open(new TestRuntimeRequest(1L, Paths.get("."), null));
        }}, "requires seed");
        String prior = System.getProperty(B173ServerLifecycleSettings.SERVER_PROPERTY);
        try {
            System.clearProperty(B173ServerLifecycleSettings.SERVER_PROPERTY);
            rejects(new Checked() { @Override public void run() {
                B173ServerLifecycleSettings.load();
            }}, "missing system property");
            Path fake = Files.createTempFile("worldline-fake-b173-server-", ".jar");
            try {
                Files.write(fake, new byte[(int) B173ServerLifecycleSettings.SERVER_BYTES]);
                System.setProperty(B173ServerLifecycleSettings.SERVER_PROPERTY, fake.toString());
                rejects(new Checked() { @Override public void run() {
                    B173ServerLifecycleSettings.load();
                }}, "SHA-256 mismatch");
            } finally { Files.deleteIfExists(fake); }
        } finally {
            if (prior == null) System.clearProperty(B173ServerLifecycleSettings.SERVER_PROPERTY);
            else System.setProperty(B173ServerLifecycleSettings.SERVER_PROPERTY, prior);
        }
        System.out.println("B173ServerLifecycleProviderTest passed");
    }

    private static void rejects(Checked action, String fragment) throws Exception {
        try { action.run(); throw new AssertionError("invalid lifecycle input was accepted"); }
        catch (IllegalArgumentException error) { require(error.getMessage().contains(fragment),
                "unexpected lifecycle rejection: " + error.getMessage()); }
        catch (IllegalStateException error) { require(error.getMessage().contains(fragment),
                "unexpected lifecycle rejection: " + error.getMessage()); }
    }

    private static void descriptorMatchesRows(int rows) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get("smokes",
                "b173-lifecycle-provider-cycle", "smoke.properties"));
        String prefix = "expected.signal=";
        String signal = lines.stream().filter(line -> line.startsWith(prefix)).findFirst()
                .orElseThrow(() -> new AssertionError("lifecycle expected signal is absent"))
                .substring(prefix.length());
        String layers = signal.substring(signal.indexOf("layers=") + 7,
                signal.indexOf(",reload="));
        require(signal.contains("rows=" + rows + ",passed=" + rows + ",")
                        && layers.split("[+]", -1).length == rows,
                "lifecycle expected row/layer cardinality drift");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private interface Checked { void run() throws Exception; }
}
