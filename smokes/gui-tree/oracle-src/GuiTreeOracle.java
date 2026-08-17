import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import worldline.trace.CanonicalStateTrace;

/** Independent official-JAR oracle for the inventory UI tree. */
public final class GuiTreeOracle {
    private static final long SEED = 17320110707L;

    private GuiTreeOracle() {}

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        OracleClientBoundaries.Client client = new OracleClientBoundaries.Client();
        client.k = new gr("Worldline", "offline");
        client.z = new kv(); client.z.y = 0;
        client.I = OracleClientBoundaries.allocateWithoutConstructor(Statistics.class);
        client.p = new OracleClientBoundaries.Textures(client.z);
        client.v = new uq(client); client.c = new ob(client); client.t = new px(client);
        client.g = OracleClientBoundaries.allocateWithoutConstructor(n.class);
        fd world = new fd(new OracleClientWorld(SEED, "gui-tree"), "gui-tree", SEED);
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) world.c(x, z);
        dc player = new dc(client, world, client.k, 0);
        player.a = new lr(client.z); player.c(8.5D, 66.0D, 8.5D, 0.0F, 0.0F);
        client.f = world; client.h = player; client.i = player;
        client.j = new dn(world, client.p); world.r.setSeed(SEED);
        CanonicalStateTrace trace = new CanonicalStateTrace(SEED, "screen", "nodes", "slot0", "count0",
                "slot44", "count44");
        record(trace, "closed", client);
        tap(18); tick(client);
        require(client.r instanceof ue, "oracle inventory screen missing");
        id screen = (id) client.r;
        require(screen.j.e.size() == 45 && ((gp) screen.j.e.get(0)).a() == null, "oracle slot tree failed");
        client.c.a(screen.j.f, 0, 0, false, player);
        record(trace, "opened", client);
        tap(1); tick(client);
        require(client.r == null && !Display.isCreated(), "oracle close failed");
        record(trace, "closed2", client);
        client.J = false;
        System.out.println("WORLDLINE_GUI_SOURCE="
                + net.minecraft.client.Minecraft.class.getProtectionDomain().getCodeSource().getLocation());
        System.out.println("WORLDLINE_GUI_TRACE=" + trace.value());
        System.out.println("WORLDLINE_GUI_SIGNATURE=" + trace.signature());
        System.out.println("WORLDLINE_GUI_API=screen,slot,click");
    }

    private static void record(CanonicalStateTrace trace, String label, net.minecraft.client.Minecraft client) {
        if (!(client.r instanceof ue)) { trace.record(label, 0, 0, -1, 0, -1, 0); return; }
        id screen = (id) client.r;
        iz first = ((gp) screen.j.e.get(0)).a(), last = ((gp) screen.j.e.get(44)).a();
        trace.record(label, 1, 1 + screen.j.e.size(), first == null ? -1 : first.c, first == null ? 0 : first.a,
                last == null ? -1 : last.c, last == null ? 0 : last.a);
    }

    private static void tap(int key) { Keyboard.worldlinePush(key, true, (char) 0); Keyboard.worldlinePush(key, false, (char) 0); }

    private static void tick(net.minecraft.client.Minecraft client) {
        try {
            java.lang.reflect.Field field = net.minecraft.client.Minecraft.class.getDeclaredField("V");
            field.setAccessible(true); field.setInt(client, field.getInt(client) + 1); client.k();
        } catch (ReflectiveOperationException error) { throw new IllegalStateException(error); }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    /** Neutralizes stats I/O reached when the inventory key opens a screen. */
    static final class Statistics extends xi {
        private Statistics() { super(null, (java.io.File) null); }
        @Override public void d() {}
        @Override public void c() {}
        @Override public void b() {}
        @Override public boolean a(ny achievement) { return true; }
        @Override public void a(vr stat, int amount) {}
    }
}
