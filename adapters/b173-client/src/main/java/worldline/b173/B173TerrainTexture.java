package worldline.b173;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** Loads the official terrain atlas into one native OpenGL context. */
final class B173TerrainTexture {
    private B173TerrainTexture() { }

    static int load(Path clientJar) throws Exception {
        return load(clientJar, "terrain.png", 256, 256);
    }

    static int load(Path clientJar, String asset, int width, int height) throws Exception {
        BufferedImage image;
        try (JarFile jar = new JarFile(clientJar.toFile())) {
            JarEntry entry = jar.getJarEntry(asset);
            if (entry == null) throw new IllegalStateException("official texture is absent: " + asset);
            try (InputStream input = jar.getInputStream(entry)) { image = ImageIO.read(input); }
        }
        if (image == null || image.getWidth() != width || image.getHeight() != height) {
            throw new IllegalStateException("official texture dimensions drifted: " + asset);
        }
        ByteBuffer pixels = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        for (int y = image.getHeight() - 1; y >= 0; y--) for (int x = 0; x < image.getWidth(); x++) {
            int rgba = image.getRGB(x, y);
            pixels.put((byte) (rgba >> 16)).put((byte) (rgba >> 8)).put((byte) rgba)
                    .put((byte) (rgba >> 24));
        }
        pixels.flip();
        IntBuffer textures = BufferUtils.createIntBuffer(1);
        GL11.glGenTextures(textures);
        int texture = textures.get(0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(),
                image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        return texture;
    }
}
