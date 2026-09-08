import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/** Harmless child used to exercise executor process and byte-capture boundaries. */
public final class ExecutorFixtureProbe {
    private ExecutorFixtureProbe() { }

    public static void main(String[] arguments) throws Exception {
        require(arguments.length > 0, "missing fixture scenario");
        switch (arguments[0]) {
            case "normal" -> write(System.out, "normal=" + required(arguments, 1) + "\n");
            case "stderr" -> write(System.err, "stderr=" + required(arguments, 1) + "\n");
            case "exit" -> {
                write(System.out, "exit=" + required(arguments, 1) + "\n");
                System.exit(Integer.parseInt(arguments[1]));
            }
            case "bulk" -> bulk(Integer.parseInt(required(arguments, 1)));
            case "crash" -> crash();
            case "environment" -> environment();
            case "interrupted-root" -> interruptedRoot(required(arguments, 1), required(arguments, 2));
            case "interrupted-child" -> interruptedChild(required(arguments, 1), required(arguments, 2));
            case "owner-target" -> ownerTarget();
            case "process-limit" -> processLimit();
            case "sleep" -> BoundedSleep.millis(Long.parseLong(required(arguments, 1)));
            case "tree-root" -> tree("tree-child", true);
            case "tree-child" -> tree("tree-leaf", false);
            case "tree-leaf" -> BoundedSleep.millis(60_000L);
            default -> throw new IllegalArgumentException("unknown fixture scenario");
        }
    }

    private static void bulk(int bytes) throws Exception {
        require(bytes > 65_536 && bytes <= 1_048_576, "invalid bulk fixture size");
        byte[] block = new byte[8192]; java.util.Arrays.fill(block, (byte) 'O');
        int remaining = bytes;
        while (remaining > 0) {
            int count = Math.min(block.length, remaining); System.out.write(block, 0, count); remaining -= count;
        }
        System.out.flush();
    }

    private static void environment() throws Exception {
        require("injected-value".equals(System.getenv("WORLDLINE_FIXTURE_VALUE")),
                "injected environment value is absent");
        require(System.getenv("WORLDLINE_FORBIDDEN") == null, "removed environment value survived");
        String temporary = System.getenv("TEMP");
        require(temporary != null && Path.of(temporary).getFileName().toString().equals("private temp"),
                "private temporary path drift");
        write(System.out, "environment=injected,removed,space-path\n");
    }

    private static void crash() throws Exception {
        write(System.out, "crash=ready\n"); Runtime.getRuntime().halt(-1_073_741_819);
    }

    private static void interruptedRoot(String marker, String completed) throws Exception {
        require(!marker.isBlank(), "empty interruption marker");
        new ProcessBuilder(java(), "-Xms16m", "-Xmx128m", "-XX:-UsePerfData",
                Path.of("tools/containers/ExecutorFixtureProbe.java").toAbsolutePath().toString(),
                "interrupted-child", marker, completed).start();
        write(System.out, "interrupted=ready\n"); BoundedSleep.millis(60_000L);
    }

    private static void interruptedChild(String marker, String completed) throws Exception {
        require(!marker.isBlank(), "empty interruption marker"); BoundedSleep.millis(2_000L);
        java.nio.file.Files.writeString(Path.of(completed), "escaped", StandardCharsets.UTF_8);
    }

    private static void ownerTarget() throws Exception {
        write(System.out, "owner-target=ready\n"); BoundedSleep.millis(60_000L);
    }

    private static void processLimit() throws Exception {
        try {
            Process child = new ProcessBuilder(java(), "-Xms16m", "-Xmx128m", "-XX:-UsePerfData",
                    Path.of("tools/containers/ExecutorFixtureProbe.java").toAbsolutePath().toString(),
                    "normal", "unexpected-child").start();
            child.destroyForcibly(); child.waitFor();
            throw new IllegalStateException("active-process limit allowed a child");
        } catch (java.io.IOException expected) {
            write(System.err, "process-limit=blocked\n"); System.exit(31);
        }
    }

    private static void tree(String child, boolean announce) throws Exception {
        new ProcessBuilder(java(), "-Xms16m", "-Xmx128m", "-XX:-UsePerfData",
                Path.of("tools/containers/ExecutorFixtureProbe.java").toAbsolutePath().toString(), child).start();
        if (announce) write(System.out, "tree=ready\n");
        BoundedSleep.millis(60_000L);
    }

    private static void write(java.io.PrintStream stream, String value) throws Exception {
        stream.write(value.getBytes(StandardCharsets.UTF_8)); stream.flush();
    }
    private static String required(String[] values, int index) {
        require(index < values.length && !values[index].isEmpty(), "missing fixture argument"); return values[index];
    }
    private static String java() {
        return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
