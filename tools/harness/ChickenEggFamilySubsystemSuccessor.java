/** Exact reviewed successors for the honest public chicken-and-egg family subsystem. */
final class ChickenEggFamilySubsystemSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "7730fcc25def95961f8e65c4e9457cfa69c1e26f6d5b10fe28a5429bce22b1d5",
                "a585a4d036cea9a4ab0e09ca55902b5efd96fa69862d6e244341381d8bd96ce1"},
        {"behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv",
                "ff0d545055737b68d15ce55093cbee94950823b572d1cd16d984ee8b11d9d0fe",
                "0357784f21efd5f1ab0113c7a2ec708ea8f99e3ade5724441864bce965ab08b5"},
        {"docs/ATLAS.md",
                "db4d74f9d0d69c04e87f5e7aee8fc0b1d377a956c0687c3cf842278d44ca7797",
                "40daaae8250f332efdde2559c3e6ed9a7c3d5055d0f94b8feabba1a9d29d585e"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "7248d4d1a600b8fa06546ea79c77efbaabccf4f4077f82147c62c1db5de7fdf5",
                "f5411891876dc1a3ea13b416d06cbae98556314cbf72c8baf3ef532d8be1c213"},
        {"docs/TESTKIT.md",
                "60fbc0859703eaa60fadf8bb4a1d53e8d84fec6058cf5e70df3df0dbeaf5fdf4",
                "689fde38cbd2dc9ea6333c28e5122f24da6dc22e232363fbd192d8c4d419406b"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "5fe4d6d90f6dd177758680be3e7dce8a4d46bc0b6353103a3ffc74344b153467",
                "36319db96636f7db0053f14a2a76ab0d3c5843fbc00c6607811893213cfda110"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "a48397e3ffaa6212ae16d6385ddde2d5143d8ec18bc29b021c06ac20addaa830",
                "e30c4052d98f9aef5441215383e95b0fcf23872818186d7087cf35e53a96ce3a"},
        {"release/testkit-artifacts.lock",
                "a204b1b5a379b4f10567aeb3dcf891ead658d26b86e37f52c6edbb93a3ba172d",
                "8686ce9f6793f0fd38871b12bb783520d815b5d9abbad5300027b947523c73f9"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "873a14221c154b173d3c1cd593c938137a891834a209c1e6035fde67458836ad",
                "fc39e2cfa12118c7363a2f03fff1ebd2b57d8499558c9b973d2cc73efbf77f23"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "a3dbedf775f8531ebbb3b0af2a182a4bc58a7ea6a997390cbd979f1aae77b69c",
                "3e43f3732a32c3e753740801f95c2eb2fd2af2ef90705f5b3224083027da8b41"}
    };

    private ChickenEggFamilySubsystemSuccessor() { }

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
                    "chicken egg-family successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "chicken egg-family successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "chicken egg-family successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
