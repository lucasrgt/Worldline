/** Exact reviewed successors for the final consumer-visible TestKit hashes. */
final class FinalArtifactHashTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {"tooling/gradle-plugin/src/main/java/dev/worldline/gradle/WorldlineDistribution.java",
                "5d10cf545e2424bdd7469e6b7e89bf7ec6bcba2688384efb48f71ba4428a7fe3",
                "38f0247fcf5a89facba15d1907d3d2bc349c6b762bdc9eabeb66312ed17fed63"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "c1cba67598c00568905d6daaab77e8631548bae35c7b218a57b80259bbd5e359",
                "78e5a80cba903507e13f8581503d2403ab2b3a9fe7080bfb7254a99d8e826782"}
    };

    private FinalArtifactHashTrainSourceSuccessor() { }

    static boolean carries(String relative, String prior, String current) {
        for (String[] successor : SUCCESSORS)
            if (relative.equals(successor[0]) && prior.equals(successor[1])
                    && current.equals(successor[2])) return true;
        return false;
    }

    static void selfTest() {
        for (String[] successor : SUCCESSORS) {
            require(carries(successor[0], successor[1], successor[2]),
                    "final artifact hash successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "final artifact hash successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "final artifact hash successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
