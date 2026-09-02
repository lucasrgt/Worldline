/** Exact reviewed train successors for GitHub Actions dependency upgrades. */
final class GithubActionsTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {".github/workflows/codeql.yml",
                "fc083ecb4dbaa43c4310b3af15153763640c44c1055ddeb19a1c136300db9aeb",
                "698edfeaf324a796c14722afd1e6c705581fde941442fe7b69a54c6c234a5fbf"},
        {".github/workflows/publish-testkit.yml",
                "5b15806af319facc7692253560249c4c3ee4fa47930a8cdc43b9fd4c6a574775",
                "31c39eabca6e95f982fe4b70447f42c07084d8d85c0c9bc132597908ba5f9f65"},
        {".github/workflows/runtime-verify.yml",
                "25362b9c207f012c8c11913601a13862356628d018c92a6d2727c43d21da117f",
                "6dee7ab4bc9438decbca0e0a6112888e922952a128ee9b5a958829e95a050ce2"},
        {".github/workflows/testkit-external-consumer.yml",
                "bc9c5803576b927d3038f3525dee4a5a4184e809c6bad91cccb2ae58d0b340c1",
                "eb563d9cdd50a6953c54669c8707d193acfd7cf064c7f0467d596f39af6af964"},
        {".github/workflows/verify.yml",
                "e1690f65c30e7cdca14efddfb49a0c104f9152faf1c9a4c18d1331f536db6ab5",
                "3791a5e00d2f5ccddc531974704bfd6204e382f293032566d9bf2609473c2fa3"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "78e5a80cba903507e13f8581503d2403ab2b3a9fe7080bfb7254a99d8e826782",
                "aed0b8adc4278f6dbaf7ed45d219d1363ec442884d6d5ba211ea07ddc56ebd99"}
    };

    private GithubActionsTrainSourceSuccessor() { }

    static boolean carries(String relative, String prior, String current) {
        for (String[] successor : SUCCESSORS)
            if (relative.equals(successor[0]) && prior.equals(successor[1])
                    && current.equals(successor[2])) return true;
        return false;
    }

    static void selfTest() {
        for (String[] successor : SUCCESSORS) {
            require(carries(successor[0], successor[1], successor[2]),
                    "GitHub Actions train successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "GitHub Actions train successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "GitHub Actions train successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
