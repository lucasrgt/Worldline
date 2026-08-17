package worldline.kernel;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.GameEntity;
import worldline.api.GamePlayer;
import worldline.api.GamePosition;
import worldline.api.GameWorld;
import worldline.api.ItemCensus;
import worldline.api.ItemRecipe;
import worldline.api.RuntimeState;
import worldline.api.WorldSource;
import worldline.invariants.InvariantEngine;
import worldline.invariants.RecipeBook;

public final class ControlledMinecraftRuntimeTest {
    private ControlledMinecraftRuntimeTest() {}

    public static void main(String[] arguments) {
        validLifecycleDelegatesInOrder();
        invalidTransitionsFailClosed();
        countedTicksValidateAndDelegate();
        domainAccessIsLifecycleGuarded();
        backendFailureDoesNotAdvanceState();
        closeIsIdempotent();
        watchedTicksObservePlayerItems();
        watchedStandardHoldsOnStaticWorld();
        System.out.println("ControlledMinecraftRuntimeTest passed");
    }

    private static void validLifecycleDelegatesInOrder() {
        RecordingBackend backend = new RecordingBackend();
        ControlledMinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        WorldSource source = WorldSource.at(Paths.get("local", "worlds", "fixture"));

        runtime.bootHeadless();
        runtime.loadWorld(source);
        runtime.tick();

        equal(RuntimeState.WORLD_LOADED, runtime.state(), "state after tick");
        equal(Arrays.asList("boot", "load:" + source.path(), "tick"), backend.events, "backend calls");
    }

    private static void invalidTransitionsFailClosed() {
        RecordingBackend backend = new RecordingBackend();
        ControlledMinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);

        expectFailure(runtime::tick, "expected WORLD_LOADED");
        equal(RuntimeState.NEW, runtime.state(), "state after rejected tick");
        equal(0, backend.events.size(), "backend calls after rejected tick");
    }

    private static void countedTicksValidateAndDelegate() {
        RecordingBackend backend = new RecordingBackend();
        ControlledMinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        runtime.loadWorld(WorldSource.at(Paths.get("memory", "counted-ticks")));

        runtime.tick(2);
        runtime.tick(0);
        expectFailure(() -> runtime.tick(-1), "tick count must not be negative");

        equal(2L, backend.events.stream().filter("tick"::equals).count(), "counted ticks");
    }

    private static void backendFailureDoesNotAdvanceState() {
        RecordingBackend backend = new RecordingBackend();
        backend.failBoot = true;
        ControlledMinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);

        expectFailure(runtime::bootHeadless, "backend boot failed");
        equal(RuntimeState.NEW, runtime.state(), "state after failed boot");
    }

    private static void domainAccessIsLifecycleGuarded() {
        ControlledMinecraftRuntime runtime = new ControlledMinecraftRuntime(new RecordingBackend());
        expectFailure(runtime::world, "expected WORLD_LOADED");
        runtime.bootHeadless();
        runtime.loadWorld(WorldSource.at(Paths.get("memory", "domain-access")));
        expectFailure(runtime::world, "world automation is unavailable");
        expectFailure(runtime::ui, "ui automation is unavailable");
        runtime.close();
        expectFailure(runtime::player, "expected WORLD_LOADED");
    }

    private static void closeIsIdempotent() {
        RecordingBackend backend = new RecordingBackend();
        ControlledMinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);

        runtime.close();
        runtime.close();

        equal(RuntimeState.CLOSED, runtime.state(), "closed state");
        equal(Arrays.asList("close"), backend.events, "close calls");
    }

    private static void watchedTicksObservePlayerItems() {
        InventoryBackend backend = new InventoryBackend();
        ControlledMinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        runtime.loadWorld(WorldSource.at(Paths.get("memory", "invariants")));
        backend.player.census = ItemCensus.of(265, 10);
        runtime.watch(InvariantEngine.itemConservation());
        runtime.tick();
        backend.player.census = ItemCensus.empty();
        backend.world.census = ItemCensus.of(265, 10);
        runtime.tick();
        backend.world.census = ItemCensus.of(265, 11);
        expectFailure(runtime::tick, "item 265 grew from 10 to 11");
        backend.world.blocks = ItemCensus.of(1, 2);
        backend.world.census = ItemCensus.empty();
        runtime.watch(InvariantEngine.itemConservation(RecipeBook.of(Collections.singletonList(
                new ItemRecipe(ItemCensus.of(1, 1), ItemCensus.of(4, 1))))));
        runtime.tick();
        backend.world.blocks = ItemCensus.of(1, 1);
        backend.world.census = ItemCensus.of(4, 1);
        runtime.tick();
        backend.world.census = ItemCensus.of(4, 2);
        expectFailure(runtime::tick, "item 4 grew from 1 to 2");
        equal(6L, backend.events.stream().filter("tick"::equals).count(), "observer runs after tick");
        equal(0, runtime.recipes().size(), "default recipe book is empty");
        runtime.close();
        expectFailure(() -> runtime.watch(InvariantEngine.itemConservation()), "CLOSED");
    }

    private static void watchedStandardHoldsOnStaticWorld() {
        InventoryBackend backend = new InventoryBackend();
        ControlledMinecraftRuntime runtime = new ControlledMinecraftRuntime(backend);
        runtime.bootHeadless();
        runtime.loadWorld(WorldSource.at(Paths.get("memory", "standard")));
        runtime.watch(InvariantEngine.standard(runtime));
        runtime.tick();
        runtime.tick();
    }

    private static void expectFailure(Runnable action, String messagePart) {
        try {
            action.run();
            throw new AssertionError("Expected failure containing: " + messagePart);
        } catch (RuntimeException error) {
            if (!error.getMessage().contains(messagePart)) {
                throw new AssertionError("Unexpected failure: " + error.getMessage(), error);
            }
        }
    }

    private static void equal(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static class RecordingBackend implements GameBackend {
        final List<String> events = new ArrayList<>();
        private boolean failBoot;

        @Override
        public void bootHeadless() {
            if (failBoot) {
                throw new IllegalStateException("backend boot failed");
            }
            events.add("boot");
        }

        @Override
        public void loadWorld(WorldSource source) {
            events.add("load:" + source.path());
        }

        @Override
        public void tick() {
            events.add("tick");
        }

        @Override
        public void close() {
            events.add("close");
        }
    }

    private static final class InventoryBackend extends RecordingBackend {
        private final FakePlayer player = new FakePlayer();
        private final FakeWorld world = new FakeWorld();

        @Override public GamePlayer player() { return player; }
        @Override public GameWorld world() { return world; }
    }

    private static final class FakeWorld implements GameWorld {
        ItemCensus census = ItemCensus.empty();
        ItemCensus blocks = ItemCensus.empty();

        @Override public long time() { return 0L; }
        @Override public BlockState block(BlockPosition position) { return new BlockState(0, 0); }
        @Override public boolean setBlock(BlockPosition position, BlockState state) { return false; }
        @Override public List<GameEntity> entities() { return Collections.emptyList(); }
        @Override public ItemCensus items() { return census; }
        @Override public ItemCensus blocks() { return blocks; }
    }

    private static final class FakePlayer implements GamePlayer {
        ItemCensus census = ItemCensus.empty();

        @Override public String username() { return "fake"; }
        @Override public int health() { return 20; }
        @Override public int selectedHotbarSlot() { return 0; }
        @Override public void selectHotbarSlot(int slot) { }
        @Override public ItemCensus items() { return census; }
        @Override public int id() { return 1; }
        @Override public String type() { return "minecraft:player"; }
        @Override public GamePosition position() { return new GamePosition(0.0D, 64.0D, 0.0D); }
        @Override public boolean alive() { return true; }
        @Override public void teleport(GamePosition position) { }
    }
}
