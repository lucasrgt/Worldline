package worldline.m794;

import aero.modellib.Aero_MeshChunkBaker;
import aero.modellib.test.AeroTestMod;
import aero.modellib.test.ChunkBakedShowcaseBlock;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderManager;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas;
import org.lwjgl.opengl.GL11;

/** Separate native return-value checks after all measured/captured frames. */
public final class ProductionChunkDispatchProbe {
  private static final int X = 16, Y = 120, Z = 16;
  private ProductionChunkDispatchProbe() {}

  public static void run(Minecraft game) {
    if (game.world.getBlockId(X, Y, Z) != 0)
      throw new IllegalStateException("Chunk dispatch probe requires its reserved air position");
    Block registered = AeroTestMod.chunkBakedShowcaseBlock;
    BlockRenderManager renderer = new BlockRenderManager(game.world);
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT | GL11.GL_CLIENT_PIXEL_STORE_BIT);
    GL11.glColorMask(false, false, false, false);
    GL11.glDepthMask(false);
    try {
      game.world.setBlockWithoutNotifyingNeighbors(X, Y, Z, Block.COBBLESTONE.id);
      String vanilla = observe(renderer, Block.COBBLESTONE);
      String nullBlock = observe(renderer, null);
      game.world.setBlockWithoutNotifyingNeighbors(X, Y, Z, registered.id);
      String mesh = observe(renderer, registered);
      Aero_MeshChunkBaker.register(registered.id, ChunkBakedShowcaseBlock.MODEL, () -> {
        throw new IllegalStateException("expected M794 chunk dispatch bake failure");
      });
      String failedMesh = observe(renderer, registered);
      if (!"return:true".equals(mesh) || !"return:false".equals(failedMesh))
        throw new IllegalStateException("Registered chunk dispatch return drift: " + mesh + "/" + failedMesh);
      File directory = new File(System.getProperty("worldline.m794.metrics")).getParentFile();
      try (PrintWriter out = new PrintWriter(new FileWriter(new File(directory, "dispatch.properties")))) {
        out.println("vanilla=" + vanilla);
        out.println("null=" + nullBlock);
        out.println("registered=" + mesh);
        out.println("failed=" + failedMesh);
      }
    } catch (Exception error) {
      throw new IllegalStateException("M794 native chunk dispatch probe failed", error);
    } finally {
      Aero_MeshChunkBaker.register(registered.id, ChunkBakedShowcaseBlock.MODEL, () -> {
        Atlas.Sprite sprite = registered.getAtlas().getTexture(Block.COBBLESTONE.textureId);
        return new float[] {(float) sprite.getStartU(), (float) sprite.getEndU(),
            (float) sprite.getStartV(), (float) sprite.getEndV()};
      });
      game.world.setBlockWithoutNotifyingNeighbors(X, Y, Z, 0);
      GL11.glPopClientAttrib();
      GL11.glPopAttrib();
    }
  }

  private static String observe(BlockRenderManager renderer, Block block) {
    Tessellator.INSTANCE.startQuads();
    try {
      return "return:" + renderer.render(block, X, Y, Z);
    } catch (RuntimeException error) {
      return "throw:" + error.getClass().getName();
    } finally {
      Tessellator.INSTANCE.draw();
      int error = GL11.glGetError();
      if (error != GL11.GL_NO_ERROR)
        throw new IllegalStateException("Native chunk dispatch GL error: " + error);
    }
  }
}
