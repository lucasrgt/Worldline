package worldline.smoke.b173piston;

import net.minecraft.src.Block;
import net.minecraft.src.PistonBlockTextures;
import net.minecraft.src.World;

/** Proves every reachable base, head, and moving metadata state through piston events. */
final class PistonDomainProbe {
    final int baseMask, headMask, movingMask;
    final int normalMoving, normalHead, stickyMoving, stickyHead;

    private PistonDomainProbe(int baseMask, int headMask, int movingMask,
            int normalMoving, int normalHead, int stickyMoving, int stickyHead) {
        this.baseMask = baseMask;
        this.headMask = headMask;
        this.movingMask = movingMask;
        this.normalMoving = normalMoving;
        this.normalHead = normalHead;
        this.stickyMoving = stickyMoving;
        this.stickyHead = stickyHead;
    }

    static PistonDomainProbe execute(World world) {
        int bases = 0, heads = 0, moving = 0;
        int normalMoving = 0, normalHead = 0, stickyMoving = 0, stickyHead = 0;
        for (int kind = 0; kind < 2; kind++) {
            int id = kind == 0 ? 33 : 29;
            for (int direction = 0; direction < 6; direction++) {
                int index = kind * 6 + direction;
                int x = -24 + direction * 8, y = 80, z = -16 + kind * 16;
                int frontX = x + PistonBlockTextures.field_31051_b[direction];
                int frontY = y + PistonBlockTextures.field_31054_c[direction];
                int frontZ = z + PistonBlockTextures.field_31053_d[direction];
                require(world.setBlockAndMetadataWithNotify(x, y, z, id, direction),
                        "base placement failed " + index);
                require(state(world, x, y, z) == id * 100 + direction,
                        "unextended state drifted " + index);
                bases |= 1 << direction;
                Block.blocksList[id].playBlock(world, x, y, z, 0, direction);
                int movingMetadata = direction | (kind == 1 ? 8 : 0);
                require(state(world, x, y, z) == id * 100 + (direction | 8),
                        "extended base drifted " + index);
                require(state(world, frontX, frontY, frontZ) == 3600 + movingMetadata,
                        "moving state drifted " + index);
                net.minecraft.src.TileEntityPiston movingTile =
                        (net.minecraft.src.TileEntityPiston) world.getBlockTileEntity(
                                frontX, frontY, frontZ);
                require(movingTile != null && movingTile.getStoredBlockID() == 34
                        && movingTile.func_31005_e() == movingMetadata,
                        "moving payload drifted " + index + ": "
                                + (movingTile == null ? "null" : movingTile.getStoredBlockID()
                                        + ":" + movingTile.func_31005_e()));
                bases |= 1 << (direction | 8);
                moving |= 1 << movingMetadata;
                if (direction == 5 && kind == 0)
                    normalMoving = state(world, frontX, frontY, frontZ);
                if (direction == 5 && kind == 1)
                    stickyMoving = state(world, frontX, frontY, frontZ);
                require(settle(world, frontX, frontY, frontZ) == 3,
                        "moving tile duration drifted " + index);
                int settled = state(world, frontX, frontY, frontZ);
                require(settled == 3400 + movingMetadata,
                        "head state drifted " + index + ": " + settled);
                heads |= 1 << movingMetadata;
                if (direction == 5 && kind == 0)
                    normalHead = state(world, frontX, frontY, frontZ);
                if (direction == 5 && kind == 1)
                    stickyHead = state(world, frontX, frontY, frontZ);
            }
        }
        int expected = rangeMask();
        require(bases == expected && heads == expected && moving == expected,
                "piston metadata domain is incomplete");
        require(Block.pistonExtension.quantityDropped(world.rand) == 0
                && Block.pistonMoving.idDropped(0, world.rand) == 0,
                "internal piston item route drifted");
        return new PistonDomainProbe(bases, heads, moving,
                normalMoving, normalHead, stickyMoving, stickyHead);
    }

    String domains() {
        return "29=0..5+8..13,33=0..5+8..13,34=0..5+8..13,36=0..5+8..13";
    }
    String materialization() {
        require(normalMoving == 3605 && normalHead == 3405
                && stickyMoving == 3613 && stickyHead == 3413,
                "canonical materialization drifted");
        return "normal=33:5>36:5>34:5,sticky=29:5>36:13>34:13,items=34:none+36:none";
    }

    private static int rangeMask() {
        int value = 0;
        for (int direction = 0; direction < 6; direction++)
            value |= 1 << direction | 1 << (direction | 8);
        return value;
    }
    static int state(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) * 100 + world.getBlockMetadata(x, y, z);
    }
    static int settle(World world, int x, int y, int z) {
        int steps = 0;
        boolean prior = world.singleplayerWorld;
        world.singleplayerWorld = true;
        try {
            while (world.getBlockId(x, y, z) == 36 && steps < 5) {
                net.minecraft.src.TileEntityPiston tile = (net.minecraft.src.TileEntityPiston)
                        world.getBlockTileEntity(x, y, z);
                require(tile != null, "moving piston tile is absent");
                tile.updateEntity();
                steps++;
            }
        } finally {
            world.singleplayerWorld = prior;
        }
        return steps;
    }
    static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
}
