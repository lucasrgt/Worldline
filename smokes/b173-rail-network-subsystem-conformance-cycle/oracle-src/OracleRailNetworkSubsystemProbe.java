import java.lang.reflect.Method;
import java.util.Random;

/** Official-name counterpart of the complete native rail-network probe. */
final class OracleRailNetworkSubsystemProbe {
    final int[] normalRail, poweredRail, detectorRail;
    final int supportMask;

    private OracleRailNetworkSubsystemProbe(int[] normalRail, int[] poweredRail,
            int[] detectorRail, int supportMask) {
        this.normalRail = normalRail;
        this.poweredRail = poweredRail;
        this.detectorRail = detectorRail;
        this.supportMask = supportMask;
    }

    static OracleRailNetworkSubsystemProbe execute(dj world) {
        world.B = false;
        Random random = new Random(17320110660L);

        require(world.e(8, 65, 8, 66), "normal rail placement failed");
        int normalDomain = domain(world, 8, 65, 8, new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        world.c(8, 65, 8, 0);
        int normalFlat = height(na.aH, world, 8, 65, 8);
        world.c(8, 65, 8, 2);
        int normalSlope = height(na.aH, world, 8, 65, 8);
        world.c(8, 65, 8, 9);
        na.aH.a(world, 8, 65, 8, random);
        int[] normal = row(normalDomain, normalFlat, normalSlope,
                collision(na.aH, world, 8, 65, 8), light(66),
                na.n[66] ? 1 : 0, world.c(8, 65, 8), 0);

        require(world.e(16, 65, 8, 27), "powered rail placement failed");
        int poweredDomain = domain(world, 16, 65, 8,
                new int[] {0, 1, 2, 3, 4, 5, 8, 9, 10, 11, 12, 13});
        world.c(16, 65, 8, 2);
        int poweredSlope = height(na.U, world, 16, 65, 8);
        world.c(16, 65, 8, 10);
        int poweredBitSlope = height(na.U, world, 16, 65, 8);
        world.c(16, 65, 8, 8);
        na.U.a(world, 16, 65, 8, random);
        int[] powered = row(poweredDomain, poweredSlope, poweredBitSlope,
                collision(na.U, world, 16, 65, 8), light(27),
                na.n[27] ? 1 : 0, world.c(16, 65, 8), 0);

        require(world.e(24, 65, 8, 28), "detector rail placement failed");
        world.c(24, 65, 8, 2);
        int detectorSlope = height(na.V, world, 24, 65, 8);
        world.c(24, 65, 8, 10);
        int detectorBitSlope = height(na.V, world, 24, 65, 8);
        world.c(24, 65, 8, 0);
        int detectorStart = world.c(24, 65, 8);
        pr cart = new pr(world, 24.5D, 65D, 8.5D, 0);
        require(world.b(cart), "detector minecart join failed");
        cz trigger = cz.b(24.125D, 65D, 8.125D, 24.875D, 65.25D, 8.875D);
        require(world.a(pr.class, trigger).size() == 1,
                "detector minecart precondition failed");
        evaluate((jn) na.V, world, 24, 65, 8, detectorStart);
        int detectorOn = world.c(24, 65, 8);
        remove(world, cart);
        na.V.a(world, 24, 65, 8, random);
        int detectorOff = world.c(24, 65, 8);
        int[] detector = row(detectorStart, detectorOn, detectorOff,
                collision(na.V, world, 24, 65, 8), light(28),
                na.n[28] ? 1 : 0, na.V.c(), detectorSlope * 1000 + detectorBitSlope);

        OracleRailNetworkSubsystemProbe result = new OracleRailNetworkSubsystemProbe(
                normal, powered, detector, supportMask(world));
        result.validate();
        return result;
    }

    String normalRail() {
        return "states=0-9,bounds=0:20+2:100,collision=none,light=0/0,tick=stable";
    }

    String poweredRail() {
        return "states=0-5+8-13,bounds=2:100+10:20,collision=none,light=0/0,tick=stable";
    }

    String detectorRail() {
        return "states=0>8>0,bounds=2:100+10:20,collision=none,light=0/0,tick=20";
    }

    String support() { return "27+28+66=air+single-drop"; }

    private void validate() {
        require(matches(normalRail, new int[] {1023, 20, 100, 0, 0, 0, 9, 0}),
                "normal rail contract drifted: " + describe(normalRail));
        require(matches(poweredRail, new int[] {16191, 100, 20, 0, 0, 0, 8, 0}),
                "powered rail contract drifted: " + describe(poweredRail));
        require(matches(detectorRail, new int[] {0, 8, 0, 0, 0, 1, 20, 100020}),
                "detector rail contract drifted: " + describe(detectorRail));
        require(supportMask == 7, "rail support-loss matrix drifted: " + supportMask);
    }

    private static int domain(dj world, int x, int y, int z, int[] states) {
        int mask = 0;
        for (int state : states) {
            world.c(x, y, z, state);
            require(world.c(x, y, z) == state, "rail metadata domain rejected " + state);
            mask |= 1 << state;
        }
        return mask;
    }

    private static int supportMask(dj world) {
        int mask = 0;
        mask |= supportLoss(world, 32, 27) ? 1 : 0;
        mask |= supportLoss(world, 40, 28) ? 2 : 0;
        mask |= supportLoss(world, 48, 66) ? 4 : 0;
        return mask;
    }

    private static boolean supportLoss(dj world, int x, int id) {
        int before = world.b.size();
        require(world.e(x, 65, 8, id), "supported rail placement failed");
        require(world.e(x, 64, 8, 0), "rail support removal failed");
        if (world.a(x, 65, 8) != 0)
            na.m[id].b(world, x, 65, 8, 0);
        return world.a(x, 65, 8) == 0 && world.b.size() - before == 1;
    }

    private static void remove(dj world, pr cart) {
        cart.J();
        world.e();
        world.b.remove(cart);
    }

    private static int height(na block, dj world, int x, int y, int z) {
        block.a((pb) world, x, y, z);
        return (int) Math.round(block.bw * 160D);
    }

    private static int collision(na block, dj world, int x, int y, int z) {
        return block.e(world, x, y, z) == null ? 0 : 1;
    }

    private static int light(int id) { return na.q[id] * 100 + na.s[id]; }

    private static void evaluate(jn rail, dj world, int x, int y, int z, int metadata) {
        try {
            Method method = jn.class.getDeclaredMethod("f", dj.class,
                    int.class, int.class, int.class, int.class);
            method.setAccessible(true);
            method.invoke(rail, world, x, y, z, metadata);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("detector evaluator unavailable", failure);
        }
    }

    private static int[] row(int a, int b, int c, int d, int e, int f, int g, int h) {
        return new int[] {a, b, c, d, e, f, g, h};
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
