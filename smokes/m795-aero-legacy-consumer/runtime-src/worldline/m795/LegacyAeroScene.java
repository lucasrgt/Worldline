package worldline.m795;

import aero.modellib.Aero_AnimationState;
import aero.modellib.Aero_MeshRenderer;
import aero.modellib.animation.Aero_AnimationBundle;
import aero.modellib.animation.Aero_AnimationClip;
import aero.modellib.animation.Aero_AnimationDefinition;
import aero.modellib.animation.Aero_Easing;
import aero.modellib.model.Aero_MeshModel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import net.minecraft.src.NBTTagCompound;
import org.lwjgl.opengl.GL11;

/** Public native adapter contract: direct/list equivalence and persisted animation playback. */
public final class LegacyAeroScene {
  private LegacyAeroScene() {}
  public static void execute() throws Exception {
    Aero_MeshModel model = new Aero_MeshModel("native-consumer", triangle(), 1, Collections.emptyMap());
    try (LegacyAeroSurface surface = new LegacyAeroSurface()) {
      byte[][] images = new byte[7][];
      surface.clear();
      Aero_MeshRenderer.renderModel(model, 0, 0, 0, 0, 1);
      images[0] = surface.capture();
      for (int i = 1; i <= 3; i++) {
        surface.clear();
        Aero_MeshRenderer.renderModelAtRest(model, 0, 0, 0, 0, 1);
        images[i] = surface.capture();
        require(model.getAtRestListIds() != null, "native display lists did not activate");
        if (i == 2) {
          int[] ids = model.getAtRestListIds().clone();
          Aero_MeshRenderer.disposeModel(model);
          require(model.getAtRestListIds() == null, "native model disposal retained lists");
          for (int id : ids) require(id == 0 || !GL11.glIsList(id), "native model disposal leaked GL list");
        }
      }
      animation(surface, images);
      for (int i = 1; i <= 4; i++) require(Arrays.equals(images[0], images[i]), "native exact render mismatch " + i);
      require(!Arrays.equals(images[0], images[5]), "native animation did not advance");
      require(Arrays.equals(images[5], images[6]), "native NBT round trip changed rendering");
      Path output = Paths.get(System.getProperty("worldline.aero.legacy.output"));
      Files.createDirectories(output);
      for (int i = 0; i < images.length; i++) Files.write(output.resolve("capture-" + i + ".rgba"), images[i]);
    } finally {
      Aero_MeshRenderer.disposeModel(model);
    }
  }
  private static void animation(LegacyAeroSurface surface, byte[][] images) {
    Aero_AnimationClip clip = Aero_AnimationClip.builder("spin")
                                  .length(1)
                                  .bone("rotor")
                                  .rotation(new float[] {0, 1}, new float[][] {{0, 0, 0}, {0, 0, 90}},
                                      new Aero_Easing[] {Aero_Easing.LINEAR, Aero_Easing.LINEAR})
                                  .endBone()
                                  .build();
    Aero_AnimationBundle bundle = new Aero_AnimationBundle(
        Collections.singletonMap("spin", clip), Collections.emptyMap(), Collections.emptyMap());
    Aero_AnimationDefinition definition = Aero_AnimationDefinition.builder().state(0, "spin").build();
    Aero_MeshModel model =
        new Aero_MeshModel("animated-native-consumer", empty(), 1, Collections.singletonMap("rotor", triangle()));
    Aero_AnimationState state = definition.createState(bundle);
    try {
      for (int i = 4; i <= 6; i++) {
        if (i == 5)
          for (int tick = 0; tick < 12; tick++) state.tick();
        if (i == 6) {
          NBTTagCompound nbt = new NBTTagCompound();
          state.writeToNBT(nbt);
          Aero_AnimationState restored = definition.createState(bundle);
          restored.readFromNBT(nbt);
          require(restored.getCurrentState() == state.getCurrentState()
                  && restored.getInterpolatedTime(1) == state.getInterpolatedTime(1),
              "native NBT state drift");
          state = restored;
        }
        surface.clear();
        Aero_MeshRenderer.renderAnimated(model, bundle, definition, state, 0, 0, 0, 1, 1);
        images[i] = surface.capture();
      }
    } finally {
      Aero_MeshRenderer.disposeModel(model);
    }
  }
  private static float[][][] triangle() {
    float[][][] groups = empty();
    groups[0] = new float[][] {{-0.6F, -0.6F, 0, 0, 0, 0.6F, -0.6F, 0, 1, 0, 0, 0.6F, 0, 0.5F, 1}};
    return groups;
  }
  private static float[][][] empty() {
    return new float[][][] {new float[0][], new float[0][], new float[0][], new float[0][]};
  }
  private static void require(boolean value, String message) {
    LegacyAeroSurface.require(value, message);
  }
}
