package worldline.smoke.minecartboosterb173;

import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.b173server.B173FixtureSupport;
import worldline.b173server.B173WireClient;

/** Raised parallel north-south rails with one powered driver lane. */
final class MinecartBoosterArm {
    private static final int LENGTH = 4;
    final BlockPosition[] driver = new BlockPosition[LENGTH];
    final BlockPosition[] booster = new BlockPosition[LENGTH];
    final int column;

    private MinecartBoosterArm(BlockPosition[] driverPads, BlockPosition[] boosterPads,
            int column) {
        for (int index = 0; index < LENGTH; index++) {
            driver[index] = BlockFace.UP.adjacent(driverPads[index]);
            booster[index] = BlockFace.UP.adjacent(boosterPads[index]);
        }
        this.column = column;
    }

    static MinecartBoosterArm build(B173WireClient actor, RemoteChunkSnapshot initial,
            int chunkX, int chunkZ) throws Exception {
        BlockPosition top = foundation(initial, chunkX, chunkZ);
        int column = 0;
        actor.selectHeldSlot(0);
        while (B173FixtureSupport.water(at(initial,
                BlockFace.UP.adjacent(top), chunkX, chunkZ).legacyId())) {
            top = B173FixtureSupport.place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            require(++column <= 15, "water column exceeded minecart booster fixture");
        }
        for (int lift = 0; lift < 8; lift++) {
            top = B173FixtureSupport.place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
            column++;
        }
        BlockPosition[] driverPads = lane(actor, top);
        BlockPosition[] boosterPads = lane(actor,
                B173FixtureSupport.place(actor, top, BlockFace.EAST, 1));
        wall(actor, driverPads[0]);
        wall(actor, boosterPads[0]);
        bumper(actor, driverPads[LENGTH - 1]);
        bumper(actor, boosterPads[LENGTH - 1]);
        MinecartBoosterArm arm = new MinecartBoosterArm(driverPads, boosterPads, column);
        arm.rails(actor, driverPads, boosterPads);
        return arm;
    }

    private void rails(B173WireClient actor, BlockPosition[] driverPads,
            BlockPosition[] boosterPads) throws Exception {
        actor.selectHeldSlot(1);
        placeRailLine(actor, driverPads);
        placeRailLine(actor, boosterPads);
        actor.awaitBlock(driver[1], new BlockState(66, 0));
        actor.awaitBlock(driver[2], new BlockState(66, 0));
        actor.awaitBlock(booster[1], new BlockState(66, 0));
        actor.awaitBlock(booster[2], new BlockState(66, 0));
        require(driver[1].x() + 1 == booster[1].x()
                && driver[1].z() == booster[1].z(), "parallel rail origin drift");
    }

    private static void placeRailLine(B173WireClient actor, BlockPosition[] pads) throws Exception {
        int[] order = {0, 2, 3, 1};
        for (int index : order) actor.placeHeldBlock(pads[index], BlockFace.UP);
    }

    private static BlockPosition[] lane(B173WireClient actor, BlockPosition start) throws Exception {
        BlockPosition[] pads = new BlockPosition[LENGTH];
        pads[0] = start;
        for (int index = 1; index < LENGTH; index++)
            pads[index] = B173FixtureSupport.place(actor, pads[index - 1], BlockFace.SOUTH, 1);
        return pads;
    }

    private static void wall(B173WireClient actor, BlockPosition start) throws Exception {
        BlockPosition north = B173FixtureSupport.place(actor, start, BlockFace.NORTH, 1);
        B173FixtureSupport.place(actor, north, BlockFace.UP, 1);
    }
    private static void bumper(B173WireClient actor, BlockPosition end) throws Exception {
        BlockPosition south = B173FixtureSupport.place(actor, end, BlockFace.SOUTH, 1);
        B173FixtureSupport.place(actor, south, BlockFace.UP, 1);
    }

    private static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
        for (int x = 4; x <= 8; x++) for (int z = 4; z <= 8; z++)
            for (int y = 126; y >= 1; y--)
                if (chunk.blockAt(x, y, z).legacyId() == 3
                        && B173FixtureSupport.water(chunk.blockAt(x, y + 1, z).legacyId()))
                    return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
        throw new IllegalStateException("no deterministic minecart booster foundation");
    }
    private static BlockState at(RemoteChunkSnapshot chunk, BlockPosition position,
            int chunkX, int chunkZ) {
        return chunk.blockAt(B173FixtureSupport.local(position.x(), chunkX), position.y(),
                B173FixtureSupport.local(position.z(), chunkZ));
    }
    static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
