/** Exact source successors for the repository launcher and its fresh-JVM regression test. */
final class AtlasLauncherSourceSuccessor {
    private static final String[][] SOURCES = {
        {"tools/replay/Replay.java",
                "52fff7782a711481254b526af31b967fcb63a96b008b8b0f57b830e9dc7ea3e1",
                "d5eefbdb24b53509dfe3902f4cfd6cc70c44d362c779f801fd6f7ab61db4782f"},
        {"modules/cli/src/test/java/worldline/cli/WorldlineCliTest.java",
                "0feafc35e4382af27c32e060abce6a485c08ddb21bd974abc1f0a4e1c2f203fa",
                "00f239aaa39f7dae73dceffae64b68a3057a669a1c40f4c8eb1772fa6743a5be"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "5e7b4760d0e4baf564f67ff0c11290c7a3bc607a7c3f792e611f1ee44559f1ee",
                "2a7af9529a2f52db04c86ba68b7c6952fa73da1a69360efae2ec2048350df372"}
    };

    private AtlasLauncherSourceSuccessor() { }

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
                throw new IllegalStateException("Atlas launcher successor drift: " + source[0]);
            }
        }
        if (carries("unreviewed", "old", "new")) {
            throw new IllegalStateException("Atlas launcher successor scope drift");
        }
    }
}
