package worldline.api;

/** Object-capable session extended with Packet25 painting spawn and Packet29 destroy waits. */
public interface PaintingObservationSession extends ObjectObservationSession {
    RemotePaintingSpawn awaitPaintingSpawn();
    int awaitPaintingDestroy(int entityId);
}
