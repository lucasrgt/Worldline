package worldline.m777;

import net.minecraft.entity.player.ClientPlayerEntity;

/** Deterministic first-sight turns, stable views, and near-panel teleport. */
final class MatrixScene {
    private MatrixScene() {}

    static void place(ClientPlayerEntity player, int frame) {
        double x = 32.5D, y = 86.0D, z = 32.5D;
        float yaw;
        if (frame <= 3) yaw = 180.0F;
        else if (frame <= 6) yaw = 270.0F;
        else if (frame <= 9) yaw = 0.0F;
        else if (frame <= 12) yaw = 90.0F;
        else if (frame <= 120) yaw = 180.0F;
        else if (frame <= 240) yaw = 270.0F;
        else if (frame <= 360) yaw = 0.0F;
        else if (frame <= 480) yaw = 90.0F;
        else {
            x = 16.5D;
            yaw = (float) ((frame - 480) * 9 % 360);
        }
        player.velocityX = player.velocityY = player.velocityZ = 0.0D;
        player.setPositionAndAngles(x, y, z, yaw, 0.0F);
    }

    static int phase(int frame) {
        if (frame <= 60) return 0;
        if (frame <= 120) return 1;
        if (frame <= 240) return 2;
        if (frame <= 360) return 3;
        if (frame <= 480) return 4;
        return 5;
    }
}
