/** Official-name counterpart of the native vegetation ecology matrix. */
final class OracleVegetationEcologyProbe {
    private static final int[] IDS = {2, 6, 18, 31, 59, 83};
    final int[][] rows;

    private OracleVegetationEcologyProbe(int[][] rows) {
        this.rows = rows;
    }

    static OracleVegetationEcologyProbe execute(dj world) {
        world.B = false;
        int[][] rows = new int[IDS.length][];
        for (int index = 0; index < IDS.length; index++) {
            int id = IDS[index], x = 8 + index * 4;
            prepareSupport(world, x, id);
            require(world.e(x, 65, 8, id), "vegetation placement failed: " + id);
            int domain = domain(world, x, id);
            world.c(x, 65, 8, 0);
            int shape = shape(na.m[id], world, x, id);
            int light = claimedLight(id) ? na.q[id] * 100 + na.s[id] : -1;
            int neighbor = neighbor(world, x, id);
            rows[index] = new int[] {id, domain, shape, light, neighbor,
                    world.c(x, 65, 8)};
        }
        OracleVegetationEcologyProbe result = new OracleVegetationEcologyProbe(rows);
        result.validate();
        return result;
    }

    String states() {
        return "2:0,6:0+1+2+8+9+10,18:0+1+2+4+5+6+8+9+10+12+13+14,"
                + "31:0+1+2,59:0-7,83:0-15";
    }

    String shapes() { return "2+18:full,6+59+83:passable"; }

    String light() { return "2:255/0,6+59+83:0/0"; }

    String neighbors() { return "2:stable,6+59:support-drop,18:decay-mark"; }

    private void validate() {
        int[][] expected = {
            {2, 1, 1, 25500, 1, 0},
            {6, 1799, 0, 0, 2, 0},
            {18, 30583, 1, -1, 3, 8},
            {31, 7, -1, -1, -1, 0},
            {59, 255, 0, 0, 2, 0},
            {83, 65535, 0, 0, -1, 0}
        };
        require(rows.length == expected.length, "vegetation row inventory drifted");
        for (int index = 0; index < rows.length; index++)
            require(matches(rows[index], expected[index]),
                    "vegetation row drifted: " + describe(rows[index]));
    }

    private static void prepareSupport(dj world, int x, int id) {
        int support = id == 59 ? 60 : (id == 6 || id == 31 || id == 83 ? 3 : 1);
        if (world.a(x, 64, 8) != support)
            require(world.e(x, 64, 8, support), "vegetation support failed: " + id);
        if (id == 83)
            require(world.e(x + 1, 64, 8, 9), "sugar cane water failed");
    }

    private static int domain(dj world, int x, int id) {
        int[] states;
        if (id == 6)
            states = new int[] {0, 1, 2, 8, 9, 10};
        else if (id == 18)
            states = new int[] {0, 1, 2, 4, 5, 6, 8, 9, 10, 12, 13, 14};
        else if (id == 31)
            states = range(3);
        else if (id == 59)
            states = range(8);
        else if (id == 83)
            states = range(16);
        else
            states = range(1);
        int mask = 0;
        for (int state : states) {
            world.c(x, 65, 8, state);
            require(world.c(x, 65, 8) == state,
                    "vegetation metadata rejected: " + id + ":" + state);
            mask |= 1 << state;
        }
        return mask;
    }

    private static int[] range(int count) {
        int[] values = new int[count];
        for (int index = 0; index < count; index++)
            values[index] = index;
        return values;
    }

    private static int shape(na block, dj world, int x, int id) {
        if (id == 31)
            return -1;
        cz box = block.e(world, x, 65, 8);
        if (id == 2 || id == 18) {
            require(box != null, "full vegetation collision box missing: " + id);
            return scaled(box.a - x) == 0 && scaled(box.b - 65D) == 0
                    && scaled(box.c - 8D) == 0 && scaled(box.d - x) == 160
                    && scaled(box.e - 65D) == 160
                    && scaled(box.f - 8D) == 160 ? 1 : -1;
        }
        return box == null ? 0 : -2;
    }

    private static boolean claimedLight(int id) {
        return id == 2 || id == 6 || id == 59 || id == 83;
    }

    private static int neighbor(dj world, int x, int id) {
        if (id == 31 || id == 83)
            return -1;
        na block = na.m[id];
        if (id == 2) {
            block.b(world, x, 65, 8, 1);
            return world.a(x, 65, 8) == 2 && world.c(x, 65, 8) == 0 ? 1 : 0;
        }
        if (id == 18) {
            require(world.e(x + 1, 65, 8, 17), "leaf support log placement failed");
            require(world.e(x + 1, 65, 8, 0), "leaf support log removal failed");
            return world.a(x, 65, 8) == 18 && world.c(x, 65, 8) == 8 ? 3 : 0;
        }
        require(world.e(x, 64, 8, 0), "vegetation support removal failed: " + id);
        if (world.a(x, 65, 8) != 0)
            block.b(world, x, 65, 8, 0);
        return world.a(x, 65, 8) == 0 ? 2 : 0;
    }

    private static int scaled(double value) { return (int) Math.round(value * 160D); }

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
