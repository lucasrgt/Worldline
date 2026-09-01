/** Exact successors for the public worldgen TestKit runner artifact. */
final class WorldgenArtifactSuccessor {
    private static final String[][] SUCCESSORS = {
        {"release/testkit-artifacts.lock",
                "a54c040e8fd589731e81c2d0764bb5a811f1bd882e22445c9326a8165e896286",
                "a56079355fb4183738d61dbe296c01c03a480c65a1887a60ce67ba4841afa59e"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "92693ff1fab4c488d95a1370533f62494fc59cc3cec85c98711b38812cf9f2d1",
                "f97fdd165ef73c7ceea901f618d4eb54c87d425d16c244f6e0d9eff0f29bed54"},
        {"tools/harness/WorldgenAtlasDocumentationSuccessor.java",
                "1cf1348574d09da0db7c54cfb159811f46654b9ad9cd5e8bc67bf683c3eb41d3",
                "41c361368b6e77c7b4a9e07c448f898b35a42923852b7981d7d218773bee3077"}
    };

    private WorldgenArtifactSuccessor() { }

    static boolean carries(String relative, String prior, String current) {
        for (String[] successor : SUCCESSORS) {
            if (relative.equals(successor[0]) && prior.equals(successor[1])
                    && current.equals(successor[2])) {
                return true;
            }
        }
        return false;
    }

    static void selfTest() {
        for (String[] successor : SUCCESSORS) {
            require(carries(successor[0], successor[1], successor[2]),
                    "worldgen artifact successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "worldgen artifact successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "worldgen artifact successor allowlist drifted");
        WorldgenDataCycleAttestationSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
