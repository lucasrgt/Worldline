package worldline.smoke.chunkrestartb173;

import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.RemoteObjectSpawn;
import worldline.b173server.B173WireClient;

/** Placement and fixture helpers for the M653 restart-persistence oracle. */
final class M653FixtureSupport {
    private M653FixtureSupport() { }

    static BlockPosition placeLegacy(B173WireClient client, BlockPosition support,
            BlockFace face, int legacyId) throws Exception {
        BlockPosition target = face.adjacent(support);
        client.placeHeldBlock(support, face);
        worldline.test.WorldlineSmokeAwait.awaitEntity(client,
                () -> client.sustainTicks(1).blockAt(target.x(), target.y(), target.z()),
                state -> state.legacyId() == legacyId, "oriented block placement", 40);
        return target;
    }

    static Fixture build(B173WireClient actor, BlockPosition support) throws Exception {
        actor.selectHeldSlot(0);
        BlockPosition top = worldline.b173server.B173FixtureSupport.place(
                actor, support, BlockFace.UP, 1);
        for (int lift = 0; lift < 5; lift++) {
            top = worldline.b173server.B173FixtureSupport.place(actor, top, BlockFace.UP, 1);
            actor.moveAndObserve(0D, 1D, 0D, 1);
        }
        BlockPosition chestPad = worldline.b173server.B173FixtureSupport.place(
                actor, top, BlockFace.EAST, 1);
        BlockPosition east = worldline.b173server.B173FixtureSupport.place(
                actor, chestPad, BlockFace.EAST, 1);
        BlockPosition returnPad = worldline.b173server.B173FixtureSupport.place(
                actor, east, BlockFace.EAST, 1);
        BlockPosition railPad = worldline.b173server.B173FixtureSupport.place(
                actor, top, BlockFace.WEST, 1);
        actor.selectHeldSlot(1);
        BlockPosition chest = placeLegacy(actor, chestPad, BlockFace.UP, 54);
        actor.selectHeldSlot(2);
        BlockPosition rail = worldline.b173server.B173FixtureSupport.place(
                actor, railPad, BlockFace.UP, 66);
        return new Fixture(chest, rail, returnPad);
    }

    static RemoteObjectSpawn spawnCart(B173WireClient actor, BlockPosition rail) {
        actor.selectHeldSlot(3);
        actor.useHeldItemOnBlock(rail, BlockFace.UP);
        RemoteObjectSpawn cart = actor.awaitObjectSpawn(10);
        require(cart.type() == 10 && cart.throwerId() == 0, "minecart spawn drift");
        return cart;
    }

    static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    static final class Fixture {
        final BlockPosition chest, rail, returnPad;
        Fixture(BlockPosition chest, BlockPosition rail, BlockPosition returnPad) {
            this.chest = chest;
            this.rail = rail;
            this.returnPad = returnPad;
        }
    }
}
