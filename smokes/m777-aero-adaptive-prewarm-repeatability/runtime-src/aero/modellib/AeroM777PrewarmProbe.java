package aero.modellib;

import aero.modellib.model.Aero_MeshModel;
import aero.modellib.model.Aero_ObjLoader;

/** Milestone-private bridge for one deterministic speculative-to-urgent transition. */
public final class AeroM777PrewarmProbe {
    private static final String FIXTURE_MODEL = "/models/MegaCrusher.obj";
    private AeroM777PrewarmProbe() {}

    public static Aero_MeshModel queueFirstLoaded() {
        Aero_MeshModel model = fixtureModel();
        Aero_Prewarm.enqueueModel(model);
        return model;
    }

    public static void observeFirstLoadedHidden(int observations) {
        Aero_MeshModel model = fixtureModel();
        for (int index = 0; index < observations; index++)
            Aero_Prewarm.observeModel(model, false);
    }

    public static void promoteUrgent(Aero_MeshModel model) {
        if (model == null) throw new IllegalStateException("M777 pressure probe absent");
        Aero_Prewarm.observeModel(model, true);
    }

    private static Aero_MeshModel fixtureModel() {
        Aero_MeshModel[] loaded = Aero_ObjLoader.cachedModels();
        for (int index = 0; index < loaded.length; index++)
            if (FIXTURE_MODEL.equals(loaded[index].name)) return loaded[index];
        throw new IllegalStateException("M777 fixture model absent from early cache");
    }
}
