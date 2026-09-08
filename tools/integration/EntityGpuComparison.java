import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import worldline.testkit.EntityGpuPortabilityFixture;

/** Compares retained native captures; no Minecraft process is started by this offline tool. */
public final class EntityGpuComparison {
    private EntityGpuComparison() {
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("first-frames second-frames output-evidence");
        }
        Path first = Path.of(args[0]), second = Path.of(args[1]);
        String evidence = EntityGpuPortabilityFixture.verify(Files.readString(first.resolve("device.txt")).trim(),
                frames(first), Files.readString(second.resolve("device.txt")).trim(), frames(second)).canonical();
        Files.writeString(Path.of(args[2]), evidence, StandardCharsets.UTF_8);
        System.out.println("entity cross-device RGBA comparison passed");
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
