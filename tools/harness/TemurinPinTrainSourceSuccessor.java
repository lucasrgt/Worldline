/** Exact reviewed train successors for the resolvable Temurin CI pin. */
final class TemurinPinTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {".github/actions/worldline-test/action.yml",
                "3a02d94cba2f0e16a204174f8c53bee1292f50d55cde13cff239b9244446de78",
                "d28ab34dfcf0d5591d638fef3394ee476fe1575a9e3da4192c672fd791de879e"},
        {".github/workflows/codeql.yml",
                "698edfeaf324a796c14722afd1e6c705581fde941442fe7b69a54c6c234a5fbf",
                "b3763757d269c183d2fdaceec77598713e04a72dfef7e380b4dcbb93870dcd9a"},
        {".github/workflows/milestone-qualification.yml",
                "ee2c2466affdb5611b903d2857f5eed914a98b561b629e02a960a3eb7bf70142",
                "98d7b999181d46c065abed874db958afc1b08eaa252c0ca1997294e3e9f44806"},
        {".github/workflows/publish-testkit.yml",
                "31c39eabca6e95f982fe4b70447f42c07084d8d85c0c9bc132597908ba5f9f65",
                "2612dfbc97ff88c4172f4644206dde1733ea1eda750b2a19c183c414d186b726"},
        {".github/workflows/runtime-verify.yml",
                "6dee7ab4bc9438decbca0e0a6112888e922952a128ee9b5a958829e95a050ce2",
                "5794c332d641f5a244c6ce052a053450236cc2c60b1a006a5aa3768a624a1a5c"},
        {".github/workflows/testkit-external-consumer.yml",
                "eb563d9cdd50a6953c54669c8707d193acfd7cf064c7f0467d596f39af6af964",
                "0497bbc5a33e6e144aae388499c2b15b210f55fc4d16c51972770c7b5ca1df30"},
        {".github/workflows/verify.yml",
                "3791a5e00d2f5ccddc531974704bfd6204e382f293032566d9bf2609473c2fa3",
                "6548c191b1b351d4d8856bfe09e2596b664cdb59313fb0d536aa99994511f4de"},
        {"CONTRIBUTING.md",
                "0ebb8d8f6501837678765975c3bc39348ddddbe2a7600c726841a1caefd2b7d7",
                "c027e7edfaf6251cb5e53cfd9fc3d06766d9fe806d5a34002acb18361e6011a0"},
        {"quality/jdk-pins.properties",
                "7e658987f5850ff24e3d43e489270e4748a932bec89203ed59e047e55e04dca1",
                "6c5c057c07b024f39099ad41115f7513f509f5ededd68e339f2dffdd83759521"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "aed0b8adc4278f6dbaf7ed45d219d1363ec442884d6d5ba211ea07ddc56ebd99",
                "dd0c92624d2bfbff16074e07e4a6a0d2704d2669ffb000d7b4661e06ac03a59d"}
    };

    private TemurinPinTrainSourceSuccessor() { }

    static boolean carries(String relative, String prior, String current) {
        for (String[] successor : SUCCESSORS)
            if (relative.equals(successor[0]) && prior.equals(successor[1])
                    && current.equals(successor[2])) return true;
        return false;
    }

    static void selfTest() {
        for (String[] successor : SUCCESSORS) {
            require(carries(successor[0], successor[1], successor[2]),
                    "Temurin pin train successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "Temurin pin train successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "Temurin pin train successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
