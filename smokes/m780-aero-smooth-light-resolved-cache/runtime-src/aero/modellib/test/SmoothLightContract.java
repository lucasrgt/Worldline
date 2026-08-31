package aero.modellib.test;

import aero.modellib.model.Aero_MeshModel;

/** Package bridge for the deterministic Worldline smooth-light fixture. */
public final class SmoothLightContract {
    public static final int ROUTE_FRAMES = 240;
    public static final int MACHINES = 128;

    private SmoothLightContract() {}

    /** Builds 2,048 non-overlapping triangles with spatially varied centroids. */
    public static Aero_MeshModel denseGrid() {
        int side = 32;
        float[][] top = new float[side * side * 2][];
        int cursor = 0;
        for (int z = 0; z < side; z++) {
            for (int x = 0; x < side; x++) {
                float x0 = x * 3.0F / side, x1 = (x + 1) * 3.0F / side;
                float z0 = z * 3.0F / side, z1 = (z + 1) * 3.0F / side;
                float y = 0.25F + ((x + z) & 3) * 0.03F;
                top[cursor++] = triangle(x0, y, z0, x1, y, z0, x1, y, z1);
                top[cursor++] = triangle(x0, y, z0, x1, y, z1, x0, y, z1);
            }
        }
        float[][][] groups = new float[4][][];
        groups[Aero_MeshModel.GROUP_TOP] = top;
        groups[Aero_MeshModel.GROUP_BOTTOM] = new float[0][];
        groups[Aero_MeshModel.GROUP_NS] = new float[0][];
        groups[Aero_MeshModel.GROUP_EW] = new float[0][];
        return new Aero_MeshModel("worldline-dense-smooth-grid", groups);
    }

    private static float[] triangle(float x0, float y0, float z0,
            float x1, float y1, float z1, float x2, float y2, float z2) {
        return new float[] {x0, y0, z0, 0.0F, 0.0F, x1, y1, z1, 1.0F, 0.0F,
            x2, y2, z2, 1.0F, 1.0F};
    }
}
