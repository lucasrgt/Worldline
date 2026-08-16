package worldline.b173;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Achievement;
import net.minecraft.src.GameSettings;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.StatBase;
import net.minecraft.src.StatFileWriter;
import net.minecraft.src.UnexpectedThrowable;

/** Minimal substitutions for process boundaries that require a desktop or disk. */
final class B173Boundaries {
    private B173Boundaries() {}

    static <T> T allocateWithoutConstructor(Class<T> type) {
        try {
            Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
            Field singleton = unsafeType.getDeclaredField("theUnsafe");
            singleton.setAccessible(true);
            Object unsafe = singleton.get(null);
            Method allocate = unsafeType.getMethod("allocateInstance", Class.class);
            return type.cast(allocate.invoke(unsafe, type));
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("cannot allocate controlled boundary", error);
        }
    }

    static final class Client extends Minecraft {
        Client() { super(null, null, null, 320, 240, false); }

        @Override
        public void displayUnexpectedThrowable(UnexpectedThrowable error) {
            throw new IllegalStateException("vanilla client crashed", error.exception);
        }
    }

    static final class Textures extends RenderEngine {
        Textures(GameSettings settings) { super(null, settings); }
        @Override public int getTexture(String name) { return 0; }
        @Override public int allocateAndSetupTexture(BufferedImage image) { return 0; }
        @Override public void updateDynamicTextures() {}
    }

    static final class Statistics extends StatFileWriter {
        private Statistics() { super(null, (File) null); }
        @Override public void func_27178_d() {}
        @Override public void syncStats() {}
        @Override public void func_27175_b() {}
        @Override public boolean hasAchievementUnlocked(Achievement achievement) { return true; }
        @Override public void readStat(StatBase stat, int amount) {}
    }
}
