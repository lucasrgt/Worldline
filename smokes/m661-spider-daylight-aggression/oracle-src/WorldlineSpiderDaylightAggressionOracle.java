import worldline.trace.CanonicalTrace;

/** Direct official-name oracle for one spider daylight target differential. */
public final class WorldlineSpiderDaylightAggressionOracle {
    private static final long SEED = 66120260826L;
    private static final long DAY = 6000L;
    private static final long NIGHT = 14000L;
    private static final int MAXIMUM = 4;

    private WorldlineSpiderDaylightAggressionOracle() {
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        CanonicalTrace trace = new CanonicalTrace(SEED);
        dj world = new dj(new OracleSpiderDaylightMemorySave(SEED, "spider-daylight"),
                "spider-daylight", SEED, null);
        world.q = 1;
        for (int chunkX = -1; chunkX <= 1; chunkX++) {
            for (int chunkZ = -1; chunkZ <= 1; chunkZ++) {
                world.c(chunkX, chunkZ);
            }
        }
        setLight(world, DAY);
        em player = new em(world) {
        };
        player.c(8.5D, 65D, 8.5D);
        ProbeSpider spider = new ProbeSpider(world);
        spider.c(11.5D, 65D, 8.5D);
        require(world.b(player), "official player join failed");
        if (!world.d.contains(player)) {
            world.d.add(player);
        }
        require(world.b(spider), "official spider join failed");
        require(world.b.contains(player) && world.b.contains(spider)
                        && world.d.contains(player) && world.b.size() == 2,
                "official spider-player fixture absent");

        int spiderId = spider.aG;
        int playerId = player.aG;
        int spiderX = cell(spider.aP);
        int spiderY = cell(spider.aQ);
        int spiderZ = cell(spider.aR);
        int playerX = cell(player.aP);
        int playerY = cell(player.aQ);
        int playerZ = cell(player.aR);
        require(spider.brightness() > 0.5F, "official daylight brightness absent");
        require(absent(spider), "official spider selected a daylight target");

        setLight(world, NIGHT);
        require(spider.brightness() < 0.5F, "official night darkness absent");
        lq target = select(spider);
        require(target == player && spider.aG == spiderId && player.aG == playerId,
                "official night target or identity drifted");
        require(cell(spider.aP) == spiderX && cell(spider.aQ) == spiderY
                        && cell(spider.aR) == spiderZ && cell(player.aP) == playerX
                        && cell(player.aQ) == playerY && cell(player.aR) == playerZ,
                "official fixture geometry drifted");

        trace.record("daylight", DAY, world.b.size(), 1, 1, 1, 1, 1, MAXIMUM);
        trace.record("night", NIGHT, world.b.size(), 1, 1, 1, 1, 1, MAXIMUM);
        trace.emitTo(System.out);
    }

    private static boolean absent(ProbeSpider spider) {
        for (int attempt = 0; attempt < MAXIMUM; attempt++) {
            if (spider.target() != null) {
                return false;
            }
        }
        return true;
    }

    private static lq select(ProbeSpider spider) {
        for (int attempt = 0; attempt < MAXIMUM; attempt++) {
            lq target = spider.target();
            if (target != null) {
                return target;
            }
        }
        throw new IllegalStateException("official night target absent within maximum");
    }

    private static void setLight(dj world, long time) {
        world.a(time);
        world.g();
    }

    private static int cell(double coordinate) {
        return (int) Math.floor(coordinate);
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }

    private static final class ProbeSpider extends bn {
        ProbeSpider(dj world) {
            super(world);
        }

        lq target() {
            return o();
        }

        float brightness() {
            return c(1.0F);
        }
    }
}
