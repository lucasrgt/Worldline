import java.util.Random;

/** Official-name farmland moisture, harvest, persistence, physics, and cover probe. */
final class OracleFarmlandProbe {
    final int placementRoute, placedState, stackAfter, metadataMask;
    final int strengthClass, breakBefore, breakAfter, dropDelta, dropId, dropCount, savedState;
    final int collisionFull, visualHeight, opaque, cube, lightCode, tickMask;
    final int hydratedState, dryState, stableState, coverBefore, coverAfter;
    private OracleFarmlandProbe(int placementRoute, int placedState, int stackAfter,
            int metadataMask, int strengthClass, int breakBefore, int breakAfter, int dropDelta,
            int dropId, int dropCount, int savedState, int collisionFull, int visualHeight,
            int opaque, int cube, int lightCode, int tickMask, int hydratedState, int dryState,
            int stableState, int coverBefore, int coverAfter) {
        this.placementRoute = placementRoute;
        this.placedState = placedState;
        this.stackAfter = stackAfter;
        this.metadataMask = metadataMask;
        this.strengthClass = strengthClass;
        this.breakBefore = breakBefore;
        this.breakAfter = breakAfter;
        this.dropDelta = dropDelta;
        this.dropId = dropId;
        this.dropCount = dropCount;
        this.savedState = savedState;
        this.collisionFull = collisionFull;
        this.visualHeight = visualHeight;
        this.opaque = opaque;
        this.cube = cube;
        this.lightCode = lightCode;
        this.tickMask = tickMask;
        this.hydratedState = hydratedState;
        this.dryState = dryState;
        this.stableState = stableState;
        this.coverBefore = coverBefore;
        this.coverAfter = coverAfter;
    }
    static OracleFarmlandProbe execute(dj world) {
        em player = new em(world) {
        };
        require(world.b(20, 79, 20, 1, 0), "farmland placement support failed");
        fy stack = new fy(60, 1, 0);
        boolean placed = ej.c[60].a(stack, player, world, 20, 79, 20, 1);
        int placedState = state(world, 20, 80, 20);

        require(world.b(24, 79, 20, 1, 0) && world.b(24, 80, 20, 60, 0),
                "farmland break cell failed");
        int entities = world.b.size();
        int breakBefore = state(world, 24, 80, 20);
        float strength = na.aB.a(player);
        boolean removed = world.e(24, 80, 20, 0);
        if (removed && player.b(na.aB))
            na.aB.a(world, player, 24, 80, 20, 0);
        int breakAfter = state(world, 24, 80, 20);
        ez drop = lastDrop(world, entities);

        require(world.b(28, 79, 20, 1, 0) && world.b(28, 80, 20, 60, 0)
                && world.b(29, 80, 20, 9, 0), "farmland hydration fixture failed");
        na.aB.a(world, 28, 80, 20, new ZeroRandom());
        int hydrated = state(world, 28, 80, 20);
        iq tag = new iq();
        mg.a(world.c(1, 1), world, tag);
        hi loaded = mg.a(world, tag);
        int saved = loaded.a(12, 80, 4) * 100 + loaded.b(12, 80, 4);
        world.e(29, 80, 20, 0);
        int mask = 1 << world.c(28, 80, 20);
        for (int step = 0; step < 7; step++) {
            na.aB.a(world, 28, 80, 20, new ZeroRandom());
            mask |= 1 << world.c(28, 80, 20);
        }
        int dry = state(world, 28, 80, 20);

        cz box = na.aB.e(world, 20, 80, 20);
        int collision = box != null && box.a == 20D && box.b == 80D && box.c == 20D
                && box.d == 21D && box.e == 81D && box.f == 21D ? 1 : 0;
        int height = (int) Math.round(na.aB.bw * 10000D);
        int light = na.q[60] * 100 + na.s[60];
        na.aB.b(world, 20, 80, 20, 1);
        na.aB.b(world, 20, 80, 20, 69);
        int stable = state(world, 20, 80, 20);
        require(world.b(32, 79, 20, 1, 0) && world.b(32, 80, 20, 60, 0),
                "farmland cover fixture failed");
        int coverBefore = state(world, 32, 80, 20);
        require(world.b(32, 81, 20, 1, 0), "farmland solid cover failed");
        int coverAfter = state(world, 32, 80, 20);
        OracleFarmlandProbe result = new OracleFarmlandProbe(placed ? 1 : 0,
                placedState, stack.a, mask,
                strength > 0F && !Float.isInfinite(strength) ? 1 : 0, breakBefore, breakAfter,
                world.b.size() - entities, drop == null ? 0 : drop.a.c,
                drop == null ? 0 : drop.a.a, saved, collision, height,
                na.aB.a() ? 1 : 0, na.aB.b() ? 1 : 0, light, na.n[60] ? 1 : 0,
                hydrated, dry, stable, coverBefore, coverAfter);
        result.validate();
        return result;
    }
    String domains() {
        return "60=0..7,item-route=60x1->0,placed=60:0";
    }
    String lifecycle() {
        return "break=60:0->0:0,strength=finite,drop=3x1";
    }
    String persistence() {
        return "chunk-nbt=60:7";
    }
    String physics() {
        return "collision=full,visual-height=15/16,opaque=F,cube=F,light=255:0";
    }
    String timing() {
        return "random-enrolled=T,hydration=0->7,dry=7->0";
    }
    String neighbors() {
        return "air-above=stable-60:0,solid-cover=60:0->3:0";
    }
    private void validate() {
        require(placementRoute == 1 && placedState == 6000 && stackAfter == 0
                && metadataMask == 255, "farmland placement or moisture domain drifted");
        require(strengthClass == 1 && breakBefore == 6000 && breakAfter == 0
                && dropDelta == 1 && dropId == 3 && dropCount == 1,
                "farmland dirt-drop lifecycle drifted");
        require(savedState == 6007, "farmland chunk round trip drifted");
        require(collisionFull == 1 && visualHeight == 9375 && opaque == 0 && cube == 0
                && lightCode == 25500, "farmland physical envelope drifted");
        require(tickMask == 1 && hydratedState == 6007 && dryState == 6000,
                "farmland moisture timing drifted");
        require(stableState == 6000 && coverBefore == 6000 && coverAfter == 300,
                "farmland solid-cover response drifted");
    }
    private static ez lastDrop(dj world, int first) {
        for (int index = world.b.size() - 1; index >= first; index--) {
            Object entity = world.b.get(index);
            if (entity instanceof ez)
                return (ez) entity;
        }
        return null;
    }
    private static int state(dj world, int x, int y, int z) {
        return world.a(x, y, z) * 100 + world.c(x, y, z);
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
    private static final class ZeroRandom extends Random {
        private static final long serialVersionUID = 1L;
        @Override public int nextInt(int bound) {
            return 0;
        }
    }
}
