package worldline.api;

import java.util.Objects;

/** Immutable attempted and resulting poses across a bounded server-response window. */
public final class MovementOutcome {
    private final PlayerPose attempted, resulting;
    private final MovementDisposition disposition;

    public MovementOutcome(PlayerPose attempted, PlayerPose resulting,
            MovementDisposition disposition) {
        this.attempted = Objects.requireNonNull(attempted, "attempted");
        this.resulting = Objects.requireNonNull(resulting, "resulting");
        this.disposition = Objects.requireNonNull(disposition, "disposition");
        if (disposition == MovementDisposition.UNCHALLENGED && !attempted.equals(resulting))
            throw new IllegalArgumentException("movement disposition contradicts poses");
    }

    public PlayerPose attempted() { return attempted; }
    public PlayerPose resulting() { return resulting; }
    public MovementDisposition disposition() { return disposition; }
    public boolean corrected() { return disposition == MovementDisposition.CORRECTED; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof MovementOutcome)) return false;
        MovementOutcome value = (MovementOutcome) other;
        return attempted.equals(value.attempted) && resulting.equals(value.resulting)
                && disposition == value.disposition;
    }
    @Override public int hashCode() { return Objects.hash(attempted, resulting, disposition); }
    @Override public String toString() { return disposition + ":" + attempted + "->" + resulting; }
}
