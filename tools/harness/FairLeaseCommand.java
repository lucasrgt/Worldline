import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/** Source-launchable FIFO lease owner for one internal Gate phase. */
public final class FairLeaseCommand {
    private FairLeaseCommand() { }

    public static void main(String[] arguments) {
        try { System.exit(execute(arguments)); }
        catch (Exception error) {
            System.err.println("fair lease command failed: " + error.getMessage()); System.exit(1);
        }
    }

    private static int execute(String[] arguments) throws Exception {
        if (arguments.length < 7) throw new IllegalArgumentException("invalid fair lease command");
        Path root = Path.of(arguments[0]).toAbsolutePath().normalize();
        Path control = Path.of(arguments[1]).toAbsolutePath().normalize();
        long waitMillis = Long.parseLong(arguments[2]);
        int slots = Integer.parseInt(arguments[3]);
        boolean runtime = Boolean.parseBoolean(arguments[4]);
        boolean runtimeLease = Boolean.parseBoolean(arguments[5]);
        Path legacy = "-".equals(arguments[6]) ? null : Path.of(arguments[6]);
        List<FairFileLease> leases = new ArrayList<>();
        try {
            Path worktree = root.resolve(".worldline/verify.lock");
            leases.add(FairFileLease.acquire(root, List.of(worktree),
                    worktree.resolveSibling("verify.lock.queue"), "worktree", waitMillis));
            if (slots > 0) {
                List<Path> locks = new ArrayList<>();
                for (int index = 0; index < slots; index++)
                    locks.add(control.resolve("verify-slot-" + index + ".lock"));
                leases.add(FairFileLease.acquire(root, locks, control.resolve("verify-slots.queue"),
                        "verify-slot", waitMillis));
            }
            if (runtime) acquireRuntime(root, control, legacy, waitMillis, leases);
            List<String> command = new ArrayList<>(List.of(javaTool("java"),
                    root.resolve("tools/harness/Gate.java").toString(), "--internal"));
            for (int index = 7; index < arguments.length; index++) command.add(arguments[index]);
            ProcessBuilder builder = new ProcessBuilder(command).directory(root.toFile()).inheritIO();
            builder.environment().put("WORLDLINE_GATE_INTERNAL", "true");
            if (runtimeLease) builder.environment().put(
                    "WORLDLINE_RUNTIME_LEASE", Long.toString(ProcessHandle.current().pid()));
            Process process = builder.start();
            try { return process.waitFor(); }
            catch (InterruptedException error) {
                destroy(process); Thread.currentThread().interrupt(); throw error;
            }
        } finally {
            for (int index = leases.size() - 1; index >= 0; index--) leases.get(index).close();
        }
    }

    private static void acquireRuntime(Path root, Path control, Path legacy, long waitMillis,
            List<FairFileLease> leases) throws Exception {
        Path runtime = control.resolve("official-b173.lock");
        leases.add(FairFileLease.acquire(root, List.of(runtime),
                runtime.resolveSibling("official-b173.lock.queue"), "runtime", waitMillis));
        if (legacy != null && !legacy.toAbsolutePath().normalize().equals(runtime))
            leases.add(FairFileLease.acquire(root, List.of(legacy),
                    legacy.resolveSibling(legacy.getFileName() + ".queue"),
                    "legacy-runtime", waitMillis));
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin",
                name + (windows ? ".exe" : "")).toString();
    }

    private static void destroy(Process process) {
        process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }

/** Cross-process FIFO ticket queue in front of one lock or a lock-slot pool. */
static final class FairFileLease implements AutoCloseable {
    private final FileChannel channel;
    private final FileLock lock;
    private final Path path;

    private FairFileLease(FileChannel channel, FileLock lock, Path path) {
        this.channel = channel; this.lock = lock; this.path = path;
    }

    static FairFileLease acquire(Path root, List<Path> locks, Path queue,
            String kind, long waitMillis) throws Exception {
        long deadline = System.currentTimeMillis() + waitMillis;
        Files.createDirectories(queue);
        Path ticket = issue(queue, deadline);
        boolean announced = false;
        try {
            while (System.currentTimeMillis() < deadline) {
                removeDeadTickets(queue);
                if (ticket.equals(first(queue))) {
                    for (Path path : locks) {
                        FairFileLease lease = tryAcquire(root, path, kind);
                        if (lease != null) { Files.deleteIfExists(ticket); return lease; }
                    }
                }
                if (!announced) {
                    System.out.println("  gate: queued for " + kind + " lease as " + ticket.getFileName());
                    announced = true;
                }
                Thread.sleep(200L);
            }
            throw new IllegalStateException("timed out waiting for " + kind + " lease");
        } finally { Files.deleteIfExists(ticket); }
    }

    private static Path issue(Path queue, long deadline) throws Exception {
        Path guard = queue.resolve("sequence.lock");
        while (System.currentTimeMillis() < deadline) {
            try (FileChannel channel = FileChannel.open(guard, StandardOpenOption.CREATE,
                    StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                try {
                    FileLock lock = channel.tryLock();
                    if (lock == null) { Thread.sleep(10L); continue; }
                    try (lock) {
                        long value = next(channel);
                        String name = "%020d-%d-%s.ticket".formatted(value,
                                ProcessHandle.current().pid(), UUID.randomUUID());
                        Path ticket = queue.resolve(name);
                        Files.writeString(ticket, "pid=" + ProcessHandle.current().pid()
                                + "\ncreated=" + Instant.now() + "\n", StandardCharsets.UTF_8,
                                StandardOpenOption.CREATE_NEW);
                        return ticket;
                    }
                } catch (OverlappingFileLockException error) { Thread.sleep(10L); }
            }
        }
        throw new IllegalStateException("timed out issuing FIFO lock ticket");
    }

    private static long next(FileChannel channel) throws Exception {
        channel.position(0); ByteBuffer input = ByteBuffer.allocate((int) channel.size());
        while (input.hasRemaining() && channel.read(input) >= 0) { }
        String text = new String(input.array(), StandardCharsets.UTF_8).trim();
        long value = text.isEmpty() ? 1L : Long.parseLong(text) + 1L;
        byte[] output = (value + "\n").getBytes(StandardCharsets.UTF_8);
        channel.truncate(0); channel.position(0); channel.write(ByteBuffer.wrap(output)); channel.force(true);
        return value;
    }

    private static Path first(Path queue) throws Exception {
        try (Stream<Path> paths = Files.list(queue)) {
            return paths.filter(path -> path.toString().endsWith(".ticket"))
                    .min(Comparator.comparing(path -> path.getFileName().toString())).orElse(null);
        }
    }

    private static void removeDeadTickets(Path queue) throws Exception {
        try (Stream<Path> paths = Files.list(queue)) {
            for (Path ticket : paths.filter(path -> path.toString().endsWith(".ticket")).toList()) {
                long pid = pid(ticket);
                if (pid > 0 && ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false)) continue;
                Files.deleteIfExists(ticket);
            }
        }
    }

    private static long pid(Path ticket) {
        try {
            for (String line : Files.readAllLines(ticket, StandardCharsets.UTF_8))
                if (line.startsWith("pid=")) return Long.parseLong(line.substring(4));
        } catch (Exception ignored) { }
        return -1L;
    }

    private static FairFileLease tryAcquire(Path root, Path path, String kind) throws IOException {
        Files.createDirectories(path.toAbsolutePath().normalize().getParent());
        FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE,
                StandardOpenOption.READ, StandardOpenOption.WRITE);
        try {
            FileLock lock = channel.tryLock();
            if (lock == null) { channel.close(); return null; }
            String metadata = "kind=" + kind + "\npid=" + ProcessHandle.current().pid()
                    + "\nstarted=" + Instant.now() + "\nroot=" + root + "\n";
            channel.truncate(0); channel.position(0);
            channel.write(ByteBuffer.wrap(metadata.getBytes(StandardCharsets.UTF_8))); channel.force(true);
            return new FairFileLease(channel, lock, path);
        } catch (OverlappingFileLockException error) {
            channel.close(); return null;
        } catch (IOException | RuntimeException error) {
            try { channel.close(); } catch (IOException close) { error.addSuppressed(close); }
            throw error;
        }
    }

    @Override public void close() {
        try { lock.release(); channel.close(); }
        catch (IOException error) { throw new IllegalStateException("could not release lock " + path, error); }
    }

    boolean valid() { return lock.isValid(); }
}
}
