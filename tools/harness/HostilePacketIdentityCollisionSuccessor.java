/** Exact reviewed successors for rejecting hostile packet identity collisions. */
final class HostilePacketIdentityCollisionSuccessor {
    private static final String[][] SUCCESSORS = {
        {"modules/testapi/src/main/java/worldline/testkit/HostileBehaviorFixture.java",
                "26d7089413af36f0af066d0b436449c453f6d360de610238c3a08d5523e9c988",
                "fbd209e564c8ae24d246acca807cb5fa178c7f9a1afaecee4006c391dc8ac1c6"},
        {"release/testkit-artifacts.lock",
                "24d9376916cc918d147574d393d121d58494c86096a31faa2eabfbeb892f82fe",
                "75829a44bda0a2a01abff67824fbc99ddbbde2420d9d178798af6d82e25424f1"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "df722fd8f8c0fb7fbbb221183dcfaacf273af6c566b29ce0d9cba36ee472106f",
                "947d66ff5299a13c4cdf57c4a7b84ae677dc4eb4770f811c1e125f257da1a233"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "26f34ac8dbf78f692cf9448d2b05f6b7e5d1141c9b68098cec1d165ffd5eb3fa",
                "460fd0cd6eb5b571a10a65f3f452200684a07c9566a588dcdccf6da65da008b4"}
    };

    private HostilePacketIdentityCollisionSuccessor() { }

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
                    "hostile packet identity successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "hostile packet identity successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "hostile packet identity successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
