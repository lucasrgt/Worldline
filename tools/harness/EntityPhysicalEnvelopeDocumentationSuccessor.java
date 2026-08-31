/** Exact reviewed generated-document successors for the entity envelope catalog. */
final class EntityPhysicalEnvelopeDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/INDEX.md",
                "264ff16d2ea5ede8617be1515839fd07d797e9af5d0963a84bbe00771848287e",
                "0887e4edde3f9493abcff13fced318e9b0b439334c6a1629c5b20aafbb32950c"},
        {"docs/generated/MILESTONES.md",
                "f1d81629d6ea71f6feddad39906627673e2aa6e4c8f313ade874a2af6e848304",
                "1d23cdb468828e149b68a6aa2f38b5d590cdb898816549f7c1701d2f16448a49"},
        {"docs/generated/STATUS.md",
                "ecda9c340434300086f5da7079b5bc43f0ec01bf3618bd7db12b89ebe11f6c5a",
                "8f8508d24e77f43abb8c4a1ff7cc73719d20ab00d0a14cb9cd36a430b1ede82f"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "797056371c16b71e63f566a60938416512ba77d03371a82b5f6172d7c22b9bdc",
                "388763ed9d05d0d909e3bb686f44806c37bd55fb5cf426e8f2e05f072cbb2f6c"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "06a3bee7b9e7af8ef3b9026b06f385c60544d7e74d964ef2f5580451f4ed2625",
                "c1f86ecc69848d1984856d584475c8a72ed4bd0f0a1f57ee8deea1ee143e2e63"}
    };

    private EntityPhysicalEnvelopeDocumentationSuccessor() { }

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
                    "entity envelope documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "entity envelope documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "entity envelope documentation successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
