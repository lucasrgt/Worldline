package worldline.b173server;

import worldline.api.MovementDisposition;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;

/** Samples and resets one fixed west-cutout trajectory across the cake cell. */
final class B173CakeCollisionProbe {
    private B173CakeCollisionProbe() { }

    static MovementDisposition sample(B173WireClient client, PlayerPose origin) {
        MovementOutcome outcome = client.moveAndObserve(0D, 0D, 1D, 10);
        PlayerPose result = outcome.resulting();
        for (int attempt = 0; attempt < 8 && !close(result, origin); attempt++) {
            result = client.moveAndObserve(origin.x() - result.x(), origin.y() - result.y(),
                    origin.z() - result.z(), 10).resulting();
        }
        if (!close(result, origin)) {
            throw new IllegalStateException("cake collision probe did not reset");
        }
        return outcome.disposition();
    }

    static PlayerPose restore(B173WireClient client, PlayerPose origin, PlayerPose current) {
        PlayerPose result = current;
        for (int attempt = 0; attempt < 8 && !close(result, origin); attempt++) {
            result = client.moveAndObserve(origin.x() - result.x(), origin.y() - result.y(),
                    origin.z() - result.z(), 10).resulting();
        }
        if (!close(result, origin)) throw new IllegalStateException("cake lane restore failed");
        return result;
    }

    private static boolean close(PlayerPose left, PlayerPose right) {
        return Math.abs(left.x() - right.x()) <= 0.002D
                && Math.abs(left.y() - right.y()) <= 0.002D
                && Math.abs(left.z() - right.z()) <= 0.002D;
    }
}
