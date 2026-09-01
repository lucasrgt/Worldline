package worldline.b173server;

import java.util.ArrayList;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;

/** Exercises every standing-sign rotation through item placement and direct breaking. */
final class B173SignDomainProbe {
    private static final BlockState AIR = new BlockState(0, 0);
    private static final RemoteItemStack SIGN_ITEM = new RemoteItemStack(323, 1, 0);

    private B173SignDomainProbe() { }

    static Result execute(B173WireClient client, int signsBefore) {
        List<Integer> metadata = new ArrayList<Integer>();
        BlockState brokenFrom = null; RemoteItemStack drop = null; int afterFirst = -1;
        client.selectHeldSlot(1);
        for (int value = 0; value <= 15; value++) {
            client.look(value * 22.5F - 180F, 0F);
            client.useHeldItemOnBlock(B173SignSubsystemArena.STANDING_SUPPORT, BlockFace.UP);
            BlockState expected = new BlockState(63, value);
            client.awaitBlock(B173SignSubsystemArena.STANDING, expected); metadata.add(value);
            if (value == 0) {
                afterFirst = awaitSignCount(client, signsBefore - 1);
                brokenFrom = expected;
            }
            client.beginBreak(B173SignSubsystemArena.STANDING);
            client.finishBreak(B173SignSubsystemArena.STANDING);
            client.awaitBlock(B173SignSubsystemArena.STANDING, AIR);
            if (value == 0) drop = client.awaitDroppedItem(SIGN_ITEM).item();
        }
        if (signsBefore != 20 || afterFirst != 19 || brokenFrom == null || drop == null) {
            throw new IllegalStateException("standing-sign gameplay lifecycle drift: inventory="
                    + signsBefore + "->" + afterFirst + ",broken=" + brokenFrom + ",drop=" + drop);
        }
        return new Result(metadata, signsBefore, afterFirst, brokenFrom, AIR, drop);
    }

    private static int awaitSignCount(B173WireClient client, int expected) {
        int observed = B173SignSubsystemArena.count(client.inventory(), 323);
        for (int attempt = 0; attempt < 20 && observed != expected; attempt++) {
            client.sustainTicks(1);
            observed = B173SignSubsystemArena.count(client.inventory(), 323);
        }
        return observed;
    }

    static final class Result {
        final List<Integer> metadata; final int before, afterFirst;
        final BlockState brokenFrom, brokenTo; final RemoteItemStack drop;
        Result(List<Integer> metadata, int before, int afterFirst,
                BlockState brokenFrom, BlockState brokenTo, RemoteItemStack drop) {
            this.metadata = metadata; this.before = before; this.afterFirst = afterFirst;
            this.brokenFrom = brokenFrom; this.brokenTo = brokenTo; this.drop = drop;
        }
    }
}
