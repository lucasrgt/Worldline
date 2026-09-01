/** Exact reviewed successor for the complete sheep fixture's conformance test plan. */
final class SheepLifecycleTestPlanSuccessor {
    private static final String[][] SUCCESSORS = {
        {"modules/testkit/src/test/java/worldline/testkit/SheepLifecycleFixtureTest.java",
                "3fa78e7f404406f2c8c356c7ee81a52d4a7ecfbe0875509f2cabed156e45cbde",
                "85ee9931fcfeb6e057cbafd0c6d80679e9889c45ffce0059fcd2d55348fbd66a"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "45f2f1053f7373cc699efcade0be01dd2379ae79748b286398113a31a7b20d19",
                "873a14221c154b173d3c1cd593c938137a891834a209c1e6035fde67458836ad"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "89c45c392df675de8c801db6e09be1c7916a72120b3072dc04ee9770904cdbb1",
                "a3dbedf775f8531ebbb3b0af2a182a4bc58a7ea6a997390cbd979f1aae77b69c"}
    };

    private SheepLifecycleTestPlanSuccessor() { }

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
                    "sheep test-plan successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "sheep test-plan successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "sheep test-plan successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
