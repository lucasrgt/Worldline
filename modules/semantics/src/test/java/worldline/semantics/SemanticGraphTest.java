package worldline.semantics;

public final class SemanticGraphTest {
    private SemanticGraphTest() {}

    public static void main(String[] arguments) {
        SemanticCatalog catalog = SemanticCatalog.standard();
        SemanticGraph first = SemanticGraph.of(catalog);
        SemanticGraph second = SemanticGraph.of(catalog);
        require(first.tokens().size() == SemanticRoles.categories().size(), "token count");
        require(first.tokens().contains("CLOCK"), "clock token");
        require(first.tokens().contains("SAVE"), "save token");
        require(first.tokens().contains("BLOCK_TICK"), "hyphenated category token");
        require(first.tokens().contains("TILE_ENTITY"), "tile entity category token");
        require(!first.readers("BLOCK_TICK").isEmpty(), "block tick readers");
        require(!first.readers("REDSTONE").isEmpty(), "redstone readers");
        require(!first.writers("REDSTONE").isEmpty(), "redstone writers");
        require(!first.readers("RECIPE").isEmpty(), "recipe TestKit readers");
        require(!first.readers("TILE_ENTITY").isEmpty(), "tile entity TestKit readers");
        require(!first.readers("CLOCK").isEmpty(), "clock readers");
        require(!first.writers("WORLD").isEmpty(), "world writers");
        require(first.render().contains("complete=true"), "graph completeness");
        require(first.render().equals(second.render()), "graph drifted");
        require(first.edges().contains("CLIENT_TICK_ROOT deps CLOCK"), "tick depends on clock");
        try {
            first.readers("ENERGY");
            throw new AssertionError("expected unknown token");
        } catch (IllegalArgumentException expected) { }
        System.out.println("SemanticGraphTest passed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
