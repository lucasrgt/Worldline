package worldline.b173server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
        require(rows.size() == 7 && rows.get(0).id().equals("cobblestone")
                && rows.get(1).id().equals("dirt") && rows.get(2).id().equals("empty-chest")
                && rows.get(3).id().equals("stone") && rows.get(4).id().equals("planks")
                && rows.get(5).id().equals("sandstone") && rows.get(6).id().equals("brick"),
                "lifecycle fixture row order drifted");
        require(rows.get(0).drops().layer() == ConformanceLayer.ARCHETYPE
                && rows.get(1).drops().layer() == ConformanceLayer.ARCHETYPE
                && rows.get(2).drops().layer() == ConformanceLayer.SINGULAR,
                "lifecycle three-layer routing drifted");
        require(rows.get(0).placementSlot().inventorySlot() == 37
                && rows.get(6).placementSlot().inventorySlot() == 43
                && rows.stream().allMatch(row -> row.breakSlot().inventorySlot() == 44),
                "lifecycle provisioned slots drifted");
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

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private interface Checked { void run() throws Exception; }
}
