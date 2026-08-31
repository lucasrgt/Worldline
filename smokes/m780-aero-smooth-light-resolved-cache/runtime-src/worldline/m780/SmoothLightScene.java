package worldline.m780;

import net.minecraft.entity.player.ClientPlayerEntity;

/** Orbit, traversal, fast spin, teleport, and close inspection in 240 frames. */
final class SmoothLightScene {
    static final int ROUTE_FRAMES = 240;
    static final int CHECKPOINTS = 24;
    static final int CAPTURE_STRIDE = ROUTE_FRAMES / CHECKPOINTS;

    private SmoothLightScene() {}

    static void place(ClientPlayerEntity player, int frame) {
        double x, y, z, targetX, targetY, targetZ;
        if (frame < 60) {
            double angle = frame * Math.PI * 2.0D / 60.0D;
            x = 36.0D + Math.sin(angle) * 25.0D;
            y = 80.0D + Math.sin(angle * 2.0D) * 5.0D;
            z = 36.0D + Math.cos(angle) * 25.0D;
            targetX = targetZ = 36.0D;
            targetY = 80.0D;
        } else if (frame < 120) {
            double t = (frame - 60) / 59.0D;
            x = 10.0D + t * 52.0D;
            y = 76.0D + t * 8.0D;
            z = 36.0D + Math.sin(t * Math.PI * 2.0D) * 8.0D;
            targetX = 68.0D;
            targetY = 80.0D;
            targetZ = 36.0D;
        } else if (frame < 180) {
            x = z = 36.0D;
            y = 80.0D;
            double angle = (frame - 120) * Math.PI * 4.0D / 60.0D;
            targetX = x - Math.sin(angle) * 30.0D;
            targetY = 80.0D + Math.sin(angle * 0.5D) * 8.0D;
            targetZ = z + Math.cos(angle) * 30.0D;
        } else {
            double angle = (frame - 180) * Math.PI * 2.0D / 60.0D;
            x = 61.0D + Math.sin(angle) * 8.0D;
            y = 78.0D + Math.cos(angle * 2.0D) * 4.0D;
            z = 36.0D + Math.cos(angle) * 8.0D;
            targetX = 68.0D;
            targetY = 80.0D;
            targetZ = 36.0D;
        }
        face(player, x, y, z, targetX, targetY, targetZ);
    }

    private static void face(ClientPlayerEntity player, double x, double y, double z,
            double targetX, double targetY, double targetZ) {
        double dx = targetX - x, dy = targetY - y, dz = targetZ - z;
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        player.velocityX = player.velocityY = player.velocityZ = 0.0D;
        player.setPositionAndAngles(x, y, z, yaw, pitch);
        player.prevX = player.lastTickX = player.x;
        player.prevY = player.lastTickY = player.y;
        player.prevZ = player.lastTickZ = player.z;
        player.prevYaw = player.yaw;
        player.prevPitch = player.pitch;
    }
}
