package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Worldline-owned overlay intercepts for the pinned Aero extension. */
final class AeroSemantics {
    private static final String CALLBACK =
            "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
    private static final String RETURNABLE =
            "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;";

    private AeroSemantics() { }

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                mapping("AERO_SAVE_BATCH_INTERCEPT", "mixin/WorldlineSaveBudgetMixin",
                        "worldlineSaveBatch", "(IZ)I", "m18-save-attribution"),
                mapping("AERO_COMPILE_BEGIN_INTERCEPT", "mixin/WorldlineChunkProbeMixin",
                        "worldlineCompileBegin",
                        "(Lnet/minecraft/entity/LivingEntity;Z" + RETURNABLE + ")V",
                        "m14-chunk-backlog"),
                mapping("AERO_SCHEDULE_INTERCEPT", "mixin/WorldlineChunkCallerMixin",
                        "worldlineSchedule", "(Lnet/minecraft/client/render/WorldRenderer;"
                                + "Lnet/minecraft/entity/LivingEntity;Z)Z",
                        "m15-chunk-contract"),
                mapping("AERO_CAPTURE_INTERCEPT", "mixin/WorldlineCaptureMixin",
                        "worldlineCapture", "(" + CALLBACK + ")V",
                        "m12-aero-reproduction"),
                mapping("AERO_SAVE_FORCE", "WorldlineSaveForce", "markDirty",
                        "(Lnet/minecraft/world/World;I)I", "m19-forced-autosave"),
                mapping("AERO_RELOAD_INTERCEPT", "mixin/WorldlineChunkProbeMixin",
                        "worldlineReloaded", "(" + CALLBACK + ")V", "m14-chunk-backlog"),
                mapping("AERO_FRAME_BEGIN_INTERCEPT", "mixin/WorldlineChunkProbeFrameMixin",
                        "worldlineProbeBegin", "(F" + CALLBACK + ")V",
                        "m17-scheduler-hardening"),
                mapping("AERO_VERTEX_INTERCEPT", "mixin/WorldlineTessellatorProbeMixin",
                        "worldlineVertex", "(DDD" + CALLBACK + ")V",
                        "m13-aero-differential"),
                mapping("AERO_REBUILD_INTERCEPT", "mixin/WorldlineChunkBuilderProbeMixin",
                        "worldlineRebuilt", "(" + CALLBACK + ")V", "m15-chunk-contract")));
    }

    private static SemanticMapping mapping(String role, String owner, String name,
            String descriptor, String evidence) {
        return SemanticMapping.of("aero", role, "worldline/aero/" + owner, "method", name,
                descriptor, "AERO", "AERO", "AERO", evidence, "", 9998);
    }
}
