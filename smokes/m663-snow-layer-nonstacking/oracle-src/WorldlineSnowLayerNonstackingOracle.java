import worldline.trace.CanonicalTrace;

/** Executes paired native snowfall and dry schedulers against the official JAR. */
public final class WorldlineSnowLayerNonstackingOracle {
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

    private WorldlineSnowLayerNonstackingOracle() {
    }

    public static void main(String[] arguments) {
        OracleSnowLayerWorld wet = world(true, "snowfall");
        OracleSnowLayerWorld dry = world(false, "dry");
        int[] wetState = wet.observation();
        int[] dryState = dry.observation();
        requireContext(wetState, dryState);
        requireUnformed(wetState);
        requireDry(dryState);
        int formationPass = 0;
        while (wetState[STATE] != SNOW
                && formationPass < MAXIMUM_FORMATION_PASSES) {
            formationPass++;
            wet.ambientPass();
            dry.ambientPass();
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
            wet.ambientPass();
            dry.ambientPass();
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
    }

    private static OracleSnowLayerWorld world(boolean snowfall, String name) {
        OracleSnowLayerWorld world = new OracleSnowLayerWorld(
                new OracleSnowMemorySaveHandler(SEED, name, snowfall), name, SEED, snowfall);
        world.prepare();
        world.r.setSeed(SEED);
        require(world.observation()[STATE] == AIR,
                "official fixture did not begin as air");
        return world;
    }

    private static void requireContext(int[] wet, int[] dry) {
        require(wet[COLD_BIOME] == 1 && dry[COLD_BIOME] == 1,
                "official snow fixture left the cold biome");
        require(wet[BLOCK_LIGHT] < 10 && dry[BLOCK_LIGHT] < 10,
                "official snow fixture crossed the block-light boundary");
    }

    private static void requireUnformed(int[] wet) {
        require(wet[STATE] == AIR && wet[METADATA] == 0
                        && wet[ABOVE] == AIR && wet[ABOVE_METADATA] == 0
                        && wet[COLUMN_SNOW] == 0,
                "official snowfall cell changed before its first layer");
    }

    private static void requireSingleLayer(int[] wet) {
        require(wet[FOUND] == 1 && wet[STATE] == SNOW
                        && wet[METADATA] == 0 && wet[ABOVE] == AIR
                        && wet[ABOVE_METADATA] == 0 && wet[RAINING] == 1
                        && wet[COLUMN_SNOW] == 1,
                "official snow layer stacked or lost its rain cause");
    }

    private static void requireDry(int[] dry) {
        require(dry[RAINING] == 0 && dry[STATE] == AIR
                        && dry[METADATA] == 0 && dry[ABOVE] == AIR
                        && dry[ABOVE_METADATA] == 0 && dry[COLUMN_SNOW] == 0,
                "official dry control gained snow");
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
