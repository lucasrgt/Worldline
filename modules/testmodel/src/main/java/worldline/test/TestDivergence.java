package worldline.test;

/** Last semantic observation available when an assertion diverged. */
public final class TestDivergence implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final long tick;
    private final String field, role;

    public TestDivergence(long tick, String field, String role) {
        this.tick = tick; this.field = field; this.role = role;
    }
    public long tick() { return tick; }
    public String field() { return field; }
    public String role() { return role; }
}
