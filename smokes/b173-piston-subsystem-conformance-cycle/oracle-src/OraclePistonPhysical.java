import java.util.ArrayList;
import worldline.testapi.TestObservationWindow;

/** Official-name collision, light, tick, and neighbor transition probe. */
final class OraclePistonPhysical {
    final int baseBoxes, headBoxes, movingBoxes, lightSum, randomMask, idleTicks;
    final int normalExtended, normalRetracted, stickyExtended, stickyRetracted;
    final int headUnsupported, movingHeld, movingSettled;

    private OraclePistonPhysical(int baseBoxes, int headBoxes, int movingBoxes,
            int lightSum, int randomMask, int idleTicks, int normalExtended, int normalRetracted,
            int stickyExtended, int stickyRetracted, int headUnsupported,
            int movingHeld, int movingSettled) {
        this.baseBoxes = baseBoxes;
        this.headBoxes = headBoxes;
        this.movingBoxes = movingBoxes;
        this.lightSum = lightSum;
        this.randomMask = randomMask;
        this.idleTicks = idleTicks;
        this.normalExtended = normalExtended;
        this.normalRetracted = normalRetracted;
        this.stickyExtended = stickyExtended;
        this.stickyRetracted = stickyRetracted;
        this.headUnsupported = headUnsupported;
        this.movingHeld = movingHeld;
        this.movingSettled = movingSettled;
    }

    static OraclePistonPhysical execute(dj world) {
        int y = 96;
        int baseBoxes = boxes(world, na.aa, -20, y, 20, 13, false);
        int headBoxes = boxes(world, na.ab, -12, y, 20, 5, false);
        int movingBoxes = boxes(world, na.ad, -4, y, 20, 5, true);
        int light = 0, random = 0, index = 0;
        for (int id : new int[] {29, 33, 34, 36}) {
            light += na.q[id] + na.s[id];
            if (na.n[id])
                random |= 1 << index;
            index++;
        }

        world.b(20, y, 20, 33, 5);
        world.b(20, y, 24, 29, 5);
        world.b(25, y, 20, 33, 13);
        world.b(26, y, 20, 34, 5);
        TestObservationWindow idleWindow = new TestObservationWindow();
        idleWindow.observe(() -> {
            for (int tick = 0; tick < 20; tick++) {
                world.e();
                world.h();
            }
            return null;
        }, 20);
        OraclePistonDomain.require(OraclePistonDomain.state(world, 20, y, 20) == 3305
                && OraclePistonDomain.state(world, 20, y, 24) == 2905
                && OraclePistonDomain.state(world, 26, y, 20) == 3405,
                "idle piston tick policy drifted");

        int[] normal = pulse(world, 33, -20, y, 32);
        int[] sticky = pulse(world, 29, -20, y, 40);
        world.b(-4, y, 32, 33, 13);
        world.b(-3, y, 32, 34, 5);
        world.e(-4, y, 32, 0);
        na.ab.b(world, -3, y, 32, 0);
        int unsupported = OraclePistonDomain.state(world, -3, y, 32);

        world.b(4, y, 32, 36, 5);
        world.a(4, y, 32, mz.a(34, 5, 5, true, false));
        na.ad.b(world, 4, y, 32, 1);
        int held = OraclePistonDomain.state(world, 4, y, 32);
        OraclePistonDomain.require(world.b(4, y, 32) != null,
                "moving piston lost its tile on neighbor update");
        OraclePistonDomain.require(OraclePistonDomain.settle(world, 4, y, 32) == 3,
                "moving piston tile duration drifted");
        int settled = OraclePistonDomain.state(world, 4, y, 32);
        OraclePistonPhysical result = new OraclePistonPhysical(baseBoxes, headBoxes, movingBoxes,
                light, random, (int) idleWindow.observedTicks(), normal[0], normal[1], sticky[0], sticky[1],
                unsupported, held, settled);
        result.validate();
        return result;
    }

    String collision() {
        return "base=1:full,head=2:plate+rod,moving=1:translated";
    }
    String light() {
        return "29=0:0,33=0:0,34=0:0,36=0:0";
    }
    String ticks() {
        return "random=FFFF,idle=33:5+29:5+34:5@20-window,moving=36:5->34:5@3-te";
    }
    String neighbors() {
        return "normal=33:5->13->5,sticky=29:5->13->5,"
                + "head=34:5->0:0,moving-te=held";
    }

    private void validate() {
        OraclePistonDomain.require(baseBoxes == 1 && headBoxes == 2 && movingBoxes == 1,
                "piston collision family drifted");
        OraclePistonDomain.require(lightSum == 0 && randomMask == 0 && idleTicks == 20,
                "piston light or random tick policy drifted");
        OraclePistonDomain.require(normalExtended == 3313 && normalRetracted == 3305
                && stickyExtended == 2913 && stickyRetracted == 2905,
                "piston neighbor pulse drifted");
        OraclePistonDomain.require(headUnsupported == 0 && movingHeld == 3605
                && movingSettled == 3405, "internal piston transition drifted");
    }

    private static int boxes(dj world, na block, int x, int y, int z,
            int metadata, boolean moving) {
        world.b(x, y, z, block.bn, metadata);
        if (moving)
            world.a(x, y, z, mz.a(1, 0, 5, true, false));
        if (moving)
            return block.e(world, x, y, z) == null ? 0 : 1;
        ArrayList<cz> values = new ArrayList<cz>();
        block.a(world, x, y, z, cz.a(x - 1, y - 1, z - 1, x + 2, y + 2, z + 2), values);
        return values.size();
    }

    private static int[] pulse(dj world, int id, int x, int y, int z) {
        world.b(x, y, z, id, 5);
        world.b(x - 1, y - 1, z, 1, 0);
        world.b(x - 1, y, z, na.aR.bn, 5);
        na.m[id].b(world, x, y, z, na.aR.bn);
        int extensionSteps = OraclePistonDomain.settle(world, x + 1, y, z);
        OraclePistonDomain.require(extensionSteps == 3,
                "piston extension tile duration drifted: steps=" + extensionSteps
                        + ",base=" + OraclePistonDomain.state(world, x, y, z)
                        + ",front=" + OraclePistonDomain.state(world, x + 1, y, z));
        int extended = OraclePistonDomain.state(world, x, y, z);
        world.e(x - 1, y, z, 0);
        na.m[id].b(world, x, y, z, 0);
        int retractionSteps = OraclePistonDomain.settle(world, x, y, z);
        OraclePistonDomain.require(retractionSteps == 3,
                "piston retraction tile duration drifted: steps=" + retractionSteps
                        + ",base=" + OraclePistonDomain.state(world, x, y, z));
        return new int[] {extended, OraclePistonDomain.state(world, x, y, z)};
    }
}
