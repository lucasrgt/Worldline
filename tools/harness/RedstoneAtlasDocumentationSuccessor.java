/** Exact train successors after regenerated redstone Atlas qualification status. */
final class RedstoneAtlasDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "e285c3d85c1ec171ca450a7e23a90e5f187fdbd620a9ff05df2a656f189c9b09",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "cc9033f7e59c4a18a2462dbd61d078d5c88976c0a2095cd9479ea5f88528586d",
                "a9ea5de994012025eccd33131f8a7dd898cc7068580f14070fb9f483f87dc48a"},
        {"tools/harness/RedstoneAtlasTrainSourceSuccessor.java",
                "5a17554a52b24c7c8b6114b1368c09d091505695ab42d7d73577dc1ef91d8b17",
                "f5d817cf1584f1f66a5acc9f2d4dc813d17330ee0fcc9d08edaf72186a46f853"}
    };

    private RedstoneAtlasDocumentationSuccessor() { }

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
                    "redstone Atlas documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "redstone Atlas documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "redstone Atlas documentation successor allowlist drifted");
        CraftingAtlasTrainSourceSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
