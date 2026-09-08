package worldline.m791;

import net.minecraft.entity.player.ClientPlayerEntity;

/** Eight covered-floor views followed by four post-teleport reveal views. */
final class OcclusionScene {
    static final int CHECKPOINTS = 12;
    static final int HOLD_FRAMES = 20;

    private OcclusionScene() {}

    static void place(ClientPlayerEntity player, int checkpoint) {
        boolean covered = checkpoint < 8;
        float yaw = covered ? checkpoint * 45.0F : (checkpoint - 8) * 90.0F;
        double y = covered ? 76.0D : 90.0D;
        player.velocityX = player.velocityY = player.velocityZ = 0.0D;
        float pitch = covered ? -20.0F : 20.0F;
        player.setPositionAndAngles(16.5D, y, 16.5D, yaw, pitch);
    }
}
