package aero.modellib.test;
import aero.modellib.Aero_AnimatedBatcher;
import aero.modellib.animation.Aero_AnimationBundle;
import aero.modellib.animation.Aero_AnimationPlayback;
import aero.modellib.model.Aero_MeshModel;
import aero.modellib.render.Aero_RenderOptions;
import java.util.HashMap;
import java.util.Map;
/** Controlled consumers exercise public playback and real production emitters. */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class ProductionModels {
    private static final Aero_AnimationBundle BASE = AnimatedMegaModelBlockEntity.BUNDLE;
    private static final Aero_AnimationPlayback[][] STATES = new Aero_AnimationPlayback[3][256];
    private static Aero_AnimationBundle nested;
    private static Aero_MeshModel alternate;
    private static int scene, frame, previousSample = -1;
    private ProductionModels() {}
    public static void update(int nextScene, int nextFrame, int sample) {
        scene = nextScene; frame = nextFrame;
        if (sample != previousSample) alternate = null;
        for (int i = 0; i < 256; i++) {
            Aero_AnimationPlayback state = state(scene == 7 ? 1 : scene == 8 ? 2 : 0, i);
            if (scene == 6) {
                if (sample != previousSample) {
                    state.setState(AnimatedMegaModelBlockEntity.STATE_IDLE);
                    state.setStateWithTransition(AnimatedMegaModelBlockEntity.STATE_SPIN, 400);
                }
                state.tick();
            } else {
                if (sample != previousSample) state.setStateWithTransition(AnimatedMegaModelBlockEntity.STATE_IDLE, 0);
                state.setState(AnimatedMegaModelBlockEntity.STATE_SPIN);
                int phases = scene == 1 ? 1 : scene == 3 ? 64 : scene == 4 ? 128 : scene == 5 ? 256 : 20;
                state.setLoopPhase((i % phases) / (float) phases);
            }
        }
        previousSample = sample;
    }
    public static void render(AnimatedMegaModelBlockEntity be, double x, double y, double z, float delta) {
        int index = ((be.y == 84 ? 256 : 0) + (be.x / 2) * 16 + be.z / 2) / 2;
        Aero_MeshModel model = AnimatedMegaModelBlockEntityRenderer.MODEL;
        if (scene == 9 && frame >= 60) {
            if (alternate == null) alternate = new Aero_MeshModel("identity-switch", model.groups,
                model.scale, model.namedGroups);
            model = alternate;
        }
        Aero_AnimatedBatcher.queueAnimated(model, AnimatedMegaModelBlockEntityRenderer.TEXTURE,
            state(scene == 7 ? 1 : scene == 8 ? 2 : 0, index), x, y, z,
            AeroLight.brightnessAbove(be.world, be.x, be.y, be.z), delta, Aero_RenderOptions.DEFAULT);
    }
    private static Aero_AnimationPlayback state(int kind, int index) {
        if (STATES[kind][index] == null) STATES[kind][index] = kind == 1 ? new CustomPlayback()
            : new Aero_AnimationPlayback(AnimatedMegaModelBlockEntity.ANIM_DEF, kind == 2 ? nested() : BASE);
        return STATES[kind][index];
    }
    private static Aero_AnimationBundle nested() {
        if (nested == null) {
            String[] names = BASE.getClip("working").boneNames;
            if (names.length < 2) throw new IllegalStateException("nested fixture requires two bones");
            Map children = new HashMap(BASE.childMap);
            children.put(names[1], names[0]);
            nested = new Aero_AnimationBundle(BASE.clips, BASE.pivots, children);
        }
        return nested;
    }
    private static final class CustomPlayback extends Aero_AnimationPlayback {
        CustomPlayback() { super(AnimatedMegaModelBlockEntity.ANIM_DEF, BASE); }
        @Override public float getInterpolatedTime(float delta) { return super.getInterpolatedTime(delta) + 0.125F; }
    }
}
