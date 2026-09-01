package worldline.smoke.b173portalblock;

import java.util.Random;
import net.minecraft.src.Block;
import net.minecraft.src.World;

/** Proves both frame axes, tick policy, and frame-loss collapse. */
final class PortalBlockDomainProbe {
    final int xCells, zCells, metadataMask, randomMask, tickBefore, tickAfter;
    final int entityDelta, neighborBefore, neighborAfter;
    private PortalBlockDomainProbe(int xCells, int zCells, int metadataMask,
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
    static PortalBlockDomainProbe execute(World world) {
        int xCells = PortalBlockFrame.buildX(world, 20, 80, 20);
        int metadata = PortalBlockFrame.metadataMaskX(world, 20, 80, 20);
        int zCells = PortalBlockFrame.buildZ(world, 32, 80, 20);
        int tickBefore = PortalBlockFrame.state(world, 21, 81, 20);
        int entities = world.loadedEntityList.size();
        Block.portal.updateTick(world, 21, 81, 20, new Random(17320110707L));
        int tickAfter = PortalBlockFrame.state(world, 21, 81, 20);
        int neighborBefore = PortalBlockFrame.countZ(world, 32, 80, 20);
        PortalBlockFrame.collapseZ(world, 32, 80, 20);
        PortalBlockDomainProbe result = new PortalBlockDomainProbe(xCells, zCells, metadata,
                Block.tickOnLoad[90] ? 1 : 0, tickBefore, tickAfter,
                world.loadedEntityList.size() - entities, neighborBefore,
                PortalBlockFrame.countZ(world, 32, 80, 20));
        result.validate();
        return result;
    }
    String domains() { return "90=0,frames=X+Z,cells=6+6"; }
    String timing() { return "scheduled=F,callback-stable=90:0,entities=0"; }
    String neighbors() { return "frame-loss=6x90:0->air"; }
    private void validate() {
        PortalBlockFrame.require(xCells == 6 && zCells == 6 && metadataMask == 1,
                "portal domain or materialization drifted");
        PortalBlockFrame.require(randomMask == 0 && tickBefore == 9000
                && tickAfter == 9000 && entityDelta == 0, "portal random tick drifted: "
                + randomMask + "/" + tickBefore + "/" + tickAfter + "/" + entityDelta);
        PortalBlockFrame.require(neighborBefore == 6 && neighborAfter == 0,
                "portal frame-loss collapse drifted");
    }
}
