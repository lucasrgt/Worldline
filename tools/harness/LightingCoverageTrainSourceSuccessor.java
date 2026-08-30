/** Exact train successors for the reviewed lighting coverage framing correction. */
final class LightingCoverageTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {"modules/coverage/src/test/java/worldline/coverage/CoverageTest.java",
                "6e20f2ea9dbb075dd5febb154969da4cb72accd3a47cdc2fc5811ae90f95c07f",
                "acd99ae8d98289606baed5cc8601c09a5f031a2d51320a601b559d92718ba9b2"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "efd7f23fb438b012428d80d60b29e324455269a061462ededa7c2bf769c89e38",
                "447efbf7c5139814fedb162d501a953b85772c641b278ef7e4c4a29bc49467b9"},
        {"tools/harness/LightingAtlasDocumentationSuccessor.java",
                "41ed9e85d81b6218c30ab1ecbbe18f2c71f07576ca79698eadb5e28d0d2e195d",
                "773e91b1bc71bd21c83fde5f61ecc4b6b0f52f2bda2173c1cda63afacc835019"}
    };

    private LightingCoverageTrainSourceSuccessor() { }

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
                    "lighting coverage train successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "lighting coverage train successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "lighting coverage train successor allowlist drifted");
        WeatherAtlasTrainSourceSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
