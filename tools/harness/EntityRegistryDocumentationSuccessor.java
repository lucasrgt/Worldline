/** Exact reviewed successor for generated entity registry milestone status. */
final class EntityRegistryDocumentationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02",
                "f1d81629d6ea71f6feddad39906627673e2aa6e4c8f313ade874a2af6e848304"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "fc31b76f4b09c0156b09fc9bc29fc93210c55829656a33cf081597f433c5101a",
                "797056371c16b71e63f566a60938416512ba77d03371a82b5f6172d7c22b9bdc"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "fbdb9015e8006f3b7adad310f5c443349421b87e78b1867a7ff4e66ae79c6290",
                "06a3bee7b9e7af8ef3b9026b06f385c60544d7e74d964ef2f5580451f4ed2625"}
    };

    private EntityRegistryDocumentationSuccessor() { }

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
                    "entity registry documentation successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "entity registry documentation successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "entity registry documentation successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
