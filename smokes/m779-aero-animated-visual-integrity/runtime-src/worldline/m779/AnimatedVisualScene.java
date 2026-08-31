package worldline.m779;

import net.minecraft.entity.player.ClientPlayerEntity;

/** One 240-frame route: orbit, walk, fast spin, teleport, and near-panel orbit. */
final class AnimatedVisualScene {
    static final int ROUTE_FRAMES = 240;
    static final int CHECKPOINTS = 24;
    static final int CAPTURE_STRIDE = ROUTE_FRAMES / CHECKPOINTS;

    private AnimatedVisualScene() {}

    static void place(ClientPlayerEntity player, int frame) {
        double x, z, targetX, targetZ;
        float pitch;
        if (frame < 60) {
            double angle = frame * Math.PI * 2.0D / 60.0D;
            x = 32.5D + Math.sin(angle) * 11.0D;
            z = 32.5D + Math.cos(angle) * 11.0D;
            targetX = targetZ = 32.5D;
            pitch = (float) (Math.sin(angle * 2.0D) * 8.0D);
        } else if (frame < 120) {
            double t = (frame - 60) / 59.0D;
            x = 16.5D + 32.0D * t;
            z = 32.5D + Math.sin(t * Math.PI) * 5.0D;
            targetX = 60.0D;
            targetZ = 29.0D;
            pitch = (float) (-6.0D + 12.0D * t);
        } else if (frame < 180) {
            x = z = 32.5D;
            double angle = (frame - 120) * Math.PI * 4.0D / 60.0D;
            targetX = x - Math.sin(angle) * 24.0D;
            targetZ = z + Math.cos(angle) * 24.0D;
            pitch = (float) (Math.sin(angle * 0.5D) * 18.0D);
        } else {
            double angle = (frame - 180) * Math.PI * 2.0D / 60.0D;
            x = 51.0D + Math.sin(angle) * 7.0D;
            z = 29.0D + Math.cos(angle) * 7.0D;
            targetX = 60.0D;
            targetZ = 29.0D;
            pitch = (float) (Math.cos(angle) * 10.0D);
        }
        float yaw = (float) Math.toDegrees(Math.atan2(-(targetX - x), targetZ - z));
        player.velocityX = player.velocityY = player.velocityZ = 0.0D;
        player.setPositionAndAngles(x, 86.0D, z, yaw, pitch);
        player.prevX = player.lastTickX = player.x;
        player.prevY = player.lastTickY = player.y;
        player.prevZ = player.lastTickZ = player.z;
        player.prevYaw = player.yaw;
        player.prevPitch = player.pitch;
    }
}
