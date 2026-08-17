package worldline.api;

import java.util.Objects;

/** Immutable multiplayer position and view orientation. */
public final class PlayerPose {
    private final double x, y, z;
    private final float yaw, pitch;

    public PlayerPose(double x, double y, double z, float yaw, float pitch) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || !Float.isFinite(yaw) || !Float.isFinite(pitch))
            throw new IllegalArgumentException("non-finite player pose");
        if (pitch < -90.0F || pitch > 90.0F) throw new IllegalArgumentException("invalid pitch");
        this.x = x; this.y = y; this.z = z; this.yaw = yaw; this.pitch = pitch;
    }

    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }
    public float yaw() { return yaw; }
    public float pitch() { return pitch; }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PlayerPose)) return false;
        PlayerPose pose = (PlayerPose) other;
        return Double.compare(x, pose.x) == 0 && Double.compare(y, pose.y) == 0
                && Double.compare(z, pose.z) == 0 && Float.compare(yaw, pose.yaw) == 0
                && Float.compare(pitch, pose.pitch) == 0;
    }

    @Override
    public int hashCode() { return Objects.hash(x, y, z, yaw, pitch); }
}
