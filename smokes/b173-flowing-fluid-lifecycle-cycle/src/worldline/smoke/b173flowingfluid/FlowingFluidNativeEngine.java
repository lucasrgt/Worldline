package worldline.smoke.b173flowingfluid;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import net.minecraft.src.Block;
import net.minecraft.src.EnumSkyBlock;
import net.minecraft.src.World;
import worldline.api.BlockState;
import worldline.api.MinecraftRuntime;
import worldline.testkit.FlowingFluidObservation;

/** Exercises one moving-fluid block through native generation and reload. */
final class FlowingFluidNativeEngine {
    private static final int SOURCE_X = 4, Y = 65;
    private final FlowingFluidLifecycleBackend backend;
    private final MinecraftRuntime runtime;
    private final int moving, still, step, delay, gateZ;
    private final FlowingFluidMemorySaveHandler saves;
    private World world;

    FlowingFluidNativeEngine(FlowingFluidLifecycleBackend backend, MinecraftRuntime runtime,
            int moving, int still, int step, int delay, int gateZ) {
        this.backend = backend;
        this.runtime = runtime;
        this.moving = moving;
        this.still = still;
        this.step = step;
        this.delay = delay;
        this.gateZ = gateZ;
        this.saves = new FlowingFluidMemorySaveHandler(FlowingFluidLifecycleBackend.SEED,
                "flowing-fluid-" + moving);
    }

    FlowingFluidObservation execute() {
        world = open();
        TreeSet<Integer> domain = prepareCascades();
        prepareGate();
        for (int tick = 1; tick <= 240; tick++) {
            runtime.tick();
            scan(domain);
        }
        BlockState blocked = state(SOURCE_X, gateZ);
        require(blocked.equals(new BlockState(still, 0)), "blocked fluid did not settle");
        require(world.setBlockWithNotify(SOURCE_X + 1, Y, gateZ, 0),
                "fluid gate did not open");
        BlockState recomputed = state(SOURCE_X, gateZ);
        int first = awaitTarget();
        BlockState saved = state(SOURCE_X + 1, gateZ);
        drainLight();
        boolean passable = Block.blocksList[moving]
                .getCollisionBoundingBoxFromPool(world, SOURCE_X + 1, Y, gateZ) == null;
        int opacity = Block.lightOpacity[moving], emission = Block.lightValue[moving];
        int blockLight = world.getSavedLightValue(
                EnumSkyBlock.Block, SOURCE_X + 1, Y, gateZ);
        int skyLight = world.getSavedLightValue(EnumSkyBlock.Sky, SOURCE_X + 1, Y, gateZ);
        world.saveWorld(true, null);
        world = open();
        BlockState reloaded = state(SOURCE_X + 1, gateZ);
        backend.bind(world);
        return new FlowingFluidObservation(moving, new ArrayList<Integer>(domain), first,
                blocked, recomputed, passable, opacity, emission, blockLight,
                skyLight, saved, reloaded);
    }

    private World open() {
        World result = new World(saves, "flowing-fluid-" + moving,
                FlowingFluidLifecycleBackend.SEED, null);
        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                result.getChunkFromChunkCoords(chunkX, chunkZ);
            }
        }
        backend.bind(result);
        return result;
    }

    private TreeSet<Integer> prepareCascades() {
        TreeSet<Integer> domain = new TreeSet<Integer>();
        for (int metadata = 0; metadata <= 7; metadata += step) {
            int distance = metadata / step;
            int z = moving == 8 ? 3 + distance * 3 : -3 - distance * 3;
            trench(z, SOURCE_X + distance);
            require(world.setBlockAndMetadata(SOURCE_X, Y, z, moving, 0),
                    "non-notifying moving-fluid seed failed");
            require(state(SOURCE_X, z).equals(new BlockState(moving, 0)),
                    "moving-fluid seed changed before observation");
            domain.add(0);
            world.scheduleUpdateTick(SOURCE_X, Y, z, moving, delay);
        }
        return domain;
    }

    private void prepareGate() {
        trench(gateZ, -1);
        require(world.setBlockAndMetadata(SOURCE_X, Y, gateZ, still, 0),
                "still-fluid gate source failed");
        require(world.setBlockAndMetadata(SOURCE_X + 1, Y, gateZ,
                Block.stone.blockID, 0), "fluid gate placement failed");
    }

    private void trench(int z, int holeX) {
        for (int x = SOURCE_X - 1; x <= SOURCE_X + 9; x++) {
            world.setBlockAndMetadata(x, Y, z, 0, 0);
            world.setBlockAndMetadata(x, Y, z - 1, Block.stone.blockID, 0);
            world.setBlockAndMetadata(x, Y, z + 1, Block.stone.blockID, 0);
            world.setBlockAndMetadata(x, Y - 1, z, Block.stone.blockID, 0);
        }
        world.setBlockAndMetadata(SOURCE_X - 1, Y, z, Block.stone.blockID, 0);
        world.setBlockAndMetadata(SOURCE_X + 9, Y, z, Block.stone.blockID, 0);
        if (holeX >= SOURCE_X) {
            world.setBlockAndMetadata(holeX, Y - 1, z, 0, 0);
        }
    }

    private void scan(TreeSet<Integer> domain) {
        int lanes = moving == 8 ? 8 : 4;
        for (int lane = 0; lane < lanes; lane++) {
            int z = moving == 8 ? 3 + lane * 3 : -3 - lane * 3;
            for (int x = SOURCE_X; x <= SOURCE_X + 8; x++) {
                collect(domain, x, Y, z);
                collect(domain, x, Y - 1, z);
            }
        }
    }

    private void collect(TreeSet<Integer> domain, int x, int y, int z) {
        if (world.getBlockId(x, y, z) == moving) {
            domain.add(world.getBlockMetadata(x, y, z));
        }
    }

    private int awaitTarget() {
        for (int tick = 1; tick <= delay + 2; tick++) {
            runtime.tick();
            if (world.getBlockId(SOURCE_X + 1, Y, gateZ) == moving) {
                return tick;
            }
        }
        throw new IllegalStateException("moving-fluid target did not enter native scheduler");
    }

    private void drainLight() {
        int passes = 0;
        while (world.func_6156_d()) {
            require(++passes <= 128, "fluid lighting queue did not drain");
        }
    }

    private BlockState state(int x, int z) {
        return new BlockState(world.getBlockId(x, Y, z), world.getBlockMetadata(x, Y, z));
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
