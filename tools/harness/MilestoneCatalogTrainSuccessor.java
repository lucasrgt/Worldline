/** Exact train source successors introduced by milestone-catalog regeneration. */
final class MilestoneCatalogTrainSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "5f2960f12faa2cbfbb3f1ecf61d98274b9c18b229e067137793f256792206337",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "2d7d47bba696e5d29586227d21eae5a6199b6d17134af06496d975594cb0aea5",
                "caa82c1b22421da1bd6ea0561f469e808e214752fcac353d3f1bd8a502303735"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "49c93f2d361a5a19988878ae431a335f2bee1fc73290af4b166da7d4559a43b6",
                "6b15f78701ee9693c5b69c18ff39a94b32164e389493135080937896c7201aa0"}
    };

    private MilestoneCatalogTrainSuccessor() { }

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
                    "milestone-catalog train successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "milestone-catalog train successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "milestone-catalog train successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
