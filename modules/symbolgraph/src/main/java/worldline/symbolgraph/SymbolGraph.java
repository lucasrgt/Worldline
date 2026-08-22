package worldline.symbolgraph;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SymbolGraph {
    private final List<SymbolRecord> records;
    private final Map<SymbolKey, SymbolRecord> indexed;

    SymbolGraph(List<SymbolRecord> records) {
        List<SymbolRecord> sorted = new ArrayList<SymbolRecord>(records);
        Collections.sort(sorted);
        Map<SymbolKey, SymbolRecord> byKey = new LinkedHashMap<SymbolKey, SymbolRecord>();
        for (SymbolRecord record : sorted) {
            if (byKey.put(record.key(), record) != null) {
                throw new IllegalArgumentException("duplicate graph identity: " + record.key());
            }
        }
        this.records = Collections.unmodifiableList(sorted);
        this.indexed = Collections.unmodifiableMap(byKey);
    }

    public List<SymbolRecord> records() { return records; }
    public SymbolRecord record(SymbolKey key) {
        SymbolRecord record = indexed.get(key);
        if (record == null) throw new IllegalArgumentException("unknown symbol: " + key);
        return record;
    }

    public Map<SymbolSide, Integer> sideCounts() {
        Map<SymbolSide, Integer> counts = new EnumMap<SymbolSide, Integer>(SymbolSide.class);
        for (SymbolSide side : SymbolSide.values()) counts.put(side, Integer.valueOf(0));
        for (SymbolRecord record : records) {
            SymbolSide side = record.side();
            counts.put(side, Integer.valueOf(counts.get(side).intValue() + 1));
        }
        return Collections.unmodifiableMap(counts);
    }

    SymbolGraph withRetroMcp(Map<SymbolKey, String> clientAliases,
            Map<SymbolKey, String> serverAliases) {
        List<SymbolRecord> enriched = new ArrayList<SymbolRecord>();
        for (SymbolRecord record : records) {
            String client = clientAliases.get(record.key());
            String server = serverAliases.get(record.key());
            enriched.add(record.withRetroMcp(client == null ? "" : client,
                    server == null ? "" : server));
        }
        return new SymbolGraph(enriched);
    }

    public String render() {
        StringBuilder text = new StringBuilder();
        text.append("kind\towner\tintermediary\tdescriptor\tclientOfficial\tserverOfficial")
                .append("\tnostalgia\tretroMcpClient\tretroMcpServer")
                .append("\tside\tinInventory\tinNostalgia\n");
        for (SymbolRecord record : records) {
            SymbolKey key = record.key();
            text.append(key.kind().name().toLowerCase()).append('\t').append(key.owner()).append('\t')
                    .append(key.name()).append('\t').append(key.descriptor()).append('\t')
                    .append(record.clientOfficial()).append('\t').append(record.serverOfficial()).append('\t')
                    .append(record.nostalgia()).append('\t').append(record.retroMcpClient()).append('\t')
                    .append(record.retroMcpServer()).append('\t')
                    .append(record.side().name().toLowerCase())
                    .append('\t').append(record.inventoryPresent()).append('\t')
                    .append(record.nostalgiaPresent()).append('\n');
        }
        return text.toString();
    }

    public String sha256() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(render().getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : digest) result.append(String.format("%02x", Integer.valueOf(value & 255)));
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 unavailable", error);
        }
    }
}
