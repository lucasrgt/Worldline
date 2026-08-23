import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Forcefully terminates a runtime worker and confirms that its process tree is dead. */
final class ProcessTreeTermination {
    private ProcessTreeTermination() { }

    static void kill(Process process, String label) throws InterruptedException {
        List<ProcessHandle> descendants = process.descendants()
                .sorted(Comparator.comparingLong(ProcessHandle::pid).reversed()).toList();
        descendants.forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
        require(process.waitFor(10, TimeUnit.SECONDS), label + " did not terminate");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (alive(descendants) && System.nanoTime() < deadline) Thread.sleep(20L);
        require(!alive(descendants), label + " descendants did not terminate");
    }

    static void selfTest() throws Exception {
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "tools/containers/TerminationTreeProbe.java", "parent")
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (process.descendants().findAny().isEmpty() && System.nanoTime() < deadline)
            Thread.sleep(20L);
        require(process.descendants().findAny().isPresent(), "termination probe did not create a child");
        kill(process, "termination probe");
        require(!process.isAlive(), "termination probe parent survived");
    }

    private static boolean alive(List<ProcessHandle> processes) {
        return processes.stream().anyMatch(ProcessHandle::isAlive);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
