/** Exact reviewed successors for the honest entity verification-floor correction. */
final class HonestEntityVerificationFloorSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/families.tsv",
                "b1c472963f3635800db69acb31a55342533065082d617c1c682b286d04c3c71d",
                "921fa11c0fe91aeb2e8dc975b3e0ad2213ab3961b807a74c9cab03aedccafe28"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "fc39e2cfa12118c7363a2f03fff1ebd2b57d8499558c9b973d2cc73efbf77f23",
                "9d97d2412119531a9aa6ba730fd89032f69abce0454bafd0eca0da2e009a404d"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "3e43f3732a32c3e753740801f95c2eb2fd2af2ef90705f5b3224083027da8b41",
                "31bbc2f0a3a6809513bec68a0dcd2f38a2beba690edc81342fb649e21e249412"}
    };

    private HonestEntityVerificationFloorSuccessor() { }

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
                    "honest entity verification-floor successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "honest entity verification-floor successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "honest entity verification-floor successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
