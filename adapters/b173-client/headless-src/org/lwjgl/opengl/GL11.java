package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/** Headless replacement for OpenGL calls reached by the controlled client. */
public final class GL11 {
    public static final int GL_TEXTURE_2D = 3553;
    public static final int GL_FRONT = 1028;
    public static final int GL_RGBA = 6408;
    public static final int GL_UNSIGNED_BYTE = 5121;
    public static final int GL_NO_ERROR = 0;
    public static final int GL_VENDOR = 7936;
    public static final int GL_RENDERER = 7937;
    public static final int GL_VERSION = 7938;
    public static final int GL_DEPTH_TEST = 2929;
    public static final int GL_BLEND = 3042;
    public static final int GL_CULL_FACE = 2884;
    public static final int GL_DITHER = 3024;
    public static final int GL_PROJECTION = 5889;
    public static final int GL_MODELVIEW = 5888;
    public static final int GL_COLOR_BUFFER_BIT = 16384;
    public static final int GL_DEPTH_BUFFER_BIT = 256;
    public static final int GL_TEXTURE_MIN_FILTER = 10241;
    public static final int GL_TEXTURE_MAG_FILTER = 10240;
    public static final int GL_TEXTURE_WRAP_S = 10242;
    public static final int GL_TEXTURE_WRAP_T = 10243;
    public static final int GL_NEAREST = 9728;
    public static final int GL_CLAMP = 10496;
    public static final int GL_RGBA8 = 32856;

    private GL11() {}

    public static void glBindTexture(int target, int texture) {}
    public static void glFinish() {}
    public static void glReadBuffer(int mode) {}
    public static void glReadPixels(int x, int y, int width, int height,
            int format, int type, ByteBuffer pixels) {}
    public static int glGetError() { return GL_NO_ERROR; }
    public static String glGetString(int name) { return "headless"; }
    public static void glViewport(int x, int y, int width, int height) {}
    public static void glEnable(int capability) {}
    public static void glDisable(int capability) {}
    public static void glMatrixMode(int mode) {}
    public static void glLoadIdentity() {}
    public static void glOrtho(double left, double right, double bottom,
            double top, double near, double far) {}
    public static void glClearColor(float red, float green, float blue, float alpha) {}
    public static void glClear(int mask) {}
    public static void glColor4f(float red, float green, float blue, float alpha) {}
    public static void glRotatef(float angle, float x, float y, float z) {}
    public static void glTranslatef(float x, float y, float z) {}
    public static void glDepthMask(boolean enabled) {}
    public static void glTexParameteri(int target, int name, int value) {}
    public static void glTexImage2D(int target, int level, int internalFormat,
            int width, int height, int border, int format, int type, ByteBuffer pixels) {}

    public static void glGenTextures(IntBuffer textures) {
        while (textures.hasRemaining()) textures.put(0);
        textures.flip();
    }

    public static void glDeleteTextures(IntBuffer textures) {}
}
