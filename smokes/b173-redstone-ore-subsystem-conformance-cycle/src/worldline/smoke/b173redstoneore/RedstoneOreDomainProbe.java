package worldline.smoke.b173redstoneore;

import java.util.Random;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

/** Proves registry identity, activation, random-tick fade, and neighbor stability. */
final class RedstoneOreDomainProbe {
    final int registryMask, activationBefore, activationAfter;
    final int randomMask, fadeBefore, fadeAfter, unlitNeighbors, glowingNeighbors;
    private RedstoneOreDomainProbe(int registryMask, int activationBefore, int activationAfter,
            int randomMask, int fadeBefore, int fadeAfter, int unlitNeighbors,
            int glowingNeighbors) {
        this.registryMask = registryMask;
        this.activationBefore = activationBefore;
        this.activationAfter = activationAfter;
        this.randomMask = randomMask;
        this.fadeBefore = fadeBefore;
        this.fadeAfter = fadeAfter;
        this.unlitNeighbors = unlitNeighbors;
        this.glowingNeighbors = glowingNeighbors;
    }
    static RedstoneOreDomainProbe execute(World world) {
        int registry = (Block.oreRedstone.blockID == 73 ? 1 : 0)
                | (Block.oreRedstoneGlowing.blockID == 74 ? 2 : 0)
                | (Block.oreRedstone.getClass() == Block.oreRedstoneGlowing.getClass() ? 4 : 0);
        int x = 20, y = 88, z = 20;
        place(world, x, y, z, 73);
        int activationBefore = state(world, x, y, z);
        EntityPlayer player = new EntityPlayer(world) { };
        player.setLocationAndAngles(x + 0.5D, y + 1D, z + 0.5D, 0F, 0F);
        Block.oreRedstone.onBlockClicked(world, x, y, z, player);
        int activationAfter = state(world, x, y, z);

        int fadeX = 24;
        place(world, fadeX, y, z, 74);
        int fadeBefore = state(world, fadeX, y, z);
        Block.oreRedstoneGlowing.updateTick(world, fadeX, y, z,
                new Random(17320110707L));
        int fadeAfter = state(world, fadeX, y, z);

        int unlitX = 28, glowingX = 32;
        place(world, unlitX, y, z, 73);
        place(world, glowingX, y, z, 74);
        Block.oreRedstone.onNeighborBlockChange(world, unlitX, y, z, 1);
        Block.oreRedstone.onNeighborBlockChange(world, unlitX, y, z, 69);
        Block.oreRedstoneGlowing.onNeighborBlockChange(world, glowingX, y, z, 1);
        Block.oreRedstoneGlowing.onNeighborBlockChange(world, glowingX, y, z, 69);
        int random = (Block.tickOnLoad[73] ? 1 : 0) | (Block.tickOnLoad[74] ? 2 : 0);
        RedstoneOreDomainProbe result = new RedstoneOreDomainProbe(registry,
                activationBefore, activationAfter, random, fadeBefore, fadeAfter,
                state(world, unlitX, y, z), state(world, glowingX, y, z));
        result.validate();
        return result;
    }
    String registry() { return "73+74=same-BlockRedstoneOre"; }
    String domains() { return "73=0,74=0,activate=73:0->74:0"; }
    String timing() { return "random=FT,activate=click,fade=74:0->73:0"; }
    String neighbors() { return "73:0+74:0=stable@1+69"; }
    private void validate() {
        require(registryMask == 7, "redstone ore registry drifted");
        require(activationBefore == 7300 && activationAfter == 7400,
                "redstone ore activation drifted");
        require(randomMask == 2 && fadeBefore == 7400 && fadeAfter == 7300,
                "redstone ore random tick drifted");
        require(unlitNeighbors == 7300 && glowingNeighbors == 7400,
                "redstone ore neighbor stability drifted");
    }
    private static void place(World world, int x, int y, int z, int id) {
        require(world.setBlockAndMetadataWithNotify(x, y, z, id, 0),
                "redstone ore placement failed: " + id);
    }
    static int state(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) * 100 + world.getBlockMetadata(x, y, z);
    }
    static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
