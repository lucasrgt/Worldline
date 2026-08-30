/** Exact train source successors introduced by provider-discovery reconciliation. */
final class ProviderDiscoveryTrainSuccessor {
    private static final String[][] SUCCESSORS = {
        {"smokes/provider-discovery.lock",
                "ba8c1eff0caa45dbf26c1aeeb4838d9843301226688ddcb2a388f8e37d6ec9f6",
                "4789dbbd3d93413f665b90ad4ae432e95d88f78e36f1a2ace8f3c40028055607"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "784106bc29fbea3b1ec018e7ff1a9e3e009036ba0ce34bda888f0052e3ec916a",
                "2d7d47bba696e5d29586227d21eae5a6199b6d17134af06496d975594cb0aea5"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "435c9e7e24803d04480dbbe31ccda92399191c1299a112b45ad7c47b5185d24c",
                "49c93f2d361a5a19988878ae431a335f2bee1fc73290af4b166da7d4559a43b6"}
    };

    private ProviderDiscoveryTrainSuccessor() { }

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
                    "provider-discovery train successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "provider-discovery train successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "provider-discovery train successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
