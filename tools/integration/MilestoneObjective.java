import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/** Makes a milestone a coherent capability package rather than one behavior atom. */
final class MilestoneObjective {
    private static final Pattern ID = Pattern.compile("m[0-9]+-[a-z0-9-]+");
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9-]{0,62}");
    private static final Pattern BINDING = Pattern.compile(
            "[a-z][A-Za-z0-9_.]*[A-Z][A-Za-z0-9_]*#[a-z][A-Za-z0-9_]*");
    private static final Pattern CENSUS_CLAIM = Pattern.compile(
            "b1\\.7\\.3:(block|entity)/[0-9]{3}#[a-z][a-z0-9-]{0,62}");
    private static final Set<String> KINDS = Set.of(
            "behavior-package", "structural-capability", "performance-package");
    private static final Set<String> LAYERS = Set.of("universal", "archetype", "singular");
    private final Path path;
    private final Properties values;

    private MilestoneObjective(Path path, Properties values) {
        this.path = path;
        this.values = values;
    }

    static MilestoneObjective load(Path root, String id, String goal) throws Exception {
        MilestoneObjective objective = load(root, id);
        require(objective.outcome().equals(goal),
                "worker goal differs from the reviewed milestone objective: " + id);
        return objective;
    }

    static MilestoneObjective load(Path root, String id, String goal, String base) throws Exception {
        MilestoneObjective objective = loadReviewed(root, id);
        require(objective.outcome().equals(goal),
                "worker goal differs from the reviewed milestone objective: " + id);
        objective.validateCensusClaims(root, base);
        return objective;
    }

    static MilestoneObjective load(Path root, String id) throws Exception {
        MilestoneObjective objective = loadReviewed(root, id);
        objective.validateCensusClaims(root, null);
        return objective;
    }

    static MilestoneObjective loadReviewed(Path root, String id) throws Exception {
        require(ID.matcher(id).matches(), "invalid milestone objective id: " + id);
        Path path = root.resolve("coordination/swarm/objectives")
                .resolve(id + ".properties");
        require(Files.isRegularFile(path), "missing reviewed milestone objective: " + path);
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        MilestoneObjective objective = new MilestoneObjective(path, values);
        objective.validate(id);
        return objective;
    }

    String outcome() {
        return required("outcome");
    }

    Path path() {
        return path;
    }

    List<String> claims() {
        return list("census.claims");
    }

    List<String> atoms() {
        return list("atoms");
    }

    String capability() {
        return required("capability");
    }

    void verifyDescriptor(Properties descriptor) {
        same(descriptor, "milestone.kind", required("kind"));
        same(descriptor, "milestone.capability", required("capability"));
        sameList(descriptor, "milestone.atoms", list("atoms"));
        sameList(descriptor, "milestone.layers", list("layers"));
        sameList(descriptor, "milestone.census.claims", list("census.claims"));
        sameList(descriptor, "milestone.acceptance", list("acceptance"));
        if ("behavior-package".equals(required("kind"))) {
            same(descriptor, "testkit.fixture", required("public.fixture"));
            sameList(descriptor, "testkit.actions", list("public.actions"));
            sameList(descriptor, "testkit.observations", list("public.observations"));
            same(descriptor, "testkit.binding", required("public.binding"));
        }
    }

    private void validate(String expectedId) {
        require("1".equals(required("schema")), "unsupported milestone objective schema");
        require(expectedId.equals(required("id")), "milestone objective identity drifted");
        String kind = required("kind");
        require(KINDS.contains(kind), "unknown milestone objective kind: " + kind);
        token(required("capability"), "capability");
        require(outcome().length() >= 40 && !outcome().toLowerCase().contains("milestone count"),
                "milestone outcome must describe user-visible capability, not count");
        tokenList("atoms", 3);
        tokenList("acceptance", 3);
        if ("behavior-package".equals(kind)) validateBehavior();
        if ("structural-capability".equals(kind)) validateStructural();
        if ("performance-package".equals(kind)) validatePerformance();
    }

    private void validateBehavior() {
        List<String> layers = list("layers");
        require(!layers.isEmpty() && LAYERS.containsAll(layers),
                "behavior package requires conformance layers");
        token(required("public.fixture"), "public fixture");
        tokenList("public.actions", 2);
        tokenList("public.observations", 2);
        require(BINDING.matcher(required("public.binding")).matches(),
                "invalid public TestKit binding");
        censusClaimList(3);
    }

    private void validateStructural() {
        tokenList("consumers", 3);
        namespacedClaims("structural", 1);
    }

    private void validatePerformance() {
        tokenList("scenes", 3);
        namespacedClaims("performance", 1);
    }

    private void censusClaimList(int minimum) {
        List<String> entries = list("census.claims");
        require(entries.size() >= minimum,
                "census.claims requires at least " + minimum + " entries");
        require(new HashSet<>(entries).size() == entries.size(), "duplicate census claim");
        for (String claim : entries) {
            require(CENSUS_CLAIM.matcher(claim).matches(), "invalid Functional Census claim: " + claim);
        }
    }

    private void namespacedClaims(String namespace, int minimum) {
        List<String> entries = list("census.claims");
        require(entries.size() >= minimum, "census.claims requires at least " + minimum + " entries");
        require(new HashSet<>(entries).size() == entries.size(), "duplicate census claim");
        for (String claim : entries) {
            require(claim.matches(namespace + "#[a-z][a-z0-9-]{0,62}"),
                    "invalid " + namespace + " claim: " + claim);
        }
    }

    private void validateCensusClaims(Path root, String revision) throws Exception {
        if (!"behavior-package".equals(required("kind"))) return;
        String base = "behavior/functional-census/";
        List<String> families = lines(root, revision, base + "families.tsv");
        java.util.Map<String, Set<String>> templatesBySubject = new java.util.HashMap<>();
        Set<String> resolved = new HashSet<>();
        for (int line = 1; line < families.size(); line++) {
            if (families.get(line).isBlank() || families.get(line).startsWith("#")) continue;
            String[] fields = families.get(line).split("\\t", -1);
            require(fields.length == 7, "invalid Functional Census family manifest");
            String directory = base + fields[1] + "/";
            Set<String> subjects = column(root, revision, directory + "subjects.tsv", 0);
            Set<String> templates = column(root, revision, directory + "templates.tsv", 0);
            for (String subject : subjects) templatesBySubject.put(subject, templates);
            resolved.addAll(resolvedClaims(root, revision, directory, subjects));
        }
        for (String claim : claims()) {
            String[] parts = claim.split("#", -1);
            Set<String> templates = parts.length == 2 ? templatesBySubject.get(parts[0]) : null;
            require(templates != null && templates.contains(parts[1]),
                    "objective references an unknown Functional Census claim: " + claim);
            require(!resolved.contains(claim), "objective references an already resolved claim: " + claim);
        }
    }

    private static Set<String> column(Path root, String revision, String relative, int index)
            throws Exception {
        Set<String> result = new HashSet<>();
        List<String> lines = lines(root, revision, relative);
        for (int line = 1; line < lines.size(); line++) {
            if (lines.get(line).isBlank() || lines.get(line).startsWith("#")) continue;
            String[] fields = lines.get(line).split("\\t", -1);
            require(fields.length > index && result.add(fields[index]),
                    "invalid census catalog: " + relative);
        }
        return result;
    }

    private static Set<String> resolvedClaims(Path root, String revision, String directory,
            Set<String> subjects) throws Exception {
        Set<String> resolved = new HashSet<>();
        List<String> claims = lines(root, revision, directory + "claims.tsv");
        Set<String> terminal = Set.of(
                "VERIFIED", "NATIVE_NONDETERMINISTIC", "NOT_APPLICABLE", "RETRACTED");
        for (int line = 1; line < claims.size(); line++) {
            if (claims.get(line).isBlank() || claims.get(line).startsWith("#")) continue;
            String[] fields = claims.get(line).split("\\t", -1);
            if (!terminal.contains(fields[5])) continue;
            if (fields[2].equals("*")) {
                for (String subject : subjects) resolved.add(subject + "#" + fields[3]);
            } else {
                resolved.add(fields[2] + "#" + fields[3]);
            }
        }
        List<String> exceptions = lines(root, revision, directory + "exceptions.tsv");
        for (int line = 1; line < exceptions.size(); line++) {
            if (exceptions.get(line).isBlank() || exceptions.get(line).startsWith("#")) continue;
            String[] fields = exceptions.get(line).split("\\t", -1);
            resolved.add(fields[0] + "#" + fields[1]);
        }
        return resolved;
    }

    private static List<String> lines(Path root, String revision, String relative) throws Exception {
        if (revision == null) {
            return Files.readAllLines(root.resolve(relative), StandardCharsets.UTF_8);
        }
        return SwarmProcess.output(root, List.of("git", "show", revision + ":" + relative), 60)
                .lines().toList();
    }

    private void same(Properties descriptor, String key, String expected) {
        require(expected.equals(required(descriptor, key)),
                "milestone descriptor differs from objective: " + key);
    }

    private void sameList(Properties descriptor, String key, List<String> expected) {
        require(expected.equals(split(required(descriptor, key))),
                "milestone descriptor differs from objective: " + key);
    }

    private List<String> list(String key) {
        return split(required(key));
    }

    private static List<String> split(String value) {
        List<String> result = new ArrayList<>();
        for (String item : value.split(",", -1)) result.add(item.trim());
        return List.copyOf(result);
    }

    private void tokenList(String key, int minimum) {
        List<String> entries = list(key);
        require(entries.size() >= minimum, key + " requires at least " + minimum + " entries");
        Set<String> unique = new HashSet<>();
        for (String value : entries) {
            token(value, key);
            require(unique.add(value), "duplicate " + key + " entry: " + value);
        }
    }

    private String required(String key) {
        return required(values, key);
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing " + key);
        return value.trim();
    }

    private static void token(String value, String label) {
        require(TOKEN.matcher(value).matches(), "invalid " + label + " token: " + value);
    }

    static void selfTest() throws Exception {
        require(CENSUS_CLAIM.matcher("b1.7.3:entity/090#movement-policy").matches(),
                "entity Functional Census claims are not routable");
        String text = "schema=1\nid=m1-block-conformance\nkind=behavior-package\n"
                + "capability=block-conformance\n"
                + "outcome=Deliver reusable block conformance across the vanilla registry.\n"
                + "atoms=place-break,drop-contract,persist-reload\n"
                + "layers=universal,archetype\n"
                + "census.claims=b1.7.3:block/001#gameplay-placement,"
                + "b1.7.3:block/001#break-transition,b1.7.3:block/001#save-reload\n"
                + "acceptance=registry-expansion,oracle-evidence,public-testkit\n"
                + "public.fixture=block-matrix\npublic.actions=place-block,break-block\n"
                + "public.observations=placed-state,drop-set\n"
                + "public.binding=worldline.testkit.BlockConformancePlan#cases\n";
        Properties values = new Properties();
        values.load(new StringReader(text));
        MilestoneObjective objective = new MilestoneObjective(Path.of("objective"), values);
        objective.validate("m1-block-conformance");
        Properties descriptor = new Properties();
        descriptor.setProperty("milestone.kind", "behavior-package");
        descriptor.setProperty("milestone.capability", "block-conformance");
        descriptor.setProperty("milestone.atoms", "place-break,drop-contract,persist-reload");
        descriptor.setProperty("milestone.layers", "universal,archetype");
        descriptor.setProperty("milestone.census.claims",
                "b1.7.3:block/001#gameplay-placement,b1.7.3:block/001#break-transition,"
                        + "b1.7.3:block/001#save-reload");
        descriptor.setProperty("milestone.acceptance",
                "registry-expansion,oracle-evidence,public-testkit");
        descriptor.setProperty("testkit.fixture", "block-matrix");
        descriptor.setProperty("testkit.actions", "place-block,break-block");
        descriptor.setProperty("testkit.observations", "placed-state,drop-set");
        descriptor.setProperty("testkit.binding", "worldline.testkit.BlockConformancePlan#cases");
        objective.verifyDescriptor(descriptor);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
