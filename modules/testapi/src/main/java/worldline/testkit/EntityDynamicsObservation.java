package worldline.testkit;

/** One normalized vertical or horizontal controlled-entity motion outcome. */
public final class EntityDynamicsObservation {
    private final boolean vertical;
    private final boolean sawAir;
    private final boolean sawGround;
    private final boolean horizontalCollision;
    private final int verticalSpanMilli;
    private final int finalXMilli;
    private final int motionXMilli;

    private EntityDynamicsObservation(boolean vertical, boolean sawAir, boolean sawGround,
            boolean horizontalCollision, int verticalSpanMilli, int finalXMilli,
            int motionXMilli) {
        this.vertical = vertical;
        this.sawAir = sawAir;
        this.sawGround = sawGround;
        this.horizontalCollision = horizontalCollision;
        this.verticalSpanMilli = verticalSpanMilli;
        this.finalXMilli = finalXMilli;
        this.motionXMilli = motionXMilli;
    }

    public static EntityDynamicsObservation vertical(int spanMilli,
            boolean sawAir, boolean sawGround) {
        if (spanMilli < 0) throw new IllegalArgumentException("vertical span");
        return new EntityDynamicsObservation(true, sawAir, sawGround,
                false, spanMilli, 0, 0);
    }

    public static EntityDynamicsObservation horizontal(int finalXMilli,
            int motionXMilli, boolean collision) {
        return new EntityDynamicsObservation(false, false, false,
                collision, 0, finalXMilli, motionXMilli);
    }

    public boolean vertical() { return vertical; }
    public boolean sawAir() { return sawAir; }
    public boolean sawGround() { return sawGround; }
    public boolean horizontalCollision() { return horizontalCollision; }
    public int verticalSpanMilli() { return verticalSpanMilli; }
    public int finalXMilli() { return finalXMilli; }
    public int motionXMilli() { return motionXMilli; }
}
