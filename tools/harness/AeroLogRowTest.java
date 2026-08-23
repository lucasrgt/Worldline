/** Proves strict parsing of the shared Aero frame vocabulary. */
final class AeroLogRowTest {
    private AeroLogRowTest() { }

    static void execute() {
        String valid = "[Aero_Frame] frameMs=1.5 compileChunksMs=0 compileChunksMaxMs=0 "
                + "gcTimeDeltaMs=0 compileChunksCalls=1 compileChunksSkipped=0 "
                + "compileBudgetSkipped=0 batchQueued=2 cellQueued=3 beViewCulled=0 visibleChunks=4";
        require(AeroLogRow.parse(valid).whole("visibleChunks") == 4, "Aero row value drifted");
        rejects(valid.replace("frameMs=1.5", "frameMs=-1"));
        rejects(valid.replace(" visibleChunks=4", ""));
        rejects(valid + " frameMs=2");
        System.out.println("  shared Aero row parser self-test: passed");
    }

    private static void rejects(String value) {
        try { AeroLogRow.parse(value); throw new IllegalStateException("expected Aero row rejection"); }
        catch (IllegalStateException expected) {
            require(!expected.getMessage().equals("expected Aero row rejection"),
                    "shared Aero row parser accepted invalid input");
        }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
