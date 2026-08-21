package worldline.api;

import java.util.*;

final class RemoteExplosionTest {
    private RemoteExplosionTest() {}
    static void run() {
        BlockPosition cell = new BlockPosition(1, 2, 3); RemoteExplosion value = new RemoteExplosion(1.5, 2.5, 3.5, 4F, Arrays.asList(cell));
        if (!value.equals(new RemoteExplosion(1.5, 2.5, 3.5, 4F, Arrays.asList(cell)))
                || value.destroyed().size() != 1 || value.strength() != 4F) throw new AssertionError("explosion value drifted");
        fail(() -> new RemoteExplosion(0, 0, 0, 0F, Collections.emptyList()));
        fail(() -> new RemoteExplosion(0, 0, 0, 4F, Arrays.asList((BlockPosition) null)));
    }
    private static void fail(Runnable action) { try { action.run(); throw new AssertionError("explosion accepted invalid value"); } catch (IllegalArgumentException expected) { } }
}
