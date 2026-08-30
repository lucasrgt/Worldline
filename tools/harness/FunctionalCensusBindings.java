import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Validates strict public TestKit binding ledgers for opted-in Census families. */
final class FunctionalCensusBindings {
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9-]{0,62}");
    private static final Pattern BINDING = Pattern.compile(
            "(worldline\\.[A-Za-z][A-Za-z0-9.]+)#[a-z][A-Za-z0-9]*");
    private static final String HEADER =
            "subject_id\ttemplate_id\tfixture\tbinding\tevidence_id";

    private FunctionalCensusBindings() { }

    static Set<String> requiredFamilies(Properties schema) {
        String value = schema.getProperty("public.binding.manifest.families");
        require(value != null && !value.trim().isEmpty(), "missing binding manifest families");
        Set<String> result = new HashSet<>();
        for (String item : value.trim().split(",", -1)) {
            require(TOKEN.matcher(item).matches() && result.add(item),
                    "invalid family token: " + item);
        }
        return Set.copyOf(result);
    }

    static Map<String, String> load(Path root, Path path, Set<String> subjects,
            Set<String> templates) throws Exception {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        require(!lines.isEmpty() && HEADER.equals(lines.get(0)),
                "TestKit binding header drifted: " + path);
        Map<String, String> result = new LinkedHashMap<>();
        for (int line = 1; line < lines.size(); line++) {
            if (lines.get(line).isBlank() || lines.get(line).startsWith("#")) continue;
            String[] fields = lines.get(line).split("\\t", -1);
            require(fields.length == 5, "TestKit binding width drifted: " + path);
            String key = fields[0] + "#" + fields[1];
            require(subjects.contains(fields[0]) && templates.contains(fields[1])
                    && result.put(key, fields[4]) == null,
                    "invalid or duplicate TestKit binding: " + key);
            require(TOKEN.matcher(fields[2]).matches() && TOKEN.matcher(fields[4]).matches(),
                    "invalid TestKit binding metadata: " + key);
            Matcher matcher = BINDING.matcher(fields[3]);
            require(matcher.matches(), "invalid public TestKit binding: " + key);
            Path source = root.resolve("modules/testapi/src/main/java")
                    .resolve(matcher.group(1).replace('.', '/') + ".java");
            require(Files.isRegularFile(source),
                    "public TestKit binding source is absent: " + key);
        }
        require(!result.isEmpty(), "required TestKit binding ledger is empty");
        return Map.copyOf(result);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
