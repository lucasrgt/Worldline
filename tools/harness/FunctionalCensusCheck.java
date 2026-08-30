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

/** Validates the finite b1.7.3 gameplay denominator and its curated proof claims. */
public final class FunctionalCensusCheck {
    private static final Pattern SUBJECT = Pattern.compile("b1\\.7\\.3:block/[0-9]{3}");
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9-]{0,62}");
    private static final Pattern SHA = Pattern.compile("[0-9a-f]{64}");
    private static final Set<String> LAYERS = Set.of("UNIVERSAL", "ARCHETYPE", "SINGULAR");
    private static final Set<String> STATUSES = Set.of("VERIFIED", "PARTIAL", "UNKNOWN",
            "NATIVE_NONDETERMINISTIC", "NOT_APPLICABLE", "RETRACTED");
    private static final Set<String> APPLICABILITY = Set.of(
            "APPLICABLE", "UNKNOWN", "NOT_APPLICABLE");
    private static final Set<String> SURFACES = Set.of(
            "PUBLIC_TESTKIT", "INTERNAL_API", "SMOKE_ONLY", "NONE");
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path census = root.resolve("behavior/functional-census/b1.7.3");

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
        Properties schema = properties(root.resolve("behavior/functional-census/schema.properties"));
        require("1".equals(required(schema, "schema")), "unsupported functional census schema");
        require("b1.7.3".equals(required(schema, "version")), "functional census version drifted");
        Table subjects = Table.read(census.resolve("subjects.tsv"),
                "subject_id", "legacy_id", "name", "registry_class");
        Table profiles = Table.read(census.resolve("profiles.tsv"),
                "subject_id", "singular", "archetypes");
        Table templates = Table.read(census.resolve("templates.tsv"),
                "template_id", "default_layer", "operation", "observable");
        Table claims = Table.read(census.resolve("claims.tsv"), "claim_id", "layer",
                "subject_id", "template_id", "applicability", "status", "oracle",
                "evidence_id", "signature", "automation_surface");
        Table exceptions = Table.read(census.resolve("exceptions.tsv"), "subject_id",
                "template_id", "status", "justification", "evidence_id");
        Map<String, Row> subjectById = subjectsById(subjects);
        Map<String, Row> templateById = templatesById(templates);
        Set<String> singularSubjects = validateProfiles(profiles, subjectById);
        Result result = validateClaims(claims, exceptions, subjectById, templateById,
                singularSubjects);
        int expectedSubjects = integer(schema, "subjects.expected");
        int expectedTemplates = integer(schema, "templates.expected");
        int candidates = Math.multiplyExact(subjects.rows.size(), templates.rows.size());
        require(subjects.rows.size() == expectedSubjects, "subject census drifted");
        require(templates.rows.size() == expectedTemplates, "template census drifted");
        require(candidates == integer(schema, "candidate.claims.expected"),
                "candidate claim denominator drifted");
        require(result.verified >= integer(schema, "verified.claims.minimum"),
                "verified claim floor regressed");
        require(result.resolved <= candidates, "resolved claims exceed denominator");
        int unknown = candidates - result.resolved;
        double proof = percent(result.verified, candidates);
        double target = Double.parseDouble(required(schema, "target.proof.percent"));
        int targetClaims = (int) Math.ceil(candidates * target / 100.0);
        double publicCoverage = percent(result.publicTestkit, candidates);
        double publicTarget = Double.parseDouble(required(schema, "target.public-testkit.percent"));
        int publicTargetClaims = (int) Math.ceil(candidates * publicTarget / 100.0);
        System.out.println("  functional census: " + subjects.rows.size() + " subjects x "
                + templates.rows.size() + " templates = " + candidates + " candidate claims");
        System.out.println("    verified=" + result.verified + ", resolved=" + result.resolved
                + ", unknown=" + unknown + ", singular-subjects=" + singularSubjects.size());
        System.out.println("    proof=" + decimal(proof) + "%, target=" + decimal(target)
                + "%, verified-claims-to-target=" + Math.max(0, targetClaims - result.verified));
        System.out.println("    public-testkit=" + result.publicTestkit + "/" + candidates
                + ", coverage=" + decimal(publicCoverage) + "%, target="
                + decimal(publicTarget) + "%, claims-to-target="
                + Math.max(0, publicTargetClaims - result.publicTestkit));
    }

    private Map<String, Row> subjectsById(Table subjects) {
        Map<String, Row> result = new LinkedHashMap<>();
        Set<Integer> legacyIds = new HashSet<>();
        for (Row row : subjects.rows) {
            String id = row.get("subject_id");
            require(SUBJECT.matcher(id).matches() && result.put(id, row) == null,
                    "invalid or duplicate subject: " + id);
            int legacy = Integer.parseInt(row.get("legacy_id"));
            require(legacy >= 1 && legacy <= 255 && legacyIds.add(legacy),
                    "invalid or duplicate legacy block id: " + legacy);
            require(TOKEN.matcher(row.get("name")).matches(), "invalid block name: " + id);
            require(row.get("registry_class").matches("Block[A-Za-z0-9]*"),
                    "invalid registry class: " + id);
            require(id.endsWith(String.format(Locale.ROOT, "/%03d", legacy)),
                    "subject and legacy id differ: " + id);
        }
        return result;
    }

    private Map<String, Row> templatesById(Table templates) {
        Map<String, Row> result = new LinkedHashMap<>();
        for (Row row : templates.rows) {
            String id = row.get("template_id");
            require(TOKEN.matcher(id).matches() && result.put(id, row) == null,
                    "invalid or duplicate template: " + id);
            require(LAYERS.contains(row.get("default_layer")), "invalid default layer: " + id);
            require(TOKEN.matcher(row.get("operation")).matches(), "invalid operation: " + id);
            require(TOKEN.matcher(row.get("observable")).matches(), "invalid observable: " + id);
        }
        return result;
    }

    private Set<String> validateProfiles(Table profiles, Map<String, Row> subjects) {
        Set<String> seen = new HashSet<>();
        Set<String> singular = new HashSet<>();
        for (Row row : profiles.rows) {
            String id = row.get("subject_id");
            require(subjects.containsKey(id) && seen.add(id), "invalid or duplicate profile: " + id);
            require(row.get("singular").equals("true") || row.get("singular").equals("false"),
                    "invalid singular disposition: " + id);
            if (Boolean.parseBoolean(row.get("singular"))) singular.add(id);
            Set<String> archetypes = new HashSet<>();
            for (String archetype : row.get("archetypes").split(",", -1)) {
                require(TOKEN.matcher(archetype).matches() && archetypes.add(archetype),
                        "invalid or duplicate archetype: " + id);
            }
        }
        require(seen.equals(subjects.keySet()), "every registry subject requires one profile");
        require(!singular.isEmpty() && singular.size() < profiles.rows.size(),
                "layer routing is degenerate");
        return Set.copyOf(singular);
    }

    private Result validateClaims(Table claims, Table exceptions, Map<String, Row> subjects,
            Map<String, Row> templates, Set<String> singularSubjects) throws Exception {
        Set<String> claimed = new HashSet<>();
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
                String expectedLayer = templates.get(template).get("default_layer");
                if (singularSubjects.contains(subject) && expectedLayer.equals("ARCHETYPE")) {
                    expectedLayer = "SINGULAR";
                }
                require(row.get("layer").equals(expectedLayer),
                        "claim layer differs from conformance route: " + subject + "#" + template);
                require(claimed.add(subject + "#" + template), "duplicate functional claim");
                if (row.get("status").equals("VERIFIED")) verified++;
                if (row.get("status").equals("VERIFIED")
                        && row.get("automation_surface").equals("PUBLIC_TESTKIT")) {
                    publicTestkit++;
                }
                if (Set.of("VERIFIED", "NATIVE_NONDETERMINISTIC", "NOT_APPLICABLE", "RETRACTED")
                        .contains(row.get("status"))) resolved++;
            }
            validateEvidence(row);
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
        require(publicTestkit <= verified, "public TestKit claims exceed verified claims");
        return new Result(resolved, verified, publicTestkit);
    }

    private void validateEvidence(Row row) throws Exception {
        if (!row.get("status").equals("VERIFIED")) return;
        require(row.get("applicability").equals("APPLICABLE"),
                "verified claim must be applicable");
        require(SHA.matcher(row.get("signature")).matches(), "verified claim lacks signature");
        Path descriptor = root.resolve("smokes").resolve(row.get("evidence_id"))
                .resolve("smoke.properties");
        require(Files.isRegularFile(descriptor), "verified claim evidence is absent");
        require(row.get("signature").equals(properties(descriptor).getProperty("expected.signature")),
                "verified claim signature differs from evidence");
    }

    private static Properties properties(Path path) throws Exception {
        Properties result = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            result.load(reader);
        }
        return result;
    }

    private static int integer(Properties values, String key) {
        return Integer.parseInt(required(values, key));
    }

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

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private record Result(int resolved, int verified, int publicTestkit) {
    }

    private record Row(Map<String, String> values) {
        String get(String key) {
            return values.get(key);
        }
    }

    private static final class Table {
        final List<Row> rows;

        private Table(List<Row> rows) {
            this.rows = rows;
        }

        static Table read(Path path, String... expected) throws Exception {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            require(!lines.isEmpty() && lines.get(0).equals(String.join("\t", expected)),
                    "TSV header drifted: " + path.getFileName());
            List<Row> rows = new ArrayList<>();
            for (int index = 1; index < lines.size(); index++) {
                String line = lines.get(index);
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] fields = line.split("\t", -1);
                require(fields.length == expected.length, "TSV width drifted: " + path.getFileName());
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
