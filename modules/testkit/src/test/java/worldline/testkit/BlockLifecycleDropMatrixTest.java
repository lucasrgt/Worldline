package worldline.testkit;
import worldline.testapi.BlockLifecycleDropMatrix;

import java.util.Collections;
import java.util.List;
import worldline.api.RemoteItemStack;

/** Contract checks for exact and nondeterministic bounded drop matrices. */
final class BlockLifecycleDropMatrixTest {
    private BlockLifecycleDropMatrixTest() { }

    static void execute() {
        RemoteItemStack dust = new RemoteItemStack(331, 1, 0);
        BlockLifecycleDropMatrix bounded = BlockLifecycleDropMatrix.repeated(dust, 4, 5);
        require(bounded.accepts(Collections.nCopies(4, dust))
                        && bounded.accepts(Collections.nCopies(5, dust))
                        && !bounded.accepts(Collections.nCopies(3, dust))
                        && !bounded.accepts(Collections.nCopies(6, dust))
                        && !bounded.accepts(Collections.nCopies(
                                4, new RemoteItemStack(348, 1, 0)))
                        && bounded.canonical().equals("331:1:0*4..5"),
                "bounded repeated drop contract drifted");
        RemoteItemStack block = new RemoteItemStack(4, 1, 0);
        BlockLifecycleDropMatrix exact = BlockLifecycleDropMatrix.exact(List.of(dust, block));
        require(exact.accepts(List.of(block, dust))
                        && !exact.accepts(List.of(dust))
                        && exact.exactDrops().equals(List.of(dust, block)),
                "exact drop contract compatibility drifted");
        rejects(() -> BlockLifecycleDropMatrix.repeated(dust, 5, 4));
        rejects(() -> BlockLifecycleDropMatrix.exact(null));
    }

    private static void rejects(Runnable action) {
        try { action.run(); throw new AssertionError("invalid drop contract was accepted"); }
        catch (IllegalArgumentException | NullPointerException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
