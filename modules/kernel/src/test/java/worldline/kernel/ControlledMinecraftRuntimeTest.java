package worldline.kernel;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import worldline.api.RuntimeState;
import worldline.api.WorldSource;

public final class ControlledMinecraftRuntimeTest {
    private ControlledMinecraftRuntimeTest() {}

    public static void main(String[] arguments) {
        validLifecycleDelegatesInOrder();
        invalidTransitionsFailClosed();
        countedTicksValidateAndDelegate();
        domainAccessIsLifecycleGuarded();
        backendFailureDoesNotAdvanceState();
        closeIsIdempotent();
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

    private static final class RecordingBackend implements GameBackend {
        private final List<String> events = new ArrayList<>();
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
}
