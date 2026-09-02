import java.nio.file.Path;

/** Emits one exact module-cache input graph as canonical redacted JSON. */
public final class ModuleInputGraphQuery {
    private ModuleInputGraphQuery() { }

    public static void main(String[] arguments) {
        try {
            if (arguments.length != 1 || !arguments[0].matches("[a-z0-9]+(?:-[a-z0-9]+)*"))
                throw new IllegalArgumentException("usage: Gate.java --module-input-graph MODULE");
            Path root = Path.of("").toAbsolutePath().normalize();
            System.out.print(ModuleInputGraph.build(root,
                    StrictProperties.load(root.resolve("harness.properties")), arguments[0]).json());
        } catch (Exception error) {
            System.err.println("module input graph query failed: " + error.getMessage());
            System.exit(1);
        }
    }
}
