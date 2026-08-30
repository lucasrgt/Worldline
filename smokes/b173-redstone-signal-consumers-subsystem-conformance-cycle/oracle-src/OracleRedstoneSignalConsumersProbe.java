import java.lang.reflect.Field;
import java.util.Random;
import java.util.TreeSet;

/** Official-name counterpart of the bounded redstone signal-consumer matrix. */
final class OracleRedstoneSignalConsumersProbe {
    private static final int[] IDS = {23, 25, 46, 50, 55, 84};
    final int[][] rows;

    private OracleRedstoneSignalConsumersProbe(int[][] rows) {
        this.rows = rows;
    }

    static OracleRedstoneSignalConsumersProbe execute(dj world) {
        world.B = false;
        int[][] rows = new int[IDS.length][];
        for (int index = 0; index < IDS.length; index++) {
            int id = IDS[index], x = 8 + index * 5;
            ensureBlock(world, x, 64, 8, 1, "support failed: " + id);
            require(world.b(x, 65, 8, id, 0), "consumer placement failed: " + id);
            int state = stateDomain(world, x, id);
            if (state >= 0)
                rawMetadata(world, x, 65, 8, 0);
            int shape = shape(na.m[id], world, x, id);
            int light = claimedPhysical(id) ? na.q[id] * 100 + na.s[id] : -1;
            int tickRate = na.m[id].c();
            int tickStable = directTickStable(world, x, id);
            int neighbor = neighbor(world, x, id);
            rows[index] = new int[] {id, state, shape, light, tickRate, tickStable, neighbor};
        }
        OracleRedstoneSignalConsumersProbe result =
                new OracleRedstoneSignalConsumersProbe(rows);
        result.validate();
        return result;
    }

    String states() {
        return "46:0+1,55:0-15,84:0+1";
    }

    String shapes() {
        return "46:full,55:passable";
    }

    String light() {
        return "46:255/0,55:0/0";
    }

    String ticks() {
        return "23:rate4+unpowered-stable,25+46+50+55+84:rate10+noop";
    }
    String neighbors() {
        return "23:powered-schedule,25:rising-edge,46:powered-prime,55:support-loss,84:noop";
    }

    private void validate() {
        int[][] expected = {
            {23, -1, -1, -1, 4, 1, 1},
            {25, -1, -1, -1, 10, 1, 1},
            {46, 3, 1, 25500, 10, 1, 1},
            {50, -1, -1, -1, 10, 1, -1},
            {55, 65535, 0, 0, 10, 1, 1},
            {84, 3, -1, -1, 10, 1, 1}
        };
        require(rows.length == expected.length, "consumer row inventory drifted");
        for (int index = 0; index < rows.length; index++)
            require(matches(rows[index], expected[index]),
                    "consumer row drifted: " + describe(rows[index]));
    }

    private static int stateDomain(dj world, int x, int id) {
        if (id != 46 && id != 55 && id != 84)
            return -1;
        int count = id == 55 ? 16 : 2, mask = 0;
        for (int state = 0; state < count; state++) {
            rawMetadata(world, x, 65, 8, state);
            require(world.c(x, 65, 8) == state,
                    "consumer metadata rejected: " + id + ":" + state);
            mask |= 1 << state;
        }
        return mask;
    }

    private static int shape(na block, dj world, int x, int id) {
        if (!claimedPhysical(id))
            return -1;
        cz box = block.e(world, x, 65, 8);
        if (id == 55)
            return box == null ? 0 : -2;
        require(box != null, "TNT collision box missing");
        return scaled(box.a - x) == 0 && scaled(box.b - 65D) == 0
                && scaled(box.c - 8D) == 0 && scaled(box.d - x) == 160
                && scaled(box.e - 65D) == 160 && scaled(box.f - 8D) == 160
                ? 1 : -1;
    }

    private static boolean claimedPhysical(int id) {
        return id == 46 || id == 55;
    }

    private static int directTickStable(dj world, int x, int id) {
        int beforeId = world.a(x, 65, 8);
        int beforeMeta = world.c(x, 65, 8);
        int beforeEntities = world.b.size();
        int beforeScheduled = scheduledCount(world);
        na.m[id].a(world, x, 65, 8, new Random(17320110855L + id));
        return world.a(x, 65, 8) == beforeId && world.c(x, 65, 8) == beforeMeta
                && world.b.size() == beforeEntities
                && scheduledCount(world) == beforeScheduled ? 1 : 0;
    }

    private static int neighbor(dj world, int x, int id) {
        if (id == 50)
            return -1;
        if (id == 55) {
            int before = world.b.size();
            require(world.e(x, 64, 8, 0), "wire support removal failed");
            if (world.a(x, 65, 8) != 0)
                na.m[id].b(world, x, 65, 8, 0);
            return world.a(x, 65, 8) == 0 && world.b.size() == before + 1 ? 1 : 0;
        }
        if (id == 84) {
            int beforeId = world.a(x, 65, 8), beforeMeta = world.c(x, 65, 8);
            int beforeEntities = world.b.size();
            na.m[id].b(world, x, 65, 8, 1);
            return world.a(x, 65, 8) == beforeId && world.c(x, 65, 8) == beforeMeta
                    && world.b.size() == beforeEntities ? 1 : 0;
        }

        preparePoweredLever(world, x);
        if (id == 23) {
            int before = scheduledCount(world);
            na.m[id].b(world, x, 65, 8, 69);
            return scheduledCount(world) == before + 1 ? 1 : 0;
        }
        if (id == 25) {
            Object tile = world.b(x, 65, 8);
            require(!booleanState(tile), "note edge state started powered");
            na.m[id].b(world, x, 65, 8, 69);
            return booleanState(tile) ? 1 : 0;
        }
        int before = world.b.size();
        na.m[id].b(world, x, 65, 8, 69);
        return world.a(x, 65, 8) == 0 && containsPrimedTnt(world, before) ? 1 : 0;
    }

    private static void preparePoweredLever(dj world, int x) {
        ensureBlock(world, x + 1, 64, 8, 1, "lever support failed");
        require(world.b(x + 1, 65, 8, 69, 1), "unpowered lever placement failed");
        rawMetadata(world, x + 1, 65, 8, 9);
        require(world.c(x + 1, 65, 8) == 9,
                "power source transition was not committed");
        require(world.q(x, 65, 8),
                "consumer was not directly powered after source transition");
        require(world.r(x, 65, 8), "consumer was not powered after source transition");
    }

    private static void rawMetadata(dj world, int x, int y, int z, int metadata) {
        hi chunk = world.c(x >> 4, z >> 4);
        chunk.b(x & 15, y, z & 15, metadata);
    }

    private static void ensureBlock(dj world, int x, int y, int z, int id, String message) {
        if (world.a(x, y, z) != id)
            require(world.e(x, y, z, id), message);
    }

    private static int scheduledCount(dj world) {
        try {
            TreeSet<?> ticks = null;
            for (Field field : dj.class.getDeclaredFields())
                if (TreeSet.class.isAssignableFrom(field.getType())) {
                    require(ticks == null, "ambiguous scheduled-tick set");
                    field.setAccessible(true);
                    ticks = (TreeSet<?>) field.get(world);
                }
            require(ticks != null, "scheduled-tick set missing");
            return ticks.size();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot inspect scheduled ticks", exception);
        }
    }

    private static boolean containsPrimedTnt(dj world, int first) {
        for (int index = first; index < world.b.size(); index++)
            if (world.b.get(index) instanceof kg)
                return true;
        return false;
    }

    private static boolean booleanState(Object tile) {
        require(tile != null, "note tile entity missing");
        try {
            Field found = null;
            for (Field field : tile.getClass().getDeclaredFields())
                if (field.getType() == Boolean.TYPE) {
                    require(found == null, "ambiguous note boolean state");
                    found = field;
                }
            require(found != null, "note boolean state missing");
            found.setAccessible(true);
            return found.getBoolean(tile);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot inspect note edge state", exception);
        }
    }

    private static int scaled(double value) {
        return (int) Math.round(value * 160D);
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
