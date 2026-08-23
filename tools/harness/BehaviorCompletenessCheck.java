import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Ratchets milestone manifests toward composable TestKit behavior contracts. */
public final class BehaviorCompletenessCheck {
    private static final Pattern MILESTONE = Pattern.compile("m(\\d+)-[a-z0-9-]+");
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9-]{0,62}");
    private static final Pattern CATALOG = Pattern.compile("define\\(\\s*\"([a-z][a-z0-9-]+)\"");
    private static final Pattern BINDING = Pattern.compile(
            "([a-z][A-Za-z0-9_.]*[A-Z][A-Za-z0-9_]*)#([a-z][A-Za-z0-9_]*)");
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Set<String> behaviorCatalog = new HashSet<String>();
    private final Set<String> contractCatalog = new HashSet<String>();
    private final Set<String> pendingNonnumeric = new HashSet<String>();
    private int pendingMax;
    private int pendingExpected;

    public static void main(String[] arguments) {
        if (arguments.length != 0) {
            System.err.println("usage: java tools/harness/BehaviorCompletenessCheck.java");
            System.exit(2);
        }
        try { new BehaviorCompletenessCheck().execute(); }
        catch (Exception error) {
            System.err.println("behavior completeness failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        loadPolicy();
        loadCatalog("WorldlineBehavior.java", behaviorCatalog);
        loadCatalog("WorldlinePlacementBehaviors.java", behaviorCatalog);
        loadCatalog("WorldlineContract.java", contractCatalog);
        int complete = 0, pending = 0, total = 0;
        for (Path manifest : manifests()) {
            total++;
            Properties smoke = load(manifest);
            String directory = manifest.getParent().getFileName().toString();
            String behavior = smoke.getProperty("behavior", "").trim();
            String contract = smoke.getProperty("testkit.contract", "").trim();
            require(behavior.isEmpty() || contract.isEmpty(),
                    "manifest declares behavior and tooling contract: " + relative(manifest));
            if (behavior.isEmpty() && contract.isEmpty()) { requirePending(directory); pending++; }
            else { validateContract(smoke, manifest, behavior.isEmpty() ? contract : behavior,
                    behavior.isEmpty() ? contractCatalog : behaviorCatalog); complete++; }
        }
        require(pending == pendingExpected,
                "backfill ratchet drift: expected " + pendingExpected + " but found " + pending);
        System.out.println("  behavior completeness: " + complete + " complete, " + pending
                + " pending backfill, " + total + " manifests");
    }

    private void loadPolicy() throws IOException {
        Properties policy = load(root.resolve("behavior/coverage.properties"));
        require("1".equals(required(policy, "schema")), "unsupported behavior coverage schema");
        pendingMax = Integer.parseInt(required(policy, "pending.max.milestone"));
        pendingExpected = Integer.parseInt(required(policy, "pending.expected"));
        String nonnumeric = policy.getProperty("pending.nonnumeric");
        require(nonnumeric != null, "missing pending.nonnumeric");
        if (!nonnumeric.trim().isEmpty())
            Collections.addAll(pendingNonnumeric, nonnumeric.trim().split(","));
    }

    private void loadCatalog(String file, Set<String> target) throws IOException {
        Path source = root.resolve("modules/api/src/main/java/worldline/api").resolve(file);
        Matcher matcher = CATALOG.matcher(new String(Files.readAllBytes(source), StandardCharsets.UTF_8));
        while (matcher.find()) require(target.add(matcher.group(1)), "duplicate contract catalog token");
        require(!target.isEmpty(), "empty contract catalog " + file);
    }

    private List<Path> manifests() throws IOException {
        try (Stream<Path> paths = Files.walk(root.resolve("smokes"), 2)) {
            return paths.filter(path -> path.getFileName().toString().equals("smoke.properties"))
                    .sorted().collect(Collectors.toList());
        }
    }

    private void requirePending(String directory) {
        Matcher matcher = MILESTONE.matcher(directory);
        if (matcher.matches()) {
            int number = Integer.parseInt(matcher.group(1));
            require(number <= pendingMax, "new milestone lacks behavior contract: " + directory);
            return;
        }
        require(pendingNonnumeric.contains(directory), "unregistered smoke lacks behavior contract: " + directory);
    }

    private void validateContract(Properties smoke, Path manifest, String identity,
            Set<String> catalog) throws IOException {
        require(TOKEN.matcher(identity).matches() && catalog.contains(identity),
                "unknown TestKit contract " + identity + " in " + relative(manifest));
        token(required(smoke, "testkit.fixture"), "fixture", manifest);
        tokenList(required(smoke, "testkit.actions"), "actions", manifest);
        tokenList(required(smoke, "testkit.observations"), "observations", manifest);
        require("equatable".equals(required(smoke, "testkit.evidence")),
                "testkit.evidence must be equatable in " + relative(manifest));
        validateBinding(required(smoke, "testkit.binding"), manifest);
        require(!"pending".equals(required(smoke, "expected.signature")),
                "behavior signature is pending in " + relative(manifest));
        require(!"pending".equals(required(smoke, "expected.signal")),
                "behavior signal is pending in " + relative(manifest));
    }

    private void validateBinding(String value, Path manifest) throws IOException {
        Matcher matcher = BINDING.matcher(value);
        require(matcher.matches(), "invalid TestKit binding in " + relative(manifest));
        String suffix = matcher.group(1).replace('.', '/') + ".java";
        List<Path> matches = new ArrayList<Path>();
        for (String tree : new String[] {"modules", "adapters"}) {
            try (Stream<Path> paths = Files.walk(root.resolve(tree))) {
                paths.filter(path -> path.toString().replace('\\', '/').endsWith(suffix)).forEach(matches::add);
            }
        }
        require(matches.size() == 1, "binding class is absent or ambiguous in " + relative(manifest));
        String source = new String(Files.readAllBytes(matches.get(0)), StandardCharsets.UTF_8);
        require(source.contains(matcher.group(2) + "("), "binding method absent in " + relative(manifest));
    }

    private void tokenList(String value, String label, Path manifest) {
        Set<String> seen = new HashSet<String>();
        for (String item : value.split(",", -1)) {
            String token = item.trim(); token(token, label, manifest);
            require(seen.add(token), "duplicate " + label + " in " + relative(manifest));
        }
    }

    private void token(String value, String label, Path manifest) {
        require(TOKEN.matcher(value).matches(), "invalid " + label + " in " + relative(manifest));
    }

    private Properties load(Path path) throws IOException {
        Properties properties = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing " + key);
        return value.trim();
    }

    private String relative(Path path) { return root.relativize(path).toString().replace('\\', '/'); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
