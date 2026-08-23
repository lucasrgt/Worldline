package worldline.m74.client;

import aero.modellib.model.Aero_MeshModel;
import aero.modellib.model.Aero_ObjLoader;

/** Client-only model resource used by the exact managed queue entrypoint. */
public final class WorldlineManagedModel {
  private static final Aero_MeshModel MODEL = Aero_ObjLoader.load("/worldline-m74-probe.obj");
  private WorldlineManagedModel() {
  }
  public static Aero_MeshModel model() {
    return MODEL;
  }
}
