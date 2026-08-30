/** Exact train source successors for the public entity lifecycle TestKit artifacts. */
final class EntityLifecycleArtifactTrainSuccessor {
    private static final String[][] SUCCESSORS = {
        {"release/testkit-artifacts.lock",
                "afcec8bdc6d67beeb3dad1e9aa47f41e97711b76b2c46588597305dd5c377596",
                "f846df93c0a471fb0a8202c7c936a6c37268074e76d9aeadefa6962c83d57e97"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "caa82c1b22421da1bd6ea0561f469e808e214752fcac353d3f1bd8a502303735",
                "416713838efcf9a3ec3cc2ecb1dd4ddfc3eef6ca17c2eefdae51eb324f40a82e"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "6b15f78701ee9693c5b69c18ff39a94b32164e389493135080937896c7201aa0",
                "6fcdd27f3974a2fc114b811d9c9482530fa63ff2391348b417050a9ca2057803"}
    };

    private EntityLifecycleArtifactTrainSuccessor() { }

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
                    "entity lifecycle artifact successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "entity lifecycle artifact successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "entity lifecycle artifact successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
