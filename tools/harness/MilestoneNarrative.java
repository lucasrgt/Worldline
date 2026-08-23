import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Generates the combined claim and cycle document for schema-v1 narratives. */
final class MilestoneNarrative {
    private MilestoneNarrative() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 1, "usage: MilestoneNarrative ID");
            Path root = Path.of("").toAbsolutePath().normalize();
            Properties descriptor = load(root.resolve("smokes").resolve(arguments[0])
                    .resolve("smoke.properties"));
            Path output = output(root, descriptor);
            Files.writeString(output, render(descriptor), StandardCharsets.UTF_8);
            System.out.println("milestone narrative updated: " + root.relativize(output));
        } catch (Exception error) {
            System.err.println("milestone narrative failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static void validate(Path root, Properties descriptor) throws IOException {
        if (!"1".equals(descriptor.getProperty("narrative.schema"))) return;
        require(descriptor.getProperty("qualification.docs", "").equals(
                descriptor.getProperty("qualification.cycle", "")),
                "generated narrative must combine qualification.docs and qualification.cycle");
        Path output = output(root, descriptor);
        require(Files.isRegularFile(output), "missing generated milestone narrative");
        require(Files.readString(output, StandardCharsets.UTF_8).equals(render(descriptor)),
                root.relativize(output) + " drifted; regenerate it with MilestoneNarrative");
    }

    static String render(Properties descriptor) {
        require("1".equals(required(descriptor, "narrative.schema")), "unsupported narrative schema");
        String id = required(descriptor, "id").toUpperCase();
        return "# " + id + " " + required(descriptor, "narrative.title") + "\n\n"
                + "<!-- Generated from smoke.properties by MilestoneNarrative. -->\n\n"
                + "## Claim\n\n" + required(descriptor, "narrative.claim") + "\n\n"
                + "## Qualification cycle\n\n" + required(descriptor, "narrative.cycle") + "\n\n"
                + "Expected signal: `" + required(descriptor, "expected.signal") + "`.\n\n"
                + "Frozen semantic SHA-256: `" + required(descriptor, "expected.signature") + "`.\n";
    }

    private static Path output(Path root, Properties descriptor) {
        Path docs = root.resolve("docs").toAbsolutePath().normalize();
        Path output = root.resolve(required(descriptor, "qualification.docs")).normalize();
        require(output.startsWith(docs), "generated narrative must live under docs/");
        return output;
    }

    private static Properties load(Path path) throws IOException {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); }
        return values;
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key, "").trim();
        require(!value.isEmpty(), "missing narrative field: " + key); return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
