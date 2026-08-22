package worldline.symbolgraph;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Deterministic evidence queue derived only from exact namespace and bytecode gaps. */
public final class MappingQualificationQueue {
    private final List<Item> items;
    private final String body;

    private MappingQualificationQueue(List<Item> source) {
        List<Item> ordered = new ArrayList<Item>(source);
        Collections.sort(ordered);
        items = Collections.unmodifiableList(ordered);
        StringBuilder text = new StringBuilder("schema=2\nitems=").append(items.size()).append('\n');
        text.append("item\tpriority\tgap\tidentity\tside\tkind\towner\tname\tdescriptor")
                .append("\tnostalgia\tretroClient\tretroServer\tnextEvidence\n");
        for (Item item : items) text.append(item.render()).append('\n');
        body = text.toString();
    }

    public static MappingQualificationQueue create(Path clientJar, Path serverJar,
            Path intermediaryArchive, Path nostalgiaArchive, Path retroDescriptor, Path retroTiny)
            throws Exception {
        TinyMapping intermediary = MappingArchive.read(intermediaryArchive, "mappings/mappings.tiny");
        TinyMapping nostalgia = MappingArchive.read(nostalgiaArchive, "mappings/mappings.tiny");
        MappingPin.load(retroDescriptor).verify(retroTiny);
        TinyMapping retro;
        try (Reader reader = Files.newBufferedReader(retroTiny, StandardCharsets.UTF_8)) {
            retro = new TinyV2Reader().read(reader);
        }
        SymbolGraph base = new SymbolGraphBuilder().build(intermediary, nostalgia);
        SymbolGraph graph = new RetroMcpImport().apply(base, intermediary, retro).graph();
        OfficialBytecodeAudit audit = new OfficialBytecodeAudit();
        return create(graph,
                audit.compare(OfficialJarInventory.read(clientJar), intermediary, "clientOfficial"),
                audit.compare(OfficialJarInventory.read(serverJar), intermediary, "serverOfficial"));
    }

    static MappingQualificationQueue create(SymbolGraph graph,
            OfficialBytecodeAudit.Report client, OfficialBytecodeAudit.Report server) {
        List<Item> values = new ArrayList<Item>();
        official(values, "client", client);
        official(values, "server", server);
        NamespaceAudit.Report namespaces = new NamespaceAudit().inspect(graph);
        for (NamespaceIssue issue : NamespaceIssue.values()) {
            if (issue == NamespaceIssue.MATCH) continue;
            for (SymbolKey key : namespaces.findings(issue)) {
                SymbolRecord record = graph.record(key);
                values.add(Item.namespace(issue, record));
            }
        }
        return new MappingQualificationQueue(values);
    }

    public List<Item> items() { return items; }
    public String sha256() { return digest(body); }
    public String render() { return body + "queue.sha256=" + sha256() + "\n"; }

    private static void official(List<Item> items, String side, OfficialBytecodeAudit.Report report) {
        for (Map.Entry<OfficialSymbolKey, OfficialGapKind> gap : report.gaps().entrySet())
            items.add(Item.official(side, gap.getKey(), gap.getValue()));
    }

    private static String digest(String text) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder value = new StringBuilder();
            for (byte item : bytes) value.append(String.format("%02x", Integer.valueOf(item & 255)));
            return value.toString();
        } catch (java.security.NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 unavailable", error);
        }
    }

    public static final class Item implements Comparable<Item> {
        private final int priority;
        private final String gap, identity, side, kind, owner, name, descriptor;
        private final String nostalgia, retroClient, retroServer, nextEvidence;

        private Item(int priority, String gap, String identity, String side, SymbolKind kind,
                String owner, String name, String descriptor, String nostalgia,
                String retroClient, String retroServer, String nextEvidence) {
            this.priority = priority; this.gap = field(gap); this.identity = field(identity);
            this.side = field(side); this.kind = kind.name().toLowerCase(); this.owner = field(owner);
            this.name = field(name); this.descriptor = field(descriptor); this.nostalgia = field(nostalgia);
            this.retroClient = field(retroClient); this.retroServer = field(retroServer);
            this.nextEvidence = field(nextEvidence);
        }

        static Item official(String side, OfficialSymbolKey key, OfficialGapKind gap) {
            return new Item(officialPriority(gap), gap.name(), "official", side, key.kind(),
                    key.owner(), key.name(), key.descriptor(), "", "", "",
                    "official-bytecode+behavior-or-cross-version");
        }

        static Item namespace(NamespaceIssue issue, SymbolRecord record) {
            SymbolKey key = record.key();
            return new Item(namespacePriority(issue), issue.name(), "intermediary",
                    record.side().name().toLowerCase(), key.kind(), key.owner(), key.name(),
                    key.descriptor(), record.nostalgia(), record.retroMcpClient(),
                    record.retroMcpServer(), namespaceEvidence(issue));
        }

        public int priority() { return priority; }
        public String gap() { return gap; }
        public String identity() { return identity; }
        public String side() { return side; }
        public String kind() { return kind; }
        public String owner() { return owner; }
        public String name() { return name; }
        public String descriptor() { return descriptor; }
        public String nostalgia() { return nostalgia; }
        public String retroClient() { return retroClient; }
        public String retroServer() { return retroServer; }
        public String nextEvidence() { return nextEvidence; }
        public String id() { return digest(canonical()); }
        public String canonical() {
            return priority + "|" + gap + "|" + identity + "|" + side + "|" + kind + "|"
                    + owner + "|" + name + "|" + descriptor;
        }
        @Override public int compareTo(Item other) {
            if (priority != other.priority) return priority < other.priority ? -1 : 1;
            return canonical().compareTo(other.canonical());
        }
        String render() {
            return id() + "\t" + priority + "\t" + gap + "\t" + identity + "\t" + side + "\t" + kind
                    + "\t" + dash(owner) + "\t" + name + "\t" + dash(descriptor)
                    + "\t" + dash(nostalgia) + "\t" + dash(retroClient) + "\t"
                    + dash(retroServer) + "\t" + nextEvidence;
        }

        private static int officialPriority(OfficialGapKind gap) {
            if (gap == OfficialGapKind.UNMAPPED_FIELD) return 20;
            if (gap == OfficialGapKind.UNMAPPED_METHOD) return 30;
            if (gap == OfficialGapKind.CONSTRUCTOR) return 80;
            if (gap == OfficialGapKind.CLASS_INITIALIZER) return 90;
            if (gap == OfficialGapKind.OFFICIAL_ONLY_OWNER_MEMBER) return 100;
            return 110;
        }
        private static int namespacePriority(NamespaceIssue issue) {
            if (issue == NamespaceIssue.AMBIGUOUS) return 0;
            if (issue == NamespaceIssue.SIDE_CONFLICT) return 10;
            if (issue == NamespaceIssue.WORLDLINE_MISSING) return 40;
            if (issue == NamespaceIssue.RETROMCP_MISSING) return 50;
            if (issue == NamespaceIssue.NOSTALGIA_MISSING) return 60;
            if (issue == NamespaceIssue.NAME_DIFFERENCE) return 70;
            throw new IllegalArgumentException("non-gap namespace issue " + issue);
        }
        private static String namespaceEvidence(NamespaceIssue issue) {
            if (issue == NamespaceIssue.WORLDLINE_MISSING)
                return "official-identity-before-external-alias-adoption";
            if (issue == NamespaceIssue.NAME_DIFFERENCE)
                return "behavior-or-cross-version-before-name-choice";
            if (issue == NamespaceIssue.AMBIGUOUS || issue == NamespaceIssue.SIDE_CONFLICT)
                return "resolve-exact-identity-conflict";
            return "exact-identity+independent-cross-source";
        }
        private static String field(String value) {
            if (value == null || value.indexOf('\t') >= 0 || value.indexOf('\n') >= 0
                    || value.indexOf('\r') >= 0) throw new IllegalArgumentException("invalid queue field");
            return value;
        }
        private static String dash(String value) { return value.isEmpty() ? "-" : value; }
    }
}
