/** Exact successors for the canonical M622 behavior-manifest framing. */
final class MobAiBehaviorManifestSuccessor {
    private static final String[][] SUCCESSORS = {
        {"smokes/m622-pathfinding-matrix/smoke.properties",
                "37151e3b3534f2ed85bec1557fbaee0a73aad30c3a2333d1c1f6bae453248921",
                "1a212e001797f1e7ae64e381d9aa092232fc08c9871dd2f3786220cb8e7e9b0f"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "447f286d984c7156461a10042f58d1e832944da5fedfdf85a656af20db6e7544",
                "e3fcc1a871bdb18804ea0256e0e490df5d2dd6663b4b5b92e6ccfd01b944706d"},
        {"tools/harness/MobAiAtlasDocumentationSuccessor.java",
                "cd4dc3f9c720642da1bd9ef39f0665f4eb951fe64d3fea004630be107cbad89e",
                "ce633f96bd28fa3f9e61bc1b64e1a12cbd5b4450aa87352b7462c64c5d6fcfa3"}
    };

    private MobAiBehaviorManifestSuccessor() { }

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
                    "mob-AI behavior manifest successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "mob-AI behavior manifest successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "mob-AI behavior manifest successor allowlist drifted");
        MobAiBehaviorCatalogPlacementSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
