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

    void apply(B173ClientBackend backend) {
        if (kind == KEY) backend.key(a, b != 0, (char) c);
        else if (kind == MOUSE) backend.mouse(a, b != 0, c, d, (int) value);
        else if (kind == RESEED) backend.reseed(value);
        else throw new IllegalStateException("unknown replay action " + kind);
    }
}
