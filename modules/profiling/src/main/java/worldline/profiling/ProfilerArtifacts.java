package worldline.profiling;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Atomic persistence boundary for canonical profiler artifacts. */
public final class ProfilerArtifacts {
    private static final long MAX_ARTIFACT_BYTES = 520L * 1024L * 1024L;
    private ProfilerArtifacts() {}

    public static void write(Path target, ProfilerRun run) throws IOException {
        if (target == null || run == null) throw new NullPointerException("profiler artifact write");
        Path absolute = target.toAbsolutePath().normalize();
        Path parent = absolute.getParent();
        if (parent == null) throw new IllegalArgumentException("profiler artifact has no parent");
        Files.createDirectories(parent);
        byte[] encoded = ProfilerRunCodec.encode(run);
        require(encoded.length <= MAX_ARTIFACT_BYTES, "profiler artifact exceeds file limit");
        Path temporary = Files.createTempFile(parent, ".worldline-profiler-", ".tmp");
        try {
            Files.write(temporary, encoded);
            try {
                Files.move(temporary, absolute, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unavailable) {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporary); }
    }

    public static ProfilerRun read(Path source) throws IOException {
        if (source == null) throw new NullPointerException("profiler artifact read");
        long size = Files.size(source);
        require(size > 0L && size <= MAX_ARTIFACT_BYTES, "invalid profiler artifact size");
        return ProfilerRunCodec.decode(Files.readAllBytes(source));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
