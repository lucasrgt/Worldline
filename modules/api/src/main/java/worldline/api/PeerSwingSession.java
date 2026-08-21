package worldline.api;

/** One authenticated held-item swing and one named peer animation observation. */
public interface PeerSwingSession extends ChestRetrievalSession {
    RemoteSwingRequest swingHeldItem();
    RemotePeerSwing awaitPeerSwing(String username);
}
