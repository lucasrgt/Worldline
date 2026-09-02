/** Exact reviewed source successors produced while reconciling the two completed trains. */
final class ReconciledTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {"smokes/composite-cycle-migration.lock",
                "cfbe03018482beb18f9f96c8b783b354b23e3648cc1633d34373cbdb5cb8127b",
                "c7c68898d543ef8fec970156c6b3e2f1cc4316e3fdb5f0c2baa98c6564f45c63"},
        {"smokes/data-driven-migration.lock",
                "52d97cc2e9481e6ee40d4bf3f1bccd9ad7df75cdd50d6dd179930fcdb61031b6",
                "ff29c552f992c5c97b9c906ca75ee42961e75113c596dc5ed9b8cbf4d00397eb"},
        {"smokes/gui-workbench.lock",
                "e0c29c3b67e4933f45a1f272b43f74e752fc48c112a31ac2f1506765d3ec7cf3",
                "89cd17f83bfe5a49ccfb90734f87cf8139d697f2633edae4f7f074949429e993"},
        {"smokes/telemetry-migration.lock",
                "d17c9341b7626bc4cde39befe01897378a3ecbf981aaa1d9b073fe12a0b8abad",
                "88b4ca3003fba6f601dd5c7b187e78846cd8b7887a77a5269c37c3c56d3233fd"},
        {"tools/harness/CompositeCycleMigration.java",
                "9b27a633abf9a97ba61a24356669ab852cb8ac1733155f85a94c4dbdd31218be",
                "7afc626a32eb9bbc79d4f9997e91a4acc6d2bf7f6b9985351eafac11e6eee041"},
        {"tools/harness/DataDrivenCycleMigration.java",
                "8582e0dc46b8b839de43014f2baf370e44f9b19a3b91ab38433c316290c18dee",
                "24b358c09fe2cb36a22dd9348483c8f157ea108ed03893976b601bb78a52ccce"},
        {"tools/harness/GuiWorkbenchPinMigration.java",
                "7ae6f241275c44a9ab50786701281b96fe2fbea7f144275f832b96510ca7b18e",
                "15e5c6f90ddd41e120270867d8814c6747c12ed375af30c74a47febbc6461a30"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "50c110353a11a59755a33474df55093014a8ec58e2b11efb38d45d2cf0d1d569",
                "1b237ee007df4a1a55990af9348d18c14865c9b2e4a375c76c0e476d1681e3b4"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "5386c0fb7895f75a04a282019cb61eea714bb78c3be8507f0c7dcfb389d426cb",
                "76cf6e91d27ce26b13c6e14e96003f6907ca7c999c9be3aabfbdfe8d7acd8975"},
        {"tools/harness/SmokeSupport.java",
                "767e44f398efc3bd92392e8c881ff8471c03168c31198b321266107b92cfe9e7",
                "f7ef4a17cf108645992f39d8a53ee43a20f41922dc9555ad9852a0abe9e0e1c8"},
        {"tools/smoke/DataDrivenCycle.java",
                "d48a057785848c9daeb26ef4a7f97792be8bdf17367ef7e9bf5cdefebb1c2911",
                "217c93545fee75c2b0493b67903a0a676d8c0584e9a8d36461b854d75dddd76f"},
        {"tools/harness/CandidateReadiness.java",
                "fbd12f00a08e2d17c8a41033e3efa6b02456b4f8e9451af6db1ddebc49f70031",
                "1112c53e31975e0e0fa17cfb5ef7747bbfb678f10e3b76a3f4dfe181493276eb"},
        {"tools/harness/CandidatePolicyReadiness.java",
                "62cd0afe992621be577d2c56199820f3be74fead2214dedfe08c4a90b6cb0781",
                "94f8a4242e4ddd0e567683430204f1e957b8eb3b27e1784ce1f67723f93b1a36"},
        {"tools/harness/DataDrivenRefreshEvidence.java",
                "c8795fdd0bd82b880b5b614d66277d195db0462b77240a5f9d4939357a53ceb6",
                "73d5bd7ad3ebd746612c78c5f3e869d0194f004d3fe35ffa7e3eac2781d860bf"},
        {"smokes/m747-shears-leaf-durability/src/worldline/smoke/shearsleafdurab173/ShearsLeafDurabilitySmoke.java",
                "64e5321f6ef6bb82eee2affba3d34ea7bdc2f55085779eefddace59881b22519",
                "69b92223b8a0442d40ab591ad971c74ab3155670a396e470e9010df1a15ead48"},
        {"smokes/m763-torch-support-break/src/worldline/smoke/torchsupportbreakb173/TorchSupportBreakSmoke.java",
                "13495b319199afe992a51c93c8a56d87520720b8e6535b3123da27d2b19b6c16",
                "51305cbefc870fe13cbfc8af57339fd54d0ca7aec3e534732fb1b3af752750c7"},
        {"smokes/m790-aero-default-rollback/runner-src/M790Runtime.java",
                "fc19eb21857596a415be96b3de946c33bd83f4581e141fcdc51029900ce534f5",
                "4b89475ca82a6806701807e1c84b4493c2b842d12d49c5ceadb0ad134eb155e2"},
        {"smokes/m790-aero-default-rollback/runtime-src/worldline/m790/HexDigest.java",
                "c4ce85ceb431c119a6755ace5d82cadf9a32d7e5e67e717069969e5e6e388616",
                "removed"},
        {"smokes/m790-aero-default-rollback/runtime-src/worldline/m790/QueueReuseProbe.java",
                "2be7dc0dd6257c6285642d2caff4c48bd2d7c89080d038b7e8067c1d4dcc4b06",
                "54bbc9896a3ec1581c9e95ab108d6cd174971ba3ff31fcbf1a3f3b3662a47689"}
    };

    private ReconciledTrainSourceSuccessor() { }

    static boolean carries(String relative, String prior, String current) {
        for (String[] successor : SUCCESSORS)
            if (relative.equals(successor[0]) && prior.equals(successor[1])
                    && current.equals(successor[2])) return true;
        return false;
    }

    static void selfTest() {
        for (String[] successor : SUCCESSORS) {
            require(carries(successor[0], successor[1], successor[2]),
                    "reconciled train successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "reconciled train successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "reconciled train successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
