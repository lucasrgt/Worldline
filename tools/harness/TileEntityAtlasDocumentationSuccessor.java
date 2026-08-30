/** Exact train successors after regenerated tile-entity Atlas qualification status. */
final class TileEntityAtlasDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "8a8c97cab9d3ae0972250927904947bb63d848c5617e13cc3d55669334aa1ae8",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "76c5a896684502eff22e952897af0ecd801ad822c6470c662acd17cbf6d52235",
                "d8163b11c4b0a28db96accfdb70fe06592b7c3160e36a8ebdc0ef5ade5be83f6"},
        {"tools/harness/TileEntityAtlasTrainSourceSuccessor.java",
                "3ed1b4e1b1b6773459db04b84245f9ddcd86627c81914a3fa294de06462cf0f9",
                "7685788513566a469bc876ca788acd9ac11e128d5cebfe5e2624750908628717"}
    };

    private TileEntityAtlasDocumentationSuccessor() { }

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
                    "tile entity Atlas documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "tile entity Atlas documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "tile entity Atlas documentation successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
