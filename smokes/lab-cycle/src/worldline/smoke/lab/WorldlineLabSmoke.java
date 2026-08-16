package worldline.smoke.lab;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import worldline.api.WorldSource;
import worldline.b173.B173Checkpoint;
import worldline.b173.B173Comparison;
import worldline.b173.B173Experiments;
import worldline.b173.B173Keys;
import worldline.b173.B173Mod;
import worldline.b173.B173Observation;
import worldline.b173.B173Runtime;
import worldline.b173.B173Runtimes;
import worldline.trace.CanonicalStateTrace;

/** Proves replay, branches, semantic GUI control, and isolated mod loading. */
public final class WorldlineLabSmoke {
    private static final long SEED = 17320110707L;
    private static final long RNG_SEED = 2026071501L;
    private static final WorldSource WORLD = WorldSource.at(Paths.get("memory", "lab-cycle"));

    private WorldlineLabSmoke() {}

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 1, "expected the benchmark mod JAR path");
        B173Checkpoint checkpoint = checkpoint();
        B173Observation restored = restored(checkpoint);
        B173Observation replayed = restored(checkpoint);
        require(restored.fingerprint().equals(replayed.fingerprint()),
                "fresh restore and replay diverged");
        B173Comparison branches = B173Experiments.compare(checkpoint, runtime -> {},
                runtime -> runtime.tap(B173Keys.SLOT_1 + 4), 1);
        require(branches.diverged() && branches.baseline().selectedSlot() == 2
                && branches.alternative().selectedSlot() == 4,
                "hypothesis branches did not isolate the intervention");
        int slots = gui(checkpoint);
        int[] blocks = mod(checkpoint, Paths.get(arguments[0]));
        CanonicalStateTrace trace = new CanonicalStateTrace(SEED, "checkpointTick", "events",
                "restoreSlot", "baselineSlot", "alternativeSlot", "guiSlots",
                "baseBlock65", "modBlock65");
        trace.record("lab", checkpoint.tick(), checkpoint.eventCount(), restored.selectedSlot(),
                branches.baseline().selectedSlot(), branches.alternative().selectedSlot(), slots,
                blocks[0], blocks[1]);
        System.out.println("WORLDLINE_LAB_TRACE=" + trace.value());
        System.out.println("WORLDLINE_LAB_SIGNATURE=" + trace.signature());
        System.out.println("WORLDLINE_LAB_CAPABILITIES=snapshot,restore,replay,branch,gui,mod");
        System.out.println("WORLDLINE_MOD_SOURCE=probe-mod.jar");
    }

    private static B173Checkpoint checkpoint() {
        B173Runtime runtime = B173Runtimes.create(SEED);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WORLD);
            runtime.reseed(RNG_SEED);
            runtime.scheduler().afterTicks(2, () -> runtime.tap(B173Keys.SLOT_1 + 2));
            runtime.tick(4);
            require(runtime.scheduler().pendingActions() == 0, "checkpoint scheduler is not drained");
            return runtime.checkpoint();
        } finally {
            runtime.close();
        }
    }

    private static B173Observation restored(B173Checkpoint checkpoint) {
        B173Runtime runtime = checkpoint.restore();
        try {
            require(runtime.observe().fingerprint().equals(checkpoint.stateFingerprint()),
                    "restored state does not equal checkpoint");
            return runtime.observe();
        } finally {
            runtime.close();
        }
    }

    private static int gui(B173Checkpoint checkpoint) {
        B173Runtime runtime = checkpoint.restore();
        try {
            runtime.gui().openInventory();
            runtime.tick();
            runtime.gui().screen("GuiInventory");
            int count = runtime.gui().slotCount();
            require(count > 0 && runtime.gui().slot(0).index() == 0, "slot selector failed");
            runtime.gui().clickSlot(0, 0);
            runtime.gui().closeScreen();
            runtime.tick();
            require(runtime.gui().screenName().isEmpty(), "semantic close action failed");
            return count;
        } finally {
            runtime.close();
        }
    }

    private static int[] mod(B173Checkpoint checkpoint, Path jar) throws Exception {
        int base;
        B173Runtime baseline = checkpoint.restore();
        try { baseline.tick(); base = baseline.observe().blockColumn()[1]; }
        finally { baseline.close(); }
        URL url = jar.toUri().toURL();
        try (URLClassLoader loader = new URLClassLoader(new URL[] {url}, B173Mod.class.getClassLoader())) {
            Class<? extends B173Mod> type = Class.forName("worldline.benchmark.ProbeMod", true, loader)
                    .asSubclass(B173Mod.class);
            require(type.getProtectionDomain().getCodeSource().getLocation().equals(url),
                    "benchmark mod was not loaded from its isolated JAR");
            B173Runtime modified = checkpoint.restore();
            try {
                modified.installMod(type.getDeclaredConstructor().newInstance());
                modified.tick();
                return new int[] {base, modified.observe().blockColumn()[1]};
            } finally { modified.close(); }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
