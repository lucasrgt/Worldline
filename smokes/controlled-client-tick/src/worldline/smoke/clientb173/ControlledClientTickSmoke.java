package worldline.smoke.clientb173;

import java.nio.file.Paths;
import worldline.api.WorldSource;
import worldline.b173.B173Keys;
import worldline.b173.B173Observation;
import worldline.b173.B173CompassProbe;
import worldline.b173.B173PhysicsProbe;
import worldline.b173.B173Runtime;
import worldline.b173.B173Runtimes;
import worldline.invariants.InvariantEngine;
import worldline.trace.CanonicalStateTrace;
import worldline.trace.CanonicalTrace;

/** Executes bootHeadless, loadWorld, and tick(1) through the public lifecycle. */
public final class ControlledClientTickSmoke {
    private static final long SEED = 17320110707L;
    private static final long RNG_SEED = 2026071501L;
    private static final String STATE_TRACE = "WORLDLINE_STATE_TRACE=";
    private static final String STATE_SIGNATURE = "WORLDLINE_STATE_SIGNATURE=";

    private ControlledClientTickSmoke() {}

    public static void main(String[] arguments) {
        B173Runtime runtime = B173Runtimes.create(SEED);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "worldline-client-cycle")));
            MetadataRecipeContract.verify(runtime.stackRecipes());
            System.out.println("WORLDLINE_METADATA_RECIPES=families-8,recipes-25");
            runtime.watch(InvariantEngine.standard(runtime));
            runtime.reseed(RNG_SEED);
            runtime.scheduler().afterTicks(2, () -> runtime.tap(B173Keys.SLOT_1 + 2));
            CanonicalTrace trace = new CanonicalTrace(SEED);
            B173Observation loaded = runtime.observe();
            record(trace, "loaded", loaded);
            runtime.tick(1);
            B173Observation tick = runtime.observe();
            record(trace, "tick1", tick);
            require(tick.clientTick() == 1 && tick.worldTime() == 1L,
                    "client and world did not advance exactly once");
            runtime.assertHeadless();
            System.out.println("WORLDLINE_CLIENT_ROOT=Minecraft.runTick");
            System.out.println("WORLDLINE_CLIENT_HEADLESS=true");
            System.out.println("WORLDLINE_CLIENT_SOURCE=" + runtime.minecraftClassSource());
            trace.emitTo(System.out);
            CanonicalStateTrace states = stateTrace();
            record(states, "loaded", loaded);
            record(states, "tick1", tick);
            for (int index = 2; index <= 16; index++) {
                runtime.tick();
                record(states, "tick" + index, runtime.observe());
            }
            B173Observation end = runtime.observe();
            require(end.clientTick() == 16 && end.worldTime() == 16L,
                    "tick(N) did not advance the client and world to 16");
            require(end.selectedSlot() == 2, "scheduled keyboard input was not applied");
            require(runtime.clock().millis() == 1_000_800L
                    && end.clientClockMillis() == runtime.clock().millis(),
                    "instrumented client did not use the virtual clock");
            require(runtime.fileSystem().operations().contains("world.loadInfo")
                    && runtime.fileSystem().operations().contains("chunk.load"),
                    "virtual filesystem did not observe world I/O");
            require(!runtime.networkConnected(), "headless runtime unexpectedly connected a network");
            require(runtime.timerThreadAlive(), "vanilla timer thread escaped supervision");
            System.out.println(STATE_TRACE + states.value());
            System.out.println(STATE_SIGNATURE + states.signature());
            B173PhysicsProbe.SlowBlocks slow = B173PhysicsProbe.slowBlocks(runtime, 8);
            B173PhysicsProbe.LadderClimb ladder = B173PhysicsProbe.ladder(runtime, 10);
            System.out.println("WORLDLINE_PHYSICS_TRACE=v2|slow=" + slow.trace()
                    + "|ladder=" + ladder.trace() + "|compass="
                    + B173CompassProbe.trace(runtime));
            System.out.println("WORLDLINE_BOUNDARIES=clock,input,rng,scheduler,filesystem,network,threading");
        } finally {
            runtime.close();
        }
        require(!runtime.timerThreadAlive(), "vanilla timer thread survived runtime close");
        verifyFilesystemFailure();
    }

    private static CanonicalStateTrace stateTrace() {
        return new CanonicalStateTrace(SEED, "clientTick", "worldTime", "rngSeed", "entities",
                "cloudTick", "guiTick", "rendererTick", "playerX", "playerY",
                "playerZ", "health", "slot", "block64", "block65");
    }

    private static void record(CanonicalStateTrace trace, String label, B173Observation value) {
        int[] blocks = value.blockColumn();
        trace.record(label, value.clientTick(), value.worldTime(), value.rngSeed(), value.entityCount(),
                value.cloudTick(), value.guiTick(), value.rendererTick(), value.playerXBits(),
                value.playerYBits(), value.playerZBits(), value.health(), value.selectedSlot(),
                blocks[0], blocks[1]);
    }

    private static void record(CanonicalTrace trace, String label, B173Observation value) {
        int[] blocks = value.blockColumn();
        trace.record(label, value.worldTime(), value.entityCount(),
                value.clientTick(), blocks[0], blocks[1]);
    }

    private static void verifyFilesystemFailure() {
        B173Runtime failing = B173Runtimes.create(SEED);
        failing.bootHeadless();
        try {
            failing.fileSystem().failNext("world.loadInfo");
            try {
                failing.loadWorld(WorldSource.at(Paths.get("memory", "failing-world")));
                throw new IllegalStateException("filesystem failure was not injected");
            } catch (IllegalStateException expected) {
                require(expected.getMessage().contains("injected filesystem failure"),
                        "unexpected filesystem failure: " + expected);
            }
        } finally {
            failing.close();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
