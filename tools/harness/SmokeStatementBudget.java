import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

/** Enforces statement ceilings while legacy packing debt can only decrease. */
final class SmokeStatementBudget {
    private final Path root;
    private final Properties config = new Properties();
    private final Properties policy = new Properties();
    private final Properties debt = new Properties();

    SmokeStatementBudget(Path root) throws IOException {
        this.root = root;
        load(root.resolve("harness.properties"), config);
        load(root.resolve("quality/source-policy.properties"), policy);
        load(root.resolve("quality/smoke-statement-debt.properties"), debt);
    }

    public static void main(String[] arguments) throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        SmokeStatementBudget budget = new SmokeStatementBudget(root);
        if (List.of(arguments).equals(List.of("--inventory"))) budget.inventory();
        else budget.execute();
    }

    void execute() throws IOException {
        inspect("tools/smoke", integer(config, "smoke.runner.max.statements"));
        inspect("smokes", integer(config, "smoke.max.statements"));
        inspectSharedHelpers();
    }

    void candidate(Path runner, Path smoke) throws IOException {
        inspectFiles("tools/smoke", List.of(runner), integer(config, "smoke.runner.max.statements"), false);
        try (Stream<Path> files = Files.walk(smoke)) {
            inspectFiles("smokes", files.filter(this::javaFile).toList(),
                    integer(config, "smoke.max.statements"), false);
        }
    }

    private void inventory() throws IOException {
        for (String scope : List.of("tools/smoke", "smokes")) {
            int limit = integer(config, scope.equals("smokes")
                    ? "smoke.max.statements" : "smoke.runner.max.statements");
            int packed = 0, maximum = 0;
            try (Stream<Path> files = Files.walk(root.resolve(scope))) {
                for (Path file : files.filter(this::javaFile).toList()) {
                    Metrics metrics = measure(Files.readString(file, StandardCharsets.UTF_8));
                    packed += metrics.packedLines; maximum = Math.max(maximum, metrics.maximumPerLine);
                    if (metrics.statements > limit) System.out.println(root.relativize(file).toString()
                            .replace('\\', '/') + "=" + metrics.statements);
                }
            }
            System.out.println(scope + ".packed.lines=" + packed);
            System.out.println(scope + ".max.statements.per.line=" + maximum);
        }
    }

    private void inspect(String scope, int limit) throws IOException {
        Set<Path> tracked = SmokeTrackedFiles.read(root);
        try (Stream<Path> files = Files.walk(root.resolve(scope))) {
            inspectFiles(scope, files.filter(tracked::contains).filter(this::javaFile).toList(), limit, true);
        }
    }

    private void inspectFiles(String scope, List<Path> files, int limit, boolean complete) throws IOException {
        int statements = 0, packedLines = 0, maximumLine = 0, debtFiles = 0;
        Set<String> observedDebt = new HashSet<>();
        for (Path file : files) {
            Metrics metrics = measure(Files.readString(file, StandardCharsets.UTF_8));
            statements += metrics.statements;
            packedLines += metrics.packedLines;
            maximumLine = Math.max(maximumLine, metrics.maximumPerLine);
            String relative = root.relativize(file.toAbsolutePath().normalize()).toString().replace('\\', '/');
            String allowance = debt.getProperty(relative);
            if (metrics.statements > limit) {
                debtFiles++; observedDebt.add(relative);
                require(allowance != null, relative + " has " + metrics.statements + "/" + limit
                        + " statements and is not recorded as legacy debt");
                require(metrics.statements <= integer(debt, relative), relative + " statement debt grew: "
                        + metrics.statements + " > " + allowance);
            } else require(allowance == null, "stale statement debt entry: " + relative);
        }
        if (complete) {
            for (String path : debt.stringPropertyNames())
                if (path.startsWith(scope + "/")) require(observedDebt.contains(path),
                        "unobserved statement debt entry: " + path);
            checkMaximum(scope + ".packed.lines", packedLines);
            checkMaximum(scope + ".max.statements.per.line", maximumLine);
        }
        System.out.println("  " + scope + " statements: " + statements + " (max file " + limit
                + "; legacy debt " + debtFiles + "; packed lines " + packedLines + ")");
    }

    private void inspectSharedHelpers() throws IOException {
        Path shared = root.resolve("smokes/shared");
        Set<String> declared = new HashSet<>(); int consumers = 0;
        try (Stream<Path> descriptors = Files.walk(root.resolve("smokes"))) {
            for (Path descriptor : descriptors.filter(path -> path.getFileName().toString()
                    .equals("smoke.properties")).toList()) {
                Properties values = new Properties(); load(descriptor, values);
                String raw = values.getProperty("shared.inputs", "").trim();
                if (raw.isEmpty()) continue;
                consumers++;
                for (String value : raw.split(",")) {
                    String path = value.trim();
                    require(path.matches("smokes/shared/[a-z0-9/-]+"),
                            "unsafe shared helper path: " + path);
                    require(Files.isDirectory(root.resolve(path)), "missing shared helper: " + path);
                    declared.add(path.substring("smokes/shared/".length()).split("/")[0]);
                }
            }
        }
        int helpers = 0;
        try (Stream<Path> families = Files.list(shared)) {
            for (Path family : families.filter(Files::isDirectory).toList()) {
                String name = family.getFileName().toString();
                require(declared.contains(name), "unreferenced shared helper family: " + name);
                try (Stream<Path> files = Files.walk(family)) {
                    int count = (int) files.filter(this::javaFile).count();
                    require(count > 0, "shared helper family has no Java source: " + name);
                    helpers += count;
                }
            }
        }
        System.out.println("  shared smoke helpers: " + helpers + " files, " + consumers + " consumers");
    }

    static Metrics measure(String source) {
        int statements = 0, packed = 0, maximum = 0, line = 0;
        boolean string = false, character = false, lineComment = false, blockComment = false, escaped = false;
        boolean pendingFor = false; int parentheses = 0, forParentheses = -1;
        StringBuilder identifier = new StringBuilder();
        for (int index = 0; index < source.length(); index++) {
            char value = source.charAt(index), next = index + 1 < source.length() ? source.charAt(index + 1) : 0;
            if (lineComment) {
                if (value == '\n') { lineComment = false; maximum = Math.max(maximum, line);
                    if (line > 1) packed++; line = 0; }
                continue;
            }
            if (blockComment) {
                if (value == '*' && next == '/') { blockComment = false; index++; }
                else if (value == '\n') { maximum = Math.max(maximum, line); if (line > 1) packed++; line = 0; }
                continue;
            }
            if (string || character) {
                if (escaped) escaped = false;
                else if (value == '\\') escaped = true;
                else if (string && value == '"') string = false;
                else if (character && value == '\'') character = false;
                if (value == '\n') { maximum = Math.max(maximum, line); if (line > 1) packed++; line = 0; }
                continue;
            }
            if (value == '/' && next == '/') { lineComment = true; index++; continue; }
            if (value == '/' && next == '*') { blockComment = true; index++; continue; }
            if (value == '"') { string = true; pendingFor = false; continue; }
            if (value == '\'') { character = true; pendingFor = false; continue; }
            if (Character.isJavaIdentifierPart(value)) { identifier.append(value); continue; }
            if (!identifier.isEmpty()) {
                pendingFor = identifier.toString().equals("for"); identifier.setLength(0);
            }
            if (value == '(') {
                parentheses++; if (pendingFor) forParentheses = parentheses; pendingFor = false;
            } else if (value == ')') {
                if (parentheses == forParentheses) forParentheses = -1;
                parentheses = Math.max(0, parentheses - 1); pendingFor = false;
            } else if (value == ';') {
                statements++; if (forParentheses < 0) line++; pendingFor = false;
            } else if (!Character.isWhitespace(value)) pendingFor = false;
            if (value == '\n') { maximum = Math.max(maximum, line); if (line > 1) packed++; line = 0; }
        }
        maximum = Math.max(maximum, line); if (line > 1) packed++;
        return new Metrics(statements, packed, maximum);
    }

    private boolean javaFile(Path path) { return Files.isRegularFile(path) && path.toString().endsWith(".java"); }
    private void checkMaximum(String key, int actual) {
        int maximum = integer(policy, key);
        require(actual <= maximum, key + " grew: " + actual + " > " + maximum);
    }
    private static void load(Path path, Properties target) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { target.load(reader); }
    }
    private static int integer(Properties values, String key) {
        String value = values.getProperty(key); require(value != null, "missing policy key: " + key);
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    static final class Metrics {
        final int statements, packedLines, maximumPerLine;
        Metrics(int statements, int packedLines, int maximumPerLine) {
            this.statements = statements; this.packedLines = packedLines; this.maximumPerLine = maximumPerLine;
        }
    }
}
