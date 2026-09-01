package worldline.m787;

import net.minecraft.entity.player.ClientPlayerEntity;

/** Cold first-frame, orbit, traverse, spin, teleport, and close route. */
final class ColdEntryScene {
    static final int FRAMES = 240;
    static final int[] CAPTURE_FRAMES = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        30, 45, 59, 60, 75, 90, 105, 119, 120, 121, 135, 150,
        165, 179, 180, 181, 190, 200, 209, 210, 211, 220, 230, 239
    };

    private ColdEntryScene() {}

    static int captureIndex(int frame) {
        for (int index = 0; index < CAPTURE_FRAMES.length; index++) {
            if (CAPTURE_FRAMES[index] == frame) return index;
        }
        return -1;
    }

    static void place(ClientPlayerEntity player, int frame) {
        int local = frame % FRAMES;
        double x, y, z, tx, ty, tz;
        if (local < 60) {
            double angle = local * Math.PI * 2.0D / 60.0D;
            x = 20.0D + Math.sin(angle) * 42.0D;
            y = 96.0D + Math.sin(angle * 2.0D) * 10.0D;
            z = 20.0D + Math.cos(angle) * 42.0D;
            tx = tz = 20.0D;
            ty = 91.0D;
        } else if (local < 120) {
            double t = (local - 60) / 59.0D;
            x = -8.0D + t * 72.0D;
            y = 82.0D + Math.sin(t * Math.PI) * 28.0D;
            z = 17.0D + Math.sin(t * Math.PI * 2.0D) * 12.0D;
            tx = 42.0D;
            ty = 88.0D;
            tz = 20.0D;
        } else if (local < 180) {
            x = z = 20.0D;
            y = 94.0D;
            double angle = (local - 120) * Math.PI * 8.0D / 60.0D;
            tx = x - Math.sin(angle) * 35.0D;
            ty = 92.0D + Math.sin(angle * 0.25D) * 24.0D;
            tz = z + Math.cos(angle) * 35.0D;
        } else if (local < 210) {
            x = z = 39.0D;
            y = 88.0D;
            tx = tz = 34.0D;
            ty = 92.0D;
        } else {
            double angle = (local - 210) * Math.PI * 2.0D / 30.0D;
            x = 36.0D + Math.sin(angle) * 10.0D;
            y = 78.0D + Math.cos(angle * 2.0D) * 7.0D;
            z = 36.0D + Math.cos(angle) * 10.0D;
            tx = tz = 36.0D;
            ty = 84.0D;
        }
        face(player, x, y, z, tx, ty, tz);
    }

    private static void face(ClientPlayerEntity player, double x, double y, double z,
            double tx, double ty, double tz) {
        double dx = tx - x, dy = ty - y, dz = tz - z;
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
