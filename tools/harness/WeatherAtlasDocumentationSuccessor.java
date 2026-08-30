/** Exact train successors after regenerated weather Atlas qualification status. */
final class WeatherAtlasDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "7824c79962893e54dd033b35ef53d3939c9d0e3dcd1dce44a8d8e95c6582938d",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "511a6c4da084f61d79b9d351f748660509ac3889132be24ecc00f98e1ece439b",
                "913a5a76b8c914d6853b18185528b526513bd1362d7a75670813f49c5963af01"},
        {"tools/harness/WeatherAtlasTrainSourceSuccessor.java",
                "56d3aa7a25075db1e315ddbf98ccf0c423bde69518c6b3275963e5074eba7aea",
                "61e4010f2d36987c80bd6d62196934f202b5d1906127e0e82a19d58220b45f82"}
    };

    private WeatherAtlasDocumentationSuccessor() { }

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
                    "weather Atlas documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "weather Atlas documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "weather Atlas documentation successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
