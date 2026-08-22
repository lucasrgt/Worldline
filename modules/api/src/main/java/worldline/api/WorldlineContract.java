package worldline.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/** Public identity for reusable TestKit tooling contracts that are not vanilla behaviors. */
public final class WorldlineContract {
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9-]{0,62}");
    private static final Map<String, WorldlineContract> DEFINITIONS =
            new LinkedHashMap<String, WorldlineContract>();
    public static final WorldlineContract REPRODUCTION_BUNDLE = define("reproduction-bundle",
            "Canonical portable bundle creation, parsing, and replay provenance");
    public static final WorldlineContract TRACE_DIVERGENCE = define("trace-divergence",
            "Exact first divergence between canonical state traces");
    public static final WorldlineContract MOD_LOADING = define("mod-loading",
            "Descriptor inspection and controlled compatible mod loading");
    public static final WorldlineContract MOD_VERSION_DIFFERENCE = define("mod-version-difference",
            "Artifact-attested behavioral comparison across mod versions");
    public static final WorldlineContract SCENARIO_MINIMIZATION = define("scenario-minimization",
            "Deterministic reduction that preserves an exact divergence predicate");
    public static final WorldlineContract MOD_API_LIFECYCLE = define("mod-api-lifecycle",
            "Controlled mod lifecycle, domain handles, and scheduled actions");
    public static final WorldlineContract MOD_TEST_RUN = define("mod-test-run",
            "One-command controlled mod execution with attested durable results");
    public static final WorldlineContract MOD_DEPENDENCY_GRAPH = define("mod-dependency-graph",
            "Deterministic dependency ordering and fail-closed graph validation");
    public static final WorldlineContract SCENARIO_DSL = define("scenario-dsl",
            "Strict scenario authoring, validation, and deterministic execution grammar");
    public static final WorldlineContract FUZZ_CAMPAIGN = define("fuzz-campaign",
            "Seeded differential scenario generation, execution, and minimization");
    public static final WorldlineContract SCENARIO_DEBUGGING = define("scenario-debugging",
            "Deterministic reverse navigation and trace-field watchpoints");
    public static final WorldlineContract SCENARIO_PROFILING = define("scenario-profiling",
            "Tick profile aggregation and explicit performance-budget decisions");
    public static final WorldlineContract SCENARIO_COVERAGE = define("scenario-coverage",
            "Semantic scenario category and role coverage with threshold decisions");
    public static final WorldlineContract TRACE_HTML = define("trace-html",
            "Deterministic self-contained trace and divergence evidence pages");
    private static final Map<String, WorldlineContract> ALL;
    static {
        register(REPRODUCTION_BUNDLE, TRACE_DIVERGENCE, MOD_LOADING,
                MOD_VERSION_DIFFERENCE, SCENARIO_MINIMIZATION, MOD_API_LIFECYCLE,
                MOD_TEST_RUN, MOD_DEPENDENCY_GRAPH, SCENARIO_DSL, FUZZ_CAMPAIGN,
                SCENARIO_DEBUGGING, SCENARIO_PROFILING, SCENARIO_COVERAGE, TRACE_HTML);
        ALL = Collections.unmodifiableMap(new LinkedHashMap<String, WorldlineContract>(DEFINITIONS));
    }
    private final String token, subject;

    private WorldlineContract(String token, String subject) { this.token = token; this.subject = subject; }
    public String token() { return token; }
    public String subject() { return subject; }
    public static Map<String, WorldlineContract> all() { return ALL; }
    public static WorldlineContract require(String token) {
        WorldlineContract value = token == null ? null : ALL.get(token.trim());
        if (value == null) throw new IllegalArgumentException("unknown TestKit contract " + token);
        return value;
    }
    @Override public boolean equals(Object other) {
        return other instanceof WorldlineContract && token.equals(((WorldlineContract) other).token);
    }
    @Override public int hashCode() { return Objects.hash(token); }

    private static WorldlineContract define(String token, String subject) {
        if (!TOKEN.matcher(token).matches() || subject == null || subject.isEmpty()
                || subject.indexOf('\n') >= 0 || subject.indexOf('\r') >= 0)
            throw new IllegalArgumentException("invalid TestKit contract");
        return new WorldlineContract(token, subject);
    }
    private static void register(WorldlineContract... values) {
        for (WorldlineContract value : values)
            if (DEFINITIONS.put(value.token, value) != null)
                throw new IllegalStateException("duplicate TestKit contract " + value.token);
    }
}
