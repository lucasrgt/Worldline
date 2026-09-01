/** Exact successors after regenerated dimension Atlas qualification status. */
final class DimensionAtlasDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "19663fbd297d32c75f743ca42f35a54700bf8caa9530f30f713ad17c48cff8a7",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "2bf204e702a0c2c64dd437edd5403d01ff4152b512d6de67fa51433fdbcb1df3",
                "41180cc39fe00006fcbc7c598958c2054db7ab1f703b7abc4fe5ce8bdfc9b18d"},
        {"tools/harness/DimensionAtlasTrainSourceSuccessor.java",
                "915c070c2b114218989ed4e0403994271ebc8e292fd95d6fe6789e24cb276cc9",
                "606bf0b61c36820a6a0e941efb09387ff62398f052c7a6f8896744e1e5cbd701"}
    };
    private DimensionAtlasDocumentationSuccessor() { }
    static boolean carries(String relative, String prior, String current) {
        for (String[] successor : SUCCESSORS)
            if (relative.equals(successor[0]) && prior.equals(successor[1])
                    && current.equals(successor[2])) return true;
        return false;
    }
    static void selfTest() {
        for (String[] successor : SUCCESSORS) {
            require(carries(successor[0], successor[1], successor[2]),
                    "dimension Atlas documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "dimension Atlas documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "dimension Atlas documentation successor allowlist drifted");
        DimensionAtlasFormattingSuccessor.selfTest();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
