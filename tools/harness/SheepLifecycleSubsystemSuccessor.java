/** Exact reviewed successors for the complete public sheep lifecycle subsystem. */
final class SheepLifecycleSubsystemSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "73d361e3140f1ef19b951b739b92f7a1740b9225942db675ed0e87249252e7ba",
                "7730fcc25def95961f8e65c4e9457cfa69c1e26f6d5b10fe28a5429bce22b1d5"},
        {"behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv",
                "d8f4eeae865de3cd37607559687447ff1fe865e724cb4421b018cf13c5d40c1c",
                "ff0d545055737b68d15ce55093cbee94950823b572d1cd16d984ee8b11d9d0fe"},
        {"docs/ATLAS.md",
                "49ad0095480f2ecbb0ffe3e48628dbde18e012b64887e47f0659a8b0ea910f02",
                "db4d74f9d0d69c04e87f5e7aee8fc0b1d377a956c0687c3cf842278d44ca7797"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "8b51620c671196d94d2212407b548db05f387e2f87d5d3e5d18c3a9e698c09fa",
                "7248d4d1a600b8fa06546ea79c77efbaabccf4f4077f82147c62c1db5de7fdf5"},
        {"docs/TESTKIT.md",
                "127da0a855b72ea9e86055e1292487d5b10190ba4044cde67490836420a81cd1",
                "60fbc0859703eaa60fadf8bb4a1d53e8d84fec6058cf5e70df3df0dbeaf5fdf4"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "b61afbba5e89477b0625ea241512c6296ae7b18181aaa6ff1e65987e674306b4",
                "5fe4d6d90f6dd177758680be3e7dce8a4d46bc0b6353103a3ffc74344b153467"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "b9b8a11e7fea9e45d549325d3bb0b2de3c27810444f358def729200c073f82d0",
                "a48397e3ffaa6212ae16d6385ddde2d5143d8ec18bc29b021c06ac20addaa830"},
        {"release/testkit-artifacts.lock",
                "904d6bcfde3746882f3a6dc00963a5bf26473675488ef48b0052ef4b1af67587",
                "a204b1b5a379b4f10567aeb3dcf891ead658d26b86e37f52c6edbb93a3ba172d"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "f3d89581c500170da18b3dfb5122a98cb8d6b13cb239f5a723611dc75ad8d6a7",
                "45f2f1053f7373cc699efcade0be01dd2379ae79748b286398113a31a7b20d19"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "ecfd5dec1b8c7e77c1e28dd79b143dda15c18e99536b61e8e2d89fe66d2bf9fa",
                "89c45c392df675de8c801db6e09be1c7916a72120b3072dc04ee9770904cdbb1"}
    };

    private SheepLifecycleSubsystemSuccessor() { }

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
                    "sheep lifecycle successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "sheep lifecycle successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "sheep lifecycle successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
