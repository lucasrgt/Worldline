package worldline.smoke.poweredcreeperb173;

import net.minecraft.src.EntityCreeper;
import net.minecraft.src.EntityLightningBolt;
import net.minecraft.src.World;
import worldline.api.scenario.PoweredCreeperActions;
import worldline.api.scenario.PoweredCreeperEvidence;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Native mapped-world adapter for one same-identity creeper lightning trial. */
final class PoweredCreeperBackend implements GameBackend, PoweredCreeperActions {
    private static final int X = 8;
    private static final int Y = 65;
    private static final int Z = 8;
    private final long seed;
    private World world;
    private EntityCreeper creeper;
    private PoweredCreeperEvidence.CreeperState initial;
    private PoweredCreeperEvidence.Trial trial;

    PoweredCreeperBackend(long seed) {
        this.seed = seed;
    }

    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new PoweredCreeperMemorySave(seed, name), name, seed, null);
        world.difficultySetting = 1;
        for (int chunkX = -1; chunkX <= 1; chunkX++) {
            for (int chunkZ = -1; chunkZ <= 1; chunkZ++) {
                world.getChunkFromChunkCoords(chunkX, chunkZ);
            }
        }
        creeper = new EntityCreeper(world);
        creeper.setPosition(X + 0.5D, Y, Z + 0.5D);
        require(world.entityJoinedWorld(creeper), "mapped creeper join failed");
        initial = current();
        require(!initial.powered() && world.loadedEntityList.contains(creeper),
                "mapped creeper unpowered precondition absent");
    }

    @Override public PoweredCreeperEvidence.Trial strike() {
        require(trial == null, "powered-creeper strike already performed");
        PoweredCreeperEvidence.CreeperState prerequisite = current();
        require(!prerequisite.powered(), "creeper powered before lightning join");
        EntityLightningBolt lightning =
                new EntityLightningBolt(world, creeper.posX, creeper.posY, creeper.posZ);
        boolean joined = world.entityJoinedWorld(lightning)
                && world.loadedEntityList.contains(lightning)
                && world.loadedEntityList.contains(creeper);
        PoweredCreeperEvidence.LightningStrike observed =
                new PoweredCreeperEvidence.LightningStrike(lightning.entityId,
                        cell(lightning.posX), cell(lightning.posY), cell(lightning.posZ),
                        joined, !lightning.isDead);
        creeper.onStruckByLightning(lightning);
        trial = new PoweredCreeperEvidence.Trial(initial, prerequisite, observed, current());
        return trial;
    }

    @Override public PoweredCreeperEvidence.CreeperState current() {
        require(creeper != null, "mapped creeper is absent");
        return new PoweredCreeperEvidence.CreeperState(creeper.entityId,
                cell(creeper.posX), cell(creeper.posY), cell(creeper.posZ),
                creeper.getPowered());
    }

    @Override public void tick() {
        require(world != null, "mapped powered-creeper world is absent");
        world.tick();
        world.updateEntities();
    }

    void recordInitial(CanonicalTrace trace) {
        trace.record("initial", world.getWorldTime(), world.loadedEntityList.size(),
                initial.powered() ? 0 : 1, initial.cellX(), initial.cellY(), initial.cellZ());
    }

    void recordOutcome(CanonicalTrace trace, PoweredCreeperEvidence evidence) {
        PoweredCreeperEvidence.LightningStrike lightning = trial.strike();
        trace.record("strike", 0L, 2,
                lightning.joined() ? 1 : 0, lightning.alive() ? 1 : 0,
                trial.prerequisite().powered() ? 0 : 1,
                lightning.entityId() != trial.before().entityId() ? 1 : 0);
        trace.record("powered", 0L, 2,
                evidence.identityPreserved() ? 1 : 0,
                evidence.powered() ? 1 : 0,
                evidence.strikeAtCreeper() ? 1 : 0);
        trace.record("held", world.getWorldTime(), world.loadedEntityList.size(),
                evidence.identityPreserved() ? 1 : 0,
                evidence.heldPowered() ? 1 : 0,
                evidence.strikeAtCreeper() ? 1 : 0,
                evidence.strikeObserved() ? 1 : 0);
    }

    @Override public void close() {
        trial = null;
        initial = null;
        creeper = null;
        world = null;
    }

    private static int cell(double coordinate) {
        return (int) Math.floor(coordinate);
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
