import java.io.File;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Official-name counterparts of the controlled client boundaries. */
final class OracleClientBoundaries {
    private OracleClientBoundaries() {}

    static <T> T allocateWithoutConstructor(Class<T> type) {
        try {
            Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
            Field singleton = unsafeType.getDeclaredField("theUnsafe");
            singleton.setAccessible(true);
            Object unsafe = singleton.get(null);
            Method allocate = unsafeType.getMethod("allocateInstance", Class.class);
            return type.cast(allocate.invoke(unsafe, type));
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("cannot allocate oracle boundary", error);
        }
    }

    static final class Client extends net.minecraft.client.Minecraft {
        Client() { super(null, null, null, 320, 240, false); }

        @Override
        public void a(mh error) {
            throw new IllegalStateException("official client crashed", error.b);
        }
    }

    static final class Textures extends ji {
        Textures(kv settings) { super(null, settings); }

        @Override
        public int b(String name) { return 0; }

        @Override
        public int a(BufferedImage image) { return 0; }

        @Override
        public void a() {}
    }

    static final class Statistics extends xi {
        private Statistics() { super(null, (File) null); }

        @Override
        public void d() {}

        @Override
        public boolean a(ny achievement) { return true; }

        @Override
        public void a(vr stat, int amount) {}
    }
}
