package worldline.m778;

import net.minecraft.entity.player.ClientPlayerEntity;

/** Twelve deterministic central and near-panel camera checkpoints. */
final class VisualScene {
    static final int CHECKPOINTS = 12;
    static final int HOLD_FRAMES = 20;

    private VisualScene() {}

    static void place(ClientPlayerEntity player, int checkpoint) {
        boolean central = checkpoint < 8;
        double x = central ? 32.5D : 16.5D;
        double y = 86.0D;
        double z = 32.5D;
        float yaw = central ? checkpoint * 45.0F : (checkpoint - 8) * 90.0F;
        player.velocityX = player.velocityY = player.velocityZ = 0.0D;
        player.setPositionAndAngles(x, y, z, yaw, 0.0F);
    }
}
