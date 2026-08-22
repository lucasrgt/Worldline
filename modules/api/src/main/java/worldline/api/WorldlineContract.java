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
    public static final WorldlineContract CLIENT_RUNTIME_EQUIVALENCE = define("client-runtime-equivalence",
            "Mapped and official client tick traces agree under controlled inputs");
    public static final WorldlineContract SERVER_RUNTIME_EQUIVALENCE = define("server-runtime-equivalence",
            "Mapped and official server world tick traces agree under controlled inputs");
    public static final WorldlineContract DOMAIN_API_EQUIVALENCE = define("domain-api-equivalence",
            "Public world, block, entity, and player operations match the official oracle");
    public static final WorldlineContract DURABLE_SNAPSHOT = define("durable-snapshot",
            "Canonical runtime snapshots restore exactly and reject drifted inputs");
    public static final WorldlineContract UI_TREE_EQUIVALENCE = define("ui-tree-equivalence",
            "Semantic UI trees match the official screen and slot topology");
    public static final WorldlineContract RUNTIME_LAB = define("runtime-lab",
            "Integrated snapshot, replay, branching, UI, and mod laboratory boundary");
    public static final WorldlineContract RUNTIME_CENSUS = define("runtime-census",
            "Canonical registry census sections and rows from the controlled runtime");
    public static final WorldlineContract SEED_ATLAS = define("seed-atlas",
            "Deterministic official-server terrain pages for a seed and radius");
    public static final WorldlineContract UI_EXPORT = define("ui-export",
            "Deterministic self-contained semantic UI tree page");
    public static final WorldlineContract DEDICATED_SERVER_CONTROL = define("dedicated-server-control",
            "Official dedicated-server lifecycle, command, save, and shutdown control");
    public static final WorldlineContract MULTIPLAYER_SESSION = define("multiplayer-session",
            "Bounded protocol-14 connection, native client, and peer communication session");
    public static final WorldlineContract PLAYER_PERSISTENCE = define("player-persistence",
            "Server-authored player state remains observable after clean disconnect");
    public static final WorldlineContract PLAYER_POSE = define("player-pose",
            "Acknowledged multiplayer pose, movement, correction, and persistence boundary");
    public static final WorldlineContract REMOTE_WORLD_VIEW = define("remote-world-view",
            "Decoded immutable remote chunks, lifecycle cache, updates, and terrain views");
    public static final WorldlineContract MOVEMENT_ROUTE = define("movement-route",
            "Resolved movement routes, recovery, observation, control, correlation, and batches");
    public static final WorldlineContract INVENTORY_SESSION = define("inventory-session",
            "Server-authoritative inventory, held item, drop, collection, and placement session");
    public static final WorldlineContract CONTAINER_TRANSACTION = define("container-transaction",
            "Window topology, acknowledged transactions, crafting, furnace, and storage session");
    public static final WorldlineContract COMBAT_SESSION = define("combat-session",
            "Peer attack, incoming health, equipment durability, and persisted damage session");
    public static final WorldlineContract AERO_RUNTIME_SESSION = define("aero-runtime-session",
            "Pinned Aero client sessions with bounded server, event, and diagnostic evidence");
    public static final WorldlineContract AERO_PAIRED_EXPERIMENT = define("aero-paired-experiment",
            "Fresh-process balanced Aero experiments with explicit descriptive-only claims");
    public static final WorldlineContract AERO_FRAME_CENSUS = define("aero-frame-census",
            "Complete bounded Aero frame censuses with attested capture and stage observations");
    public static final WorldlineContract AERO_CACHE_LIFECYCLE = define("aero-cache-lifecycle",
            "Aero page-cache invalidation, membership transition, and recovery observations");
    public static final WorldlineContract AERO_DIAGNOSTIC_CAPTURE = define("aero-diagnostic-capture",
            "Pinned Aero reproduction, attribution, scheduler, and visual diagnostic evidence");
    public static final WorldlineContract AERO_SAVE_WINDOW = define("aero-save-window",
            "Aero frame windows that attribute live, skipped, and budgeted world saves");
    private static final Map<String, WorldlineContract> ALL;
    static {
        register(REPRODUCTION_BUNDLE, TRACE_DIVERGENCE, MOD_LOADING,
                MOD_VERSION_DIFFERENCE, SCENARIO_MINIMIZATION, MOD_API_LIFECYCLE,
                MOD_TEST_RUN, MOD_DEPENDENCY_GRAPH, SCENARIO_DSL, FUZZ_CAMPAIGN,
                SCENARIO_DEBUGGING, SCENARIO_PROFILING, SCENARIO_COVERAGE, TRACE_HTML,
                CLIENT_RUNTIME_EQUIVALENCE, SERVER_RUNTIME_EQUIVALENCE, DOMAIN_API_EQUIVALENCE,
                DURABLE_SNAPSHOT, UI_TREE_EQUIVALENCE, RUNTIME_LAB, RUNTIME_CENSUS,
                SEED_ATLAS, UI_EXPORT, DEDICATED_SERVER_CONTROL, MULTIPLAYER_SESSION,
                PLAYER_PERSISTENCE, PLAYER_POSE, REMOTE_WORLD_VIEW, MOVEMENT_ROUTE,
                INVENTORY_SESSION, CONTAINER_TRANSACTION, COMBAT_SESSION,
                AERO_RUNTIME_SESSION, AERO_PAIRED_EXPERIMENT, AERO_FRAME_CENSUS,
                AERO_CACHE_LIFECYCLE, AERO_DIAGNOSTIC_CAPTURE, AERO_SAVE_WINDOW);
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
