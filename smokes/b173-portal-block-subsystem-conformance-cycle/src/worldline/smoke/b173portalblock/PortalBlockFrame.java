package worldline.smoke.b173portalblock;

import net.minecraft.src.Block;
import net.minecraft.src.World;

/** Builds and inspects canonical four-by-five portal frames in both axes. */
final class PortalBlockFrame {
    private PortalBlockFrame() { }
    static int buildX(World world, int x, int y, int z) {
        for (int dx = 0; dx < 4; dx++)
            for (int dy = 0; dy < 5; dy++)
                if (dx == 0 || dx == 3 || dy == 0 || dy == 4)
                    set(world, x + dx, y + dy, z, 49);
        require(Block.portal.tryToCreatePortal(world, x + 1, y + 1, z),
                "X portal frame did not materialize");
        return countX(world, x, y, z);
    }
    static int buildZ(World world, int x, int y, int z) {
        for (int dz = 0; dz < 4; dz++)
            for (int dy = 0; dy < 5; dy++)
                if (dz == 0 || dz == 3 || dy == 0 || dy == 4)
                    set(world, x, y + dy, z + dz, 49);
        require(Block.portal.tryToCreatePortal(world, x, y + 1, z + 1),
                "Z portal frame did not materialize");
        return countZ(world, x, y, z);
    }
    static int countX(World world, int x, int y, int z) {
        int count = 0;
        for (int dx = 1; dx <= 2; dx++)
            for (int dy = 1; dy <= 3; dy++)
                if (world.getBlockId(x + dx, y + dy, z) == 90) count++;
        return count;
    }
    static int countZ(World world, int x, int y, int z) {
        int count = 0;
        for (int dz = 1; dz <= 2; dz++)
            for (int dy = 1; dy <= 3; dy++)
                if (world.getBlockId(x, y + dy, z + dz) == 90) count++;
        return count;
    }
    static int metadataMaskX(World world, int x, int y, int z) {
        int mask = 0;
        for (int dx = 1; dx <= 2; dx++)
            for (int dy = 1; dy <= 3; dy++)
                mask |= 1 << world.getBlockMetadata(x + dx, y + dy, z);
        return mask;
    }
    static void collapseZ(World world, int x, int y, int z) {
        world.setBlockWithNotify(x, y + 2, z, 0);
        for (int dz = 1; dz <= 2; dz++)
            for (int dy = 1; dy <= 3; dy++)
                if (world.getBlockId(x, y + dy, z + dz) == 90)
                    Block.portal.onNeighborBlockChange(world, x, y + dy, z + dz, 49);
    }
    private static void set(World world, int x, int y, int z, int id) {
        require(world.setBlockAndMetadataWithNotify(x, y, z, id, 0),
                "portal frame write failed: " + x + "/" + y + "/" + z);
    }
    static int state(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) * 100 + world.getBlockMetadata(x, y, z);
    }
    static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
