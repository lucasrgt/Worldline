package worldline.smoke.m15;

import aero.modellib.experimental.Aero_ChunkWorkContract;
import aero.modellib.experimental.Aero_ChunkWorkContract.Queue;
import aero.modellib.experimental.Aero_ChunkWorkContract.Result;
import aero.modellib.experimental.Aero_ChunkWorkContract.Status;

/** Exercises every explicit M15 contract outcome without mapped game types. */
public final class ChunkWorkContractSmoke {
    private ChunkWorkContractSmoke() {}

    public static void main(String[] arguments) {
        Result complete = Aero_ChunkWorkContract.execute(queue(0, 0), 2);
        Result accepted = Aero_ChunkWorkContract.execute(queue(2, 10), 2);
        Result stalled = Aero_ChunkWorkContract.execute(queue(0, 10), 2);
        require(complete.status == Status.COMPLETE && complete.accepted == 0);
        require(accepted.status == Status.ACCEPTED_DEFERRED
                && accepted.accepted == 2 && accepted.remaining == 10);
        require(stalled.status == Status.STALLED_DEFERRED && stalled.accepted == 0);
        require(complete.endCurrentFrame() && accepted.endCurrentFrame()
                && stalled.endCurrentFrame());
        System.out.println("WORLDLINE_M15_CONTRACT_STATES=PASS");
    }

    private static Queue queue(int accepted, int remaining) {
        return new Queue() {
            @Override public int accept(int limit) { return accepted; }
            @Override public int remaining() { return remaining; }
        };
    }

    private static void require(boolean condition) {
        if (!condition) throw new IllegalStateException("contract state drifted");
    }
}
