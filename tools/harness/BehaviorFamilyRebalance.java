import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Applies the reviewed placement taxonomy without changing executable smoke inputs. */
public final class BehaviorFamilyRebalance {
    private BehaviorFamilyRebalance() { }

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--apply")),
                    "usage: BehaviorFamilyRebalance --apply");
            Path root = Path.of("").toAbsolutePath().normalize(); int changed = 0;
            for (Map.Entry<String, String> assignment : BehaviorFamilyAssignments.values().entrySet()) {
                Path path = root.resolve("smokes").resolve(assignment.getKey()).resolve("smoke.properties");
                require(Files.isRegularFile(path), "missing placement descriptor " + assignment.getKey());
                String source = Files.readString(path, StandardCharsets.UTF_8).replace("\r\n", "\n");
                String current = behavior(source), target = assignment.getValue();
                require(current.equals("block-placement-persistence") || current.equals(target),
                        "unexpected placement behavior " + assignment.getKey() + ": " + current);
                if (!current.equals(target)) {
                    source = source.replace("behavior=" + current + "\n", "behavior=" + target + "\n");
                    Files.writeString(path, source, StandardCharsets.UTF_8); changed++;
                }
            }
            System.out.println("placement behavior families: " + changed + " descriptors updated");
        } catch (Exception error) {
            System.err.println("behavior family rebalance failed: " + error.getMessage()); System.exit(1);
        }
    }

    private static String behavior(String source) {
        String found = "";
        for (String line : source.split("\n")) if (line.startsWith("behavior=")) {
            require(found.isEmpty(), "duplicate behavior property"); found = line.substring(9).trim();
        }
        require(!found.isEmpty(), "missing behavior property"); return found;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
