package worldline.testkit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
import worldline.test.TestPlan;
import worldline.test.TestRuntimeProvider;
import worldline.test.TestRuntimeRequest;
import worldline.test.TestRuntimeSession;
import worldline.test.WorldlineSpec;
import static worldline.test.Expect.expect;
import static worldline.test.Worldline.pos;
import static worldline.test.Worldline.test;
import static worldline.test.Worldline.worldline;

/** Contract checks for fluent configuration, diagnostics, timeout, and batch events. */
public final class TestKitContractTest {
    private TestKitContractTest() {
    }
    public static void main(String[] arguments) throws Exception {
        BlockConformancePlanTest.execute();
        BlockLifecycleDropMatrixTest.execute();
        BlockLifecycleFixtureTest.execute();
        TerrainCraftedSolidStabilityFixtureTest.execute();
        ServerAclFixtureTest.execute();
        ServerEntryPolicyFixtureTest.execute();
        DoorSoundFixtureTest.execute();
        MinecartBoosterFixtureTest.execute();
        ChunkReloadFixtureTest.execute();
        ChunkRestartFixtureTest.execute();
        DungeonGenerationFixtureTest.execute();
        ChestAccessFixtureTest.execute();
        PortalInvalidFrameFixtureTest.execute();
        PortalReentryCooldownFixtureTest.execute();
        SkyBrightnessCycleFixtureTest.execute();
        RainStopFixtureTest.execute();
        NaturalSlimeSpawnFixtureTest.execute();
        SleepQuorumFixtureTest.execute();
        IceFormationFixtureTest.execute();
        FlowingWaterFreezeFixtureTest.execute();
        FlowingFluidLifecycleFixtureTest.execute();
        SnowAccumulationFixtureTest.execute();
        SnowLayerNonstackingFixtureTest.execute();
        NaturalWolfPackFixtureTest.execute();
        Protocol14EdgeFixtureTest.execute();
        MapDataContentFixtureTest.execute();
        BonemealWheatFixtureTest.execute();
        RemainingOpaqueCubePhysicalEnvelopeFixtureTest.execute();
        TntChainFixtureTest.execute();
        CreeperTntDifferentialFixtureTest.execute();
        PoweredCreeperFixtureTest.execute();
        SpiderDaylightAggressionFixtureTest.execute();
        NotePitchFixtureTest.execute();
        CakeServingFixtureTest.execute();
        Path root = Files.createTempDirectory("worldline-test-contract-");
        Provider provider = new Provider();
        RunnerOptions options = new RunnerOptions().provider(provider).artifacts(root.resolve("artifacts"))
                .snapshots(root.resolve("snapshots")).runtimeLock(root.resolve("runtime.lock"));
        Events events = new Events();
        TestRunResult batch = new TestRunner().run(Arrays.asList(new Fluent(), new Passing()), options, events);
        require(batch.passed() && batch.tests().size() == 2, "multi-spec run failed");
        require(events.files == 2 && events.started == 2, "collection events failed");
        require(provider.seeds.get(0) == 991L && batch.tests().get(0).seed() == 991L,
                "fluent seed was not applied");
        TestRunResult divergence = new TestRunner().run(new Divergence(), options, null);
        TestResult failed = divergence.tests().get(0);
        require(failed.divergenceTick() == 0 && "BLOCK_STATE".equals(failed.divergenceRole())
                && failed.divergenceField().equals("block[1,64,2]"), "semantic divergence missing");
        TestRunResult interrupted = new TestRunner().run(new Interrupted(),
                new RunnerOptions().artifacts(root.resolve("interrupted")), null);
        require(interrupted.count(TestStatus.INTERRUPTED) == 1, "interruption was not preserved");
        TestRunResult timeout = new TestRunner().run(new Slow(), new RunnerOptions().provider(provider)
                .runtimeLock(root.resolve("timeout.lock")).artifacts(root.resolve("timeout")).timeout(250), null);
        require(timeout.count(TestStatus.FAILED) == 1 && contains(timeout.tests().get(0), "timeout-inventory.txt")
                && trace(timeout.tests().get(0)), "timeout diagnostics missing");
        TestRunResult required = new TestRunner().run(new RuntimeRequired(),
                new RunnerOptions().artifacts(root.resolve("required")), null);
        require(required.count(TestStatus.FAILED) == 1
                && required.tests().get(0).errorMessage().contains("E2201"), "runtime requirement was ignored");
        TestRunResult mapping = new TestRunner().run(new UnmappedWrite(), options, null);
        require(mapping.count(TestStatus.FAILED) == 1
                && mapping.tests().get(0).errorMessage().contains("E2103"), "unmapped write was accepted");
        TestRunResult bail = new TestRunner().run(Arrays.asList(new Divergence(), new Passing()),
                new RunnerOptions().provider(provider).runtimeLock(root.resolve("bail.lock"))
                        .artifacts(root.resolve("bail")).bail(1), null);
        require(bail.count(TestStatus.FAILED) == 1 && bail.count(TestStatus.INTERRUPTED) == 1,
                "project-wide bail was not preserved");
        System.out.println("TestKitContractTest passed");
    }
    private static final class Fluent extends WorldlineSpec {
        @Override protected void define() {
            test("fluent", worldline().runtime("fake").seed(991L).run(context -> {
                expect(context.seed()).toEqual(991L);
                expect(context.health()).toEqual(20);
            }));
        }
    }
    private static final class Passing extends WorldlineSpec {
        @Override protected void define() {
            test("second file", context -> expect(true).toBeTrue());
        }
    }
    private static final class Divergence extends WorldlineSpec {
        @Override protected void define() {
            test("block divergence", context -> expect(context.block(pos(1, 64, 2)))
                    .toEqual(new BlockState(20, 0)));
        }
    }
    private static final class Interrupted extends WorldlineSpec {
        @Override protected void define() {
            test("interrupt", context -> {
                throw new InterruptedException("stop");
            });
        }
    }
    private static final class Slow extends WorldlineSpec {
        @Override protected void define() {
            test("timeout", context -> {
                context.tick();
                Thread.sleep(10_000);
            });
        }
    }
    private static final class RuntimeRequired extends WorldlineSpec {
        @Override protected void define() {
            test("requires runtime", worldline().runtime("fake").run(context -> {
            }));
        }
    }
    private static final class UnmappedWrite extends WorldlineSpec {
        @Override protected void define() {
            test("raw block write", context -> context.setBlock(pos(1, 64, 2), new BlockState(20, 0)));
        }
    }
    private static boolean contains(TestResult result, String name) {
        for (Path path : result.artifacts()) {
            if (path.getFileName().toString().equals(name)) {
                return true;
            }
        }
        return false;
    }
    private static boolean trace(TestResult result) {
        for (Path path : result.artifacts()) {
            if (path.getFileName().toString().endsWith(".wltrace")) {
                return true;
            }
        }
        return false;
    }
    private static final class Events implements TestReporter {
        int files, started;
        @Override public void runStarted(List<TestPlan> plans, int selected) {
            started = selected;
        }
        @Override public void fileCollected(TestPlan plan) {
            files++;
        }
    }
    private static final class Provider implements TestRuntimeProvider {
        final List<Long> seeds = new java.util.ArrayList<>();
        final AtomicInteger sessions = new AtomicInteger();
        @Override public String runtimeId() {
            return "fake";
        }
        @Override public TestRuntimeSession open(TestRuntimeRequest request) {
            seeds.add(request.seed());
            sessions.incrementAndGet();
            Runtime runtime = new Runtime();
            return new TestRuntimeSession() {
                @Override public AutomatedMinecraftRuntime runtime() {
                    return runtime;
                }
                @Override public void close() {
                    runtime.close();
                }
            };
        }
    }
    private static final class Runtime implements AutomatedMinecraftRuntime {
        private final Player player = new Player();
        private int ticks;
        @Override public void bootHeadless() {
        }
        @Override public void loadWorld(worldline.api.WorldSource source) {
        }
        @Override public void tick() {
            ticks++;
        }
        @Override public RuntimeState state() {
            return RuntimeState.WORLD_LOADED;
        }
        @Override public GameWorld world() {
            return new World(ticks);
        }
        @Override public GamePlayer player() {
            return player;
        }
        @Override public void close() {
        }
    }
    private static final class World implements GameWorld {
        private final int ticks;
        World(int ticks) {
            this.ticks = ticks;
        }
        @Override public long time() {
            return ticks;
        }
        @Override public BlockState block(BlockPosition position) {
            return new BlockState(0, 0);
        }
        @Override public boolean setBlock(BlockPosition position, BlockState state) {
            return true;
        }
        @Override public List<GameEntity> entities() {
            return Collections.emptyList();
        }
        @Override public ItemCensus items() {
            return ItemCensus.empty();
        }
        @Override public ItemCensus blocks() {
            return ItemCensus.empty();
        }
    }
    private static final class Player implements GamePlayer {
        @Override public int id() {
            return 1;
        }
        @Override public String type() {
            return "player";
        }
        @Override public GamePosition position() {
            return new GamePosition(0, 64, 0);
        }
        @Override public boolean alive() {
            return true;
        }
        @Override public void teleport(GamePosition position) {
        }
        @Override public String username() {
            return "Test";
        }
        @Override public int health() {
            return 20;
        }
        @Override public int selectedHotbarSlot() {
            return 0;
        }
        @Override public void selectHotbarSlot(int slot) {
        }
        @Override public ItemCensus items() {
            return ItemCensus.empty();
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
