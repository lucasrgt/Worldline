package worldline.symbolgraph;

public enum SymbolSide {
    CLIENT, SERVER, SHARED, UNRESOLVED;

    static SymbolSide fromAliases(String clientOfficial, String serverOfficial) {
        boolean client = !clientOfficial.isEmpty();
        boolean server = !serverOfficial.isEmpty();
        if (client && server) return SHARED;
        if (client) return CLIENT;
        if (server) return SERVER;
        return UNRESOLVED;
    }
}
