/** Exact successors for the dimension-aware distributed runner artifact. */
final class DimensionArtifactSuccessor {
    private static final String[][] SUCCESSORS = {
        {"release/testkit-artifacts.lock",
                "395fd8bca3bb0023c3c4aa7eb43c24374039463b0e170961e08c2e05981959a7",
                "a54c040e8fd589731e81c2d0764bb5a811f1bd882e22445c9326a8165e896286"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "6b0e9cc19a33e37c678c768254e0dfd4806c8a5a6ff9916b2572eff2bd5ff344",
                "e52ec8b121ecbb17d9a8d88490b032d31f4fbaab7628d87ceaae73bf02f6d7e6"},
        {"tools/harness/DimensionAtlasFormattingSuccessor.java",
                "eca5797b0c9df6b6ce848b11b863f1aa2a056e8de0fb3a76e5769e5b61be70a0",
                "a8750cf9b15f4342e05c66bde6ec717e6d3ecac583ddabb5807f58e223882875"}
    };
    private DimensionArtifactSuccessor() { }
    static boolean carries(String relative, String prior, String current) {
        for (String[] successor : SUCCESSORS)
            if (relative.equals(successor[0]) && prior.equals(successor[1])
                    && current.equals(successor[2])) return true;
        return false;
    }
    static void selfTest() {
        for (String[] successor : SUCCESSORS) {
            require(carries(successor[0], successor[1], successor[2]),
                    "dimension artifact successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "dimension artifact successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "dimension artifact successor allowlist drifted");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
