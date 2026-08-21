package worldline.test;

/** Original source location captured while a test is collected. */
public final class TestLocation {
    private final String file;
    private final int line;

    TestLocation(String file, int line) { this.file = file; this.line = line; }
    public String file() { return file; }
    public int line() { return line; }
    @Override public String toString() { return file + ":" + line; }
}
