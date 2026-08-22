package worldline.b173server;

import worldline.api.PlayerPose;
import worldline.api.RemoteRespawn;

/** Reusable bounded walk below the void kill plane and Packet9 respawn boundary. */
public final class B173VoidDeath {
    private static final double KILL_Y = -64D;

    private B173VoidDeath() {}

    public static Outcome walkAndRespawn(B173WireClient actor, PlayerPose initial) {
        if (actor == null || initial == null || initial.y() >= 0D || initial.y() <= KILL_Y
                || actor.health() != 20) throw new IllegalArgumentException("invalid void walk baseline");
        PlayerPose pose = initial;
        int steps = 0;
        while (pose.y() > KILL_Y) {
            if (++steps > 16 || actor.health() <= 0)
                throw new IllegalStateException("void walk failed before kill plane");
            pose = actor.moveAndObserve(0D, -Math.min(9D, pose.y() + 72D), 0D, 1).resulting();
        }
        if (pose.y() > KILL_Y || actor.health() <= 0)
            throw new IllegalStateException("void kill plane drift");
        int waited = 0;
        while (actor.health() > 0) {
            if (++waited > 400) throw new IllegalStateException("void Packet8 health zero absent");
            actor.sustainTicks(1);
        }
        int dead = actor.health();
        if (dead == 0 && actor.awaitHealth(0) != 0)
            throw new IllegalStateException("void Packet8 health zero drift");
        RemoteRespawn respawn = actor.respawn();
        if (!respawn.equals(new RemoteRespawn(0, 0, 20)) || actor.dimension() != 0 || actor.health() != 20)
            throw new IllegalStateException("void Packet9 respawn drift");
        actor.sustainTicks(1);
        PlayerPose after = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
        if (after.y() < 0D) throw new IllegalStateException("respawn pose remained in void");
        return new Outcome(steps, dead, pose, respawn, after);
    }

    public static final class Outcome {
        private final int steps, deadHealth;
        private final PlayerPose deathPose, respawnPose;
        private final RemoteRespawn respawn;

        private Outcome(int steps, int deadHealth, PlayerPose deathPose,
                RemoteRespawn respawn, PlayerPose respawnPose) {
            this.steps = steps; this.deadHealth = deadHealth; this.deathPose = deathPose;
            this.respawn = respawn; this.respawnPose = respawnPose;
        }

        public int steps() { return steps; }
        public int deadHealth() { return deadHealth; }
        public PlayerPose deathPose() { return deathPose; }
        public RemoteRespawn respawn() { return respawn; }
        public PlayerPose respawnPose() { return respawnPose; }
    }
}
