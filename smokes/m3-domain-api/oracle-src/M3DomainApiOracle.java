import java.lang.reflect.Field;
import org.lwjgl.opengl.Display;
import worldline.trace.CanonicalStateTrace;

/** Independent official-JAR oracle for the stable M3 domain semantics. */
public final class M3DomainApiOracle {
    private static final long SEED = 17320110707L;
    private M3DomainApiOracle() {}

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        OracleClientBoundaries.Client client = new OracleClientBoundaries.Client();
        client.k = new gr("Worldline", "offline");
        client.z = new kv(); client.z.y = 0;
        client.I = OracleClientBoundaries.allocateWithoutConstructor(OracleClientBoundaries.Statistics.class);
        client.p = new OracleClientBoundaries.Textures(client.z);
        client.v = new uq(client); client.c = new ob(client); client.t = new px(client);
        client.g = OracleClientBoundaries.allocateWithoutConstructor(n.class);
        fd world = new fd(new OracleClientWorld(SEED, "m3-domain-api"), "m3-domain-api", SEED);
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) world.c(x, z);
        dc player = new dc(client, world, client.k, 0);
        player.a = new lr(client.z); player.c(8.5D, 66.0D, 8.5D, 0.0F, 0.0F);
        client.f = world; client.h = player; client.i = player;
        client.j = new dn(world, client.p); world.r.setSeed(SEED);
        require(world.a(8, 64, 8) == 1 && world.e(8, 64, 8) == 0, "oracle stone missing");
        require(world.a(8, 65, 8) == 0 && player.l.equals("Worldline"), "oracle identity failed");
        CanonicalStateTrace trace = trace();
        record(trace, "loaded", world, player);
        require(world.b(8, 65, 8, 20, 3), "oracle block mutation failed");
        player.e(10.5D, 66.0D, 10.5D); player.c.c = 4;
        record(trace, "mutated", world, player);
        for (int tick = 1; tick <= 3; tick++) { setTicks(client, ticks(client) + 1); client.k(); }
        record(trace, "tick3", world, player);
        require(world.t() == 3L && player.c.c == 4 && !Display.isCreated(), "oracle final invariant failed");
        client.J = false;
        System.out.println("WORLDLINE_M3_SOURCE="
                + net.minecraft.client.Minecraft.class.getProtectionDomain().getCodeSource().getLocation());
        System.out.println("WORLDLINE_M3_TRACE=" + trace.value());
        System.out.println("WORLDLINE_M3_SIGNATURE=" + trace.signature());
        System.out.println("WORLDLINE_M3_API=world,block,entity,player");
    }

    private static CanonicalStateTrace trace() {
        return new CanonicalStateTrace(SEED, "time", "block64", "meta64", "block65", "meta65",
                "entities", "playerId", "alive", "x", "y", "z", "health", "slot");
    }

    private static void record(CanonicalStateTrace trace, String label, fd world, dc player) {
        trace.record(label, world.t(), world.a(8, 64, 8), world.e(8, 64, 8),
                world.a(8, 65, 8), world.e(8, 65, 8), world.b.size() + 1L, player.aD,
                player.be ? 0 : 1, Double.doubleToLongBits(player.aM),
                Double.doubleToLongBits(player.aN), Double.doubleToLongBits(player.aO),
                player.Y, player.c.c);
    }

    private static int ticks(net.minecraft.client.Minecraft client) { return field(client).getInt(client); }
    private static void setTicks(net.minecraft.client.Minecraft client, int value) { field(client).setInt(client, value); }
    private static IntField field(net.minecraft.client.Minecraft client) { return new IntField(client, "V"); }

    private static final class IntField {
        private final Object target; private final Field field;
        IntField(Object target, String name) {
            this.target = target;
            try { field = target.getClass().getSuperclass().getDeclaredField(name); field.setAccessible(true); }
            catch (ReflectiveOperationException error) { throw new IllegalStateException(error); }
        }
        int getInt(Object ignored) { try { return field.getInt(target); }
            catch (IllegalAccessException error) { throw new IllegalStateException(error); } }
        void setInt(Object ignored, int value) { try { field.setInt(target, value); }
            catch (IllegalAccessException error) { throw new IllegalStateException(error); } }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
