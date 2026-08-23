import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Renders the machine verify receipt as a compact GitHub job summary. */
public final class VerifySummary {
    private VerifySummary() { }

    public static void main(String[] arguments) {
        try {
            Path path = Path.of(arguments.length == 0
                    ? ".worldline/reports/verify.json" : arguments[0]);
            if (!Files.isRegularFile(path)) {
                System.out.println("## Worldline Gate\n\nNo `verify.json` was produced."); return;
            }
            System.out.print(render(Files.readString(path, StandardCharsets.UTF_8)));
        } catch (Exception error) {
            System.err.println("verify summary failed: " + error.getMessage()); System.exit(1);
        }
    }

    static String render(String json) {
        Map<String, Object> root = MiniJson.object(json);
        StringBuilder output = new StringBuilder("## Worldline Gate\n\n");
        output.append("- Profile: `").append(MiniJson.string(root, "profile")).append("`\n");
        output.append("- Status: **").append(MiniJson.string(root, "status")).append("**\n");
        output.append("- Elapsed: ").append(MiniJson.integer(root, "elapsed_ms")).append(" ms\n\n");
        output.append("| Stage | Status | Elapsed (ms) |\n| --- | --- | ---: |\n");
        for (Object value : MiniJson.array(root, "stages")) {
            Map<String, Object> stage = MiniJson.asObject(value, "stage");
            output.append("| ").append(MiniJson.string(stage, "name")).append(" | ")
                    .append(MiniJson.string(stage, "status")).append(" | ")
                    .append(MiniJson.integer(stage, "elapsed_ms")).append(" |\n");
        }
        return output.toString();
    }
}
