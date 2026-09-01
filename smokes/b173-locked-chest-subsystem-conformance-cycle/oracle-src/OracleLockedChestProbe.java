import java.util.Random;

/** Official-name locked-chest placement, lifecycle, persistence, and physics probe. */
final class OracleLockedChestProbe {
    final int placementRoute, placedState, stackAfter, metadataMask;
    final int strengthClass, breakBefore, breakAfter, dropDelta, dropId, dropCount, savedState;
    final int collision, lightCode, tickMask, tickBefore, tickAfter, neighborState;
    private OracleLockedChestProbe(int placementRoute, int placedState, int stackAfter,
            int metadataMask, int strengthClass, int breakBefore, int breakAfter, int dropDelta,
            int dropId, int dropCount, int savedState, int collision, int lightCode, int tickMask,
            int tickBefore, int tickAfter, int neighborState) {
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
        this.collision = collision;
        this.lightCode = lightCode;
        this.tickMask = tickMask;
        this.tickBefore = tickBefore;
        this.tickAfter = tickAfter;
        this.neighborState = neighborState;
    }
    static OracleLockedChestProbe execute(dj world) {
        em player = new em(world) {
        };
        require(world.b(20, 79, 20, 1, 0), "locked-chest placement support failed");
        fy stack = new fy(95, 1, 0);
        boolean placed = ej.c[95].a(stack, player, world, 20, 79, 20, 1);
        int placedState = state(world, 20, 80, 20);

        require(world.b(24, 80, 20, 95, 0), "locked-chest break cell failed");
        int entities = world.b.size();
        int breakBefore = state(world, 24, 80, 20);
        float strength = na.bk.a(player);
        boolean removed = world.e(24, 80, 20, 0);
        if (removed && player.b(na.bk))
            na.bk.a(world, player, 24, 80, 20, 0);
        int breakAfter = state(world, 24, 80, 20);
        ez drop = lastDrop(world, entities);

        require(world.b(28, 80, 20, 95, 0), "locked-chest persistence cell failed");
        iq tag = new iq();
        mg.a(world.c(1, 1), world, tag);
        hi loaded = mg.a(world, tag);
        int saved = loaded.a(12, 80, 4) * 100 + loaded.b(12, 80, 4);

        cz box = na.bk.e(world, 20, 80, 20);
        int collision = box != null && box.a == 20D && box.b == 80D && box.c == 20D
                && box.d == 21D && box.e == 81D && box.f == 21D ? 1 : 0;
        int light = na.q[95] * 100 + na.s[95];
        require(world.b(32, 80, 20, 95, 0), "locked-chest neighbor cell failed");
        na.bk.b(world, 32, 80, 20, 1);
        na.bk.b(world, 32, 80, 20, 69);
        int neighbor = state(world, 32, 80, 20);
        int tickBefore = state(world, 20, 80, 20);
        na.bk.a(world, 20, 80, 20, new Random(17320110795L));
        int tickAfter = state(world, 20, 80, 20);
        OracleLockedChestProbe result = new OracleLockedChestProbe(placed ? 1 : 0,
                placedState, stack.a, 1 << world.c(20, 80, 20),
                Float.isInfinite(strength) ? 1 : 0, breakBefore, breakAfter,
                world.b.size() - entities, drop == null ? 0 : drop.a.c,
                drop == null ? 0 : drop.a.a, saved, collision, light, na.n[95] ? 1 : 0,
                tickBefore, tickAfter, neighbor);
        result.validate();
        return result;
    }
    String domains() {
        return "95=0,item-route=95x1->0,placed=95:0";
    }
    String lifecycle() {
        return "break=95:0->0:0,strength=infinite,drop=95x1";
    }
    String persistence() {
        return "chunk-nbt=95:0";
    }
    String physics() {
        return "collision=full,light=255:15";
    }
    String timing() {
        return "random-enrolled=T,callback=95:0->0:0";
    }
    String neighbors() {
        return "stone+lever=stable-95:0";
    }
    private void validate() {
        require(placementRoute == 1 && placedState == 9500 && stackAfter == 0
                && metadataMask == 1, "locked-chest item placement or domain drifted");
        require(strengthClass == 1 && breakBefore == 9500 && breakAfter == 0
                && dropDelta == 1 && dropId == 95 && dropCount == 1,
                "locked-chest harvest lifecycle drifted");
        require(savedState == 9500, "locked-chest chunk round trip drifted");
        require(collision == 1 && lightCode == 25515,
                "locked-chest physical envelope drifted");
        require(tickMask == 1 && tickBefore == 9500 && tickAfter == 0,
                "locked-chest tick removal drifted");
        require(neighborState == 9500, "locked-chest neighbor stability drifted");
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
}
