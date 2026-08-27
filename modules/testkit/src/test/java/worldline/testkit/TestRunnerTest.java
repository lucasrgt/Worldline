package worldline.testkit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import worldline.api.AutomatedMinecraftRuntime;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.GameEntity;
import worldline.api.GamePlayer;
import worldline.api.GamePosition;
import worldline.api.GameWorld;
import worldline.api.ItemCensus;
import worldline.api.RuntimeState;
import worldline.test.TestRuntimeProvider;
import worldline.test.TestRuntimeRequest;
import worldline.test.TestRuntimeSession;
import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.it;
import static worldline.test.Worldline.afterAll;
import static worldline.test.Worldline.beforeAll;
import static worldline.test.Worldline.test;

/** Isolation, retry visibility, states, artifacts, and fail-closed selection tests. */
public final class TestRunnerTest {
    private TestRunnerTest() {}
    public static void main(String[] arguments) throws Exception {
        Path root = Files.createTempDirectory("worldline-testkit-"); FakeProvider provider = new FakeProvider();
        RunnerOptions options = new RunnerOptions().provider(provider).artifacts(root.resolve("artifacts"))
                .snapshots(root.resolve("snapshots")).runtimeLock(root.resolve("runtime.lock"))
                .minimize(true).minimizeBudget(8);
        RecordingReporter reporter = new RecordingReporter();
        Sample sample = new Sample(); TestRunResult result = new TestRunner().run(sample, options, reporter);
        require(result.tests().size() == 5 && result.count(TestStatus.PASSED) == 1, "result count");
        require(result.count(TestStatus.FLAKY) == 1 && result.count(TestStatus.FAILED) == 1,
                "flaky and failed states");
        require(result.count(TestStatus.SKIPPED) == 1 && result.count(TestStatus.TODO) == 1,
                "skip and todo states");
        require(provider.opened.get() == provider.closed.get() && provider.opened.get() >= 4,
                "fresh sessions were not closed");
        require(provider.paths.contains("passes") && provider.paths.contains("flaky is visible")
                && provider.paths.contains("fails with scenario"),
                "provider did not receive qualified test paths");
        require(!result.tests().get(2).artifacts().isEmpty(), "failure artifacts absent");
        require(reporter.started == 3 && reporter.finished == 5, "reporter lifecycle");
        require(sample.allHooks.get() == 11, "beforeAll/afterAll lifecycle");
        TestRunResult empty = new TestRunner().run(new Empty(), new RunnerOptions(), null);
        require(!empty.passed() && "no tests matched".equals(empty.fatalError()), "empty run passed");
        TestRunResult only = new TestRunner().run(new Only(), new RunnerOptions().ci(true), null);
        require(!only.passed() && only.fatalError().contains(".only"), "CI allowed only");
        snapshots(root); reporters(root);
        System.out.println("TestRunnerTest passed");
    }

    private static final class Sample extends WorldlineSpec {
        private final AtomicInteger flaky = new AtomicInteger();
        private final AtomicInteger allHooks = new AtomicInteger();
        @Override protected void define() {
            beforeAll(() -> allHooks.incrementAndGet()); afterAll(() -> allHooks.addAndGet(10));
            test("passes", context -> { context.tick(); expect(context.seed()).toEqual(173L); });
            it("flaky is visible", context -> {
                if (flaky.getAndIncrement() == 0) throw new AssertionError("first attempt");
            }).retry(1);
            test("fails with scenario", context -> {
                context.step("one tick", step -> step.tick()); expect(1).toEqual(2);
            });
            test("skipped", context -> {}).skip(); test("todo", context -> {}).todo();
        }
    }
    private static final class Empty extends WorldlineSpec { @Override protected void define() {} }
    private static final class Only extends WorldlineSpec {
        @Override protected void define() { test("focused", context -> {}).only(); }
    }
    private static final class SnapshotSpec extends WorldlineSpec {
        private final int value; SnapshotSpec(int value) { this.value = value; }
        @Override protected void define() {
            test("snapshot", context -> expect(Arrays.asList("value", value)).toMatchSnapshot("state"));
        }
    }
    private static final class Passing extends WorldlineSpec {
        @Override protected void define() { test("passes", context -> expect(true).toBeTrue()); }
    }
    private static void snapshots(Path root) {
        RunnerOptions update = new RunnerOptions().artifacts(root.resolve("snapshot-artifacts"))
                .snapshots(root.resolve("snapshots")).updateSnapshots(true);
        require(new TestRunner().run(new SnapshotSpec(1), update, null).passed(), "snapshot update failed");
        RunnerOptions check = new RunnerOptions().artifacts(root.resolve("snapshot-check"))
                .snapshots(root.resolve("snapshots"));
        require(new TestRunner().run(new SnapshotSpec(1), check, null).passed(), "snapshot match failed");
        require(!new TestRunner().run(new SnapshotSpec(2), check, null).passed(), "snapshot drift passed");
    }
    private static void reporters(Path root) throws Exception {
        Path json = root.resolve("report.json"), junit = root.resolve("report.xml");
        TestReporter reporter = new CompositeReporter(new JsonReporter(json), new JUnitReporter(junit));
        TestRunResult result = new TestRunner().run(new Passing(),
                new RunnerOptions().artifacts(root.resolve("report-artifacts")), reporter);
        require(result.passed() && Files.readString(json).contains("\"passed\":true")
                && Files.readString(junit).contains("<testsuite"), "machine reporters failed");
        try { new JsonReporter(java.nio.file.Paths.get("release/forbidden.json"));
            throw new AssertionError("protected output was accepted"); }
        catch (IllegalArgumentException expected) { /* expected */ }
    }
    private static final class RecordingReporter implements TestReporter {
        int started, finished;
        @Override public void testStarted(String path, int attempt) { if (attempt == 1) started++; }
        @Override public void testFinished(TestResult result) { finished++; }
    }
    private static final class FakeProvider implements TestRuntimeProvider {
        final AtomicInteger opened = new AtomicInteger(), closed = new AtomicInteger();
        final List<String> paths = Collections.synchronizedList(new ArrayList<String>());
        @Override public String runtimeId() { return "fake"; }
        @Override public TestRuntimeSession open(TestRuntimeRequest request) {
            paths.add(request.testPath());
            opened.incrementAndGet(); FakeRuntime runtime = new FakeRuntime();
            return new TestRuntimeSession() {
                @Override public AutomatedMinecraftRuntime runtime() { return runtime; }
                @Override public void close() { runtime.close(); closed.incrementAndGet(); }
            };
        }
    }
    private static final class FakeRuntime implements AutomatedMinecraftRuntime {
        private final FakePlayer player = new FakePlayer(); private int ticks;
        @Override public void bootHeadless() {}
        @Override public void loadWorld(worldline.api.WorldSource source) {}
        @Override public void tick() { ticks++; }
        @Override public RuntimeState state() { return RuntimeState.WORLD_LOADED; }
        @Override public GameWorld world() { return new FakeWorld(ticks); }
        @Override public GamePlayer player() { return player; }
        @Override public void close() {}
    }
    private static final class FakeWorld implements GameWorld {
        private final int ticks; FakeWorld(int ticks) { this.ticks = ticks; }
        @Override public long time() { return ticks; }
        @Override public BlockState block(BlockPosition position) { return new BlockState(0, 0); }
        @Override public boolean setBlock(BlockPosition position, BlockState state) { return true; }
        @Override public List<GameEntity> entities() { return Collections.emptyList(); }
        @Override public ItemCensus items() { return ItemCensus.empty(); }
        @Override public ItemCensus blocks() { return ItemCensus.empty(); }
    }
    private static final class FakePlayer implements GamePlayer {
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
}
