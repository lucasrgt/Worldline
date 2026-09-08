package worldline.b173server;

import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.ToolProvider;

/** Constructor-only shell: every player operation remains inherited from native EntityPlayer. */
final class B173NativePlayerFactory implements AutoCloseable {
    private final Path directory;
    private final URLClassLoader loader;
    private final Class<?> player;
    private final B173NativeServerSymbols names;
    B173NativePlayerFactory(B173NativeServerSymbols names) throws Exception {
        this.names = names;
        directory = Files.createTempDirectory("worldline-native-player-");
        String source = "public final class WorldlineNativePlayer extends " + names.type("EntityPlayer").getName()
                + " { public WorldlineNativePlayer(" + names.type("World").getName() + " world) { super(world); } }";
        Path file = directory.resolve("WorldlineNativePlayer.java");
        Files.write(file, source.getBytes(StandardCharsets.UTF_8));
        int result = ToolProvider.getSystemJavaCompiler().run(null, null, null, "--release", "8", "-proc:none",
                "-classpath", System.getProperty("java.class.path"), "-d", directory.toString(), file.toString());
        B173NativeEntityScene.require(result == 0, "native player constructor compilation failed");
        loader = new URLClassLoader(new java.net.URL[] {directory.toUri().toURL()}, getClass().getClassLoader());
        player = Class.forName("WorldlineNativePlayer", true, loader);
        B173NativeEntityScene.require(player.getDeclaredMethods().length == 0
                && player.getSuperclass() == names.type("EntityPlayer"), "native player overrides behavior");
    }
    Object create(Object world) throws Exception {
        Object value = player.getConstructor(names.type("World")).newInstance(world);
        names.setAs(value, "EntityPlayer", "username", "WorldlineNative");
        return value;
    }
    @Override public void close() throws java.io.IOException {
        loader.close();
        Files.deleteIfExists(directory.resolve("WorldlineNativePlayer.class"));
        Files.deleteIfExists(directory.resolve("WorldlineNativePlayer.java"));
        Files.delete(directory);
    }
}
