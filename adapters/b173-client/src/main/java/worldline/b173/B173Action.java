package worldline.b173;

/** One realized deterministic boundary event in replay order. */
final class B173Action {
    private static final int KEY = 1;
    private static final int MOUSE = 2;
    private static final int RESEED = 3;
    final int tick;
    private final int kind;
    private final int a;
    private final int b;
    private final int c;
    private final int d;
    private final long value;

    private B173Action(int tick, int kind, int a, int b, int c, int d, long value) {
        this.tick = tick;
        this.kind = kind;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.value = value;
    }

    static B173Action key(int tick, int key, boolean pressed, char character) {
        return new B173Action(tick, KEY, key, pressed ? 1 : 0, character, 0, 0L);
    }

    static B173Action mouse(int tick, int button, boolean pressed, int wheel, int x, int y) {
        return new B173Action(tick, MOUSE, button, pressed ? 1 : 0, wheel, x, y);
    }

    static B173Action reseed(int tick, long seed) {
        return new B173Action(tick, RESEED, 0, 0, 0, 0, seed);
    }

    static B173Action decoded(int tick, int kind, int a, int b, int c, int d, long value) {
        require(tick >= 0, "action tick must not be negative");
        require(b == 0 || b == 1, "action pressed flag must be 0 or 1");
        if (kind == KEY) {
            require(c >= Character.MIN_VALUE && c <= Character.MAX_VALUE && d == 0 && value == 0L,
                    "invalid key action");
        } else if (kind == MOUSE) {
            require(value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE, "invalid mouse y");
        } else if (kind == RESEED) {
            require(a == 0 && b == 0 && c == 0 && d == 0, "invalid reseed action");
        } else throw new IllegalArgumentException("unknown replay action " + kind);
        return new B173Action(tick, kind, a, b, c, d, value);
    }

    int kind() { return kind; }
    int a() { return a; }
    int b() { return b; }
    int c() { return c; }
    int d() { return d; }
    long value() { return value; }

    void apply(B173ClientBackend backend) {
        if (kind == KEY) backend.key(a, b != 0, (char) c);
        else if (kind == MOUSE) backend.mouse(a, b != 0, c, d, (int) value);
        else if (kind == RESEED) backend.reseed(value);
        else throw new IllegalStateException("unknown replay action " + kind);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
