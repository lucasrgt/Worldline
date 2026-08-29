/** Official-name furnace domains and complete smelting transition. */
final class OracleFurnaceDomain {
    final int idleMask, activeMask, ignition, progress, extinction;
    final int completion;
    final int neighborCode, tickMask;
    private OracleFurnaceDomain(int idleMask, int activeMask, int ignition, int progress,
            int completion, int extinction, int neighborCode, int tickMask) {
        this.idleMask = idleMask;
        this.activeMask = activeMask;
        this.ignition = ignition;
        this.progress = progress;
        this.completion = completion;
        this.extinction = extinction;
        this.neighborCode = neighborCode;
        this.tickMask = tickMask;
    }
    static OracleFurnaceDomain execute(dj world) {
        int idleMask = 0;
        int activeMask = 0;
        for (int metadata = 2; metadata <= 5; metadata++) {
            int x = -32 + metadata * 8;
            int y = 84;
            int z = -24;
            place(world, x, y, z, 61, metadata);
            require(state(world, x, y, z) == 6100 + metadata,
                    "idle state drifted " + metadata);
            idleMask |= 1 << metadata;
            place(world, x, y, z, 62, metadata);
            require(state(world, x, y, z) == 6200 + metadata,
                    "active state drifted " + metadata);
            activeMask |= 1 << metadata;
        }
        int x = 20;
        int y = 90;
        int z = 20;
        place(world, x, y, z, 61, 4);
        ln ignitionTile = tile(world, x, y, z);
        ignitionTile.a(0, new fy(12, 1, 0));
        ignitionTile.a(1, new fy(263, 1, 0));
        ignitionTile.g_();
        int ignition = state(world, x, y, z);
        int progress = ignitionTile.a * 10000 + ignitionTile.c;
        require(ignition == 6204 && progress == 16000001,
                "ignition drifted: " + ignition + "/" + progress);

        int completeX = 24;
        place(world, completeX, y, z, 61, 3);
        ln completeTile = tile(world, completeX, y, z);
        completeTile.a(0, new fy(12, 1, 0));
        completeTile.a(1, new fy(263, 1, 0));
        for (int tick = 0; tick < 200; tick++) {
            completeTile.g_();
        }
        fy output = completeTile.d_(2);
        int completion = state(world, completeX, y, z) * 100000
                + stack(output) + completeTile.a;
        require(completion == 620303402, "complete smelt drifted: " + completion);
        completeTile.a = 1;
        completeTile.g_();
        int extinction = state(world, completeX, y, z);
        require(extinction == 6103, "extinction drifted: " + extinction);

        na.aC.b(world, completeX, y, z, 1);
        na.aD.b(world, -16, 84, -24, 1);
        int neighbors = state(world, completeX, y, z) * 10000
                + state(world, -16, 84, -24);
        int ticks = (na.n[61] ? 1 : 0) | (na.n[62] ? 2 : 0);
        OracleFurnaceDomain result = new OracleFurnaceDomain(idleMask, activeMask, ignition,
                progress, completion, extinction, neighbors, ticks);
        result.validate();
        return result;
    }
    String domains() {
        return "61=2..5,62=2..5";
    }
    String materialization() {
        return "item61=61:2..5,smelt=61:4>62:4>61:3";
    }
    String timing() {
        return "random=FF,tile=burn1600+cook200,output=20x1:0";
    }
    String neighbors() {
        return "stable=61:3+62:2,orientation=2..5";
    }
    private void validate() {
        require(idleMask == 60 && activeMask == 60, "furnace state domain incomplete");
        require(neighborCode == 61036202 && tickMask == 0,
                "neighbor or tick policy drifted: " + neighborCode + "/" + tickMask);
    }
    private static ln tile(dj world, int x, int y, int z) {
        return (ln) world.b(x, y, z);
    }
    private static void place(dj world, int x, int y, int z, int id, int metadata) {
        require(world.e(x, y, z, id), "furnace block placement failed");
        world.c(x, y, z, metadata);
    }
    private static int state(dj world, int x, int y, int z) {
        return world.a(x, y, z) * 100 + world.c(x, y, z);
    }
    private static int stack(fy value) {
        return value.c * 100 + value.a;
    }
    static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
