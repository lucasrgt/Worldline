/** Exact successors for the dedicated-server semantic runner artifact. */
final class DedicatedServerArtifactSuccessor {
    private static final String[][] SUCCESSORS = {
        {"release/testkit-artifacts.lock",
                "a56079355fb4183738d61dbe296c01c03a480c65a1887a60ce67ba4841afa59e",
                "120612d5618446e7d16560153b43eca0a8ffae37095607bb4c2f17b5fbc889d5"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "3d78f8095018149d749be0b438db0517a71ee8022f585539589d3ad122ecc7cc",
                "7940135b8bcca3217205388c1f8e2869d42616875fe06edcc961ad50f0d9d762"},
        {"tools/harness/DedicatedServerAtlasDocumentationSuccessor.java",
                "1fda6e51a670c6c81ab1d54942f5a38846845aeb360503920e737d4427536e7c",
                "aff320764f9045065fa4b5cea0aa18cf5df671251dcf2b2d3f1e742a58ed4f9f"}
    };

    private DedicatedServerArtifactSuccessor() { }

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
                    "dedicated server artifact successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "dedicated server artifact successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "dedicated server artifact successor allowlist drifted");
        MappingAtlasSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
