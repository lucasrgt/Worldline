/** Exact train source successors introduced by the reviewed TestKit artifact refresh. */
final class TestKitArtifactTrainSourceSuccessor {
    private TestKitArtifactTrainSourceSuccessor() { }

    static boolean carries(String relative, String prior, String current) {
        return exact(relative, prior, current, "release/testkit-artifacts.lock",
                "dea09867ea1a1943b2b9d8121adf968ee6822ac4c92e62041b871b1a97de92e8",
                "e28de382e36501a77ae786bd4ebb246a8b2d8558611195da7a98469c2e067e3c")
                || exact(relative, prior, current,
                        "tools/harness/TrainGeneratedDocumentationMigration.java",
                        "7b4f27cd4e44e2a47e94b000d63af463eea88f0eeddabb32508e1520e540889c",
                        "17411fe81fbf959b16ca9e349bb732c12a6f8c3fbabb0ef5dae93fd3639a385e")
                || exact(relative, prior, current, "tools/harness/HarnessFeatureSelfTest.java",
                        "cc30674744a136922cb8e5be2fa89516549eeef7229a2a923a4d6fb327226ffc",
                        "06214c272a99ab7c40f0d0ad383860e04fe6f0b6b746aef55ccc47314149c622");
    }

    private static boolean exact(String relative, String prior, String current,
            String expectedRelative, String expectedPrior, String expectedCurrent) {
        return relative.equals(expectedRelative) && prior.equals(expectedPrior)
                && current.equals(expectedCurrent);
    }

    static void selfTest() {
        require(carries("release/testkit-artifacts.lock",
                        "dea09867ea1a1943b2b9d8121adf968ee6822ac4c92e62041b871b1a97de92e8",
                        "e28de382e36501a77ae786bd4ebb246a8b2d8558611195da7a98469c2e067e3c")
                        && carries("tools/harness/TrainGeneratedDocumentationMigration.java",
                                "7b4f27cd4e44e2a47e94b000d63af463eea88f0eeddabb32508e1520e540889c",
                                "17411fe81fbf959b16ca9e349bb732c12a6f8c3fbabb0ef5dae93fd3639a385e")
                        && carries("tools/harness/HarnessFeatureSelfTest.java",
                                "cc30674744a136922cb8e5be2fa89516549eeef7229a2a923a4d6fb327226ffc",
                                "06214c272a99ab7c40f0d0ad383860e04fe6f0b6b746aef55ccc47314149c622")
                        && !carries("release/testkit-artifacts.lock", "old", "unreviewed")
                        && !carries("tools/harness/TrainGeneratedDocumentationMigration.java",
                                "7b4f27cd4e44e2a47e94b000d63af463eea88f0eeddabb32508e1520e540889c",
                                "unreviewed"),
                "TestKit artifact train source successor drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
