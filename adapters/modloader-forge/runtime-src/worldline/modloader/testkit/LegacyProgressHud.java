package worldline.modloader.testkit;

import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.util.Base64;
import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.ScaledResolution;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.BufferUtils;

/** Native-font diagnostics drawn only by the controlled TestKit client probe. */
public final class LegacyProgressHud {
    private String[] lines;
    private int captures;
    private boolean rendered;
    private ByteBuffer backdrop;
    private int backdropWidth, backdropHeight;

    public void update(String encoded) {
        if (encoded == null || encoded.length() > 8192)
            throw new IllegalArgumentException("invalid HUD payload");
        String[] next = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8).split("\n", -1);
        if (next.length == 0 || next.length > 16) throw new IllegalArgumentException("invalid HUD rows");
        for (String line : next) {
            if (line.length() > 256) throw new IllegalArgumentException("HUD line too long");
            for (int index = 0; index < line.length(); index++)
                if (line.charAt(index) < 32 || line.charAt(index) > 126)
                    throw new IllegalArgumentException("HUD text must be visible ASCII");
        }
        lines = next;
    }

    public void draw(Minecraft client, boolean present) {
        if (lines == null) return;
        ScaledResolution scale = new ScaledResolution(client.gameSettings, client.displayWidth, client.displayHeight);
        int width = scale.getScaledWidth(), height = scale.getScaledHeight();
        int right = width - 4, bottom = height - 4, textWidth = Math.min(200, width - 8);
        int rows = lines.length;
        int top = bottom - rows * 10, matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushClientAttrib(GL11.GL_CLIENT_PIXEL_STORE_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPushMatrix(); GL11.glLoadIdentity();
        GL11.glOrtho(0, client.displayWidth, client.displayHeight, 0, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPushMatrix(); GL11.glLoadIdentity();
        try {
            GL11.glDisable(GL11.GL_DEPTH_TEST); GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_CULL_FACE); GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            background(client, textWidth + 8, width, height, present);
            GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glLoadIdentity();
            GL11.glOrtho(0, width, height, 0, -1, 1);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glEnable(GL11.GL_ALPHA_TEST);
            FontRenderer font = client.fontRenderer;
            for (int index = 0; index < rows; index++) {
                int y = top + index * 10;
                int available = y >= height - 34 ? Math.max(40, width / 2 - 98) : textWidth;
                String line = fit(font, lines[index], available);
                int color = index == 0 ? 0x70D6E0 : lines[index].startsWith("Failure:")
                        || lines[index].startsWith("Last failure:") ? 0xFF9999 : 0xEEEEEE;
                if (index == 0 && lines[0].contains("FAILED")) color = 0xFF7777;
                else if (index == 0 && lines[0].contains("PASSED")) color = 0x88DD88;
                font.drawStringWithShadow(line, right - font.getStringWidth(line), y, color);
            }
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPopMatrix();
            GL11.glMatrixMode(matrixMode); GL11.glPopClientAttrib(); GL11.glPopAttrib();
        }
        // Present diagnostics while the game is waiting for a command; never call runTick or renderWorld.
        if (present) {
            if (!rendered) { System.out.println("WORLDLINE_TEST_HUD=rendered"); rendered = true; }
            if (Boolean.getBoolean("worldline.legacy.testkit.hud.capture") && captures++ < 3) {
                GL11.glPushClientAttrib(GL11.GL_CLIENT_PIXEL_STORE_BIT);
                try { System.out.println(net.minecraft.src.ScreenShotHelper.saveScreenshot(
                        Minecraft.getMinecraftDir(), client.displayWidth, client.displayHeight)); }
                finally { GL11.glPopClientAttrib(); }
            }
            Display.update();
        }
    }

    private void background(Minecraft client, int strip, int width, int height, boolean present) {
        int pixelsWide = Math.min(client.displayWidth, (strip * client.displayWidth + width - 1) / width);
        int pixelsHigh = Math.min(client.displayHeight, (168 * client.displayHeight + height - 1) / height);
        boolean resized = pixelsWide != backdropWidth || pixelsHigh != backdropHeight;
        if (resized || backdrop == null) {
            backdropWidth = pixelsWide; backdropHeight = pixelsHigh;
            backdrop = BufferUtils.createByteBuffer(pixelsWide * pixelsHigh * 3);
        }
        int left = client.displayWidth - pixelsWide;
        backdrop.clear();
        if (!present || resized) {
            GL11.glReadBuffer(GL11.GL_BACK); GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glReadPixels(left, 0, pixelsWide, pixelsHigh, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, backdrop);
        } else {
            // Restore the untouched frame underneath old text while the controlled game is waiting.
            GL11.glDrawBuffer(GL11.GL_BACK); GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            GL11.glPixelZoom(1, 1); GL11.glRasterPos2i(left, client.displayHeight);
            GL11.glDrawPixels(pixelsWide, pixelsHigh, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, backdrop);
        }
    }

    private static String fit(FontRenderer font, String text, int width) {
        if (font.getStringWidth(text) <= width) return text;
        int end = text.length();
        while (end > 0 && font.getStringWidth(text.substring(0, end) + "...") > width) end--;
        return text.substring(0, end) + "...";
    }
}
