package org.lwjgl.opengl;

import java.nio.IntBuffer;

/** Headless replacement for OpenGL calls reached by the controlled client. */
public final class GL11 {
    public static final int GL_TEXTURE_2D = 3553;

    private GL11() {}

    public static void glBindTexture(int target, int texture) {}

    public static void glGenTextures(IntBuffer textures) {
        while (textures.hasRemaining()) textures.put(0);
        textures.flip();
    }
}
