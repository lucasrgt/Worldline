import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/** Loads evidence-bound rejected contracts and their semantic exclusion identities. */
final class RejectionRegistry {
    private static final Set<String> CLASSES = Set.of("hypothesis-contradicted",
            "fixture-oracle-insufficient", "instability", "harness-process-defect");
    private RejectionRegistry() { }

    static List<Entry> load(Path root, Path evidenceRoot) throws Exception {
        Path registry = root.resolve("coordination/swarm/rejection-registry.properties");
        Properties values = properties(registry);
        require("1".equals(values.getProperty("schema")), "invalid rejection registry schema");
        int count = integer(values, "entry.count");
        require(count > 0, "rejection registry is empty");
        List<Entry> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (int index = 0; index < count; index++) {
            String prefix = "entry." + index + ".";
            String id = required(values, prefix + "id");
            String semantic = required(values, prefix + "semantic");
            String classification = required(values, prefix + "class");
            String scar = required(values, prefix + "scar");
            Path disposition = root.resolve(required(values, prefix + "disposition")).normalize();
            String archive = required(values, prefix + "archive");
            String archiveSha = required(values, prefix + "archive.sha256").toLowerCase(Locale.ROOT);
            require(ids.add(id) && id.matches("m[0-9]+-[a-z0-9-]+"), "duplicate rejection: " + id);
            require(semantic.matches("[a-z0-9-]+") && CLASSES.contains(classification),
                    "invalid rejection classification: " + id);
            require(scar.matches("NYA-[A-Z0-9]+") && archiveSha.matches("[0-9a-f]{64}"),
                    "invalid rejection evidence: " + id);
            Properties dispositionValues = properties(disposition);
            require(id.equals(dispositionValues.getProperty("id"))
                    && "REJECTED".equals(dispositionValues.getProperty("disposition"))
                    && archiveSha.equals(dispositionValues.getProperty("archive.sha256", "")
                            .toLowerCase(Locale.ROOT)), "disposition drift: " + id);
            if (evidenceRoot != null) {
                Path artifact = evidenceRoot.resolve(archive).normalize();
                require(artifact.startsWith(evidenceRoot) && Files.isRegularFile(artifact),
                        "missing historical rejection archive: " + archive);
                require(archiveSha.equals(SwarmEvidenceArchive.sha256(artifact).toLowerCase(Locale.ROOT)),
                        "historical rejection archive drift: " + archive);
            }
            boolean approved = Boolean.parseBoolean(values.getProperty(
                    prefix + "revalidation.approved", values.getProperty(
                            "default.revalidation.approved", "false")));
            String change = values.getProperty(prefix + "revalidation.change.sha256", "");
            String negative = values.getProperty(prefix + "revalidation.negative.sha256", "");
            String positive = values.getProperty(prefix + "revalidation.positive.sha256", "");
            String duplicateOf = values.getProperty(prefix + "duplicate.of", "").trim();
            require(duplicateOf.isBlank() || duplicateOf.matches("m[0-9]+-[a-z0-9-]+"),
                    "invalid duplicate rejection reference: " + id);
            require(!approved || change.matches("[0-9a-f]{64}")
                    && negative.matches("[0-9a-f]{64}") && positive.matches("[0-9a-f]{64}"),
                    "approved revalidation lacks objective change and dual proof: " + id);
            result.add(new Entry(id, semantic, aliases(values.getProperty(prefix + "aliases", "")),
                    classification, scar, archive, archiveSha, approved, change, negative, positive,
                    duplicateOf));
        }
        return List.copyOf(result);
    }

    static void requireAllowed(List<Entry> entries, String id, String goal) {
        String semantic = semantic(id), normalizedGoal = normalize(goal);
        List<Entry> blocked = entries.stream().filter(entry -> entry.blocks(id, semantic,
                normalizedGoal))
                .toList();
        if (blocked.isEmpty()) return;
        Entry exact = blocked.stream().filter(entry -> entry.id.equals(id)).findFirst().orElse(null);
        if (exact != null && exact.revalidationApproved) return;
        throw new IllegalStateException("rejected semantic contract is excluded: " + semantic
                + " by " + blocked.stream().map(Entry::id).distinct().sorted().toList());
    }

    static String semantic(String id) {
        return id.toLowerCase(Locale.ROOT).replaceFirst("^m[0-9]+-", "");
    }

    static void selfTest() {
        Entry rejected = new Entry("m674-minecart-collision-transfer", "minecart-collision-transfer",
                Set.of("minecart-collision"), "fixture-oracle-insufficient", "NYA-TEST", "bad.zip",
                "0".repeat(64), false, "", "", "", "");
        boolean blocked = false;
        try { requireAllowed(List.of(rejected), "m660-minecart-collision-transfer", "collision"); }
        catch (IllegalStateException expected) { blocked = true; }
        require(blocked, "historical equivalent was accepted");
        requireAllowed(List.of(rejected), "m662-chicken-fall-immunity", "chicken fall immunity");
        Entry reopened = new Entry(rejected.id, rejected.semantic, rejected.aliases,
                rejected.classification, rejected.scar, rejected.archive, rejected.archiveSha,
                true, "1".repeat(64), "2".repeat(64), "3".repeat(64), "");
        requireAllowed(List.of(reopened), rejected.id, "same milestone objective revalidation");
        Entry duplicate = new Entry("m666-infinite-water-source", "infinite-water-source",
                Set.of("water-source-regeneration"), "harness-process-defect", "NYA-OWNER",
                "duplicate.zip", "4".repeat(64), false, "", "", "",
                "m753-water-source-regeneration");
        requireAllowed(List.of(duplicate), "m753-water-source-regeneration",
                "water source regeneration");
        boolean duplicateBlocked = false;
        try {
            requireAllowed(List.of(duplicate), duplicate.id, "infinite water source");
        } catch (IllegalStateException expected) {
            duplicateBlocked = true;
        }
        require(duplicateBlocked, "duplicate rejection did not block its exact ID");
    }

    private static Properties properties(Path path) throws Exception {
        require(Files.isRegularFile(path), "missing registry input: " + path);
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    private static int integer(Properties values, String key) {
        return Integer.parseInt(required(values, key));
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key);
        return value.trim();
    }
    private static Set<String> aliases(String value) {
        if (value.isBlank()) return Set.of();
        return Set.of(value.split(","));
    }
    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    record Entry(String id, String semantic, Set<String> aliases, String classification, String scar,
            String archive, String archiveSha, boolean revalidationApproved, String changeSha,
            String negativeSha, String positiveSha, String duplicateOf) {
        boolean matches(String candidate, String goal) {
            return semantic.equals(candidate) || goal.contains(semantic)
                    || aliases.stream().anyMatch(alias -> candidate.equals(alias) || goal.contains(alias));
        }
        boolean blocks(String candidateId, String candidate, String goal) {
            if (!duplicateOf.isBlank()) {
                return id.equals(candidateId);
            }
            return matches(candidate, goal);
        }
    }
}
