import java.util.Random;

/** Official-name frame, tick-policy, and neighbor probe. */
final class OraclePortalBlockDomain {
    final int xCells, zCells, metadataMask, randomMask, tickBefore, tickAfter;
    final int entityDelta, neighborBefore, neighborAfter;
    private OraclePortalBlockDomain(int xCells, int zCells, int metadataMask,
            int randomMask, int tickBefore, int tickAfter, int entityDelta,
            int neighborBefore, int neighborAfter) {
        this.xCells = xCells;
        this.zCells = zCells;
        this.metadataMask = metadataMask;
        this.randomMask = randomMask;
        this.tickBefore = tickBefore;
        this.tickAfter = tickAfter;
        this.entityDelta = entityDelta;
        this.neighborBefore = neighborBefore;
        this.neighborAfter = neighborAfter;
    }
    static OraclePortalBlockDomain execute(dj world) {
        int xCells = OraclePortalBlockFrame.buildX(world, 20, 80, 20);
        int metadata = OraclePortalBlockFrame.metadataMaskX(world, 20, 80, 20);
        int zCells = OraclePortalBlockFrame.buildZ(world, 32, 80, 20);
        int tickBefore = OraclePortalBlockFrame.state(world, 21, 81, 20);
        int entities = world.b.size();
        na.bf.a(world, 21, 81, 20, new Random(17320110707L));
        int tickAfter = OraclePortalBlockFrame.state(world, 21, 81, 20);
        int neighborBefore = OraclePortalBlockFrame.countZ(world, 32, 80, 20);
        OraclePortalBlockFrame.collapseZ(world, 32, 80, 20);
        OraclePortalBlockDomain result = new OraclePortalBlockDomain(xCells, zCells, metadata,
                na.n[90] ? 1 : 0, tickBefore, tickAfter, world.b.size() - entities,
                neighborBefore, OraclePortalBlockFrame.countZ(world, 32, 80, 20));
        result.validate();
        return result;
    }
    String domains() { return "90=0,frames=X+Z,cells=6+6"; }
    String timing() { return "scheduled=F,callback-stable=90:0,entities=0"; }
    String neighbors() { return "frame-loss=6x90:0->air"; }
    private void validate() {
        OraclePortalBlockFrame.require(xCells == 6 && zCells == 6 && metadataMask == 1,
                "portal domain or materialization drifted");
        OraclePortalBlockFrame.require(randomMask == 0 && tickBefore == 9000
                && tickAfter == 9000 && entityDelta == 0, "portal random tick drifted: "
                + randomMask + "/" + tickBefore + "/" + tickAfter + "/" + entityDelta);
        OraclePortalBlockFrame.require(neighborBefore == 6 && neighborAfter == 0,
                "portal frame-loss collapse drifted");
    }
}
