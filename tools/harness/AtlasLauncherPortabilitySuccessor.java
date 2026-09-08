/** Exact follow-up for candidate-local launcher tests and the extracted successor registry. */
final class AtlasLauncherPortabilitySuccessor {
    private static final String[][] SOURCES = {
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "2a7af9529a2f52db04c86ba68b7c6952fa73da1a69360efae2ec2048350df372",
                "f0d7882129afb020d6e30cfc462f6dd470983b7fcb2f1ff5f337492ca9a2fcdc"},
        {"modules/cli/src/test/java/worldline/cli/RepositoryLauncherTest.java",
                "7fe7f6e6307aed1149c0025462f7d75fb69a151893d54d563eb56c34f3818cf5",
                "f04f27ad19e6e48cbb3612978b9c18b48086cfa24db657bc1216f9b75b2459d9"}
    };

    private AtlasLauncherPortabilitySuccessor() { }

    static boolean carries(String path, String prior, String current) {
        for (String[] source : SOURCES) {
            if (path.equals(source[0]) && prior.equals(source[1])
                    && current.equals(source[2])) return true;
        }
        return false;
    }

    static void selfTest() {
        for (String[] source : SOURCES) {
            if (!carries(source[0], source[1], source[2])
                    || carries(source[0], source[1], "unreviewed")) {
                throw new IllegalStateException("Atlas launcher portability drift: " + source[0]);
            }
        }
        if (carries("unreviewed", "old", "new")) {
            throw new IllegalStateException("Atlas launcher portability scope drift");
        }
    }
}
