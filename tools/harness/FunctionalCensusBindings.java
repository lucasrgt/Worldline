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
            "(worldline\\.(?:testkit|testapi)\\.[A-Z][A-Za-z0-9]+)#([a-z][A-Za-z0-9]*)");
    private static final List<String> PUBLIC_MODULES = List.of("testapi", "testkit");
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
            Path source = publicSource(root, matcher.group(1));
            require(source != null, "public TestKit binding source is absent: " + key);
            String java = Files.readString(source, StandardCharsets.UTF_8);
            String simpleName = matcher.group(1).substring(matcher.group(1).lastIndexOf('.') + 1);
            require(Pattern.compile("\\bpublic\\s+(?:(?:final|abstract)\\s+)?(?:class|interface)\\s+"
                    + Pattern.quote(simpleName) + "\\b").matcher(java).find(),
                    "TestKit binding type is not public: " + key);
            require(Pattern.compile("\\bpublic\\s+(?:static\\s+)?[^;{}]*\\b"
                    + Pattern.quote(matcher.group(2)) + "\\s*\\(", Pattern.DOTALL)
                    .matcher(java).find(), "TestKit binding method is not public: " + key);
            for (String sourceLine : java.split("\\R", -1)) {
                String imported = sourceLine.trim();
                if (!imported.startsWith("import worldline.")) continue;
                require(imported.startsWith("import worldline.api.")
                        || imported.startsWith("import worldline.extension.")
                        || imported.startsWith("import worldline.test.")
                        || imported.startsWith("import worldline.testapi.")
                        || imported.startsWith("import worldline.testkit."),
                        "public TestKit binding imports internal code: " + key);
            }
        }
        require(!result.isEmpty(), "required TestKit binding ledger is empty");
        return Map.copyOf(result);
    }

    private static Path publicSource(Path root, String bindingClass) {
        String relative = bindingClass.replace('.', '/') + ".java";
        for (String module : PUBLIC_MODULES) {
            Path source = root.resolve("modules").resolve(module).resolve("src/main/java")
                    .resolve(relative);
            if (Files.isRegularFile(source)) return source;
        }
        return null;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
