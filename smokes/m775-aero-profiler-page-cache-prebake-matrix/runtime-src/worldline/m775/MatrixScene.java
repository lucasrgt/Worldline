package worldline.m775;

import aero.modellib.test.WorldlineM775Rehydrator;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ClientPlayerEntity;

/** Deterministic entry, walk, turn, mutation, teleport, and recovery journey. */
final class MatrixScene {
    private MatrixScene() {}

    static void place(ClientPlayerEntity player, int frame) {
        double x = 8.5D, z = 8.5D;
        float yaw = 0.0F;
        if (frame > 80 && frame <= 320) z = Math.min(38.5D, 8.5D + (frame - 80) * 0.25D);
        if (frame > 200 && frame <= 320) yaw = (float) ((frame - 200) * 9 % 360);
        if (frame > 320) {
            x = z = 72.5D;
            yaw = (float) ((frame - 320) * 5 % 360);
        }
        player.velocityX = player.velocityY = player.velocityZ = 0.0D;
        player.setPositionAndAngles(x, 100.0D, z, yaw, 4.0F);
    }

    static void act(Minecraft game, int frame) {
        if (frame == 150) WorldlineM775Rehydrator.mutation(game.world, false);
        if (frame == 180) WorldlineM775Rehydrator.mutation(game.world, true);
        if (frame > 450 || frame % 8 != 1) return;
        int chunkX = floor(game.player.x) >> 4;
        int chunkZ = floor(game.player.z) >> 4;
        double radians = Math.toRadians(game.player.yaw);
        int forwardX = (int) Math.round(-Math.sin(radians));
        int forwardZ = (int) Math.round(Math.cos(radians));
        dirty(game, chunkX, chunkZ);
        dirty(game, chunkX + 1, chunkZ);
        dirty(game, chunkX - 1, chunkZ);
        dirty(game, chunkX + forwardX * 2, chunkZ + forwardZ * 2);
    }

    private static void dirty(Minecraft game, int chunkX, int chunkZ) {
        int x = (chunkX << 4) + 8, z = (chunkZ << 4) + 8;
        game.worldRenderer.setBlocksDirty(x, 100, z, x, 100, z);
    }

    private static int floor(double value) {
        int integer = (int) value;
        return value < integer ? integer - 1 : integer;
    }
}
