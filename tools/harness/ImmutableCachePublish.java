import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicInteger;

/** Bounded Windows-safe no-replace publication for private immutable caches. */
final class ImmutableCachePublish {
    private static final int ATTEMPTS = 8;

    private ImmutableCachePublish() { }

    static void move(Path source, Path target) throws IOException {
        retry(source, target, ImmutableCachePublish::moveOnce, BoundedSleep::millis);
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-cache-publish-").toAbsolutePath().normalize();
        try {
            Path source = Files.createDirectories(root.resolve("source"));
            Path target = root.resolve("target"); Files.writeString(source.resolve("value"), "stable\n");
            AtomicInteger calls = new AtomicInteger();
            retry(source, target, (from, to) -> {
                if (calls.incrementAndGet() < 3) throw new AccessDeniedException(from.toString());
                moveOnce(from, to);
            }, millis -> { });
            require(calls.get() == 3 && Files.readString(target.resolve("value")).equals("stable\n")
                    && !Files.exists(source), "cache publication retry drifted");
            Path blocked = Files.createDirectories(root.resolve("blocked"));
            Path absent = root.resolve("absent"); AtomicInteger exhausted = new AtomicInteger();
            try {
                retry(blocked, absent, (from, to) -> {
                    exhausted.incrementAndGet(); throw new AccessDeniedException(from.toString());
                }, millis -> { });
                throw new IllegalStateException("exhausted cache publication was accepted");
            } catch (AccessDeniedException expected) {
                require(exhausted.get() == ATTEMPTS && Files.isDirectory(blocked)
                        && !Files.exists(absent), "cache publication exhaustion lost state");
            }
            Path collisionSource = Files.createDirectories(root.resolve("collision-source"));
            Path collisionTarget = Files.createDirectories(root.resolve("collision-target"));
            try {
                retry(collisionSource, collisionTarget, ImmutableCachePublish::moveOnce, millis -> { });
                throw new IllegalStateException("cache publication replaced a target");
            } catch (IOException expected) {
                require(Files.isDirectory(collisionSource) && Files.isDirectory(collisionTarget),
                        "cache collision did not preserve both trees");
            }
        } finally { SafeTreeDelete.delete(root); }
    }

    private static void retry(Path source, Path target, Mover mover, Sleeper sleeper) throws IOException {
        require(source.isAbsolute() && source.normalize().equals(source)
                && target.isAbsolute() && target.normalize().equals(target)
                && source.getParent().equals(target.getParent()),
                "immutable cache publication paths are not normalized siblings");
        require(directory(source), "immutable cache staging directory is absent");
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            require(!Files.exists(target, LinkOption.NOFOLLOW_LINKS),
                    "immutable cache target already exists");
            try {
                mover.move(source, target);
                require(directory(target) && !Files.exists(source, LinkOption.NOFOLLOW_LINKS),
                        "immutable cache publication did not move the complete tree");
                return;
            } catch (AccessDeniedException error) {
                if (attempt == ATTEMPTS || Files.exists(target, LinkOption.NOFOLLOW_LINKS)) throw error;
                try { sleeper.sleep(Math.min(200L, 25L << Math.min(attempt - 1, 3))); }
                catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IOException("immutable cache publication interrupted", interrupted);
                }
            }
        }
        throw new IllegalStateException("unreachable cache publication state");
    }

    private static void moveOnce(Path source, Path target) throws IOException {
        try { Files.move(source, target, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException error) { Files.move(source, target); }
    }
    private static boolean directory(Path path) {
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path);
    }
    private static void require(boolean value, String message) throws IOException {
        if (!value) throw new IOException(message);
    }
    @FunctionalInterface private interface Mover {
        void move(Path source, Path target) throws IOException;
    }
    @FunctionalInterface private interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }
}
