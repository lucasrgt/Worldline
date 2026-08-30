import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/** Validates every finite b1.7.3 Functional Census family and its curated claims. */
public final class FunctionalCensusCheck {
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9-]{0,62}");
    private static final Pattern SHA = Pattern.compile("[0-9a-f]{64}");
    private static final Set<String> KINDS = Set.of("block", "entity");
    private static final Set<String> LAYERS = Set.of("UNIVERSAL", "ARCHETYPE", "SINGULAR");
    private static final Set<String> STATUSES = Set.of("VERIFIED", "PARTIAL", "UNKNOWN",
            "NATIVE_NONDETERMINISTIC", "NOT_APPLICABLE", "RETRACTED");
    private static final Set<String> APPLICABILITY = Set.of(
            "APPLICABLE", "UNKNOWN", "NOT_APPLICABLE");
    private static final Set<String> SURFACES = Set.of(
            "PUBLIC_TESTKIT", "INTERNAL_API", "SMOKE_ONLY", "NONE");
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        if (arguments.length != 0) {
            System.err.println("usage: java tools/harness/FunctionalCensusCheck.java");
            System.exit(2);
        }
        try {
            new FunctionalCensusCheck().execute();
        } catch (Exception error) {
            System.err.println("functional census failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        Path base = root.resolve("behavior/functional-census");
        Properties schema = properties(base.resolve("schema.properties"));
        require("2".equals(required(schema, "schema")), "unsupported functional census schema");
        String version = required(schema, "version");
        Set<String> bindingFamilies = FunctionalCensusBindings.requiredFamilies(schema);
        Table families = Table.read(base.resolve("families.tsv"), "family_id", "data_path",
                "subject_kind", "subjects_expected", "templates_expected",
                "candidate_claims_expected", "verified_claims_minimum");
        require(!families.rows.isEmpty(), "functional census has no families");
        Set<String> ids = new HashSet<>();
        Result total = new Result(0, 0, 0);
        int candidates = 0;
        for (Row family : families.rows) {
            String id = family.get("family_id"), kind = family.get("subject_kind");
            require(TOKEN.matcher(id).matches() && ids.add(id), "invalid or duplicate family: " + id);
            require(KINDS.contains(kind), "unsupported subject kind: " + kind);
            Path data = base.resolve(family.get("data_path")).normalize();
            require(data.startsWith(base), "family data escapes census root: " + id);
            FamilyResult result = validateFamily(data, family, version, kind,
                    bindingFamilies.contains(id));
            total = total.plus(result.result);
            candidates = Math.addExact(candidates, result.candidates);
            print(id, result.subjects, result.templates, result.candidates, result.singular,
                    result.result, schema);
        }
        require(ids.containsAll(bindingFamilies), "binding manifest names an unknown family");
        System.out.println("  functional census aggregate: " + ids.size() + " families, "
                + candidates + " candidate claims");
        printCoverage("aggregate", candidates, total, schema);
    }

    private FamilyResult validateFamily(Path data, Row family, String version, String kind,
            boolean bindingsRequired)
            throws Exception {
        Table subjects = Table.read(data.resolve("subjects.tsv"),
                "subject_id", "legacy_id", "name", "registry_class");
        Table profiles = Table.read(data.resolve("profiles.tsv"),
                "subject_id", "singular", "archetypes");
        Table templates = Table.read(data.resolve("templates.tsv"),
                "template_id", "default_layer", "operation", "observable");
        Table claims = Table.read(data.resolve("claims.tsv"), "claim_id", "layer",
                "subject_id", "template_id", "applicability", "status", "oracle",
                "evidence_id", "signature", "automation_surface");
        Table exceptions = Table.read(data.resolve("exceptions.tsv"), "subject_id",
                "template_id", "status", "justification", "evidence_id");
        Map<String, Row> subjectById = subjects(subjects, version, kind);
        Map<String, Row> templateById = templates(templates);
        Set<String> singular = profiles(profiles, subjectById);
        Map<String, String> bindings = bindingsRequired
                ? FunctionalCensusBindings.load(root, data.resolve("testkit-bindings.tsv"),
                        subjectById.keySet(), templateById.keySet())
                : Map.of();
        Result result = claims(claims, exceptions, subjectById, templateById, singular,
                bindings, bindingsRequired);
        int candidates = Math.multiplyExact(subjects.rows.size(), templates.rows.size());
        require(subjects.rows.size() == integer(family, "subjects_expected"),
                family.get("family_id") + " subject census drifted");
        require(templates.rows.size() == integer(family, "templates_expected"),
                family.get("family_id") + " template census drifted");
        require(candidates == integer(family, "candidate_claims_expected"),
                family.get("family_id") + " candidate denominator drifted");
        require(result.verified >= integer(family, "verified_claims_minimum"),
                family.get("family_id") + " verified claim floor regressed");
        require(result.resolved <= candidates, "resolved claims exceed denominator");
        return new FamilyResult(subjects.rows.size(), templates.rows.size(), candidates,
                singular.size(), result);
    }

    private Map<String, Row> subjects(Table table, String version, String kind) {
        Map<String, Row> result = new LinkedHashMap<>();
        Set<Integer> legacyIds = new HashSet<>();
        Pattern subject = Pattern.compile(Pattern.quote(version + ":" + kind + "/") + "[0-9]{3}");
        String classPrefix = kind.equals("block") ? "Block" : "Entity";
        for (Row row : table.rows) {
            String id = row.get("subject_id");
            require(subject.matcher(id).matches() && result.put(id, row) == null,
                    "invalid or duplicate subject: " + id);
            int legacy = Integer.parseInt(row.get("legacy_id"));
            require(legacy >= 1 && legacy <= 255 && legacyIds.add(legacy),
                    "invalid or duplicate legacy " + kind + " id: " + legacy);
            require(TOKEN.matcher(row.get("name")).matches(), "invalid subject name: " + id);
            require(row.get("registry_class").matches(classPrefix + "[A-Za-z0-9]*"),
                    "invalid registry class: " + id);
            require(id.endsWith(String.format(Locale.ROOT, "/%03d", legacy)),
                    "subject and legacy id differ: " + id);
        }
        return result;
    }

    private Map<String, Row> templates(Table table) {
        Map<String, Row> result = new LinkedHashMap<>();
        for (Row row : table.rows) {
            String id = row.get("template_id");
            require(TOKEN.matcher(id).matches() && result.put(id, row) == null,
                    "invalid or duplicate template: " + id);
            require(LAYERS.contains(row.get("default_layer")), "invalid default layer: " + id);
            require(TOKEN.matcher(row.get("operation")).matches(), "invalid operation: " + id);
            require(TOKEN.matcher(row.get("observable")).matches(), "invalid observable: " + id);
        }
        return result;
    }

    private Set<String> profiles(Table table, Map<String, Row> subjects) {
        Set<String> seen = new HashSet<>(), singular = new HashSet<>();
        for (Row row : table.rows) {
            String id = row.get("subject_id");
            require(subjects.containsKey(id) && seen.add(id), "invalid or duplicate profile: " + id);
            require(Set.of("true", "false").contains(row.get("singular")),
                    "invalid singular disposition: " + id);
            if (Boolean.parseBoolean(row.get("singular"))) singular.add(id);
            Set<String> archetypes = new HashSet<>();
            for (String archetype : row.get("archetypes").split(",", -1)) {
                require(TOKEN.matcher(archetype).matches() && archetypes.add(archetype),
                        "invalid or duplicate archetype: " + id);
            }
        }
        require(seen.equals(subjects.keySet()), "every registry subject requires one profile");
        require(!singular.isEmpty() && singular.size() < table.rows.size(), "layer routing is degenerate");
        return Set.copyOf(singular);
    }

    private Result claims(Table claims, Table exceptions, Map<String, Row> subjects,
            Map<String, Row> templates, Set<String> singular, Map<String, String> bindings,
            boolean bindingsRequired) throws Exception {
        Set<String> claimed = new HashSet<>();
        Set<String> publicClaims = new HashSet<>();
        int verified = 0, resolved = 0, publicTestkit = 0;
        for (Row row : claims.rows) {
            token(row.get("claim_id"), "claim id");
            require(LAYERS.contains(row.get("layer")), "invalid claim layer");
            require(APPLICABILITY.contains(row.get("applicability")), "invalid applicability");
            require(STATUSES.contains(row.get("status")), "invalid claim status");
            require(SURFACES.contains(row.get("automation_surface")), "invalid automation surface");
            String template = row.get("template_id");
            require(templates.containsKey(template), "unknown claim template: " + template);
            List<String> targets = row.get("subject_id").equals("*")
                    ? new ArrayList<>(subjects.keySet()) : List.of(row.get("subject_id"));
            for (String subject : targets) {
                require(subjects.containsKey(subject), "unknown claim subject: " + subject);
                String layer = templates.get(template).get("default_layer");
                if (singular.contains(subject) && layer.equals("ARCHETYPE")) layer = "SINGULAR";
                require(row.get("layer").equals(layer),
                        "claim layer differs from route: " + subject + "#" + template);
                require(claimed.add(subject + "#" + template), "duplicate functional claim");
                if (row.get("status").equals("VERIFIED")) verified++;
                if (row.get("status").equals("VERIFIED")
                        && row.get("automation_surface").equals("PUBLIC_TESTKIT")) {
                    publicTestkit++;
                    String key = subject + "#" + template;
                    publicClaims.add(key);
                    if (bindingsRequired) {
                        String binding = bindings.get(key);
                        require(binding != null, "public entity claim lacks TestKit binding: " + key);
                        require(binding.equals(row.get("evidence_id")),
                                "TestKit binding evidence differs: " + key);
                    }
                }
                if (Set.of("VERIFIED", "NATIVE_NONDETERMINISTIC", "NOT_APPLICABLE", "RETRACTED")
                        .contains(row.get("status"))) resolved++;
            }
            evidence(row);
        }
        for (Row row : exceptions.rows) {
            String key = row.get("subject_id") + "#" + row.get("template_id");
            require(subjects.containsKey(row.get("subject_id"))
                    && templates.containsKey(row.get("template_id")), "unknown exception target");
            require(Set.of("NOT_APPLICABLE", "NATIVE_NONDETERMINISTIC", "RETRACTED")
                    .contains(row.get("status")), "invalid exception status");
            require(row.get("justification").length() >= 20 && !row.get("evidence_id").isBlank(),
                    "exception requires evidence-backed justification");
            require(claimed.add(key), "claim and exception overlap: " + key);
            resolved++;
        }
        if (bindingsRequired) require(publicClaims.equals(bindings.keySet()),
                "entity TestKit binding ledger differs from public claims");
        require(publicTestkit <= verified, "public TestKit claims exceed verified claims");
        return new Result(resolved, verified, publicTestkit);
    }

    private void evidence(Row row) throws Exception {
        if (!row.get("status").equals("VERIFIED")) return;
        require(row.get("applicability").equals("APPLICABLE"), "verified claim must be applicable");
        require(SHA.matcher(row.get("signature")).matches(), "verified claim lacks signature");
        Path descriptor = root.resolve("smokes").resolve(row.get("evidence_id"))
                .resolve("smoke.properties");
        require(Files.isRegularFile(descriptor), "verified claim evidence is absent");
        require(row.get("signature").equals(properties(descriptor).getProperty("expected.signature")),
                "verified claim signature differs from evidence: " + row.get("claim_id"));
    }

    private static void print(String id, int subjects, int templates, int candidates, int singular,
            Result result, Properties schema) {
        System.out.println("  functional census [" + id + "]: " + subjects + " subjects x "
                + templates + " templates = " + candidates + " candidate claims");
        System.out.println("    verified=" + result.verified + ", resolved=" + result.resolved
                + ", unknown=" + (candidates - result.resolved) + ", singular-subjects=" + singular);
        printCoverage(id, candidates, result, schema);
    }

    private static void printCoverage(String id, int candidates, Result result, Properties schema) {
        double target = Double.parseDouble(required(schema, "target.proof.percent"));
        double publicTarget = Double.parseDouble(required(schema, "target.public-testkit.percent"));
        int targetClaims = (int) Math.ceil(candidates * target / 100.0);
        int publicClaims = (int) Math.ceil(candidates * publicTarget / 100.0);
        System.out.println("    " + id + " proof=" + decimal(percent(result.verified, candidates))
                + "%, target=" + decimal(target) + "%, verified-claims-to-target="
                + Math.max(0, targetClaims - result.verified));
        System.out.println("    " + id + " public-testkit=" + result.publicTestkit + "/" + candidates
                + ", coverage=" + decimal(percent(result.publicTestkit, candidates)) + "%, target="
                + decimal(publicTarget) + "%, claims-to-target="
                + Math.max(0, publicClaims - result.publicTestkit));
    }

    private static Properties properties(Path path) throws Exception {
        Properties result = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            result.load(reader);
        }
        return result;
    }

    private static int integer(Row row, String key) { return Integer.parseInt(row.get(key)); }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing " + key);
        return value.trim();
    }
    private static void token(String value, String label) {
        require(TOKEN.matcher(value).matches(), "invalid " + label + ": " + value);
    }
    private static double percent(int numerator, int denominator) {
        return denominator == 0 ? 0 : numerator * 100.0 / denominator;
    }
    private static String decimal(double value) { return String.format(Locale.ROOT, "%.2f", value); }

    private record Result(int resolved, int verified, int publicTestkit) {
        Result plus(Result other) {
            return new Result(resolved + other.resolved, verified + other.verified,
                    publicTestkit + other.publicTestkit);
        }
    }
    private record FamilyResult(int subjects, int templates, int candidates, int singular,
            Result result) {}
    private record Row(Map<String, String> values) { String get(String key) { return values.get(key); } }

    private static final class Table {
        final List<Row> rows;
        private Table(List<Row> rows) { this.rows = rows; }
        static Table read(Path path, String... expected) throws Exception {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            require(!lines.isEmpty() && lines.get(0).equals(String.join("\t", expected)),
                    "TSV header drifted: " + path);
            List<Row> rows = new ArrayList<>();
            for (int index = 1; index < lines.size(); index++) {
                String line = lines.get(index);
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] fields = line.split("\t", -1);
                require(fields.length == expected.length, "TSV width drifted: " + path);
                Map<String, String> values = new HashMap<>();
                for (int field = 0; field < expected.length; field++) {
                    require(!fields[field].isBlank(), "blank TSV field: " + expected[field]);
                    values.put(expected[field], fields[field]);
                }
                rows.add(new Row(Map.copyOf(values)));
            }
            return new Table(List.copyOf(rows));
        }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
