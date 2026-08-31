package worldline.m783;

import aero.modellib.test.WorldlineM783Rehydrator;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ClientPlayerEntity;

/** Repeated walk, turn, teleport, mutation, and settle route. */
final class VisualScene {
    private VisualScene() {}

    static void place(ClientPlayerEntity player, int frame) {
        int cycle = ((frame - 1) % 600) + 1;
        double x = 8.5D, z = 8.5D;
        float yaw = 0.0F;
        if (cycle > 100 && cycle <= 220) z = 8.5D + (cycle - 100) * 0.5D;
        else if (cycle > 220 && cycle <= 340) {
            z = 68.5D;
            yaw = (float) ((cycle - 220) * 6 % 360);
        } else if (cycle > 340 && cycle <= 460) {
            x = z = 72.5D;
            yaw = (float) ((cycle - 340) * 9 % 360);
        } else if (cycle > 460 && cycle <= 560) {
            x = Math.max(8.5D, 72.5D - (cycle - 460) * 0.64D);
            z = 72.5D;
            yaw = 90.0F;
        }
        player.velocityX = player.velocityY = player.velocityZ = 0.0D;
        player.setPositionAndAngles(x, 100.0D, z, yaw, 4.0F);
    }

    static void act(Minecraft game, int frame) {
        int cycle = ((frame - 1) % 600) + 1;
        if (cycle == 150) WorldlineM783Rehydrator.mutation(game.world, false);
        if (cycle == 180) WorldlineM783Rehydrator.mutation(game.world, true);
        if (cycle > 540 || cycle % 8 != 1) return;
        int chunkX = floor(game.player.x) >> 4;
        int chunkZ = floor(game.player.z) >> 4;
        double radians = Math.toRadians(game.player.yaw);
        int forwardX = (int) Math.round(-Math.sin(radians));
        int forwardZ = (int) Math.round(Math.cos(radians));
        dirty(game, chunkX, chunkZ, true, frame);
        dirty(game, chunkX + 1, chunkZ, true, frame);
        dirty(game, chunkX + forwardX * 2, chunkZ + forwardZ * 2, false, frame);
        dirty(game, chunkX - forwardX * 3, chunkZ - forwardZ * 3, false, frame);
    }

    private static void dirty(Minecraft game, int chunkX, int chunkZ,
                              boolean visible, int frame) {
        if (visible) VisualProbe.dirtyVisible(chunkX, chunkZ, frame);
        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;
        game.worldRenderer.setBlocksDirty(x, 100, z, x, 100, z);
    }

    private static int floor(double value) {
        int integer = (int) value;
        return value < integer ? integer - 1 : integer;
    }
}
