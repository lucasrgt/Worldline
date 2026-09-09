package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Controlled held-item arm animation on the protocol; does not control raster timing. */
final class AnimationSemantics {
    private AnimationSemantics() { }

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("animation", "ANIMATION_SWING_REQUEST",
                        "worldline/api/PeerSwingSession", "method", "swingHeldItem",
                        "()Lworldline/api/RemoteSwingRequest;",
                        "", "ANIMATION", "NETWORK,ANIMATION", "m69-peer-swing", "", 9998),
                SemanticMapping.of("animation", "ANIMATION_PEER_OBSERVATION",
                        "worldline/api/PeerSwingSession", "method", "awaitPeerSwing",
                        "(Ljava/lang/String;)Lworldline/api/RemotePeerSwing;",
                        "ANIMATION", "", "NETWORK,ANIMATION", "m69-peer-swing", "", 9998),
                SemanticMapping.of("animation", "ANIMATION_PEER_KIND",
                        "worldline/api/RemotePeerSwing", "method", "animation", "()I",
                        "ANIMATION", "", "ANIMATION", "m69-peer-swing", "", 9998)));
    }
}
