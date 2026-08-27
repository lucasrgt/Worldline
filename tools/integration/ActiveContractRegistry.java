import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/** Reserves one semantic contract for its active RETRYABLE milestone ID. */
final class ActiveContractRegistry {
    private ActiveContractRegistry() {
    }

    static List<Entry> load(Path root) throws Exception {
        Properties values = properties(root.resolve("coordination/swarm/active-contracts.properties"));
        require("1".equals(values.getProperty("schema")), "invalid active-contract registry schema");
        int count = Integer.parseInt(required(values, "entry.count"));
        List<Entry> result = new ArrayList<>();
        Set<String> semantics = new HashSet<>();
        for (int index = 0; index < count; index++) {
            String prefix = "entry." + index + ".";
            String id = required(values, prefix + "id");
            String semantic = required(values, prefix + "semantic");
            Path disposition = root.resolve(required(values, prefix + "disposition")).normalize();
            String archiveSha = required(values, prefix + "archive.sha256").toLowerCase(Locale.ROOT);
            require(id.matches("m[0-9]+-[a-z0-9-]+") && semantic.matches("[a-z0-9-]+")
                    && semantics.add(semantic), "invalid active semantic owner: " + id);
            require(archiveSha.matches("[0-9a-f]{64}"), "invalid active archive SHA: " + id);
            Properties state = properties(disposition);
            require(id.equals(state.getProperty("id"))
                    && "RETRYABLE".equals(state.getProperty("disposition"))
                    && archiveSha.equals(state.getProperty("archive.sha256", "")
                            .toLowerCase(Locale.ROOT)), "active disposition drift: " + id);
            result.add(new Entry(id, semantic, aliases(values.getProperty(prefix + "aliases", "")),
                    required(values, prefix + "owner"), required(values, prefix + "expiry")));
        }
        return List.copyOf(result);
    }

    static void requireAllowed(List<Entry> entries, String id, String goal) {
        String semantic = RejectionRegistry.semantic(id);
        String normalizedGoal = goal.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        List<Entry> owners = entries.stream().filter(entry -> entry.matches(semantic, normalizedGoal))
                .toList();
        if (owners.isEmpty() || owners.stream().allMatch(entry -> entry.id.equals(id))) {
            return;
        }
        throw new IllegalStateException("semantic contract already has an active owner: "
                + owners.stream().map(Entry::id).sorted().toList());
    }

    static void selfTest() {
        Entry owner = new Entry("m753-water-source-regeneration", "water-source-regeneration",
                Set.of("infinite-water-source"), "orchestrator", "terminal-disposition");
        requireAllowed(List.of(owner), owner.id, "water source regeneration");
        boolean blocked = false;
        try {
            requireAllowed(List.of(owner), "m666-infinite-water-source", "infinite water source");
        } catch (IllegalStateException expected) {
            blocked = true;
        }
        require(blocked, "equivalent ID bypassed its active canonical owner");
    }

    private static Properties properties(Path path) throws Exception {
        require(Files.isRegularFile(path), "missing active-contract input: " + path);
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key);
        return value.trim();
    }
    private static Set<String> aliases(String value) {
        return value.isBlank() ? Set.of() : Set.of(value.split(","));
    }
    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
    record Entry(String id, String semantic, Set<String> aliases, String owner, String expiry) {
        boolean matches(String candidate, String goal) {
            return semantic.equals(candidate) || goal.contains(semantic)
                    || aliases.stream().anyMatch(alias -> candidate.equals(alias)
                            || goal.contains(alias));
        }
    }
}
