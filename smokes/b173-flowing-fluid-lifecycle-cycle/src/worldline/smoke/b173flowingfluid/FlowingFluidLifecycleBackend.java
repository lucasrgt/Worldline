package worldline.smoke.b173flowingfluid;

import java.nio.file.Paths;
import net.minecraft.src.Block;
import net.minecraft.src.World;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.MinecraftRuntime;
import worldline.api.WorldSource;
import worldline.kernel.ControlledMinecraftRuntime;
import worldline.testkit.FlowingFluidLifecycleObservation;
import worldline.testkit.FlowingFluidLifecycleScenario;
import worldline.testkit.FlowingFluidObservation;
import worldline.kernel.GameBackend;

/** Mapped official-server implementation of the public moving-fluid lifecycle scenario. */
public final class FlowingFluidLifecycleBackend
        implements GameBackend, FlowingFluidLifecycleScenario {
    static final long SEED = 17_320_110_707L;
    private World current;
    private boolean loaded;

    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override public void loadWorld(WorldSource source) {
        if (source == null) {
            throw new NullPointerException("flowing-fluid world source");
        }
        loaded = true;
    }

    @Override public void tick() {
        if (current == null) {
            throw new IllegalStateException("flowing-fluid world is absent");
        }
        current.tick();
    }

    @Override public FlowingFluidLifecycleObservation observe() {
        MinecraftRuntime runtime = new ControlledMinecraftRuntime(this);
        runtime.bootHeadless();
        try {
            runtime.loadWorld(WorldSource.at(Paths.get("memory", "flowing-fluid-lifecycle")));
            FlowingFluidObservation water = new FlowingFluidNativeEngine(this, runtime,
                    Block.waterMoving.blockID, Block.waterStill.blockID, 1, 5, 30).execute();
            FlowingFluidObservation lava = new FlowingFluidNativeEngine(this, runtime,
                    Block.lavaMoving.blockID, Block.lavaStill.blockID, 2, 30, -18).execute();
            return new FlowingFluidLifecycleObservation(water, lava, ReloadBoundary.CHUNK_RELOAD);
        } finally {
            runtime.close();
        }
    }

    void bind(World world) {
        if (!loaded) {
            throw new IllegalStateException("flowing-fluid runtime is not loaded");
        }
        current = world;
    }

    @Override public void close() {
        current = null;
        loaded = false;
    }
}
