import java.util.Random;

/** Official-name fire age, placement, harvest, persistence, physics, and support probe. */
final class OracleFireProbe {
    final int placementRoute, placedState, stackAfter, metadataMask;
    final int strengthClass, breakBefore, breakAfter, dropDelta, savedState;
    final int collisionNull, collidable, lightCode, tickMask, tickRate;
    final int supportedState, lossBefore, lossAfter;
    private OracleFireProbe(int placementRoute, int placedState, int stackAfter,
            int metadataMask, int strengthClass, int breakBefore, int breakAfter, int dropDelta,
            int savedState, int collisionNull, int collidable, int lightCode, int tickMask,
            int tickRate, int supportedState, int lossBefore, int lossAfter) {
        this.placementRoute = placementRoute;
        this.placedState = placedState;
        this.stackAfter = stackAfter;
        this.metadataMask = metadataMask;
        this.strengthClass = strengthClass;
        this.breakBefore = breakBefore;
        this.breakAfter = breakAfter;
        this.dropDelta = dropDelta;
        this.savedState = savedState;
        this.collisionNull = collisionNull;
        this.collidable = collidable;
        this.lightCode = lightCode;
        this.tickMask = tickMask;
        this.tickRate = tickRate;
        this.supportedState = supportedState;
        this.lossBefore = lossBefore;
        this.lossAfter = lossAfter;
    }
    static OracleFireProbe execute(dj world) {
        em player = new em(world) {
        };
        require(world.b(20, 79, 20, 87, 0), "fire placement support failed");
        fy stack = new fy(51, 1, 0);
        boolean placed = ej.c[51].a(stack, player, world, 20, 79, 20, 1);
        int placedState = state(world, 20, 80, 20);

        require(world.b(24, 79, 20, 87, 0) && world.b(24, 80, 20, 51, 0),
                "fire break cell failed");
        int entities = world.b.size();
        int breakBefore = state(world, 24, 80, 20);
        float strength = na.as.a(player);
        boolean removed = world.e(24, 80, 20, 0);
        if (removed && player.b(na.as))
            na.as.a(world, player, 24, 80, 20, 0);
        int breakAfter = state(world, 24, 80, 20);

        require(world.b(28, 79, 20, 87, 0) && world.b(28, 80, 20, 51, 0),
                "fire persistence cell failed");
        int mask = 1;
        for (int age = 1; age <= 15; age++) {
            na.as.a(world, 28, 80, 20, new MaxRandom());
            mask |= 1 << world.c(28, 80, 20);
        }
        iq tag = new iq();
        mg.a(world.c(1, 1), world, tag);
        hi loaded = mg.a(world, tag);
        int saved = loaded.a(12, 80, 4) * 100 + loaded.b(12, 80, 4);

        int collisionNull = na.as.e(world, 20, 80, 20) == null ? 1 : 0;
        int light = na.q[51] * 100 + na.s[51];
        na.as.b(world, 20, 80, 20, 1);
        na.as.b(world, 20, 80, 20, 69);
        int supported = state(world, 20, 80, 20);
        require(world.b(32, 79, 20, 1, 0) && world.b(32, 80, 20, 51, 0),
                "fire support-loss cell failed");
        int lossBefore = state(world, 32, 80, 20);
        world.e(32, 79, 20, 0);
        int lossAfter = state(world, 32, 80, 20);
        OracleFireProbe result = new OracleFireProbe(placed ? 1 : 0, placedState,
                stack.a, mask, Float.isInfinite(strength) ? 1 : 0, breakBefore, breakAfter,
                world.b.size() - entities, saved, collisionNull, na.as.k_() ? 1 : 0,
                light, na.n[51] ? 1 : 0, na.as.c(), supported, lossBefore, lossAfter);
        result.validate();
        return result;
    }
    String domains() {
        return "51=0..15,item-route=51x1->0,placed=51:0";
    }
    String lifecycle() {
        return "break=51:0->0:0,strength=infinite,drop=none";
    }
    String persistence() {
        return "chunk-nbt=51:15";
    }
    String physics() {
        return "collision=none,collidable=F,light=0:15";
    }
    String timing() {
        return "random-enrolled=T,age=0->15,tick-rate=40";
    }
    String neighbors() {
        return "supported=stable-51:0,support-loss=51:0->0:0";
    }
    private void validate() {
        require(placementRoute == 1 && placedState == 5100 && stackAfter == 0
                && metadataMask == 65535, "fire placement or age domain drifted");
        require(strengthClass == 1 && breakBefore == 5100 && breakAfter == 0
                && dropDelta == 0, "fire empty-harvest lifecycle drifted");
        require(savedState == 5115, "fire chunk round trip drifted");
        require(collisionNull == 1 && collidable == 0 && lightCode == 15,
                "fire physical envelope drifted");
        require(tickMask == 1 && tickRate == 40, "fire tick policy drifted");
        require(supportedState == 5100 && lossBefore == 5100 && lossAfter == 0,
                "fire neighbor support lifecycle drifted");
    }
    private static int state(dj world, int x, int y, int z) {
        return world.a(x, y, z) * 100 + world.c(x, y, z);
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
    private static final class MaxRandom extends Random {
        private static final long serialVersionUID = 1L;
        @Override public int nextInt(int bound) {
            return bound - 1;
        }
    }
}
