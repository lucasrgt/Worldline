import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Canonical deterministic workload partition for local or remote campaign workers. */
record CampaignPlan(String id, long rootSeed, int shardCount, List<Workload> workloads,
        List<Shard> shards) {
    private static final String SCHEMA = "worldline.campaign-plan.v1";
    private static final Set<String> KINDS = Set.of("protocol-fuzz", "mutation", "worldgen",
            "instability", "lane-differential", "profiling", "regression", "minimization");

    static CampaignPlan create(String id, long rootSeed, int shardCount,
            List<Workload> input) throws Exception {
        token(id, "campaign id");
        require(shardCount >= 1 && shardCount <= 1_024, "shard count must be between 1 and 1024");
        require(!input.isEmpty() && input.size() <= 64, "campaign workload count is invalid");
        List<Workload> workloads = input.stream().sorted(Comparator.comparing(Workload::id)).toList();
        Set<String> ids = new HashSet<>();
        for (Workload workload : workloads) {
            token(workload.id, "workload id");
            require(KINDS.contains(workload.kind), "unknown campaign kind: " + workload.kind);
            require(workload.cases >= shardCount && workload.cases <= 1_000_000_000L,
                    "workload cases must cover every shard");
            require(ids.add(workload.id), "duplicate workload: " + workload.id);
        }
        List<Shard> shards = new ArrayList<>();
        for (int index = 0; index < shardCount; index++) {
            List<Assignment> assignments = new ArrayList<>();
            for (Workload workload : workloads) {
                long base = workload.cases / shardCount, remainder = workload.cases % shardCount;
                long start = index * base + Math.min(index, remainder);
                long count = base + (index < remainder ? 1L : 0L), end = start + count;
                assignments.add(new Assignment(workload.id, start, end,
                        seed(id, rootSeed, workload.id, index, start, end)));
            }
            shards.add(new Shard(index, List.copyOf(assignments)));
        }
        return new CampaignPlan(id, rootSeed, shardCount, List.copyOf(workloads), List.copyOf(shards));
    }

    static CampaignPlan parse(String json) throws Exception {
        Map<String, Object> value = MiniJson.object(json); exact(value,
                "schema", "id", "root_seed", "shard_count", "semantic_authority",
                "execution_authority", "workloads", "shards");
        require(SCHEMA.equals(MiniJson.string(value, "schema")), "campaign plan schema drifted");
        require("java".equals(MiniJson.string(value, "semantic_authority"))
                && "observational".equals(MiniJson.string(value, "execution_authority")),
                "campaign authority drifted");
        List<Workload> workloads = new ArrayList<>();
        for (Object item : MiniJson.array(value, "workloads")) {
            Map<String, Object> row = MiniJson.asObject(item, "workload");
            exact(row, "id", "kind", "cases");
            workloads.add(new Workload(MiniJson.string(row, "id"), MiniJson.string(row, "kind"),
                    MiniJson.integer(row, "cases")));
        }
        CampaignPlan plan = create(MiniJson.string(value, "id"), MiniJson.integer(value, "root_seed"),
                Math.toIntExact(MiniJson.integer(value, "shard_count")), workloads);
        require(plan.shards.equals(parseShards(value)), "campaign shard derivation drifted");
        require(plan.json().equals(json), "campaign plan is not canonical");
        return plan;
    }

    String json() {
        StringBuilder out = new StringBuilder("{\"schema\":").append(q(SCHEMA))
                .append(",\"id\":").append(q(id)).append(",\"root_seed\":").append(rootSeed)
                .append(",\"shard_count\":").append(shardCount)
                .append(",\"semantic_authority\":\"java\",\"execution_authority\":\"observational\"")
                .append(",\"workloads\":[");
        for (int index = 0; index < workloads.size(); index++) {
            if (index > 0) out.append(','); Workload value = workloads.get(index);
            out.append("{\"id\":").append(q(value.id)).append(",\"kind\":").append(q(value.kind))
                    .append(",\"cases\":").append(value.cases).append('}');
        }
        out.append("],\"shards\":[");
        for (int index = 0; index < shards.size(); index++) {
            if (index > 0) out.append(','); out.append(shards.get(index).json());
        }
        return out.append("]}").toString();
    }

    String sha256() throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance(
            "SHA-256").digest(json().getBytes(StandardCharsets.UTF_8))); }
    String envelope() throws Exception { return "{\n  \"schema\":\"worldline.campaign-plan-envelope.v1\",\n"
            + "  \"plan_sha256\":" + q(sha256()) + ",\n  \"plan\":" + json() + "\n}\n"; }
    Shard shard(int index) { require(index >= 0 && index < shards.size(), "invalid shard index");
        return shards.get(index); }

    private static List<Shard> parseShards(Map<String, Object> value) {
        List<Shard> shards = new ArrayList<>();
        for (Object item : MiniJson.array(value, "shards")) {
            Map<String, Object> row = MiniJson.asObject(item, "shard"); exact(row, "index", "assignments");
            List<Assignment> assignments = new ArrayList<>();
            for (Object assignment : MiniJson.array(row, "assignments")) {
                Map<String, Object> entry = MiniJson.asObject(assignment, "assignment");
                exact(entry, "workload", "start", "end_exclusive", "seed");
                assignments.add(new Assignment(MiniJson.string(entry, "workload"),
                        MiniJson.integer(entry, "start"), MiniJson.integer(entry, "end_exclusive"),
                        MiniJson.integer(entry, "seed")));
            }
            shards.add(new Shard(Math.toIntExact(MiniJson.integer(row, "index")),
                    List.copyOf(assignments)));
        }
        return shards;
    }

    private static long seed(String id, long rootSeed, String workload, int index,
            long start, long end) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (String part : List.of(SCHEMA, id, Long.toString(rootSeed), workload,
                Integer.toString(index), Long.toString(start), Long.toString(end))) {
            digest.update(part.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
        }
        return ByteBuffer.wrap(digest.digest()).getLong();
    }
    private static void exact(Map<String, Object> value, String... fields) {
        require(new ArrayList<>(value.keySet()).equals(List.of(fields)),
                "campaign JSON fields or order drifted: " + value.keySet());
    }
    private static String q(String value) { return EvidenceExplorerFormat.quote(value); }
    private static void token(String value, String label) { require(value != null
            && value.matches("[a-z0-9]+(?:-[a-z0-9]+)*"), "invalid " + label); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }

    record Workload(String id, String kind, long cases) { }
    record Assignment(String workload, long start, long endExclusive, long seed) {
        String json() { return "{\"workload\":" + q(workload) + ",\"start\":" + start
                + ",\"end_exclusive\":" + endExclusive + ",\"seed\":" + seed + "}"; }
        long cases() { return endExclusive - start; }
    }
    record Shard(int index, List<Assignment> assignments) {
        String json() { StringBuilder out = new StringBuilder("{\"index\":").append(index)
                .append(",\"assignments\":[");
            for (int item = 0; item < assignments.size(); item++) {
                if (item > 0) out.append(','); out.append(assignments.get(item).json());
            }
            return out.append("]}").toString(); }
    }
}
