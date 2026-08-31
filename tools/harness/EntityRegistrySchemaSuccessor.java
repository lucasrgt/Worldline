/** Exact reviewed successor for the universal entity registry schema refresh. */
final class EntityRegistrySchemaSuccessor {
    private static final String[][] SUCCESSORS = {
        {"smokes/schema-migration.lock",
                "d68f310efcc0b894a75014f86c647b6fa01e9dc851296b94c421ee43e78237d4",
                "279b759fde7d85c2bf28c9b8c3a6c551a58a525fd92338ea56e14d13fce68e42"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "027bfd12c7370a0e10d8fb1b4a14ac49d2ec536365348935246512d1ac045a89",
                "fc31b76f4b09c0156b09fc9bc29fc93210c55829656a33cf081597f433c5101a"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "c77efe1f04a2cefa35b3ae4cfb53d95c88894d6c9cf21518f872907b70e3c50e",
                "fbdb9015e8006f3b7adad310f5c443349421b87e78b1867a7ff4e66ae79c6290"}
    };

    private EntityRegistrySchemaSuccessor() { }

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
                    "entity registry schema successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "entity registry schema successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "entity registry schema successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
