package worldline.testapi;

import java.util.Objects;
import worldline.api.BlockState;

/** Exact state and independently optional block/sky-light planes at one cell. */
public final class BlockLightExpectation {
    public static final int ANY_LIGHT = -1;
    private final BlockState state;
    private final int blockLight, skyLight;

    public BlockLightExpectation(BlockState state, int blockLight, int skyLight) {
        this.state = Objects.requireNonNull(state, "state");
        requireLight(blockLight); requireLight(skyLight);
        this.blockLight = blockLight; this.skyLight = skyLight;
    }

    public BlockState state() { return state; }
    public int blockLight() { return blockLight; }
    public int skyLight() { return skyLight; }

    private static void requireLight(int value) {
        if (value < ANY_LIGHT || value > 15) throw new IllegalArgumentException("invalid light level");
    }
}
