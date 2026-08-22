package worldline.testkit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import worldline.api.AutomatedMinecraftRuntime;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.GameEntity;
import worldline.api.GamePlayer;
import worldline.api.GamePosition;
import worldline.api.GameUi;
import worldline.api.GameUiCapability;
import worldline.api.GameUiImage;
import worldline.api.GameUiNode;
import worldline.api.GameUiVisual;
import worldline.api.GameWorld;
import worldline.api.ItemCensus;
import worldline.api.RuntimeState;
import worldline.api.UiMinecraftRuntime;
import worldline.test.TestRuntimeProvider;
import worldline.test.TestRuntimeRequest;
import worldline.test.TestRuntimeSession;
import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.test;
import static worldline.test.Worldline.worldline;

/** Public TestKit contract for first-class, capability-gated semantic UI access. */
public final class TestKitGuiContractTest {
    private TestKitGuiContractTest() {}

    public static void main(String[] arguments) throws Exception {
        Path root = Files.createTempDirectory("worldline-test-gui-");
        TestRunResult updated = run(root.resolve("ui"), new Provider(true), true);
        Provider ui = new Provider(true);
        TestRunResult passed = run(root.resolve("ui"), ui, false);
        require(updated.passed() && passed.passed() && ui.runtime.gui.clicks == 1,
                "semantic UI test did not pass");
        require(hasArtifact(passed, "crusher.ppm"), "visual artifact missing");
        TestRunResult rejected = run(root.resolve("headless"), new Provider(false), false);
        require(rejected.count(TestStatus.FAILED) == 1
                && rejected.tests().get(0).errorMessage().contains("E2301"),
                "runtime without UI did not fail closed");
        TestRunResult failed = runFailure(root.resolve("failure"), new Provider(true));
        require(failed.count(TestStatus.FAILED) == 1
                && hasArtifact(failed, "failure.gui.txt") && hasArtifact(failed, "failure.gui.ppm"),
                "automatic GUI failure evidence missing");
        System.out.println("TestKitGuiContractTest passed");
    }

    private static TestRunResult run(Path root, Provider provider, boolean update) {
        RunnerOptions options = new RunnerOptions().provider(provider).artifacts(root.resolve("artifacts"))
                .snapshots(root.resolve("snapshots")).runtimeLock(root.resolve("runtime.lock"))
                .updateSnapshots(update);
        return new TestRunner().run(new GuiSpec(), options, null);
    }

    private static TestRunResult runFailure(Path root, Provider provider) {
        RunnerOptions options = new RunnerOptions().provider(provider).artifacts(root.resolve("artifacts"))
                .snapshots(root.resolve("snapshots")).runtimeLock(root.resolve("runtime.lock"));
        return new TestRunner().run(new FailingGuiSpec(), options, null);
    }

    private static final class GuiSpec extends WorldlineSpec {
        @Override protected void define() {
            test("semantic gui", worldline().runtime("fake").seed(173L).run(context -> {
                GameUi ui = context.ui();
                ui.getByRole(GameUiNode.SLOT).shouldHaveCount(2);
                ui.getByLabel("Input").click();
                expect(ui.getSlot(0).single().itemId()).toEqual(265);
                context.awaitUi(ui.getByText("Ready"), 2).shouldHaveCount(1);
                expect(context.screenshot("crusher")).toMatchSnapshot("crusher");
            }));
        }
    }

    private static final class FailingGuiSpec extends WorldlineSpec {
        @Override protected void define() {
            test("failed semantic gui", worldline().runtime("fake").run(context ->
                    context.ui().getByName("missing").shouldExist()));
        }
    }

    private static final class Provider implements TestRuntimeProvider {
        private final boolean ui;
        private Runtime runtime;
        Provider(boolean ui) { this.ui = ui; }
        @Override public String runtimeId() { return "fake"; }
        @Override public TestRuntimeSession open(TestRuntimeRequest request) {
            runtime = ui ? new UiRuntime() : new Runtime();
            return new TestRuntimeSession() {
                @Override public AutomatedMinecraftRuntime runtime() { return runtime; }
                @Override public void close() { runtime.close(); }
            };
        }
    }

    private static class Runtime implements AutomatedMinecraftRuntime {
        final Player player = new Player(); final World world = new World(); final FakeUi gui = new FakeUi();
        @Override public void bootHeadless() {}
        @Override public void loadWorld(worldline.api.WorldSource source) {}
        @Override public void tick() { gui.ready = true; }
        @Override public RuntimeState state() { return RuntimeState.WORLD_LOADED; }
        @Override public GameWorld world() { return world; }
        @Override public GamePlayer player() { return player; }
        @Override public void close() {}
    }

    private static final class UiRuntime extends Runtime implements UiMinecraftRuntime {
        @Override public GameUi ui() { return gui; }
    }

    private static final class FakeUi implements GameUiVisual {
        int clicks; boolean ready;
        @Override public Set<GameUiCapability> capabilities() {
            return Collections.unmodifiableSet(EnumSet.of(
                    GameUiCapability.SEMANTIC_TREE, GameUiCapability.NODE_CLICK,
                    GameUiCapability.SCREENSHOT));
        }
        @Override public String screen() { return "crusher"; }
        @Override public List<GameUiNode> nodes() {
            List<GameUiNode> value = new java.util.ArrayList<>(Arrays.asList(
                    new GameUiNode(GameUiNode.SCREEN, "crusher", -1, -1, 0),
                    new GameUiNode(GameUiNode.SLOT, "Input", 0, 265, 4),
                    new GameUiNode(GameUiNode.SLOT, "Output", 1, -1, 0)));
            if (ready) value.add(new GameUiNode("status", "ready", -1, -1, 0,
                    Collections.singletonMap("text", "Ready")));
            return value;
        }
        @Override public GameUiNode node(String role, String name) { return getByRole(role).name(name).single(); }
        @Override public GameUiNode slot(int index) { return getSlot(index).single(); }
        @Override public void openInventory() {}
        @Override public void close() {}
        @Override public void click(GameUiNode node) { clicks++; }
        @Override public GameUiImage screenshot() {
            return new GameUiImage(2, 1, new int[] {0xff112233, 0xff445566});
        }
    }

    private static final class World implements GameWorld {
        @Override public long time() { return 0; }
        @Override public BlockState block(BlockPosition position) { return new BlockState(0, 0); }
        @Override public boolean setBlock(BlockPosition position, BlockState state) { return true; }
        @Override public List<GameEntity> entities() { return Collections.emptyList(); }
        @Override public ItemCensus items() { return ItemCensus.empty(); }
        @Override public ItemCensus blocks() { return ItemCensus.empty(); }
    }

    private static final class Player implements GamePlayer {
        @Override public int id() { return 1; }
        @Override public String type() { return "player"; }
        @Override public GamePosition position() { return new GamePosition(0, 64, 0); }
        @Override public boolean alive() { return true; }
        @Override public void teleport(GamePosition position) {}
        @Override public String username() { return "Test"; }
        @Override public int health() { return 20; }
        @Override public int selectedHotbarSlot() { return 0; }
        @Override public void selectHotbarSlot(int slot) {}
        @Override public ItemCensus items() { return ItemCensus.empty(); }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static boolean hasArtifact(TestRunResult result, String name) {
        for (Path path : result.tests().get(0).artifacts()) {
            if (name.equals(path.getFileName().toString())) return true;
        }
        return false;
    }
}
