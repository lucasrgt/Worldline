package worldline.m792;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

/** Complete RGBA/depth surface independent of minimized-window back-buffer contents. */
public final class ParetoFramebuffer {
    private static int framebuffer, width, height, prior, draw, read;
    private static boolean bound;
    private ParetoFramebuffer() {}

    public static void begin(Minecraft game) {
        if (!OcclusionState.retaining()) return;
        prior = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
        draw = GL11.glGetInteger(GL11.GL_DRAW_BUFFER);
        read = GL11.glGetInteger(GL11.GL_READ_BUFFER);
        if (framebuffer == 0) create(game);
        if (width != game.displayWidth || height != game.displayHeight)
            throw new IllegalStateException("M792 framebuffer resized during acquisition");
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebuffer);
        GL11.glDrawBuffer(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
        GL11.glReadBuffer(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
        bound = true;
    }

    public static void end() {
        if (!bound) return;
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, prior);
        GL11.glDrawBuffer(draw); GL11.glReadBuffer(read);
        bound = false;
    }

    private static void create(Minecraft game) {
        if (!GLContext.getCapabilities().GL_EXT_framebuffer_object)
            throw new IllegalStateException("M792 framebuffer extension unavailable");
        int bits = GL11.glGetInteger(GL11.GL_DEPTH_BITS);
        if (bits != 24 || GL11.glGetInteger(GL11.GL_STENCIL_BITS) != 0)
            throw new IllegalStateException("M792 requires the original 24-bit depth, no-stencil surface");
        width = game.displayWidth; height = game.displayHeight;
        int renderbuffer = GL11.glGetInteger(EXTFramebufferObject.GL_RENDERBUFFER_BINDING_EXT);
        framebuffer = EXTFramebufferObject.glGenFramebuffersEXT();
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebuffer);
        attach(GL11.GL_RGBA8, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
        attach(33190, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT); // GL_DEPTH_COMPONENT24
        if (EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT)
                != EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT)
            throw new IllegalStateException("M792 framebuffer incomplete");
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, renderbuffer);
        System.out.println("[M792 framebuffer] rgba8 depth=" + bits + " width=" + width + " height=" + height);
    }

    private static void attach(int format, int attachment) {
        int buffer = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, buffer);
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, format, width, height);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            attachment, EXTFramebufferObject.GL_RENDERBUFFER_EXT, buffer);
    }
}
