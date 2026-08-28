package worldline.b173server;

import worldline.api.MovementDisposition;
import worldline.api.PlayerPose;

/** Samples fixed trajectories through both sign cells and restores the standing lane. */
final class B173SignCollisionProbe {
    private B173SignCollisionProbe() { }

    static Result standing(B173WireClient client, PlayerPose current) {
        restore(client, current, B173SignSubsystemArena.STANDING_ORIGIN);
        worldline.api.MovementOutcome outcome = client.moveAndObserve(0D, 0D, 1D, 10);
        PlayerPose restored = restore(client, outcome.resulting(),
                B173SignSubsystemArena.STANDING_ORIGIN);
        return new Result(outcome.disposition(), restored);
    }

    static Result wall(B173WireClient client, PlayerPose current) {
        restore(client, current, B173SignSubsystemArena.WALL_ORIGIN);
        worldline.api.MovementOutcome outcome = client.moveAndObserve(0D, 0D, 2D, 10);
        PlayerPose laneOrigin = restore(client, outcome.resulting(),
                B173SignSubsystemArena.WALL_ORIGIN);
        PlayerPose restored = restore(client, laneOrigin,
                B173SignSubsystemArena.STANDING_ORIGIN);
        return new Result(outcome.disposition(), restored);
    }

    static PlayerPose restore(B173WireClient client, PlayerPose current, PlayerPose target) {
        PlayerPose result = current;
        for (int attempt = 0; attempt < 8 && !close(result, target); attempt++) {
            result = B173SignSubsystemArena.move(client, result, target);
        }
        if (!close(result, target)) throw new IllegalStateException("sign lane restore failed");
        return result;
    }

    private static boolean close(PlayerPose left, PlayerPose right) {
        return Math.abs(left.x() - right.x()) <= 0.002D
                && Math.abs(left.y() - right.y()) <= 0.002D
                && Math.abs(left.z() - right.z()) <= 0.002D;
    }

    static final class Result {
        final MovementDisposition disposition; final PlayerPose pose;
        Result(MovementDisposition disposition, PlayerPose pose) {
            this.disposition = disposition; this.pose = pose;
        }
    }
}
