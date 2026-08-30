/** Exact train successors after regenerated fluid Atlas qualification status. */
final class FluidAtlasDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "277ada9b2471a208454a2a7264b8b989cc4053a10a99cdb40387bfdea03b5aae",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "d19a66f3ae6bb97985023242b6c760c751f399546aed6be6cdf3810e14c5d8e7",
                "2d7628b37317264c1462eaff947b5bf953a1fba46098f8076314c3c171905eca"},
        {"tools/harness/FluidAtlasTrainSourceSuccessor.java",
                "f8f8a4b22481ed65f87578aca92169623e6fb78b2a0ee4f03e95ecaa26cf6c25",
                "43eb1481a41cbfec55c9745baf587117f80372dd4b1ca15606c8bb866d43daa6"}
    };

    private FluidAtlasDocumentationSuccessor() { }

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
                    "fluid Atlas documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "fluid Atlas documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "fluid Atlas documentation successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
