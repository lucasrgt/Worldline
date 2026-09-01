/** Exact successor for train-reviewed evolution of a data-cycle source attestation. */
final class WorldgenDataCycleAttestationSuccessor {
    private static final String[][] SUCCESSORS = {
        {"tools/harness/DataDrivenCycleCheck.java",
                "d2452f718828676903f4549da91b472c927a722567bc56e529a2cf91448c2fc3",
                "5111e6fdfc30d0da65a429e1fc598888d871eb900ff028d2bf340c7c8545d8c9"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "f97fdd165ef73c7ceea901f618d4eb54c87d425d16c244f6e0d9eff0f29bed54",
                "b839f1ffa1bd7adb4f441c3efe96624d54faa9a79366f118ab3c14d6f61a2216"},
        {"tools/harness/WorldgenArtifactSuccessor.java",
                "9d99ccb15967c619db454aac88569361c5c3832950918500e40a72a286f71f61",
                "3664a52283b4d79ada5bebe14259e1af3b98c8846a322e91801d1350645b85b7"}
    };

    private WorldgenDataCycleAttestationSuccessor() { }

    static boolean carries(String relative, String prior, String current) {
        for (String[] successor : SUCCESSORS) {
            if (relative.equals(successor[0]) && prior.equals(successor[1])
                    && current.equals(successor[2])) {
                return true;
            }
        }
        return false;
    }

    static void selfTest() {
        for (String[] successor : SUCCESSORS) {
            require(carries(successor[0], successor[1], successor[2]),
                    "worldgen data-cycle successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "worldgen data-cycle successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "worldgen data-cycle successor allowlist drifted");
        DedicatedServerAtlasTrainSourceSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
