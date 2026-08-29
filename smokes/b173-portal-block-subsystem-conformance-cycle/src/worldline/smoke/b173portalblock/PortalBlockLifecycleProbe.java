package worldline.smoke.b173portalblock;

import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

/** Proves portal destruction, empty drops, persistence, collision, and light. */
final class PortalBlockLifecycleProbe {
    final int breakAfter, dropCount, savedCount, savedStateSum, collision, lightCode;
    private PortalBlockLifecycleProbe(int breakAfter, int dropCount, int savedCount,
            int savedStateSum, int collision, int lightCode) {
        this.breakAfter = breakAfter;
        this.dropCount = dropCount;
        this.savedCount = savedCount;
        this.savedStateSum = savedStateSum;
        this.collision = collision;
        this.lightCode = lightCode;
    }
    static PortalBlockLifecycleProbe execute(World world) {
        PortalBlockFrame.buildX(world, 36, 90, 36);
        NBTTagCompound tag = new NBTTagCompound();
        ChunkLoader.storeChunkInCompound(world.getChunkFromChunkCoords(2, 2), world, tag);
        Chunk loaded = ChunkLoader.loadChunkIntoWorldFromCompound(world, tag);
        int saved = 0, sum = 0;
        for (int x = 5; x <= 6; x++)
            for (int y = 91; y <= 93; y++) {
                int state = loaded.getBlockID(x, y, 4) * 100 + loaded.getBlockMetadata(x, y, 4);
                if (state == 9000) saved++;
                sum += state;
            }
        PortalBlockFrame.buildX(world, 44, 90, 36);
        int before = world.loadedEntityList.size();
        Block.portal.dropBlockAsItemWithChance(world, 45, 91, 36, 0, 1.0F);
        world.setBlockWithNotify(45, 91, 36, 0);
        int collision = Block.portal.getCollisionBoundingBoxFromPool(
                world, 37, 91, 36) == null ? 0 : 1;
        int light = Block.lightOpacity[90] * 100 + Block.lightValue[90];
        PortalBlockLifecycleProbe result = new PortalBlockLifecycleProbe(
                PortalBlockFrame.state(world, 45, 91, 36),
                world.loadedEntityList.size() - before, saved, sum, collision, light);
        result.validate();
        return result;
    }
    String lifecycle() { return "break=90:0->0:0,drop=none"; }
    String persistence() { return "chunk-nbt=6x90:0"; }
    String physics() { return "collision=none,light=0:11"; }
    private void validate() {
        PortalBlockFrame.require(breakAfter == 0 && dropCount == 0,
                "portal lifecycle or drop drifted");
        PortalBlockFrame.require(savedCount == 6 && savedStateSum == 54000,
                "portal chunk round trip drifted");
        PortalBlockFrame.require(collision == 0 && lightCode == 11,
                "portal collision or light drifted: " + collision + "/" + lightCode);
    }
}
