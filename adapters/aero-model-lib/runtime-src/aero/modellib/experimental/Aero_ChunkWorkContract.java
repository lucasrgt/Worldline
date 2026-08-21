package aero.modellib.experimental;

/** Explicit accepted/deferred-work contract proposed at the Aero boundary. */
public final class Aero_ChunkWorkContract {
    private Aero_ChunkWorkContract() {}

    public static Result execute(Queue queue, int limit) {
        if (queue == null) throw new IllegalArgumentException("queue is required");
        if (limit < 1) throw new IllegalArgumentException("limit must be positive");
        int accepted = queue.accept(limit);
        int remaining = queue.remaining();
        if (accepted < 0 || accepted > limit || remaining < 0)
            throw new IllegalStateException("invalid queue result");
        Status status = remaining == 0 ? Status.COMPLETE
                : accepted > 0 ? Status.ACCEPTED_DEFERRED : Status.STALLED_DEFERRED;
        return new Result(status, accepted, remaining);
    }

    public interface Queue {
        int accept(int limit);
        int remaining();
    }

    public enum Status { COMPLETE, ACCEPTED_DEFERRED, STALLED_DEFERRED }

    public static final class Result {
        public final Status status;
        public final int accepted;
        public final int remaining;

        Result(Status status, int accepted, int remaining) {
            this.status = status; this.accepted = accepted; this.remaining = remaining;
        }

        /** Deferred work must resume next frame, never inside the current hot loop. */
        public boolean endCurrentFrame() { return true; }
    }
}
