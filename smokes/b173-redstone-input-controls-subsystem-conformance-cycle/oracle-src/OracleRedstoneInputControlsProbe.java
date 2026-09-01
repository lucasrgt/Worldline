import java.util.Random;

/** Proves the native state, geometry, light, timing, and support rules of redstone inputs. */
final class OracleRedstoneInputControlsProbe {
    final int[] lever, button, stonePlate, woodenPlate;
    final int supportMask;

    private OracleRedstoneInputControlsProbe(int[] lever, int[] button, int[] stonePlate,
            int[] woodenPlate, int supportMask) {
        this.lever = lever;
        this.button = button;
        this.stonePlate = stonePlate;
        this.woodenPlate = woodenPlate;
        this.supportMask = supportMask;
    }

    static OracleRedstoneInputControlsProbe execute(dj world) {
        world.B = false;
        em player = new em(world) { };
        Random random = new Random(17320110690L);

        require(world.b(8, 65, 8, 69, 5),
                "lever state fixture failed");
        int leverStart = world.c(8, 65, 8);
        require(na.aK.a(world, 8, 65, 8, player),
                "lever activation failed");
        int leverOn = world.c(8, 65, 8);
        na.aK.a(world, 8, 65, 8, random);
        int leverLatched = world.c(8, 65, 8);
        int[] leverBounds = bounds(na.aK, world, 8, 65, 8);
        int[] lever = row(leverStart, leverOn, leverLatched, collision(na.aK, world, 8, 65, 8),
                light(69), na.n[69] ? 1 : 0, 0, leverBounds);

        require(world.e(11, 65, 8, 1), "button support failed");
        require(world.b(12, 65, 8, 77, 1),
                "button state fixture failed");
        int buttonStart = world.c(12, 65, 8);
        int[] buttonUp = bounds(na.aS, world, 12, 65, 8);
        require(na.aS.a(world, 12, 65, 8, player),
                "button activation failed");
        int buttonOn = world.c(12, 65, 8);
        int[] buttonDown = bounds(na.aS, world, 12, 65, 8);
        na.aS.a(world, 12, 65, 8, random);
        int buttonOff = world.c(12, 65, 8);
        int[] button = row(buttonStart, buttonOn, buttonOff,
                collision(na.aS, world, 12, 65, 8), light(77),
                na.n[77] ? 1 : 0, na.aS.c(),
                join(buttonUp, buttonDown));

        require(world.b(16, 65, 8, 70, 0),
                "stone plate state fixture failed");
        require(na.aL.bn == 70 && na.aL instanceof bc,
                "stone plate registry precondition failed");
        require(sensitivity((bc) na.aL) == 2,
                "stone plate sensitivity precondition failed: " + sensitivity((bc) na.aL));
        int stoneStart = world.c(16, 65, 8);
        int[] stoneUp = bounds(na.aL, world, 16, 65, 8);
        ez stoneItem = item(world, 16, 65, 8);
        evaluate((bc) na.aL, world, 16, 65, 8);
        int stoneItemState = world.c(16, 65, 8);
        remove(world, stoneItem);
        player.c(16.5D, 66.62D, 8.5D);
        require(world.b(player), "stone plate player join failed");
        cz stoneTrigger = cz.b(16.125D, 65D, 8.125D, 16.875D, 65.25D, 8.875D);
        require(world.a(hl.class, stoneTrigger).size() == 1,
                "stone plate player precondition failed");
        require(!world.B, "stone plate server-side precondition failed");
        evaluate((bc) na.aL, world, 16, 65, 8);
        int stoneOn = world.c(16, 65, 8);
        int[] stoneDown = bounds(na.aL, world, 16, 65, 8);
        remove(world, player);
        na.aL.a(world, 16, 65, 8, random);
        int stoneOff = world.c(16, 65, 8);
        int[] stone = row(stoneStart, stoneOn, stoneOff,
                collision(na.aL, world, 16, 65, 8), light(70),
                stoneItemState, na.aL.c(), join(stoneUp, stoneDown));

        require(world.b(20, 65, 8, 72, 0),
                "wooden plate state fixture failed");
        int woodStart = world.c(20, 65, 8);
        int[] woodUp = bounds(na.aN, world, 20, 65, 8);
        ez woodItem = item(world, 20, 65, 8);
        evaluate((bc) na.aN, world, 20, 65, 8);
        int woodOn = world.c(20, 65, 8);
        int[] woodDown = bounds(na.aN, world, 20, 65, 8);
        remove(world, woodItem);
        na.aN.a(world, 20, 65, 8, random);
        int woodOff = world.c(20, 65, 8);
        int[] wood = row(woodStart, woodOn, woodOff,
                collision(na.aN, world, 20, 65, 8), light(72),
                1, na.aN.c(), join(woodUp, woodDown));

        int support = supportMask(world);
        OracleRedstoneInputControlsProbe result =
                new OracleRedstoneInputControlsProbe(lever, button, stone, wood, support);
        result.validate();
        return result;
    }

    String lever() {
        return "state=5>13>5,bounds=40.0.40.120.96.120,collision=none,light=0/0,tick=latch";
    }

    String button() {
        return "state=1>9>1,bounds=0.60.50.20.100.110>0.60.50.10.100.110,"
                + "collision=none,light=0/0,tick=20";
    }

    String stonePlate() {
        return "state=0>1>0,item=ignored,bounds=10.0.10.150.10.150>10.0.10.150.5.150,"
                + "collision=none,light=0/0,tick=20";
    }

    String woodenPlate() {
        return "state=0>1>0,item=accepted,bounds=10.0.10.150.10.150>10.0.10.150.5.150,"
                + "collision=none,light=0/0,tick=20";
    }

    String support() { return "69+70+72+77=air+single-drop"; }

    private void validate() {
        require(matches(lever, new int[] {5, 13, 13, 0, 0, 0, 0,
                40, 0, 40, 120, 96, 120}),
                "lever contract drifted: " + describe(lever));
        require(matches(button, new int[] {1, 9, 1, 0, 0, 1, 20,
                0, 60, 50, 20, 100, 110, 0, 60, 50, 10, 100, 110}),
                "button contract drifted");
        require(matches(stonePlate, new int[] {0, 1, 0, 0, 0, 0, 20,
                10, 0, 10, 150, 10, 150, 10, 0, 10, 150, 5, 150}),
                "stone pressure-plate contract drifted: " + describe(stonePlate));
        require(matches(woodenPlate, new int[] {0, 1, 0, 0, 0, 1, 20,
                10, 0, 10, 150, 10, 150, 10, 0, 10, 150, 5, 150}),
                "wooden pressure-plate contract drifted: " + describe(woodenPlate));
        require(supportMask == 15, "support-loss matrix drifted");
    }

    private static int supportMask(dj world) {
        int mask = 0;
        mask |= supportLoss(world, 28, 69, 5, true) ? 1 : 0;
        mask |= supportLoss(world, 32, 77, 1, false) ? 2 : 0;
        mask |= supportLoss(world, 36, 70, 0, true) ? 4 : 0;
        mask |= supportLoss(world, 40, 72, 0, true) ? 8 : 0;
        return mask;
    }

    private static boolean supportLoss(dj world, int x, int id, int metadata, boolean floor) {
        int before = world.b.size();
        if (!floor)
            require(world.e(x - 1, 65, 8, 1), "wall support failed");
        require(world.b(x, 65, 8, id, metadata),
                "supported control placement failed");
        if (floor)
            require(world.e(x, 64, 8, 0), "floor support removal failed");
        else
            require(world.e(x - 1, 65, 8, 0), "wall support removal failed");
        if (world.a(x, 65, 8) != 0)
            na.m[id].b(world, x, 65, 8, 0);
        return world.a(x, 65, 8) == 0
                && world.b.size() - before == 1;
    }

    private static ez item(dj world, int x, int y, int z) {
        ez item = new ez(world, x + 0.5D, y + 0.1D, z + 0.5D,
                new fy(1, 1, 0));
        require(world.b(item), "pressure-plate item join failed");
        return item;
    }

    private static void remove(dj world, lq entity) {
        entity.J();
        world.e();
        world.b.remove(entity);
        world.d.remove(entity);
    }

    private static int collision(na block, dj world, int x, int y, int z) {
        cz box = block.e(world, x, y, z);
        return box == null ? 0 : 1;
    }

    private static int light(int id) {
        return na.q[id] * 100 + na.s[id];
    }

    private static int[] bounds(na block, dj world, int x, int y, int z) {
        block.a((pb) world, x, y, z);
        return new int[] {scale(block.bs), scale(block.bt), scale(block.bu),
                scale(block.bv), scale(block.bw), scale(block.bx)};
    }

    private static int scale(double value) { return (int) Math.round(value * 160D); }

    private static int sensitivity(bc plate) {
        try {
            java.lang.reflect.Field field = bc.class.getDeclaredField("a");
            field.setAccessible(true);
            Object value = field.get(plate);
            if (value == kx.a)
                return 1;
            if (value == kx.b)
                return 2;
            if (value == kx.c)
                return 3;
            return 0;
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("pressure-plate sensitivity unavailable", failure);
        }
    }

    private static void evaluate(bc plate, dj world, int x, int y, int z) {
        try {
            java.lang.reflect.Method method = bc.class.getDeclaredMethod(
                    "g", dj.class, int.class, int.class, int.class);
            method.setAccessible(true);
            method.invoke(plate, world, x, y, z);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("pressure-plate evaluator unavailable", failure);
        }
    }

    private static int[] row(int start, int active, int released, int collision, int light,
            int policy, int rate, int[] bounds) {
        int[] result = new int[7 + bounds.length];
        int[] prefix = {start, active, released, collision, light, policy, rate};
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(bounds, 0, result, prefix.length, bounds.length);
        return result;
    }

    private static int[] join(int[] first, int[] second) {
        int[] result = new int[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private static boolean matches(int[] actual, int[] expected) {
        if (actual.length != expected.length)
            return false;
        for (int index = 0; index < actual.length; index++)
            if (actual[index] != expected[index])
                return false;
        return true;
    }

    private static String describe(int[] values) {
        StringBuilder result = new StringBuilder();
        for (int value : values) {
            if (result.length() > 0)
                result.append('.');
            result.append(value);
        }
        return result.toString();
    }

    private static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
}
