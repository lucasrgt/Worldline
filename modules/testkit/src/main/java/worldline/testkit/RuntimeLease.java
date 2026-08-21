package worldline.testkit;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.ReentrantLock;

/** Process-local plus cross-process exclusive lease for official runtimes. */
final class RuntimeLease implements AutoCloseable {
    private static final ReentrantLock LOCAL = new ReentrantLock(true);
    private final FileChannel channel;
    private final FileLock lock;

    private RuntimeLease(FileChannel channel, FileLock lock) { this.channel = channel; this.lock = lock; }

    static RuntimeLease acquire(Path path) throws IOException, InterruptedException {
        Path target = path.toAbsolutePath().normalize(); Path parent = target.getParent();
        if (parent == null) throw new IllegalArgumentException("runtime lock needs a parent directory");
        Files.createDirectories(parent); LOCAL.lockInterruptibly(); FileChannel channel = null;
        try {
            channel = FileChannel.open(target, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            return new RuntimeLease(channel, channel.lock());
        } catch (IOException | RuntimeException failure) {
            if (channel != null) try { channel.close(); } catch (IOException close) { failure.addSuppressed(close); }
            LOCAL.unlock(); throw failure;
        }
    }
    @Override public void close() {
        try { lock.release(); }
        catch (IOException error) { throw new IllegalStateException("runtime lock release failed", error); }
        finally {
            try { channel.close(); }
            catch (IOException error) { throw new IllegalStateException("runtime lock close failed", error); }
            finally { LOCAL.unlock(); }
        }
    }
}
