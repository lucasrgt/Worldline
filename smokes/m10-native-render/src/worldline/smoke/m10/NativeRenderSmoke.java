package worldline.smoke.m10;

import worldline.b173.B173NativeFrame;
import worldline.b173.B173NativeRender;

/** Draws a fixed quad through a real Minecraft renderer into an offscreen OpenGL buffer. */
public final class NativeRenderSmoke {
  private NativeRenderSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 7)
      throw new IllegalArgumentException("expected renderer mapping");
    B173NativeFrame frame = B173NativeRender.render(arguments[0], arguments[1], arguments[2],
        arguments[3], arguments[4], arguments[5], arguments[6]);
    System.out.println("WORLDLINE_RENDER_ROLE=" + frame.role());
    System.out.println("WORLDLINE_RENDER_CONTEXT=" + frame.context());
    System.out.println("WORLDLINE_RENDER_DISPLAY_CREATED=" + frame.displayCreated());
    System.out.println("WORLDLINE_RENDER_GEOMETRY_PIXELS=" + frame.geometryPixels());
    System.out.println("WORLDLINE_RENDER_WORK=" + frame.work());
    System.out.println("WORLDLINE_RENDER_SHA256=" + frame.sha256());
    System.out.println("WORLDLINE_RENDER_PROVENANCE=" + frame.provenance());
    System.out.println("WORLDLINE_RENDER_GPU=" + frame.gpu());
  }
}
