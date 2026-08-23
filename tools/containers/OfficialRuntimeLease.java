import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Holds the shared official-runtime lease around a delegated backend process. */
public final class OfficialRuntimeLease {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();
    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            Options options = Options.parse(arguments); Path lock = resolve(options.lock); Files.createDirectories(lock.getParent());
            try (FileChannel channel = FileChannel.open(lock, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 FileLock lease = channel.tryLock()) {
                require(lease != null, "another official-runtime owner holds " + lock);
                require(noForeignRuntime(), "an official smoke, Minecraft JAR, or runClient process is already active");
                Process process = new ProcessBuilder(options.command).directory(ROOT.toFile()).inheritIO().start();
                if (!process.waitFor(24, TimeUnit.HOURS)) {
                    terminate(process); throw new IllegalStateException("delegated backend timed out");
                }
                if (process.exitValue() != 0) System.exit(process.exitValue());
            }
        } catch (Exception error) { System.err.println("official runtime lease failed: " + error.getMessage()); System.exit(1); }
    }
    private static Path resolve(String explicit) throws Exception {
        if (explicit != null) return absolute(explicit);
        Properties values = new Properties();
        for (Path path : List.of(ROOT.resolve("tools/containers/host-pool.properties"), ROOT.resolve(".worldline/host-pool.properties")))
            if (Files.isRegularFile(path)) try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); }
        String configured = values.getProperty("runtime.lock.path", "").trim();
        return configured.isEmpty() ? defaultLock() : absolute(configured);
    }
    private static Path defaultLock() {
        String control = System.getenv("WORLDLINE_CONTROL_DIR");
        if (control != null && !control.isBlank())
            return Path.of(control).toAbsolutePath().normalize().resolve("official-b173.lock");
        boolean windows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows");
        String base = windows ? System.getenv("LOCALAPPDATA") : System.getenv("XDG_RUNTIME_DIR");
        if (base == null || base.isBlank()) base = System.getProperty("java.io.tmpdir");
        return Path.of(base).toAbsolutePath().normalize().resolve("worldline/locks/official-b173.lock");
    }
    private static Path absolute(String value) { Path path = Path.of(value); return (path.isAbsolute() ? path : ROOT.resolve(path)).normalize(); }
    private static boolean noForeignRuntime() {
        long self = ProcessHandle.current().pid();
        return ProcessHandle.allProcesses().filter(process -> process.pid() != self).noneMatch(process -> {
            String line = process.info().commandLine().orElse("").toLowerCase(Locale.ROOT).replace('\\', '/');
            return line.contains("tools/smoke/") || line.contains("runclient")
                    || line.contains("minecraft-b1.7.3-server.jar") || line.contains("minecraft-b1.7.3-client.jar");
        });
    }
    private static void terminate(Process process) throws Exception {
        List<ProcessHandle> descendants = process.descendants()
                .sorted(java.util.Comparator.comparingLong(ProcessHandle::pid).reversed()).toList();
        descendants.forEach(ProcessHandle::destroyForcibly); process.destroyForcibly();
        require(process.waitFor(10, TimeUnit.SECONDS), "delegated backend did not terminate");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (descendants.stream().anyMatch(ProcessHandle::isAlive) && System.nanoTime() < deadline)
            Thread.sleep(20L);
        require(descendants.stream().noneMatch(ProcessHandle::isAlive),
                "delegated backend descendants did not terminate");
    }
    private static void selfTest() throws Exception {
        Path path = ROOT.resolve(".worldline/runtime-lease-self-test.lock"); Files.createDirectories(path.getParent());
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock lock = channel.tryLock()) { require(lock != null, "lease self-test failed"); }
        Process probe = new ProcessBuilder(java(), "tools/containers/TerminationTreeProbe.java", "parent")
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        awaitDescendant(probe); terminate(probe);
        System.out.println("official runtime lease self-test passed");
    }
    private static void awaitDescendant(Process process) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (process.descendants().findAny().isEmpty() && System.nanoTime() < deadline)
            Thread.sleep(20L);
        require(process.descendants().findAny().isPresent(), "termination probe did not create a child");
    }
    private static String java() { return Path.of(System.getProperty("java.home"), "bin", "java").toString(); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalArgumentException(message); }
    private record Options(String lock, List<String> command) {
        static Options parse(String[] arguments) {
            int index = 0; String lock = null;
            if (arguments.length >= 2 && arguments[0].equals("--lock")) { lock = arguments[1]; index = 2; }
            require(index < arguments.length && arguments[index++].equals("--"), "usage: OfficialRuntimeLease.java [--lock FILE] -- COMMAND");
            List<String> command = new ArrayList<>(); while (index < arguments.length) command.add(arguments[index++]);
            require(!command.isEmpty(), "missing delegated command"); return new Options(lock, List.copyOf(command));
        }
    }
}
