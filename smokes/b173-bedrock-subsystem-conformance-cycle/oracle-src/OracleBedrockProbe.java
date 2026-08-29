import java.util.Random;

/** Official-name bedrock placement, lifecycle, persistence, and physics probe. */
final class OracleBedrockProbe {
    final int placementRoute, placedState, stackAfter, metadataMask;
    final int strengthMilli, breakBefore, breakAfter, dropDelta, savedState;
    final int collision, lightCode, tickMask, tickBefore, tickAfter, neighborState;
    private OracleBedrockProbe(int placementRoute, int placedState, int stackAfter,
            int metadataMask, int strengthMilli, int breakBefore, int breakAfter, int dropDelta,
            int savedState, int collision, int lightCode, int tickMask, int tickBefore,
            int tickAfter, int neighborState) {
        this.placementRoute = placementRoute;
        this.placedState = placedState;
        this.stackAfter = stackAfter;
        this.metadataMask = metadataMask;
        this.strengthMilli = strengthMilli;
        this.breakBefore = breakBefore;
        this.breakAfter = breakAfter;
        this.dropDelta = dropDelta;
        this.savedState = savedState;
        this.collision = collision;
        this.lightCode = lightCode;
        this.tickMask = tickMask;
        this.tickBefore = tickBefore;
        this.tickAfter = tickAfter;
        this.neighborState = neighborState;
    }
    static OracleBedrockProbe execute(dj world) {
        em player = new em(world) { };
        require(world.b(20, 79, 20, 1, 0), "bedrock placement support failed");
        fy stack = new fy(7, 1, 0);
        boolean placed = ej.c[7].a(stack, player, world, 20, 79, 20, 1);
        int placedState = state(world, 20, 80, 20);

        require(world.b(24, 80, 20, 7, 0), "bedrock break cell failed");
        int entities = world.b.size();
        int breakBefore = state(world, 24, 80, 20);
        float strength = na.A.a(player);
        if (strength >= 1.0F) world.e(24, 80, 20, 0);
        int breakAfter = state(world, 24, 80, 20);

        require(world.b(28, 80, 20, 7, 0), "bedrock persistence cell failed");
        iq tag = new iq();
        mg.a(world.c(1, 1), world, tag);
        hi loaded = mg.a(world, tag);
        int saved = loaded.a(12, 80, 4) * 100 + loaded.b(12, 80, 4);

        cz box = na.A.e(world, 20, 80, 20);
        int collision = box != null && box.a == 20D && box.b == 80D && box.c == 20D
                && box.d == 21D && box.e == 81D && box.f == 21D ? 1 : 0;
        int light = na.q[7] * 100 + na.s[7];
        int tickBefore = state(world, 20, 80, 20);
        na.A.a(world, 20, 80, 20, new Random(17320110707L));
        int tickAfter = state(world, 20, 80, 20);
        na.A.b(world, 24, 80, 20, 1);
        na.A.b(world, 24, 80, 20, 69);
        OracleBedrockProbe result = new OracleBedrockProbe(placed ? 1 : 0, placedState,
                stack.a, 1 << world.c(20, 80, 20), Math.round(strength * 1000F),
                breakBefore, breakAfter, world.b.size() - entities, saved, collision, light,
                na.n[7] ? 1 : 0, tickBefore, tickAfter, state(world, 24, 80, 20));
        result.validate();
        return result;
    }
    String domains() {
        return "7=0,item-route=7x1->0,placed=7:0";
    }
    String lifecycle() {
        return "break-attempt=7:0->7:0,strength=0,drop=none";
    }
    String persistence() {
        return "chunk-nbt=7:0";
    }
    String physics() {
        return "collision=full,light=255:0";
    }
    String timing() {
        return "scheduled=F,callback-stable=7:0";
    }
    String neighbors() {
        return "stone+lever=stable-7:0";
    }
    private void validate() {
        require(placementRoute == 1 && placedState == 700 && stackAfter == 0
                && metadataMask == 1, "bedrock item placement or domain drifted");
        require(strengthMilli == 0 && breakBefore == 700 && breakAfter == 700
                && dropDelta == 0, "bedrock unbreakable lifecycle drifted");
        require(savedState == 700, "bedrock chunk round trip drifted");
        require(collision == 1 && lightCode == 25500, "bedrock physical envelope drifted");
        require(tickMask == 0 && tickBefore == 700 && tickAfter == 700,
                "bedrock tick policy drifted");
        require(neighborState == 700, "bedrock neighbor stability drifted");
    }
    private static int state(dj world, int x, int y, int z) {
        return world.a(x, y, z) * 100 + world.c(x, y, z);
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
}
