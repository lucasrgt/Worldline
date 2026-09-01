/** Exact train successors after regenerated crafting qualification status. */
final class CraftingAtlasDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "19663fbd297d32c75f743ca42f35a54700bf8caa9530f30f713ad17c48cff8a7",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "f7da956f3b153d0c2601e48c85a0ac60c11117e18672c951cdf75fd276467bcb",
                "6cc60edeb84d29979b2d11e7d1f0d9ed9eb59adb707d7dfaf247863456ef0ec2"},
        {"tools/harness/CraftingAtlasTrainSourceSuccessor.java",
                "7455a5b7398fa20690a8c01fc9d7192e8511b19f4451646665bbba01025df236",
                "adb55406f7a27e769ba576fbe7d477d8c2c004770e04701c72af5bccc76ba834"}
    };

    private CraftingAtlasDocumentationSuccessor() { }

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
                    "crafting Atlas documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "crafting Atlas documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "crafting Atlas documentation successor allowlist drifted");
        TileEntityAtlasTrainSourceSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
