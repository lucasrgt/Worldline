import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.lang.reflect.InvocationTargetException;

/** Compares retained native captures; no Minecraft process is started by this offline tool. */
public final class EntityGpuComparison {
    private EntityGpuComparison() {
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("first-frames second-frames output-evidence");
        }
        Path first = Path.of(args[0]), second = Path.of(args[1]);
        String evidence = compare(first, second);
        Files.writeString(Path.of(args[2]), evidence, StandardCharsets.UTF_8);
        System.out.println("entity cross-device RGBA comparison passed");
    }
    private static String compare(Path first, Path second) throws Exception {
        try {
            Class<?> fixture = Class.forName("worldline.testkit.EntityGpuPortabilityFixture");
            Object result = fixture.getMethod("verify", String.class, Map.class, String.class, Map.class)
                    .invoke(null, Files.readString(first.resolve("device.txt")).trim(), frames(first),
                            Files.readString(second.resolve("device.txt")).trim(), frames(second));
            return (String) result.getClass().getMethod("canonical").invoke(result);
        } catch (ClassNotFoundException error) {
            throw new IllegalArgumentException("Supply the TestKit API and runner JARs on --class-path", error);
        } catch (InvocationTargetException error) {
            if (error.getCause() instanceof Exception cause)
                throw cause;
            throw error;
        }
    }
    private static Map<String, byte[]> frames(Path directory) throws Exception {
        Map<String, byte[]> values = new TreeMap<>();
        try (var paths = Files.list(directory)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".rgba")).toList()) {
                String name = path.getFileName().toString();
                values.put(name.substring(0, name.length() - 5), Files.readAllBytes(path));
            }
        }
        return values;
    }
}
