package aero.modellib.test;

import aero.modellib.model.Aero_MeshModel;

/** Package bridge for the deterministic Worldline smooth-light fixture. */
public final class SmoothLightContract {
    public static final int ROUTE_FRAMES = 240;
    public static final int MACHINES = 128;

    private SmoothLightContract() {}

    /** Composes every named OBJ object into one immutable static smooth mesh. */
    public static Aero_MeshModel flatten(Aero_MeshModel source) {
        int[] sizes = new int[4];
        for (int group = 0; group < 4; group++) sizes[group] = source.groups[group].length;
        Aero_MeshModel.NamedGroup[] named = source.getNamedGroupArray();
        for (Aero_MeshModel.NamedGroup object : named) {
            for (int group = 0; group < 4; group++) sizes[group] += object.tris[group].length;
        }
        float[][][] merged = new float[4][][];
        for (int group = 0; group < 4; group++) {
            merged[group] = new float[sizes[group]][];
            int cursor = append(merged[group], 0, source.groups[group]);
            for (Aero_MeshModel.NamedGroup object : named) {
                cursor = append(merged[group], cursor, object.tris[group]);
            }
        }
        return new Aero_MeshModel(source.name + "-worldline-smooth", merged, source.scale,
            new java.util.HashMap());
    }

    private static int append(float[][] target, int cursor, float[][] source) {
        System.arraycopy(source, 0, target, cursor, source.length);
        return cursor + source.length;
    }
}
