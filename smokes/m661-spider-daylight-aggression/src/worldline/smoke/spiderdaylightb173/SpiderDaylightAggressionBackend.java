package worldline.smoke.spiderdaylightb173;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntitySpider;
import net.minecraft.src.World;
import worldline.api.SpiderDaylightAggressionActions;
import worldline.api.SpiderDaylightAggressionEvidence;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Native mapped-world adapter for one causal spider light differential. */
final class SpiderDaylightAggressionBackend
        implements GameBackend, SpiderDaylightAggressionActions {
    private static final long DAY = 6000L;
    private static final long NIGHT = 14000L;
    private static final int PLAYER_X = 8;
    private static final int Y = 65;
    private static final int Z = 8;
    private static final int SPIDER_X = 11;
    private final long seed;
    private World world;
    private EntityPlayer player;
    private ProbeSpider spider;

    SpiderDaylightAggressionBackend(long seed) {
        this.seed = seed;
    }

    @Override public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override @SuppressWarnings("unchecked")
    public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        world = new World(new SpiderDaylightMemorySave(seed, name), name, seed, null);
        world.difficultySetting = 1;
        for (int chunkX = -1; chunkX <= 1; chunkX++) {
            for (int chunkZ = -1; chunkZ <= 1; chunkZ++) {
                world.getChunkFromChunkCoords(chunkX, chunkZ);
            }
        }
        setLight(DAY);
        player = new EntityPlayer(world) {
        };
        player.setPosition(PLAYER_X + 0.5D, Y, Z + 0.5D);
        spider = new ProbeSpider(world);
        spider.setPosition(SPIDER_X + 0.5D, Y, Z + 0.5D);
        require(world.entityJoinedWorld(player), "mapped player join failed");
        if (!world.playerEntities.contains(player)) {
            world.playerEntities.add(player);
        }
        require(world.entityJoinedWorld(spider), "mapped spider join failed");
        require(world.loadedEntityList.contains(player)
                        && world.loadedEntityList.contains(spider)
                        && world.playerEntities.contains(player),
                "mapped spider-player fixture absent");
    }

    @Override public SpiderDaylightAggressionEvidence.Trial trial(int maximumAttempts) {
        require(maximumAttempts > 0, "invalid target-selection maximum");
        SpiderDaylightAggressionEvidence.ActorState spiderBefore = state(spider);
        SpiderDaylightAggressionEvidence.ActorState playerBefore = state(player);
        boolean daylightBright = spider.brightness() > 0.5F;
        int daylightTarget = requireAbsent(maximumAttempts);
        setLight(NIGHT);
        boolean nightDark = spider.brightness() < 0.5F;
        Entity nightTarget = select(maximumAttempts);
        require(nightTarget == player, "mapped night target was not the same player");
        return new SpiderDaylightAggressionEvidence.Trial(
                spiderBefore, playerBefore, daylightTarget, daylightBright,
                state(spider), state(player), nightTarget.entityId,
                nightDark, maximumAttempts);
    }

    @Override public void tick() {
        require(world != null, "mapped spider daylight world is absent");
    }

    void record(CanonicalTrace trace, SpiderDaylightAggressionEvidence evidence) {
        int entities = world.loadedEntityList.size();
        trace.record("daylight", DAY, entities,
                evidence.daylightTargetAbsent() ? 1 : 0,
                1,
                evidence.spiderIdentityPreserved() ? 1 : 0,
                evidence.playerIdentityPreserved() ? 1 : 0,
                evidence.geometryPreserved() ? 1 : 0,
                evidence.maximumAttempts());
        trace.record("night", NIGHT, entities,
                1,
                evidence.nightTargetPlayer() ? 1 : 0,
                evidence.spiderIdentityPreserved() ? 1 : 0,
                evidence.playerIdentityPreserved() ? 1 : 0,
                evidence.geometryPreserved() ? 1 : 0,
                evidence.maximumAttempts());
    }

    @Override public void close() {
        spider = null;
        player = null;
        world = null;
    }

    private int requireAbsent(int maximumAttempts) {
        for (int attempt = 0; attempt < maximumAttempts; attempt++) {
            Entity target = spider.selectTarget();
            if (target != null) {
                return target.entityId;
            }
        }
        return -1;
    }

    private Entity select(int maximumAttempts) {
        for (int attempt = 0; attempt < maximumAttempts; attempt++) {
            Entity target = spider.selectTarget();
            if (target != null) {
                return target;
            }
        }
        throw new IllegalStateException("mapped night target absent within maximum");
    }

    private void setLight(long time) {
        world.setWorldTime(time);
        world.calculateInitialSkylight();
    }

    private static SpiderDaylightAggressionEvidence.ActorState state(Entity entity) {
        return new SpiderDaylightAggressionEvidence.ActorState(entity.entityId,
                cell(entity.posX), cell(entity.posY), cell(entity.posZ));
    }

    private static int cell(double coordinate) {
        return (int) Math.floor(coordinate);
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }

    private static final class ProbeSpider extends EntitySpider {
        ProbeSpider(World world) {
            super(world);
        }

        Entity selectTarget() {
            return findPlayerToAttack();
        }

        float brightness() {
            return getEntityBrightness(1.0F);
        }
    }
}
