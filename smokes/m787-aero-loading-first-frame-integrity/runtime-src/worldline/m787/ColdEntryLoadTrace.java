package worldline.m787;

/** Records the synchronous vanilla restored-world loading display sequence. */
public final class ColdEntryLoadTrace {
    private static final StringBuilder SEQUENCE = new StringBuilder();
    private static boolean active;
    private static int starts, building, simulating, renderWorldCalls;

    private ColdEntryLoadTrace() {}

    static void begin() {
        SEQUENCE.setLength(0);
        starts = building = simulating = renderWorldCalls = 0;
        active = true;
    }

    public static void title(String value) {
        if (!active) return;
        starts++;
        append(value);
    }

    public static void stage(String value) {
        if (!active) return;
        if ("Building terrain".equals(value)) building++;
        if ("Simulating world for a bit".equals(value)) simulating++;
        append(value);
    }

    public static void renderWorld() {
        if (active) renderWorldCalls++;
    }

    static void finish() { active = false; }
    static int starts() { return starts; }
    static int building() { return building; }
    static int simulating() { return simulating; }
    static int renderWorldCalls() { return renderWorldCalls; }
    static String sequence() { return SEQUENCE.toString(); }

    private static void append(String value) {
        if (SEQUENCE.length() > 0) SEQUENCE.append('>');
        SEQUENCE.append(value == null ? "<null>" : value.replace('\n', ' '));
    }
}
