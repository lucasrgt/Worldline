/** Official-name face, inversion, burnout, recovery, and support probe. */
final class OracleRedstoneTorchTiming {
    final int offMask, onMask, faceMask, delay, burnoutCount, burnoutHold;
    final int recoveryAge, recoveryDelay, randomMask, supportAfter, supportDrop;
    private OracleRedstoneTorchTiming(int offMask, int onMask, int faceMask, int delay,
            int burnoutCount, int burnoutHold, int recoveryAge, int recoveryDelay,
            int randomMask, int supportAfter, int supportDrop) {
        this.offMask = offMask;
        this.onMask = onMask;
        this.faceMask = faceMask;
        this.delay = delay;
        this.burnoutCount = burnoutCount;
        this.burnoutHold = burnoutHold;
        this.recoveryAge = recoveryAge;
        this.recoveryDelay = recoveryDelay;
        this.randomMask = randomMask;
        this.supportAfter = supportAfter;
        this.supportDrop = supportDrop;
    }
    static OracleRedstoneTorchTiming execute(dj world) {
        int off = 0, on = 0, faces = 0;
        for (int metadata = 1; metadata <= 5; metadata++) {
            int x = -24 + metadata * 8, y = 84, z = -20;
            int[] support = support(world, x, y, z, metadata);
            require(world.b(x, y, z, 76, metadata), "active torch placement failed " + metadata);
            power(world, support, x, y, z, true);
            na.aR.b(world, x, y, z, 76);
            advance(world, 2);
            int idleState = state(world, x, y, z);
            require(idleState == 7500 + metadata,
                    "idle torch face drifted " + metadata + ": " + idleState);
            off |= 1 << metadata;
            power(world, support, x, y, z, false);
            na.aQ.b(world, x, y, z, 0);
            advance(world, 2);
            int activeState = state(world, x, y, z);
            require(activeState == 7600 + metadata,
                    "active torch face drifted " + metadata + ": " + activeState);
            on |= 1 << metadata;
            faces |= 1 << metadata;
        }
        int x = 20, y = 90, z = 20;
        int[] support = support(world, x, y, z, 5);
        world.b(x, y, z, 76, 5);
        for (int cycle = 1; cycle <= 8; cycle++) {
            power(world, support, x, y, z, true);
            na.aR.b(world, x, y, z, 76);
            advance(world, 2);
            require(state(world, x, y, z) == 7505, "burnout off drifted " + cycle);
            power(world, support, x, y, z, false);
            na.aQ.b(world, x, y, z, 0);
            advance(world, 2);
            int expected = cycle < 8 ? 7605 : 7505;
            require(state(world, x, y, z) == expected, "burnout threshold drifted " + cycle);
        }
        long burnoutTime = world.m();
        advance(world, 101);
        int held = state(world, x, y, z);
        na.aQ.b(world, x, y, z, 0);
        advance(world, 2);
        int recovered = state(world, x, y, z);

        int supportX = 28, supportY = 90, supportZ = 20;
        world.b(supportX, supportY - 1, supportZ, 1, 0);
        world.b(supportX, supportY, supportZ, 75, 5);
        int before = world.b.size();
        world.e(supportX, supportY - 1, supportZ, 0);
        int supportAfter = state(world, supportX, supportY, supportZ);
        int supportDrop = stack(itemAfter(world, before));
        int random = (na.n[75] ? 1 : 0) | (na.n[76] ? 2 : 0);
        OracleRedstoneTorchTiming result = new OracleRedstoneTorchTiming(off, on, faces, 2,
                8, held, (int) (world.m() - burnoutTime - 2), 2, random,
                supportAfter, supportDrop);
        require(recovered == 7605, "redstone torch did not recover");
        result.validate();
        return result;
    }
    String domains() {
        return "75=1..5,76=1..5";
    }
    String materialization() {
        return "item76=76:1..5,signal=76>75:1..5";
    }
    String timing() {
        return "random=TT,delay=2,invert=76>75>76,burnout=8@100,recovery=101+2";
    }
    String neighbors() {
        return "faces=1..5,support=75:5->0:0+drop=76x1:0";
    }
    private void validate() {
        require(offMask == 62 && onMask == 62 && faceMask == 62, "torch face domain incomplete");
        require(delay == 2 && burnoutCount == 8 && burnoutHold == 7505,
                "torch inversion or burnout drifted");
        require(recoveryAge == 101 && recoveryDelay == 2 && randomMask == 3,
                "torch recovery or random policy drifted");
        require(supportAfter == 0 && supportDrop == 760100,
                "torch support invalidation drifted");
    }
    private static int[] support(dj world, int x, int y, int z, int metadata) {
        int sx = x + (metadata == 1 ? -1 : metadata == 2 ? 1 : 0);
        int sy = y + (metadata == 5 ? -1 : 0);
        int sz = z + (metadata == 3 ? -1 : metadata == 4 ? 1 : 0);
        world.b(sx, sy, sz, 1, 0);
        return new int[] {sx, sy, sz};
    }
    private static void power(dj world, int[] support, int tx, int ty, int tz,
            boolean enabled) {
        int[][] offsets = {{1, 0, 1}, {-1, 0, 2}, {0, 1, 3}, {0, -1, 4}};
        for (int[] offset : offsets) {
            int x = support[0] + offset[0], y = support[1], z = support[2] + offset[1];
            if (x == tx && y == ty && z == tz)
                continue;
            world.b(x, y, z, enabled ? 69 : 0, enabled ? offset[2] | 8 : 0);
            return;
        }
        throw new IllegalStateException("no redstone torch power source position");
    }
    private static void advance(dj world, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            world.h();
        }
    }
    private static int state(dj world, int x, int y, int z) {
        return world.a(x, y, z) * 100 + world.c(x, y, z);
    }
    private static ez itemAfter(dj world, int index) {
        for (int current = world.b.size() - 1; current >= index; current--) {
            lq entity = (lq) world.b.get(current);
            if (entity instanceof ez)
                return (ez) entity;
        }
        throw new IllegalStateException("expected torch support drop was absent");
    }
    private static int stack(ez item) {
        return item.a.c * 10000 + item.a.a * 100 + item.a.h();
    }
    static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
}
