import worldline.testapi.TestObservationWindow;

/** Official-name metadata-domain and transition-timing probe. */
final class OracleRepeaterTiming {
    final int offMask, onMask, powerTicks, releaseTicks, stableTicks, directionMask;
    final int randomMask, supportAfter, supportDrop;

    private OracleRepeaterTiming(int offMask, int onMask, int powerTicks, int releaseTicks,
            int stableTicks, int directionMask, int randomMask, int supportAfter,
            int supportDrop) {
        this.offMask = offMask;
        this.onMask = onMask;
        this.powerTicks = powerTicks;
        this.releaseTicks = releaseTicks;
        this.stableTicks = stableTicks;
        this.directionMask = directionMask;
        this.randomMask = randomMask;
        this.supportAfter = supportAfter;
        this.supportDrop = supportDrop;
    }

    static OracleRepeaterTiming execute(dj world) {
        int off = 0;
        for (int metadata = 0; metadata < 16; metadata++) {
            int x = x(metadata), z = z(metadata), y = 82;
            supportAndInput(world, x, y, z, metadata, true);
            require(world.b(x, y, z, 93, metadata), "off repeater placement failed " + metadata);
            na.bi.b(world, x, y, z, 76);
            require(state(world, x, y, z) == 9300 + metadata,
                    "off domain drifted " + metadata);
            off |= 1 << metadata;
        }
        int powerTicks = transition(world, 94);
        int on = mask(world, 94);
        for (int metadata = 0; metadata < 16; metadata++) {
            int x = x(metadata), z = z(metadata), y = 82;
            input(world, x, y, z, metadata, false);
            na.bj.b(world, x, y, z, 0);
        }
        int releaseTicks = transition(world, 93);

        int stableX = x(15), stableZ = z(15), y = 82;
        input(world, stableX, y, stableZ, 15, true);
        na.bi.b(world, stableX, y, stableZ, 76);
        advanceCell(world, stableX, y, stableZ, 94, 8);
        int offX = 24, offZ = 24;
        world.b(offX, y - 1, offZ, 1, 0);
        world.b(offX, y, offZ, 93, 15);
        TestObservationWindow window = new TestObservationWindow();
        window.observe(() -> {
            for (int tick = 0; tick < 20; tick++) world.h();
            return null;
        }, 20);
        require(state(world, stableX, y, stableZ) == 9415
                && state(world, offX, y, offZ) == 9315, "stable repeater window drifted");

        int before = world.b.size();
        world.e(stableX, y - 1, stableZ, 0);
        int supportAfter = state(world, stableX, y, stableZ);
        int supportDrop = stack(itemAfter(world, before));
        int random = (na.n[93] ? 1 : 0) | (na.n[94] ? 2 : 0);
        OracleRepeaterTiming result = new OracleRepeaterTiming(off, on, powerTicks,
                releaseTicks, (int) window.observedTicks(), directionMask(world), random,
                supportAfter, supportDrop);
        result.validate();
        return result;
    }

    String domains() { return "93=0..15,94=0..15"; }
    String materialization() { return "item356=93:0..15,signal=93>94:0..15"; }
    String timing() {
        return "random=FF,delays=2+4+6+8,power=93>94,release=94>93,"
                + "stable=93:15+94:15@20-window";
    }
    String neighbors() {
        return "signal=all-directions,support=94:15->0:0+drop=356x1:0";
    }

    private void validate() {
        require(offMask == 0xffff && onMask == 0xffff, "repeater metadata domain incomplete");
        require(powerTicks == 2468 && releaseTicks == 2468 && directionMask == 15,
                "repeater transition timing drifted");
        require(stableTicks == 20 && randomMask == 0,
                "repeater stability or random policy drifted");
        require(supportAfter == 0 && supportDrop == 3560100,
                "repeater support invalidation drifted");
    }

    private static int transition(dj world, int targetId) {
        int code = 0;
        for (int tick = 1; tick <= 8; tick++) {
            world.h();
            for (int metadata = 0; metadata < 16; metadata++) {
                int expected = tick >= delay(metadata) ? targetId : 187 - targetId;
                require(world.a(x(metadata), 82, z(metadata)) == expected,
                        "repeater transition drifted at " + metadata + " tick " + tick);
            }
            if ((tick & 1) == 0) code = code * 10 + tick;
        }
        return code;
    }

    private static void advanceCell(dj world, int x, int y, int z, int id, int ticks) {
        for (int tick = 0; tick < ticks; tick++) world.h();
        require(world.a(x, y, z) == id, "stable repeater activation drifted");
    }

    private static int mask(dj world, int id) {
        int value = 0;
        for (int metadata = 0; metadata < 16; metadata++)
            if (state(world, x(metadata), 82, z(metadata)) == id * 100 + metadata)
                value |= 1 << metadata;
        return value;
    }
    private static int directionMask(dj world) {
        int value = 0;
        for (int direction = 0; direction < 4; direction++)
            if (world.a(x(direction), 82, z(direction)) == 93) value |= 1 << direction;
        return value;
    }
    private static void supportAndInput(dj world, int x, int y, int z,
            int metadata, boolean powered) {
        world.b(x, y - 1, z, 1, 0);
        input(world, x, y, z, metadata, powered);
    }
    private static void input(dj world, int x, int y, int z, int metadata, boolean powered) {
        int direction = metadata & 3;
        int inputX = x + (direction == 1 ? -1 : direction == 3 ? 1 : 0);
        int inputZ = z + (direction == 0 ? 1 : direction == 2 ? -1 : 0);
        world.b(inputX, y - 1, inputZ, 1, 0);
        world.b(inputX, y, inputZ, powered ? 76 : 0, powered ? 5 : 0);
    }
    private static int delay(int metadata) { return (((metadata & 12) >> 2) + 1) * 2; }
    private static int x(int metadata) { return -24 + (metadata & 3) * 12; }
    private static int z(int metadata) { return -24 + (metadata >> 2) * 12; }
    private static int state(dj world, int x, int y, int z) {
        return world.a(x, y, z) * 100 + world.c(x, y, z);
    }
    private static ez itemAfter(dj world, int index) {
        for (int current = world.b.size() - 1; current >= index; current--) {
            lq entity = (lq) world.b.get(current);
            if (entity instanceof ez) return (ez) entity;
        }
        throw new IllegalStateException("expected support-loss drop was absent");
    }
    private static int stack(ez item) { return item.a.c * 10000 + item.a.a * 100 + item.a.h(); }
    static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
}
