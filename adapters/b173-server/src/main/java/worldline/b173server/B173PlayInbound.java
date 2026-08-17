package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** Single bounded inbound pump that preserves Packet50/51 lifecycle state. */
final class B173PlayInbound {
    private final DataInputStream input;
    private final DataOutputStream output;
    private final B173RemoteWorldCache cache = new B173RemoteWorldCache();
    private final long timeoutNanos;

    B173PlayInbound(DataInputStream input, DataOutputStream output, int timeoutMillis) {
        this.input = input; this.output = output; this.timeoutNanos = timeoutMillis * 1_000_000L;
    }

    void skip(int packet) throws IOException {
        if (packet == 0) { synchronized (output) {
            output.writeByte(10); output.writeBoolean(false); output.flush(); } return; }
        if (packet == 3) { B173InboundPacket.string(input, 119); return; }
        if (packet == 50) { cache.preChunk(input); return; }
        if (packet == 51) { cache.accept(B173ChunkCodec.read(input)); return; }
        if (packet == 52) { cache.multiBlock(input); return; }
        if (packet == 53) { cache.singleBlock(input); return; }
        if (packet == 255) throw disconnect();
        B173InboundPacket.skip(input, packet);
    }

    String awaitChat() throws IOException {
        for (int count = 0; count < 4096; count++) {
            int packet = input.readUnsignedByte();
            if (packet == 3) return B173InboundPacket.string(input, 119);
            skip(packet);
        }
        throw new IOException("chat packet absent from bounded inbound window");
    }

    RemoteChunkSnapshot awaitChunk() throws IOException {
        RemoteChunkSnapshot ready = cache.firstDecoded();
        if (ready != null) return ready;
        for (int count = 0; count < 4096; count++) {
            int packet = input.readUnsignedByte();
            if (packet == 51) {
                RemoteChunkSnapshot chunk = B173ChunkCodec.read(input);
                if (cache.accept(chunk)) return chunk;
                continue;
            }
            skip(packet);
        }
        throw new IOException("chunk packet absent from bounded inbound window");
    }

    RemoteWorldView awaitWorld(int minimumChunks) throws IOException {
        if (minimumChunks < 1 || minimumChunks > RemoteWorldView.MAX_CHUNKS)
            throw new IllegalArgumentException("invalid minimum remote chunk count");
        if (cache.decoded() >= minimumChunks) return cache.snapshot();
        for (int count = 0; count < 8192; count++) {
            int packet;
            try { packet = input.readUnsignedByte(); }
            catch (IOException error) { throw new IOException("remote world stream ended with decoded="
                    + cache.decoded() + ",tracked=" + cache.tracked(), error); }
            if (packet == 51) cache.accept(B173ChunkCodec.read(input));
            else skip(packet);
            if (cache.decoded() >= minimumChunks) return cache.snapshot();
        }
        throw new IOException("remote world minimum absent from bounded inbound window");
    }

    RemoteWorldView awaitChunk(int chunkX, int chunkZ) throws IOException {
        if (cache.snapshot().containsChunk(chunkX, chunkZ)) return cache.snapshot();
        for (int count = 0; count < 16384; count++) {
            skip(input.readUnsignedByte());
            if (cache.snapshot().containsChunk(chunkX, chunkZ)) return cache.snapshot();
        }
        throw new IOException("requested remote chunk absent from bounded inbound window");
    }

    RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) throws IOException {
        if (position == null || expected == null) throw new IllegalArgumentException("null block wait");
        if (cache.matches(position, expected)) return cache.snapshot();
        Thread pulse = pulse();
        long deadline = System.nanoTime() + timeoutNanos;
        try {
            for (int count = 0; count < 8192 && System.nanoTime() < deadline; count++) {
                skip(input.readUnsignedByte());
                if (cache.matches(position, expected)) return cache.snapshot();
            }
            throw new IOException("expected block state absent before deadline");
        } finally { pulse.interrupt(); }
    }

    private Thread pulse() {
        Thread thread = new Thread(() -> { try { while (!Thread.currentThread().isInterrupted()) {
            synchronized (output) { output.writeByte(10); output.writeBoolean(false); output.flush(); }
            Thread.sleep(1000L);
        } } catch (IOException ignored) { } catch (InterruptedException stopped) {
            Thread.currentThread().interrupt(); } }, "worldline-b173-pulse");
        thread.setDaemon(true); thread.start(); return thread;
    }

    B173RemoteWorldCache cache() { return cache; }

    private IOException disconnect() throws IOException {
        return new IOException("server disconnected: " + B173InboundPacket.string(input, 256));
    }
}
