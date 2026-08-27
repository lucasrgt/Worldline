package worldline.smoke.snowlayernonstackingb173;

import java.nio.file.Paths;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.trace.CanonicalTrace;

/** Runs paired wet and dry ambient schedulers to prove snow remains one layer. */
public final class SnowLayerNonstackingSmoke {
    private static final long SEED = 1772835215L;
    private static final int MAXIMUM_FORMATION_PASSES = 16;
    private static final int MAXIMUM_SETTLING_PASSES = 8;
    private static final int AIR = 0;
    private static final int SNOW = 78;
    private static final int STATE = 0;
    private static final int METADATA = 1;
    private static final int ABOVE = 2;
    private static final int ABOVE_METADATA = 3;
    private static final int COLD_BIOME = 4;
    private static final int RAINING = 5;
    private static final int BLOCK_LIGHT = 6;
    private static final int FOUND = 7;
    private static final int COLUMN_SNOW = 8;

    private SnowLayerNonstackingSmoke() {
    }

    public static void main(String[] arguments) {
        SnowLayerNonstackingBackend wet = new SnowLayerNonstackingBackend(SEED, true);
        SnowLayerNonstackingBackend dry = new SnowLayerNonstackingBackend(SEED, false);
        MinecraftRuntime wetRuntime = new ControlledMinecraftRuntime(wet);
        MinecraftRuntime dryRuntime = new ControlledMinecraftRuntime(dry);
        wetRuntime.bootHeadless();
        dryRuntime.bootHeadless();
        try {
            wetRuntime.loadWorld(WorldSource.at(Paths.get("memory", "snowfall")));
            dryRuntime.loadWorld(WorldSource.at(Paths.get("memory", "dry")));
            int[] wetState = wet.observation();
            int[] dryState = dry.observation();
            requireContext(wetState, dryState);
            requireUnformed(wetState);
            requireDry(dryState);
            int formationPass = 0;
            while (wetState[STATE] != SNOW
                    && formationPass < MAXIMUM_FORMATION_PASSES) {
                formationPass++;
                wetRuntime.tick();
                dryRuntime.tick();
                wetState = wet.observation();
                dryState = dry.observation();
                requireContext(wetState, dryState);
                requireDry(dryState);
                if (wetState[STATE] != SNOW) {
                    requireUnformed(wetState);
                }
            }
            requireSingleLayer(wetState);
            for (int settling = 0; settling < MAXIMUM_SETTLING_PASSES; settling++) {
                wetRuntime.tick();
                dryRuntime.tick();
                wetState = wet.observation();
                dryState = dry.observation();
                requireContext(wetState, dryState);
                requireSingleLayer(wetState);
                requireDry(dryState);
            }
            CanonicalTrace trace = new CanonicalTrace(SEED);
            trace.record("formed", 0L, 0, AIR, SNOW, 0, AIR, 1, 1,
                    MAXIMUM_FORMATION_PASSES, 0);
            trace.record("settled", 0L, 0, SNOW, 0, AIR, 1, 1,
                    MAXIMUM_SETTLING_PASSES, 0);
            trace.emitTo(System.out);
        } finally {
            wetRuntime.close();
            dryRuntime.close();
        }
    }

    private static void requireContext(int[] wet, int[] dry) {
        require(wet[COLD_BIOME] == 1 && dry[COLD_BIOME] == 1,
                "snow fixture left the cold biome");
        require(wet[BLOCK_LIGHT] < 10 && dry[BLOCK_LIGHT] < 10,
                "snow fixture crossed the block-light boundary");
    }

    private static void requireUnformed(int[] wet) {
        require(wet[STATE] == AIR && wet[METADATA] == 0
                        && wet[ABOVE] == AIR && wet[ABOVE_METADATA] == 0
                        && wet[COLUMN_SNOW] == 0,
                "snowfall cell changed before its first layer");
    }

    private static void requireSingleLayer(int[] wet) {
        require(wet[FOUND] == 1 && wet[STATE] == SNOW
                        && wet[METADATA] == 0 && wet[ABOVE] == AIR
                        && wet[ABOVE_METADATA] == 0 && wet[RAINING] == 1
                        && wet[COLUMN_SNOW] == 1,
                "snow layer stacked or lost its rain cause");
    }

    private static void requireDry(int[] dry) {
        require(dry[RAINING] == 0 && dry[STATE] == AIR
                        && dry[METADATA] == 0 && dry[ABOVE] == AIR
                        && dry[ABOVE_METADATA] == 0 && dry[COLUMN_SNOW] == 0,
                "dry control gained snow");
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
