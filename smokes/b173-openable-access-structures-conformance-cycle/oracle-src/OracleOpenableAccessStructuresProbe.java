import java.util.Random;
import worldline.testapi.OpenableAccessStructuresObservation;

/** Proves the final chest, wooden-door, and trapdoor census boundaries. */
final class OracleOpenableAccessStructuresProbe {
    final int[][] rows;

    private OracleOpenableAccessStructuresProbe(int[][] rows) { this.rows = rows; }

    static OracleOpenableAccessStructuresProbe execute(dj world) {
        int[][] rows = {chest(world), woodenDoor(world), trapdoor(world)};
        OracleOpenableAccessStructuresProbe result = new OracleOpenableAccessStructuresProbe(rows);
        result.validate();
        return result;
    }

    OpenableAccessStructuresObservation observation() {
        return new OpenableAccessStructuresObservation(
                "54:scheduled=F+callback-stable+neighbor-stable",
                "64:collision=closed-x-3/16+open-z-3/16,light=0:0,"
                        + "scheduled=F+callback-stable",
                "96:meta=0..7,collision=closed-floor-3/16+open-four-faces,light=0:0,"
                        + "scheduled=F+callback-stable,"
                        + "neighbor=support-stable+support-loss-air+96x1");
    }

    private static int[] chest(dj world) {
        require(world.b(12, 80, 20, 54, 0), "chest fixture failed");
        int before = state(world, 12, 80, 20);
        na.av.a(world, 12, 80, 20, new Random(17320110901L));
        int after = state(world, 12, 80, 20);
        na.av.b(world, 12, 80, 20, 1);
        return new int[] {54, na.n[54] ? 1 : 0,
                before, after, state(world, 12, 80, 20)};
    }

    private static int[] woodenDoor(dj world) {
        placeDoor(world, 20);
        cz closed = na.aF.e(world, 20, 80, 20);
        int closedCollision = exact(closed, 20D, 80D, 20D, 20.1875D, 81D, 21D);
        ((hc) na.aF).a(world, 20, 80, 20, true);
        cz open = na.aF.e(world, 20, 80, 20);
        int openCollision = exact(open, 20D, 80D, 20D, 21D, 81D, 20.1875D);
        ((hc) na.aF).a(world, 20, 80, 20, false);
        int lowerBefore = state(world, 20, 80, 20), upperBefore = state(world, 20, 81, 20);
        na.aF.a(world, 20, 80, 20, new Random(17320110902L));
        na.aF.a(world, 20, 81, 20, new Random(17320110902L));
        return new int[] {64, closedCollision, openCollision,
                na.aF.a() ? 1 : 0, na.aF.b() ? 1 : 0,
                na.q[64] * 100 + na.s[64],
                na.n[64] ? 1 : 0, lowerBefore, state(world, 20, 80, 20),
                upperBefore, state(world, 20, 81, 20)};
    }

    private static int[] trapdoor(dj world) {
        require(world.b(32, 80, 20, 96, 0),
                "trapdoor domain fixture failed");
        int domain = 0;
        for (int metadata = 0; metadata <= 7; metadata++) {
            rawMetadata(world, 32, 80, 20, metadata);
            require(world.c(32, 80, 20) == metadata,
                    "trapdoor metadata rejected: " + metadata);
            domain |= 1 << metadata;
        }
        rawMetadata(world, 32, 80, 20, 0);
        int closed = exact(na.bl.e(world, 32, 80, 20),
                32D, 80D, 20D, 33D, 80.1875D, 21D);
        int faces = trapdoorFaces(world);
        int before = state(world, 32, 80, 20);
        na.bl.a(world, 32, 80, 20, new Random(17320110903L));
        int after = state(world, 32, 80, 20);

        require(world.e(39, 80, 20, 1)
                && world.b(40, 80, 20, 96, 3),
                "trapdoor support fixture failed");
        na.bl.b(world, 40, 80, 20, 1);
        int supported = state(world, 40, 80, 20);
        int entities = world.b.size();
        require(world.e(39, 80, 20, 0), "trapdoor support removal failed");
        na.bl.b(world, 40, 80, 20, 1);
        ez drop = lastDrop(world, entities);
        return new int[] {96, domain, closed, faces,
                na.bl.a() ? 1 : 0, na.bl.b() ? 1 : 0,
                na.q[96] * 100 + na.s[96],
                na.n[96] ? 1 : 0, before, after, supported,
                state(world, 40, 80, 20), id(drop), count(drop)};
    }

    private static int trapdoorFaces(dj world) {
        double[][] boxes = {
            {32D, 80D, 20.8125D, 33D, 81D, 21D},
            {32D, 80D, 20D, 33D, 81D, 20.1875D},
            {32.8125D, 80D, 20D, 33D, 81D, 21D},
            {32D, 80D, 20D, 32.1875D, 81D, 21D}
        };
        int mask = 0;
        for (int direction = 0; direction < boxes.length; direction++) {
            rawMetadata(world, 32, 80, 20, direction + 4);
            double[] box = boxes[direction];
            if (exact(na.bl.e(world, 32, 80, 20),
                    box[0], box[1], box[2], box[3], box[4], box[5]) == 1)
                mask |= 1 << direction;
        }
        rawMetadata(world, 32, 80, 20, 0);
        return mask;
    }

    private void validate() {
        require(matches(rows[0], new int[] {54, 0, 5400, 5400, 5400}),
                "chest timing or neighbor response drifted");
        require(matches(rows[1], new int[] {64, 1, 1, 0, 0, 0, 0,
                6400, 6400, 6408, 6408}), "wooden-door physical or tick envelope drifted");
        require(matches(rows[2], new int[] {96, 255, 1, 15, 0, 0, 0, 0,
                9600, 9600, 9603, 0, 96, 1}), "trapdoor subsystem drifted");
    }

    private static void placeDoor(dj world, int x) {
        require(world.e(x, 79, 20, 1)
                && world.b(x, 80, 20, 64, 0)
                && world.b(x, 81, 20, 64, 8),
                "wooden-door pair failed");
    }
    private static void rawMetadata(dj world, int x, int y, int z, int metadata) {
        hi chunk = world.c(x >> 4, z >> 4);
        chunk.b(x & 15, y, z & 15, metadata);
    }
    private static int exact(cz box, double a, double b, double c,
            double d, double e, double f) {
        return box != null && box.a == a && box.b == b && box.c == c
                && box.d == d && box.e == e && box.f == f ? 1 : 0;
    }
    private static ez lastDrop(dj world, int first) {
        for (int index = world.b.size() - 1; index >= first; index--)
            if (world.b.get(index) instanceof ez)
                return (ez) world.b.get(index);
        return null;
    }
    private static int id(ez drop) { return drop == null ? 0 : drop.a.c; }
    private static int count(ez drop) { return drop == null ? 0 : drop.a.a; }
    private static int state(dj world, int x, int y, int z) {
        return world.a(x, y, z) * 100 + world.c(x, y, z);
    }
    private static boolean matches(int[] actual, int[] expected) {
        if (actual.length != expected.length) return false;
        for (int index = 0; index < actual.length; index++)
            if (actual[index] != expected[index]) return false;
        return true;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
