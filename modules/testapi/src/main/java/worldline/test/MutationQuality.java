package worldline.test;

/** Adapter precision for an externally observable mutation boundary. */
public enum MutationQuality {
    PUSH,
    DIRTY_NOTIFY,
    POLL
}
