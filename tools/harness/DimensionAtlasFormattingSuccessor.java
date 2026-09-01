/** Exact successors for source-policy formatting of dimension Atlas proofs. */
final class DimensionAtlasFormattingSuccessor {
    private static final String[][] SUCCESSORS = {
        {"tools/harness/DimensionAtlasTrainSourceSuccessor.java",
                "606bf0b61c36820a6a0e941efb09387ff62398f052c7a6f8896744e1e5cbd701",
                "4340614e01d54f5ca264efd47739855f3dc53aa6930c6a7e16dd9ee2ed841ddf"},
        {"tools/harness/DimensionAtlasDocumentationSuccessor.java",
                "7ed779986dc1769fd353ab65a3a29b261baf126c81d1741fb2a077ebb6ee6db6",
                "06fb4c25344ee6347ecd785a95fa79e4dc0522471aad59f828be5a490a626ee9"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "41180cc39fe00006fcbc7c598958c2054db7ab1f703b7abc4fe5ce8bdfc9b18d",
                "6b0e9cc19a33e37c678c768254e0dfd4806c8a5a6ff9916b2572eff2bd5ff344"}
    };
    private DimensionAtlasFormattingSuccessor() { }
    static boolean carries(String relative, String prior, String current) {
        for (String[] successor : SUCCESSORS) {
            if (relative.equals(successor[0]) && prior.equals(successor[1])
                    && current.equals(successor[2])) return true;
        }
        return false;
    }
    static void selfTest() {
        for (String[] successor : SUCCESSORS) {
            require(carries(successor[0], successor[1], successor[2]),
                    "dimension Atlas formatting successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "dimension Atlas formatting successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "dimension Atlas formatting successor allowlist drifted");
        DimensionArtifactSuccessor.selfTest();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
