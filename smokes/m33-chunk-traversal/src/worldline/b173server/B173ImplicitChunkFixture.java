package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;

/** Proves edge MapChunk loads are enabled only after deliberate movement. */
public final class B173ImplicitChunkFixture {
    private B173ImplicitChunkFixture() {}

    public static void main(String[] arguments) throws Exception {
        B173RemoteWorldCache cache = new B173RemoteWorldCache();
        try { cache.accept(chunk(2, -3)); throw new AssertionError("strict cache accepted implicit load"); }
        catch (IOException expected) { }
        cache.enableImplicitLoads(); require(cache.accept(chunk(2, -3)), "implicit edge load rejected");
        require(cache.snapshot().containsChunk(2, -3), "implicit edge chunk absent");
        System.out.println("WORLDLINE_M33_IMPLICIT_CHUNK_ORACLE=PASS");
    }

    private static RemoteChunkSnapshot chunk(int x, int z) {
        return new RemoteChunkSnapshot(new RemoteChunkObservation(
                x * 16, 0, z * 16, 16, 128, 16, 1024), new byte[32768],
                new byte[16384], new byte[16384], new byte[16384]);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
