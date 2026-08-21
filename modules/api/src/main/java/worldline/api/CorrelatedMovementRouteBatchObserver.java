package worldline.api;

/** Synchronous observer of batch-indexed correlated route events. */
public interface CorrelatedMovementRouteBatchObserver {
    CorrelatedMovementRouteBatchObserver NONE = event -> { };

    void observe(CorrelatedMovementRouteBatchEvent event);
}
