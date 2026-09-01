/** Exact reviewed successors for the complete public wolf owner-state subsystem. */
final class WolfOwnerStateSubsystemSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "a585a4d036cea9a4ab0e09ca55902b5efd96fa69862d6e244341381d8bd96ce1",
                "cd461359548560cf3c94b85e0ef3d13dea5b34d44ab910bceecbd880bb4f6cbd"},
        {"behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv",
                "0357784f21efd5f1ab0113c7a2ec708ea8f99e3ade5724441864bce965ab08b5",
                "e6556022fe6ae9e7a7954c723d9c23adf835b57eaf2ee11ae87a186208f417cd"},
        {"docs/ATLAS.md",
                "40daaae8250f332efdde2559c3e6ed9a7c3d5055d0f94b8feabba1a9d29d585e",
                "ba90445bb51b14afbccd821657dff36de28ed12e4c303d5427b4725a82056e0a"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "f5411891876dc1a3ea13b416d06cbae98556314cbf72c8baf3ef532d8be1c213",
                "bbccce6d41a6209b2523b18e7f406fce410c3c57a3b828ebf6ee91e05790f701"},
        {"docs/TESTKIT.md",
                "689fde38cbd2dc9ea6333c28e5122f24da6dc22e232363fbd192d8c4d419406b",
                "a73aadd7e9aec0b2be70f49daf6101d86158a94496a90e72fb66b6f1173df44b"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "36319db96636f7db0053f14a2a76ab0d3c5843fbc00c6607811893213cfda110",
                "9504e059651ca3e72b8a50bf9537d3b03e615b51eb133985088c9e00c86bcd7d"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "e30c4052d98f9aef5441215383e95b0fcf23872818186d7087cf35e53a96ce3a",
                "f7de2a8efe1f3b899a802a98483a46228788199b4e87a9224a5068fa85927dc8"},
        {"release/testkit-artifacts.lock",
                "8686ce9f6793f0fd38871b12bb783520d815b5d9abbad5300027b947523c73f9",
                "7fd18904c2f51a75ef4dbd349f8724f0ba58d8e90195b623b838c9a25d3f16bc"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "9d97d2412119531a9aa6ba730fd89032f69abce0454bafd0eca0da2e009a404d",
                "3079633fc6069f9dec18b09026ce6da334d4364a0cf31ad7282f659da5249646"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "31bbc2f0a3a6809513bec68a0dcd2f38a2beba690edc81342fb649e21e249412",
                "da62fd8123880c3d8e25a18610723cb7e4896ab8ce248f3b437e7e0a13b0ae2b"}
    };

    private WolfOwnerStateSubsystemSuccessor() { }

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
                    "wolf owner-state successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "wolf owner-state successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "wolf owner-state successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
