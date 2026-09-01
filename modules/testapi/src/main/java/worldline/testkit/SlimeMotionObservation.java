package worldline.testkit;

/** Normalized vertical envelope and ground-state observations from one slime scene. */
public final class SlimeMotionObservation {
    private final boolean sawAir;
    private final boolean sawGround;
    private final int verticalSpanMilli;

    public SlimeMotionObservation(boolean sawAir, boolean sawGround, int verticalSpanMilli) {
        if (verticalSpanMilli < 0) throw new IllegalArgumentException("vertical span");
        this.sawAir = sawAir;
        this.sawGround = sawGround;
        this.verticalSpanMilli = verticalSpanMilli;
    }

    public boolean sawAir() { return sawAir; }
    public boolean sawGround() { return sawGround; }
    public int verticalSpanMilli() { return verticalSpanMilli; }
}
