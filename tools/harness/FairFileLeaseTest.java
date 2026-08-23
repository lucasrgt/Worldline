import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/** Proves FIFO acquisition order and safe queue cleanup under contention. */
public final class FairFileLeaseTest {
    public static void main(String[] arguments) {
        Path temporary = null;
        try {
            temporary = Files.createTempDirectory("worldline-fair-lease-");
            Path lock = temporary.resolve("resource.lock");
            Path queue = temporary.resolve("resource.queue");
            List<Integer> order = java.util.Collections.synchronizedList(new ArrayList<>());
            CountDownLatch release = new CountDownLatch(1);
            try (FairLeaseCommand.FairFileLease held = FairLeaseCommand.FairFileLease.acquire(
                    temporary, List.of(lock), queue,
                    "self-test", 10_000)) {
                require(held.valid(), "initial lease is invalid");
                Thread second = waiter(temporary, lock, queue, 2, order, null);
                second.start(); awaitTickets(queue, 1);
                Thread third = waiter(temporary, lock, queue, 3, order, release);
                third.start(); awaitTickets(queue, 2);
            }
            release.countDown();
            long deadline = System.currentTimeMillis() + 10_000;
            while (order.size() < 2 && System.currentTimeMillis() < deadline) Thread.sleep(20L);
            require(order.equals(List.of(2, 3)), "FIFO order drifted: " + order);
            require(ticketCount(queue) == 0, "lease tickets leaked");
            Path partial = queue.resolve("99999999999999999998-1-partial.ticket");
            Files.writeString(partial, "pid=");
            FairLeaseCommand.FairFileLease.removeDeadTickets(queue);
            require(Files.exists(partial), "fresh partial ticket was deleted");
            Files.setLastModifiedTime(partial, FileTime.fromMillis(System.currentTimeMillis() - 31_000L));
            FairLeaseCommand.FairFileLease.removeDeadTickets(queue);
            require(!Files.exists(partial), "abandoned partial ticket was retained");
            Path reused = queue.resolve("99999999999999999999-1-reused.ticket");
            Files.writeString(reused, "pid=" + ProcessHandle.current().pid()
                    + "\nprocess-start=1\ncreated=1970-01-01T00:00:00Z\n");
            FairLeaseCommand.FairFileLease.removeDeadTickets(queue);
            require(!Files.exists(reused), "PID-reuse ticket was retained");
            System.out.println("  fair file lease self-test: passed");
        } catch (Exception error) {
            System.err.println("fair file lease self-test failed: " + error.getMessage());
            System.exit(1);
        } finally {
            if (temporary != null) try { SafeTreeDelete.delete(temporary); }
            catch (Exception ignored) { }
        }
    }

    private static Thread waiter(Path root, Path lock, Path queue, int value,
            List<Integer> order, CountDownLatch release) {
        return Thread.ofPlatform().unstarted(() -> {
            try (FairLeaseCommand.FairFileLease ignored = FairLeaseCommand.FairFileLease.acquire(
                    root, List.of(lock), queue,
                    "self-test", 10_000)) {
                require(ignored.valid(), "queued lease is invalid");
                order.add(value);
                if (release != null) release.await();
            } catch (Exception error) { throw new IllegalStateException(error); }
        });
    }

    private static void awaitTickets(Path queue, long expected) throws Exception {
        long deadline = System.currentTimeMillis() + 5_000;
        while (ticketCount(queue) < expected && System.currentTimeMillis() < deadline) Thread.sleep(10L);
        require(ticketCount(queue) >= expected, "ticket was not issued");
    }

    private static long ticketCount(Path queue) throws Exception {
        if (!Files.isDirectory(queue)) return 0;
        try (var paths = Files.list(queue)) {
            return paths.filter(path -> path.toString().endsWith(".ticket")).count();
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
