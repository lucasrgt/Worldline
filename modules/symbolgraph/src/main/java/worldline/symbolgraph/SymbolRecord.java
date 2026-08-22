package worldline.symbolgraph;

import java.util.Objects;

/** One exact intermediary identity and its non-authoritative aliases. */
public final class SymbolRecord implements Comparable<SymbolRecord> {
    private final SymbolKey key;
    private final String clientOfficial;
    private final String serverOfficial;
    private final String nostalgia;
    private final boolean inventoryPresent;
    private final boolean nostalgiaPresent;
    private final SymbolSide side;

    SymbolRecord(SymbolKey key, String clientOfficial, String serverOfficial, String nostalgia,
            boolean inventoryPresent, boolean nostalgiaPresent) {
        this.key = Objects.requireNonNull(key, "key");
        this.clientOfficial = nonnull(clientOfficial, "clientOfficial");
        this.serverOfficial = nonnull(serverOfficial, "serverOfficial");
        this.nostalgia = nonnull(nostalgia, "nostalgia");
        this.inventoryPresent = inventoryPresent;
        this.nostalgiaPresent = nostalgiaPresent;
        this.side = SymbolSide.fromAliases(clientOfficial, serverOfficial);
        if (!inventoryPresent && (!clientOfficial.isEmpty() || !serverOfficial.isEmpty())) {
            throw new IllegalArgumentException("official alias without inventory identity");
        }
    }

    public SymbolKey key() { return key; }
    public String clientOfficial() { return clientOfficial; }
    public String serverOfficial() { return serverOfficial; }
    public String nostalgia() { return nostalgia; }
    public boolean inventoryPresent() { return inventoryPresent; }
    public boolean nostalgiaPresent() { return nostalgiaPresent; }
    public SymbolSide side() { return side; }

    public String canonical() {
        return key.canonical() + "|client=" + clientOfficial + "|server=" + serverOfficial
                + "|nostalgia=" + nostalgia + "|inventory=" + inventoryPresent
                + "|named=" + nostalgiaPresent + "|side=" + side.name();
    }

    @Override public int compareTo(SymbolRecord other) { return key.compareTo(other.key); }
    @Override public boolean equals(Object other) {
        return other instanceof SymbolRecord && canonical().equals(((SymbolRecord) other).canonical());
    }
    @Override public int hashCode() { return canonical().hashCode(); }

    private static String nonnull(String value, String label) {
        if (value == null) throw new NullPointerException(label);
        return value;
    }
}
