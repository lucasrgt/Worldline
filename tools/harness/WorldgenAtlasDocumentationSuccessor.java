/** Exact successors for generated worldgen Atlas qualification documentation. */
final class WorldgenAtlasDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02",
                "5f2960f12faa2cbfbb3f1ecf61d98274b9c18b229e067137793f256792206337"},
        {"docs/generated/STATUS.md",
                "39d859fe63fac3e6246201ad3f90ff9a06d4beace9049b01acc54cb663d86fc6",
                "5667e383206d22bb395def989c5db016b89f981403e6cd6d6d3b5148cacf0ee2"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "e630a1086619c89152c1cdc7ee3f02f0eab2d6a4f87d55db4c4c1351e0212526",
                "92693ff1fab4c488d95a1370533f62494fc59cc3cec85c98711b38812cf9f2d1"},
        {"tools/harness/WorldgenAtlasTrainSourceSuccessor.java",
                "026dc399292acce40eb8e065af60f394709bb20ee6a03958f007cfb4fb7e999c",
                "2706d55553c59db3fcf779c51122b9637a85d7651dc314497074ab7fd61968b4"}
    };

    private WorldgenAtlasDocumentationSuccessor() { }

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
                    "worldgen documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "worldgen documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "worldgen documentation successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
