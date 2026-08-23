package worldline.aero.mixin;

/** Immutable system-property contract for a Worldline capture run. */
final class WorldlineCaptureSettings {
    static final boolean ENABLED = Boolean.getBoolean("worldline.capture.enabled");
    static final long SEED = Long.getLong("worldline.capture.seed", 17320110707L);
    static final int TICKS = Integer.getInteger("worldline.capture.ticks", 240);
    static final int Y = Integer.getInteger("worldline.capture.y", 67);
    static final int MIN_BLOCK_ENTITIES = Integer.getInteger(
            "worldline.capture.minBlockEntities", 500);
    static final int MIN_WARMUP_TICKS = Integer.getInteger(
            "worldline.capture.minWarmupTicks", 0);
    static final String PATH = System.getProperty("worldline.capture.path", "stationary");
    static final int VIEW_DISTANCE = Integer.getInteger("worldline.capture.viewDistance", -1);
    static final boolean STABILIZE_SCENE = Boolean.getBoolean(
            "worldline.frameOracle.stabilizeScene");
    static final int SAVE_TICK = Integer.getInteger("worldline.capture.saveTick", -1);
    static final int DIRTY_TICK = Integer.getInteger("worldline.capture.dirtyTick", -1);
    static final int DIRTY_CHUNKS = Integer.getInteger("worldline.capture.dirtyChunks", 0);

    private WorldlineCaptureSettings() { }
}
