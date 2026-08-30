/** Exact train successors after regenerated lighting Atlas qualification status. */
final class LightingAtlasDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "f189928b8b5c14fe66b69d7726d6804cffc74e41829562f2b47fffedd1baf182",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "fce6db68190bab49ffa27c7f145d7df9610770a2075f2a9ba0aefa1ed0b9148c",
                "efd7f23fb438b012428d80d60b29e324455269a061462ededa7c2bf769c89e38"},
        {"tools/harness/LightingAtlasTrainSourceSuccessor.java",
                "4afa4a72e8015c350e87d5200a7c0c0727589ad7cb6a544b815a35d7009432d7",
                "f45a7b3da85bd1791fd968ff8ddd7198e1fb30d8fae83741e00f735003a49870"}
    };

    private LightingAtlasDocumentationSuccessor() { }

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
                    "lighting Atlas documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "lighting Atlas documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "lighting Atlas documentation successor allowlist drifted");
        LightingCoverageTrainSourceSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
