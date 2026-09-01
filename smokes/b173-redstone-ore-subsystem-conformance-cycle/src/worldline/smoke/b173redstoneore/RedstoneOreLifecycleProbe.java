package worldline.smoke.b173redstoneore;

import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkLoader;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

/** Proves glowing lifecycle, both-state persistence, collision, and light. */
final class RedstoneOreLifecycleProbe {
    final int breakAfter, dropCount, dropItem, savedUnlit, savedGlowing;
    final int unlitCollision, glowingCollision, lightCode;
    private RedstoneOreLifecycleProbe(int breakAfter, int dropCount, int dropItem,
            int savedUnlit, int savedGlowing, int unlitCollision, int glowingCollision,
            int lightCode) {
        this.breakAfter = breakAfter;
        this.dropCount = dropCount;
        this.dropItem = dropItem;
        this.savedUnlit = savedUnlit;
        this.savedGlowing = savedGlowing;
        this.unlitCollision = unlitCollision;
        this.glowingCollision = glowingCollision;
        this.lightCode = lightCode;
    }
    static RedstoneOreLifecycleProbe execute(World world) {
        int y = 92, z = 36;
        place(world, 36, y, z, 73);
        place(world, 40, y, z, 74);
        NBTTagCompound tag = new NBTTagCompound();
        ChunkLoader.storeChunkInCompound(world.getChunkFromChunkCoords(2, 2), world, tag);
        Chunk loaded = ChunkLoader.loadChunkIntoWorldFromCompound(world, tag);
        int savedUnlit = loaded.getBlockID(4, y, 4) * 100
                + loaded.getBlockMetadata(4, y, 4);
        int savedGlowing = loaded.getBlockID(8, y, 4) * 100
                + loaded.getBlockMetadata(8, y, 4);

        int x = 44;
        place(world, x, y, z, 74);
        int before = world.loadedEntityList.size();
        world.rand.setSeed(17320110707L);
        Block.oreRedstoneGlowing.dropBlockAsItemWithChance(world, x, y, z, 0, 1.0F);
        world.setBlockWithNotify(x, y, z, 0);
        int[] drops = drops(world, before);
        int unlitCollision = Block.oreRedstone.getCollisionBoundingBoxFromPool(
                world, x, y, z) == null ? 0 : 1;
        int glowingCollision = Block.oreRedstoneGlowing.getCollisionBoundingBoxFromPool(
                world, x, y, z) == null ? 0 : 1;
        int light = Block.lightOpacity[73] * 100000 + Block.lightValue[73] * 1000
                + Block.lightOpacity[74] * 100 + Block.lightValue[74];
        RedstoneOreLifecycleProbe result = new RedstoneOreLifecycleProbe(
                RedstoneOreDomainProbe.state(world, x, y, z), drops[0], drops[1],
                savedUnlit, savedGlowing, unlitCollision, glowingCollision, light);
        result.validate();
        return result;
    }
    String lifecycle() {
        return "break=74:0->0:0,drop=331x4..5:0,saved=73:0+74:0";
    }
    String physics() {
        return "collision=73:full+74:full,light=73:255:0+74:255:9";
    }
    private void validate() {
        RedstoneOreDomainProbe.require(breakAfter == 0,
                "glowing redstone ore break drifted");
        RedstoneOreDomainProbe.require(dropCount >= 4 && dropCount <= 5 && dropItem == 331,
                "glowing redstone ore drop drifted: " + dropCount + "/" + dropItem);
        RedstoneOreDomainProbe.require(savedUnlit == 7300 && savedGlowing == 7400,
                "redstone ore chunk round trip drifted");
        RedstoneOreDomainProbe.require(unlitCollision == 1 && glowingCollision == 1,
                "redstone ore collision drifted");
        RedstoneOreDomainProbe.require(lightCode == 25525509,
                "redstone ore light drifted: " + lightCode);
        RedstoneOreDomainProbe.require(Block.oreRedstoneGlowing.idDropped(
                0, new java.util.Random(17320110707L)) == 331,
                "glowing redstone ore item route drifted");
    }
    private static void place(World world, int x, int y, int z, int id) {
        RedstoneOreDomainProbe.require(world.setBlockAndMetadataWithNotify(x, y, z, id, 0),
                "redstone ore lifecycle placement failed: " + id);
    }
    private static int[] drops(World world, int index) {
        int count = 0, item = 0;
        for (int current = index; current < world.loadedEntityList.size(); current++) {
            Entity entity = (Entity) world.loadedEntityList.get(current);
            if (entity instanceof EntityItem) {
                ItemStack stack = ((EntityItem) entity).item;
                count += stack.stackSize;
                item = item == 0 ? stack.itemID : item;
                RedstoneOreDomainProbe.require(item == stack.itemID && stack.getItemDamage() == 0,
                        "mixed glowing redstone ore drops");
            }
        }
        return new int[] {count, item};
    }
}
