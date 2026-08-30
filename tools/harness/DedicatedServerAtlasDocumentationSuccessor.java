/** Exact successors for generated dedicated-server Atlas documentation. */
final class DedicatedServerAtlasDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/STATUS.md",
                "5667e383206d22bb395def989c5db016b89f981403e6cd6d6d3b5148cacf0ee2",
                "460645cca4edee2aba96ae1c234e84fa8f3e37f8126045fda658bc5b83031ada"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "d084458812d845f615917b1be3698d81c86787245a436d48d021e318bf85b16a",
                "3d78f8095018149d749be0b438db0517a71ee8022f585539589d3ad122ecc7cc"},
        {"tools/harness/DedicatedServerAtlasTrainSourceSuccessor.java",
                "48d2b3a11c7435557847be5019aff047a41d90363e986335ddf16fd158dd4fa8",
                "f30b321c74b3a45bcf93828a8ef791685fb74104dbeb589fa06510a319c47ac3"}
    };

    private DedicatedServerAtlasDocumentationSuccessor() { }

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
                    "dedicated server documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "dedicated server documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "dedicated server documentation successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
