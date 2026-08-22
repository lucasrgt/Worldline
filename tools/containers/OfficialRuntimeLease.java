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
                require(process.waitFor(24, TimeUnit.HOURS), "delegated backend timed out");
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
        return configured.isEmpty() ? Path.of(System.getProperty("user.home"), ".worldline/official-runtime.lock") : absolute(configured);
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
    private static void selfTest() throws Exception {
        Path path = ROOT.resolve(".worldline/runtime-lease-self-test.lock"); Files.createDirectories(path.getParent());
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock lock = channel.tryLock()) { require(lock != null, "lease self-test failed"); }
        System.out.println("official runtime lease self-test passed");
    }
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
