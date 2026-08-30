/** Exact train successors after regenerated mob-AI Atlas qualification status. */
final class MobAiAtlasDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "96df6b7e5bdcafb99db21c4c16179d0e0686d00c5b7106f1ea4507bc3d1ce82a",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "796b10e2bda5503e945d54e33db35a08ba966cd62a0c6be634d76c719b48e9ff",
                "447f286d984c7156461a10042f58d1e832944da5fedfdf85a656af20db6e7544"},
        {"tools/harness/MobAiAtlasTrainSourceSuccessor.java",
                "bec41a1ab3ae77ab579ccde741a6b91c86abe2636ab22ebab1cda13c33062d4c",
                "b909514f89e5b711d0113f196d004a0c8d8dd99fbc0c8127633b00fd62ac747b"}
    };

    private MobAiAtlasDocumentationSuccessor() { }

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
                    "mob-AI Atlas documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "mob-AI Atlas documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "mob-AI Atlas documentation successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
