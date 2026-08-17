package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;

/** Proves Packet50 load/unload semantics and the hard tracked-region bound. */
public final class B173RemoteWorldCacheFixture {
    private B173RemoteWorldCacheFixture() {}

    public static void main(String[] arguments) throws Exception {
        B173RemoteWorldCache cache = new B173RemoteWorldCache();
        cache.preChunk(event(-1, 2, true)); cache.accept(chunk(-1, 2));
        cache.preChunk(event(3, -4, true)); cache.accept(chunk(3, -4));
        require(cache.snapshot().loadedChunks() == 2 && cache.snapshot().containsChunk(-1, 2)
                && cache.snapshot().containsChunk(3, -4), "prechunk loads were not retained");
        cache.preChunk(event(-1, 2, false));
        require(cache.snapshot().loadedChunks() == 1 && !cache.snapshot().containsChunk(-1, 2),
                "prechunk unload did not evict only its data");
        try { cache.accept(chunk(-1, 2)); throw new AssertionError("unqualified chunk was accepted"); }
        catch (IOException expected) { }

        B173RemoteWorldCache bounded = new B173RemoteWorldCache();
        for (int index = 0; index < 256; index++) bounded.preChunk(event(index, 0, true));
        try { bounded.preChunk(event(256, 0, true)); throw new AssertionError("cache bound was ignored"); }
        catch (IOException expected) { }
        System.out.println("WORLDLINE_M30_LIFECYCLE_ORACLE=PASS");
    }

    private static DataInputStream event(int x, int z, boolean load) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        output.writeInt(x); output.writeInt(z); output.writeBoolean(load); output.close();
        return new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
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
